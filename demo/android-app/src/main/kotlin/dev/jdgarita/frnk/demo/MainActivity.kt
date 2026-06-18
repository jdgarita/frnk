package dev.jdgarita.frnk.demo

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.core.view.WindowCompat
import dev.jdgarita.frnk.demo.ext.demoFeatureEntries
import dev.jdgarita.frnk.ui.app.FrnkAppConfig
import dev.jdgarita.frnk.ui.app.FrnkAppScaffold
import dev.jdgarita.frnk.ui.app.FrnkMonetizationConfig
import dev.jdgarita.frnk.ui.bottomnav.FrnkAppInfo
import dev.jdgarita.frnk.ui.bottomnav.FrnkHomeConfig
import dev.jdgarita.frnk.ui.bottomnav.FrnkNavConfig
import dev.jdgarita.frnk.ui.bottomnav.FrnkOnboardingConfig
import dev.jdgarita.frnk.ui.bottomnav.FrnkSettingsConfig
import dev.jdgarita.frnk.ui.theme.Appearance
import dev.jdgarita.frnk.ui.theme.AppearanceController
import dev.jdgarita.frnk.ui.theme.LocalAppearanceController
import dev.jdgarita.frnk.utils.Frnk
import org.koin.compose.viewmodel.koinViewModel

/**
 * The demo's single launcher activity — and the device home for **`:ui-app`'s `FrnkAppScaffold`**, the
 * batteries-included app root the toolkit tells real hosts to use. It boots the scaffold over the Koin
 * graph `DemoApplication` already started (`bootstrapDemoKoin()`), so `FrnkAppScaffold`'s
 * `requireFrnkKoin()` assertion is satisfied and the fake `EntitlementManager` drives the live Free↔Pro
 * Settings + auto-mounted paywall.
 *
 * The rich demo content (Home showcase, Components gallery, custom Settings) lives in `:demo-shared` as
 * scaffold-agnostic builders ([demoFeatureItem], [demoFeatureEntries], [DemoHomeContent],
 * [rememberDemoSettingsState], [demoSettingsHandler], …) so this `FrnkAppScaffold` host and the iOS
 * `DemoScreen`/`FrnkTabbedNavScaffold` host render the same demo from one source. Differences this host
 * relies on `FrnkAppScaffold` to own automatically: the `ToolkitRoute.Paywall` destination (so
 * [demoFeatureEntries] omits it) and first-launch onboarding (so no `FrnkFirstLaunchOnboardingEffect`
 * here).
 *
 * `:demo-shared` itself can't depend on `:ui-app` (it would taint `DemoKit`), which is why iOS stays on
 * the bare shell — see `docs/HOST_INTEGRATION.md` §8.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            // Hoist the controller here so the activity can observe the in-app theme toggle and keep
            // the system bar icons in sync (light theme → dark icons, dark theme → light icons).
            val appearanceController = remember { AppearanceController() }
            val isDark =
                when (appearanceController.appearance) {
                    Appearance.Light -> false
                    Appearance.Dark -> true
                    Appearance.System -> isSystemInDarkTheme()
                }

            // enableEdgeToEdge()'s default style only follows the system setting; re-key on the
            // resolved in-app appearance so toggling the theme inside the app flips the bar icons too.
            LaunchedEffect(isDark) {
                val controller = WindowCompat.getInsetsController(window, window.decorView)
                controller.isAppearanceLightStatusBars = !isDark
                controller.isAppearanceLightNavigationBars = !isDark
            }

            // The shared DemoViewModel, resolved once at this host scope and fed into the scaffold's
            // slots (the same way DemoScreen does on the bare shell).
            val vm: DemoViewModel = koinViewModel()
            val state by vm.state.collectAsState()

            // hostRoutes/homeTopBar built once (and held stable across isPro flips) so the config
            // compares equal across recompositions and the nav config isn't rebuilt every frame.
            val hostRoutes = remember { demoHostRoutes() }
            val homeTopBar = remember(state.isPro) { demoHomeTopBar(state.isPro) }
            val config =
                remember(state.isPro, state.isGodMode, hostRoutes, homeTopBar) {
                    FrnkAppConfig(
                        app = FrnkAppInfo(name = "frnk", version = "v${Frnk.VERSION}"),
                        nav = FrnkNavConfig(feature = demoFeatureItem, hostRoutes = hostRoutes),
                        theme = demoPurpleThemeConfig(),
                        home = FrnkHomeConfig(topBar = homeTopBar),
                        settings = FrnkSettingsConfig(),
                        onboarding = FrnkOnboardingConfig(pages = demoOnboardingPages),
                        monetization = FrnkMonetizationConfig(paywallFeatures = demoPaywallFeatures),
                    )
                }

            FrnkAppScaffold(
                config = config,
                appearanceController = appearanceController,
                onMessage = ::toast,
                onHomeEffect = { effect -> demoHandleHomeEffect(vm, effect) },
                // The demo's custom Settings catalogue (god-mode Developer section + extra sections) and
                // handler, injected via FrnkAppScaffold's Settings overrides — keeping the Koin check,
                // auto-paywall and onboarding the scaffold owns.
                settingsState = { _ ->
                    rememberDemoSettingsState(
                        LocalAppearanceController.current.appearance,
                        state.isPro,
                        state.isGodMode,
                    )
                },
                settingsEffects = { scope -> demoSettingsHandler(scope, ::handleEffect) },
                effects = { scope -> DemoEffectCollector(vm, scope, ::handleEffect) },
                entries = { scope ->
                    demoFeatureEntries(scope = scope, state = state, onIntent = vm::send, onEffect = ::handleEffect)
                },
            ) {
                DemoHomeContent(state = state, onIntent = vm::send)
            }
        }
    }

    private fun handleEffect(effect: DemoEffect) {
        when (effect) {
            is DemoEffect.Toast -> toast(effect.message)
            // Navigation effects are consumed by DemoEffectCollector (routed into the toolkit's
            // FrnkNavDisplay back stack), so the host no longer surfaces them as toasts.
            is DemoEffect.Navigate -> Unit
        }
    }

    private fun toast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
