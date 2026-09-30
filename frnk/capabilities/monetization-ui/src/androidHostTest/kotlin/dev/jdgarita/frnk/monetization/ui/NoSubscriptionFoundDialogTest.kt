package dev.jdgarita.frnk.monetization.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import dev.jdgarita.frnk.ui.theme.FrnkLanguage
import dev.jdgarita.frnk.ui.theme.FrnkStringSource
import dev.jdgarita.frnk.ui.theme.FrnkTheme
import dev.jdgarita.frnk.ui.theme.FrnkThemeConfig
import dev.jdgarita.frnk.ui.theme.stringAppName
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * [NoSubscriptionFoundDialog]: the copy names the one store the build ships through and the product,
 * "Got It" dismisses, and `onShown` fires once per presentation. Ported from Faint's
 * `NoSubscriptionFoundDialogTest`, with the analytics moved behind the `onShown` callback.
 */
@OptIn(ExperimentalTestApi::class)
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.LEGACY)
@Config(sdk = [34], qualifiers = "w390dp-h844dp")
class NoSubscriptionFoundDialogTest {
    /** The theme names the app "Still", so the default product name reads "Still Pro". */
    private val stillTheme = FrnkThemeConfig(language = FrnkLanguage.En, stringOverrides = mapOf(stringAppName to "Still"))

    @Test
    fun renders_the_google_play_copy_and_got_it_dismisses() =
        runComposeUiTest {
            var dismissals = 0
            setContent {
                FrnkTheme(config = stillTheme) {
                    NoSubscriptionFoundDialog(onDismiss = { dismissals += 1 }, store = FrnkAppStore.GooglePlay)
                }
            }

            onNodeWithText("No Subscription Found").assertExists()
            onNodeWithText(
                "We couldn't find an active Still Pro subscription associated with your Google Play receipt. " +
                    "If you previously subscribed, make sure you are using the same Google Account used for " +
                    "the original purchase."
            ).assertExists()

            onNodeWithText("Got It").performClick()
            assertEquals(1, dismissals)
        }

    /** The iOS wording never says Google. */
    @Test
    fun names_the_app_store_on_ios() =
        runComposeUiTest {
            setContent {
                FrnkTheme(config = stillTheme) {
                    NoSubscriptionFoundDialog(onDismiss = {}, store = FrnkAppStore.AppStore)
                }
            }

            onNodeWithText(
                "We couldn't find an active Still Pro subscription associated with your App Store receipt. " +
                    "If you previously subscribed, make sure you are using the same Apple Account used for " +
                    "the original purchase."
            ).assertExists()
            onNodeWithText("Google", substring = true).assertDoesNotExist()
        }

    @Test
    fun the_product_name_is_a_parameter() =
        runComposeUiTest {
            setContent {
                FrnkTheme(config = stillTheme) {
                    NoSubscriptionFoundDialog(
                        onDismiss = {},
                        productName = FrnkStringSource.Raw("Faint Pro"),
                        store = FrnkAppStore.GooglePlay
                    )
                }
            }

            onNodeWithText("active Faint Pro subscription", substring = true).assertExists()
        }

    @Test
    fun the_spanish_catalog_localizes_every_line() =
        runComposeUiTest {
            setContent {
                FrnkTheme(config = stillTheme.copy(language = FrnkLanguage.Es)) {
                    NoSubscriptionFoundDialog(onDismiss = {}, store = FrnkAppStore.AppStore)
                }
            }

            onNodeWithText("Sin suscripción encontrada").assertExists()
            onNodeWithText("suscripción activa de Still Pro asociada a tu recibo de App Store", substring = true).assertExists()
            onNodeWithText("Entendido").assertExists()
        }

    /** One report per presentation — never one per frame. */
    @Test
    fun onShown_fires_once_across_recompositions() =
        runComposeUiTest {
            var shown = 0
            var tick by mutableIntStateOf(0)
            setContent {
                FrnkTheme(config = stillTheme) {
                    // Reading tick recomposes this scope, with a fresh onShown lambda each time.
                    val current = remember(tick) { { shown += 1 } }
                    NoSubscriptionFoundDialog(onDismiss = {}, store = FrnkAppStore.GooglePlay, onShown = current)
                }
            }
            waitForIdle()
            tick = 1
            waitForIdle()
            tick = 2
            waitForIdle()

            assertEquals(1, shown)
        }

    /** The Android build names Google Play; the iOS actual is `AppStore`. */
    @Test
    fun the_android_build_ships_through_google_play() {
        assertEquals(FrnkAppStore.GooglePlay, frnkAppStore)
    }
}