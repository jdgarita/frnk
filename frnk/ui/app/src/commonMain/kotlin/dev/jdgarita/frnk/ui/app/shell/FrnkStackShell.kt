package dev.jdgarita.frnk.ui.app.shell

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.scene.Scene
import dev.jdgarita.frnk.ui.nav.FrnkNavDisplay
import dev.jdgarita.frnk.ui.nav.FrnkPresentationViewModelStore
import dev.jdgarita.frnk.ui.nav.back
import dev.jdgarita.frnk.ui.nav.frnkEnterTransition
import dev.jdgarita.frnk.ui.nav.frnkExitTransition
import dev.jdgarita.frnk.ui.nav.frnkNestedNavConfig
import dev.jdgarita.frnk.ui.nav.navigateTo
import dev.jdgarita.frnk.ui.nav.rememberFrnkNavBackStack
import dev.jdgarita.frnk.ui.scaffolds.LocalFrnkBottomBarInset
import dev.jdgarita.frnk.ui.scaffolds.sheet.FrnkModalSheet
import kotlinx.serialization.modules.SerializersModule
import org.koin.compose.viewmodel.koinViewModel

/**
 * The single-stack app shell: one back stack of screens, with modal sheets held above it by the shell
 * rather than pushed onto it — the shape of an app with no bottom bar (Faint's, Still's). For the
 * tabbed `Home · <custom> · Settings` shape use `frnkTabbedRootModule` instead.
 *
 * **Stack.** [startRoute] seeds a saveable back stack whose routes serialize through [hostRoutes] (the
 * host's `polymorphic(NavKey::class) { subclass(…) }` module). [entries] registers each destination
 * with nav3's `entry<Route> { … }`, and receives the [FrnkShellNavigator] that pushes and pops it.
 * [transitionSpec] / [popTransitionSpec] replace frnk's slide; the pop motion also drives predictive back.
 *
 * **Sheets.** Each [FrnkShellSheet] is a [FrnkModalSheet] drawn above the stack, raised by
 * [FrnkShellNavigator.present] with its key — presenting leaves the current destination mounted
 * underneath. Which sheets are up lives in an MVI ViewModel ([frnkShellModule], in `frnkUiModules()`),
 * not in `remember`. Every presentation composes the sheet's content afresh under its own
 * [FrnkPresentationViewModelStore], so a `koinViewModel()` inside it starts from scratch each time, even
 * when re-presented before the previous exit slide has finished.
 *
 * **Back.** Within the stack, back pops (the NavDisplay owns it). A dismissible sheet closes on back —
 * through the sheet's own handler, which is where back lands on Android (the sheet is its own window),
 * with a handler here as the fallback for back that reaches the host window. A non-dismissible sheet
 * swallows back and refuses the swipe and the outside tap; only [FrnkShellNavigator.dismiss] lowers it.
 * At the start route with no sheet up, nothing here claims back, so the system takes it and leaves.
 *
 * **Known limit.** On targets where back reaches the host window rather than the sheet's own window
 * (non-Android), a `BackHandler` inside a destination pushed after the shell composed is registered later
 * than a visible sheet's fallback handler and can outrank it, so back may pop or be claimed by that
 * destination instead of closing the sheet.
 *
 * **One shell per owner.** The shell's ViewModel is keyed by class, so use one [FrnkStackShell] per
 * `ViewModelStoreOwner`. Sheet presentation is not saved across process death: a host that needs a gate
 * sheet up after a restart re-presents it itself.
 *
 * **Insets.** There is no bottom bar, so [LocalFrnkBottomBarInset] carries the navigation-bar inset for
 * the screens to clear.
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun FrnkStackShell(
    startRoute: NavKey,
    hostRoutes: SerializersModule,
    sheets: List<FrnkShellSheet>,
    modifier: Modifier = Modifier,
    transitionSpec: AnimatedContentTransitionScope<Scene<NavKey>>.() -> ContentTransform = { frnkEnterTransition() },
    popTransitionSpec: AnimatedContentTransitionScope<Scene<NavKey>>.() -> ContentTransform = { frnkExitTransition() },
    entries: EntryProviderScope<NavKey>.(navigator: FrnkShellNavigator) -> Unit
) {
    val viewModel = koinViewModel<FrnkShellViewModel>()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val navConfig = remember(hostRoutes) { frnkNestedNavConfig(hostRoutes = hostRoutes) }
    val backStack = rememberFrnkNavBackStack(navConfig, startRoute)
    val navigator = remember(backStack, viewModel) { ShellNavigator(backStack, viewModel) }
    val navEntryProvider = remember(navigator, entries) { entryProvider<NavKey> { entries(navigator) } }
    val bottomSystemInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    Box(modifier = modifier.fillMaxSize()) {
        CompositionLocalProvider(LocalFrnkBottomBarInset provides bottomSystemInset) {
            FrnkNavDisplay(
                backStack = backStack,
                modifier = Modifier.fillMaxSize(),
                entryProvider = navEntryProvider,
                transitionSpec = transitionSpec,
                popTransitionSpec = popTransitionSpec,
                predictivePopTransitionSpec = { popTransitionSpec() }
            )

            // After the NavDisplay, so a sheet's host-window back fallback is registered later and
            // outranks the stack's pop while the sheet is up.
            sheets.forEach { sheet ->
                key(sheet.key) {
                    ShellSheet(
                        sheet = sheet,
                        visible = state.isVisible(sheet.key),
                        presentation = state.presentation(sheet.key),
                        navigator = navigator
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun ShellSheet(
    sheet: FrnkShellSheet,
    visible: Boolean,
    presentation: Int,
    navigator: FrnkShellNavigator
) {
    // The fallback for back that reaches the host window rather than the sheet's own.
    BackHandler(enabled = visible && sheet.dismissible) { navigator.dismiss(sheet.key) }

    FrnkModalSheet(
        visible = visible,
        // A single close can arrive here more than once; Dismiss is idempotent. A non-dismissible sheet
        // ignores it, which is how it swallows back: the sheet's own handler always reports back here.
        onDismiss = { if (sheet.dismissible) navigator.dismiss(sheet.key) },
        dismissOnBackPress = sheet.dismissible,
        dismissOnClickOutside = sheet.dismissOnClickOutside,
        surfaceColor = sheet.surfaceColor(),
        // A swipe settles the sheet before it reports, so a non-dismissible sheet has to veto it here,
        // or it would leave the screen while the shell still holds it up. The veto also refuses the
        // sheet's programmatic hide, so it lifts once the shell has dropped the sheet itself
        // (FrnkShellNavigator.dismiss).
        canDismiss = { sheet.dismissible || !visible }
    ) {
        key(presentation) {
            FrnkPresentationViewModelStore { sheet.content(navigator) }
        }
    }
}

/** Drives the shell's one back stack and its sheet-presentation ViewModel. */
private class ShellNavigator(
    private val backStack: NavBackStack<NavKey>,
    private val viewModel: FrnkShellViewModel
) : FrnkShellNavigator {
    override fun push(route: NavKey) = backStack.navigateTo(route)

    override fun back() = backStack.back()

    override fun present(sheetKey: String) = viewModel.send(FrnkShellIntent.Present(sheetKey))

    override fun dismiss(sheetKey: String) = viewModel.send(FrnkShellIntent.Dismiss(sheetKey))
}