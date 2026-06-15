package dev.jdgarita.frnk.demo

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavKey
import com.composables.icons.lucide.Component
import com.composables.icons.lucide.Lucide
import dev.jdgarita.frnk.ui.app.FrnkAppScaffold
import dev.jdgarita.frnk.ui.atoms.FrnkText
import dev.jdgarita.frnk.ui.atoms.FrnkTextState
import dev.jdgarita.frnk.ui.atoms.FrnkTopAppBarState
import dev.jdgarita.frnk.ui.bottomnav.FrnkFeatureItem
import dev.jdgarita.frnk.ui.scaffolds.FrnkScreenScaffold
import dev.jdgarita.frnk.utils.Frnk
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

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
    /**
     * The smoke's own center "feature" tab root — a harness-local `@Serializable` route so this
     * activity doesn't borrow `:demo-shared`'s `DemoRoute`. Registered in `hostRoutes` (for nav3
     * save/restore) + `entries` (for the entry provider) below.
     */
    @Serializable
    private data object SmokeFeatureRoute : NavKey

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FrnkAppScaffold(
                appName = "frnk",
                appVersion = "v${Frnk.VERSION}",
                // The bar's one host-configurable tab, pointed at this harness's own placeholder screen.
                feature =
                    FrnkFeatureItem(
                        route = SmokeFeatureRoute,
                        label = "Feature",
                        icon = Lucide.Component,
                        iosSystemIcon = "square.grid.2x2",
                    ),
                hostRoutes =
                    SerializersModule {
                        polymorphic(NavKey::class) {
                            subclass(SmokeFeatureRoute::class, SmokeFeatureRoute.serializer())
                        }
                    },
                paywallFeatures = listOf("Unlimited everything", "No ads", "Priority support"),
                onMessage = ::toast,
                entries = {
                    entry<SmokeFeatureRoute> {
                        FrnkScreenScaffold(topBar = FrnkTopAppBarState(title = "Feature")) { padding ->
                            FrnkText(
                                state = FrnkTextState.Title(text = "Feature tab"),
                                modifier = Modifier.padding(padding),
                            )
                        }
                    }
                },
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
