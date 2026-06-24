package dev.jdgarita.frnk.demo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dev.jdgarita.frnk.ui.app.FrnkApp

/**
 * The demo's thin Android host. It owns only the Android-platform concern of `enableEdgeToEdge()`, then
 * hands off to [FrnkApp] (`:ui-app`), which drives the theme and the system-bar icon sync internally
 * from the Koin-provided `AppearanceController`. iOS's `MainViewController` calls the same entry point,
 * so both platforms render one demo from one composable.
 *
 * Boots over the Koin graph `DemoApplication` already started (`bootstrapDemoKoin()`), which satisfies
 * `FrnkApp`'s `requireFrnkKoin()` assertion and binds the fake `EntitlementManager` that drives
 * the live Free↔Pro Settings + host-wired paywall. It does **not** call `initializeFrnk(context)` —
 * that fresh-host overload is documented in `docs/HOST_INTEGRATION.md` §4.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FrnkDemoApp()
        }
    }
}