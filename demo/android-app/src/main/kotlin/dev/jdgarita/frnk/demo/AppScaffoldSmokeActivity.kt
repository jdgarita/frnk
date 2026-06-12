package dev.jdgarita.frnk.demo

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dev.jdgarita.frnk.ui.app.FrnkAppScaffold
import dev.jdgarita.frnk.ui.atoms.FrnkText
import dev.jdgarita.frnk.ui.atoms.FrnkTextState
import dev.jdgarita.frnk.ui.scaffolds.HomeEffect
import dev.jdgarita.frnk.utils.Frnk

/**
 * Device smoke for **`FrnkAppScaffold`** — the `:ui-app` batteries-included app root. Boots the
 * scaffold over the Koin graph `DemoApplication` already started (which satisfies the scaffold's
 * Koin-started assertion; the fake `EntitlementManager` drives the live Free↔Pro Settings catalogue
 * and the auto-mounted `ToolkitRoute.Paywall`).
 *
 * Not a launcher activity — start it explicitly:
 * `adb shell am start -n dev.jdgarita.frnk.demo/.AppScaffoldSmokeActivity`
 *
 * Note `initializeFrnk(context)` itself isn't called here (Koin is process-global and the demo app
 * boots `bootstrapDemoKoin()` with its fakes); a fresh host exercises that overload — see
 * `docs/HOST_INTEGRATION.md` §8.
 */
class AppScaffoldSmokeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FrnkAppScaffold(
                appName = "frnk",
                appVersion = "v${Frnk.VERSION}",
                homePrimaryActionEnabled = true,
                onHomeEffect = { effect ->
                    if (effect == HomeEffect.PrimaryActionInvoked) toast("Primary action tapped")
                },
                paywallFeatures = listOf("Unlimited everything", "No ads", "Priority support"),
                onMessage = ::toast,
            ) {
                FrnkText(state = FrnkTextState.Title(text = "FrnkAppScaffold smoke"))
                FrnkText(
                    state =
                        FrnkTextState.Body(
                            text =
                                "The batteries-included app root: theme, three tabs, default Settings " +
                                    "catalogue driven by the live EntitlementManager, auto-mounted paywall — " +
                                    "all from one composable call.",
                        ),
                )
            }
        }
    }

    private fun toast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
