package dev.jdgarita.frnk.demo.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.savedstate.serialization.SavedStateConfiguration
import com.composables.icons.lucide.Component
import com.composables.icons.lucide.House
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Settings
import dev.jdgarita.frnk.ui.app.defaultEnterTransition
import dev.jdgarita.frnk.ui.app.defaultExitTransition
import dev.jdgarita.frnk.ui.bottomnav.FrnkBottomFloatingBar
import dev.jdgarita.frnk.ui.bottomnav.FrnkNavBarDefaults
import dev.jdgarita.frnk.ui.bottomnav.FrnkNavBarItem
import dev.jdgarita.frnk.ui.nav.FrnkRoute
import dev.jdgarita.frnk.ui.nav.back
import dev.jdgarita.frnk.ui.nav.navigateTo
import dev.jdgarita.frnk.ui.scaffolds.LocalFrnkBottomBarInset
import org.koin.compose.navigation3.koinEntryProvider
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.core.context.loadKoinModules
import org.koin.core.module.Module

@OptIn(KoinExperimentalAPI::class)
@Composable
fun NestedNavScaffold(
    modifier: Modifier = Modifier,
    onSavedStateConfiguration: () -> SavedStateConfiguration,
    onNestedNavigationModule: (backStack: NavBackStack<NavKey>) -> Module,
    onBack: () -> Unit
) {
    val initialRoute = FrnkRoute.Home

    val backStack =
        rememberNavBackStack(
            configuration = onSavedStateConfiguration(),
            elements = arrayOf(initialRoute)
        )

    remember(backStack) {
        loadKoinModules(
            modules =
                listOf(
                    onNestedNavigationModule(backStack)
                )
        )
    }

    // Reserve the bar's footprint as the content's bottom inset (read unconditionally — it's a @Composable
    // getter) so screens on FrnkScreenScaffold / FrnkMviScreen pad their scrollable content above the bar
    // and the last item isn't hidden behind it. Provided as 0 while the bar is hidden.
    val reservedHeight = FrnkNavBarDefaults.reservedHeight

    Box(modifier = modifier.fillMaxSize()) {
        CompositionLocalProvider(
            LocalFrnkBottomBarInset provides reservedHeight
        ) {
            NavDisplay(
                backStack = backStack,
                onBack = { backStack.back() },
                entryProvider = koinEntryProvider(),
                entryDecorators =
                    listOf(
                        rememberSaveableStateHolderNavEntryDecorator(),
                        rememberViewModelStoreNavEntryDecorator()
                    ),
                transitionSpec = { defaultEnterTransition() },
                popTransitionSpec = { defaultExitTransition() },
                predictivePopTransitionSpec = { defaultExitTransition() }
            )
        }

        FrnkBottomFloatingBar(
            // TODO: set real iOS SF-Symbol names — iosSystemIcon is "" on every item below (placeholder).
            items =
                listOf(
                    FrnkNavBarItem(
                        key = "Home",
                        icon = Lucide.House,
                        iosSystemIcon = "",
                        label = "Home"
                    ),
                    FrnkNavBarItem(
                        key = "Components",
                        icon = Lucide.Component,
                        iosSystemIcon = "",
                        label = "Component"
                    ),
                    FrnkNavBarItem(
                        key = "Settings",
                        icon = Lucide.Settings,
                        iosSystemIcon = "",
                        label = "Settings"
                    )
                ),
            selectedIndex = backStack.lastIndex,
            onItemSelected = { index ->
                when (index) {
                    0 -> {
                        backStack.navigateTo(FrnkRoute.Home)
                    }
                    1 -> {
                        backStack.navigateTo(FrnkRoute.Custom("Components"))
                    }
                    2 -> {
                        backStack.navigateTo(FrnkRoute.Settings)
                    }
                }
            },
            modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth()
        )
    }
}