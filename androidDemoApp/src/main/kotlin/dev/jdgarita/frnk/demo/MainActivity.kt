package dev.jdgarita.frnk.demo

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.core.view.WindowCompat
import dev.jdgarita.frnk.ui.theme.Appearance
import dev.jdgarita.frnk.ui.theme.AppearanceController
import dev.jdgarita.frnk.ui.theme.FrnkTheme

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

            FrnkTheme(config = demoPurpleThemeConfig(), appearanceController = appearanceController) {
                DemoScreen(onEffect = ::handleEffect)
            }
        }
    }

    private fun handleEffect(effect: DemoEffect) {
        when (effect) {
            is DemoEffect.Toast -> Toast.makeText(this, effect.message, Toast.LENGTH_SHORT).show()
            // Navigation effects are now consumed inside DemoScreen (routed into the toolkit's
            // FrnkNavDisplay back stack), so the host no longer surfaces them as toasts.
            is DemoEffect.Navigate -> Unit
        }
    }
}
