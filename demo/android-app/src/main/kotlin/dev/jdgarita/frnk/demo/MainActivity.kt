package dev.jdgarita.frnk.demo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.core.view.WindowCompat
import dev.jdgarita.frnk.demo.ext.toast
import dev.jdgarita.frnk.ui.theme.Appearance
import dev.jdgarita.frnk.ui.theme.AppearanceController

/**
 * The demo's thin Android host. It owns only the Android-platform concerns — `enableEdgeToEdge()`, the
 * [AppearanceController] hoist + system-bar icon sync, and surfacing MVI effects as toasts — then hands
 * off to the **unified shared entry point** [FrnkDemoApp] (`:demo-shared`), which wraps `:ui-app`'s
 * batteries-included `FrnkAppScaffold`. iOS's `MainViewController` calls the very same [FrnkDemoApp], so
 * both platforms render one demo from one composable.
 *
 * Boots over the Koin graph `DemoApplication` already started (`bootstrapDemoKoin()`), which satisfies
 * `FrnkAppScaffold`'s `requireFrnkKoin()` assertion and binds the fake `EntitlementManager` that drives
 * the live Free↔Pro Settings + auto-mounted paywall. It does **not** call `initializeFrnk(context)` —
 * that fresh-host overload is documented in `docs/HOST_INTEGRATION.md` §4.
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

            FrnkDemoApp(appearanceController = appearanceController, onEffect = ::handleEffect)
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
}
