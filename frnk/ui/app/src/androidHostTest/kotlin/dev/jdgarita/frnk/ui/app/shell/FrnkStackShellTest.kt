package dev.jdgarita.frnk.ui.app.shell

import androidx.activity.ComponentActivity
import androidx.activity.ComponentDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.AndroidComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runAndroidComposeUiTest
import androidx.compose.ui.test.waitUntilDoesNotExist
import androidx.compose.ui.test.waitUntilExactlyOneExists
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.jdgarita.frnk.ui.atoms.RobolectricComposeTest
import dev.jdgarita.frnk.ui.nav.FrnkTabRoute
import dev.jdgarita.frnk.ui.theme.FrnkTheme
import kotlinx.serialization.modules.EmptySerializersModule
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.robolectric.shadows.ShadowDialog
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertNotSame
import kotlin.test.assertTrue

/**
 * [FrnkStackShell]'s host-observable contract: the navigator drives one back stack, sheets present
 * above it and dismiss on back unless they are non-dismissible, back at the root is left to the system,
 * and every presentation of a sheet gets a fresh ViewModel store.
 *
 * The routes are the toolkit's own [FrnkTabRoute]s, which `frnkNestedNavConfig` already registers, so
 * the test declares no `@Serializable` routes of its own.
 */
@OptIn(ExperimentalTestApi::class)
class FrnkStackShellTest : RobolectricComposeTest() {
    class Probe : ViewModel() {
        var cleared = false

        override fun onCleared() {
            cleared = true
        }
    }

    @BeforeTest
    fun startShellKoin() {
        startKoin { modules(frnkShellModule) }
    }

    @AfterTest
    fun stopShellKoin() {
        stopKoin()
    }

    @Test
    fun push_and_back_move_through_the_stack() =
        runAndroidComposeUiTest<ComponentActivity> {
            setShell()
            waitUntilExactlyOneExists(hasTestTag(START_TAG))

            onNodeWithTag(PUSH_TAG).performClick()
            waitUntilExactlyOneExists(hasTestTag(SECOND_TAG))
            waitUntilDoesNotExist(hasTestTag(START_TAG))

            runOnUiThread { activity!!.onBackPressedDispatcher.onBackPressed() }
            waitUntilExactlyOneExists(hasTestTag(START_TAG))
            waitUntilDoesNotExist(hasTestTag(SECOND_TAG))
        }

    @Test
    fun a_presented_sheet_shows_its_content_and_back_dismisses_it() =
        runAndroidComposeUiTest<ComponentActivity> {
            val navigator = setShell()
            waitUntilExactlyOneExists(hasTestTag(START_TAG))

            runOnIdle { navigator().present(SHEET_KEY) }
            waitUntilExactlyOneExists(hasTestTag(SHEET_TAG))

            // The sheet is its own dialog window on Android, so back lands on the dialog's dispatcher.
            val sheetDialog = runOnIdle { ShadowDialog.getLatestDialog() as ComponentDialog }
            runOnUiThread { sheetDialog.onBackPressedDispatcher.onBackPressed() }
            waitUntilDoesNotExist(hasTestTag(SHEET_TAG))
            onNodeWithTag(START_TAG).assertExists()
        }

    @Test
    fun back_at_the_root_with_no_sheet_is_not_claimed() =
        runAndroidComposeUiTest<ComponentActivity> {
            setShell()
            waitUntilExactlyOneExists(hasTestTag(START_TAG))

            runOnIdle {
                assertFalse(
                    activity!!.onBackPressedDispatcher.hasEnabledCallbacks(),
                    "nothing in the shell may claim back at the start route with no sheet up"
                )
            }
        }

    @Test
    fun a_non_dismissible_sheet_survives_back() =
        runAndroidComposeUiTest<ComponentActivity> {
            val navigator = setShell(dismissible = false)
            waitUntilExactlyOneExists(hasTestTag(START_TAG))

            runOnIdle { navigator().present(SHEET_KEY) }
            waitUntilExactlyOneExists(hasTestTag(SHEET_TAG))

            val sheetDialog = runOnIdle { ShadowDialog.getLatestDialog() as ComponentDialog }
            runOnUiThread { sheetDialog.onBackPressedDispatcher.onBackPressed() }
            waitForIdle()
            // Past the ~400ms exit slide a dismissal would have taken.
            mainClock.advanceTimeBy(EXIT_SETTLE_MILLIS)
            waitForIdle()
            onNodeWithTag(SHEET_TAG).assertExists()
            // Nor did back fall through to the host window.
            onNodeWithTag(START_TAG).assertExists()
        }

    /**
     * The host's own dismiss still lowers a non-dismissible sheet. FrnkModalSheet's `canDismiss` veto
     * (which the shell uses to refuse the swipe) also refuses a programmatic hide, so the shell lifts it
     * once it has dropped the sheet itself.
     */
    @Test
    fun the_navigator_lowers_a_non_dismissible_sheet() =
        runAndroidComposeUiTest<ComponentActivity> {
            val navigator = setShell(dismissible = false)
            waitUntilExactlyOneExists(hasTestTag(START_TAG))

            runOnIdle { navigator().present(SHEET_KEY) }
            waitUntilExactlyOneExists(hasTestTag(SHEET_TAG))

            runOnIdle { navigator().dismiss(SHEET_KEY) }
            waitUntilDoesNotExist(hasTestTag(SHEET_TAG))
        }

    @Test
    fun each_presentation_gets_a_fresh_view_model() =
        runAndroidComposeUiTest<ComponentActivity> {
            val seen = mutableListOf<Probe>()
            val navigator = setShell(sheetContent = { seen += viewModel { Probe() } })
            waitUntilExactlyOneExists(hasTestTag(START_TAG))

            runOnIdle { navigator().present(SHEET_KEY) }
            waitUntilExactlyOneExists(hasTestTag(SHEET_TAG))

            // Re-present inside the exit window, before the first presentation has unmounted: the
            // case where an unkeyed store would hand back the previous presentation's ViewModel.
            runOnIdle {
                navigator().dismiss(SHEET_KEY)
                navigator().present(SHEET_KEY)
            }
            waitForIdle()
            waitUntilExactlyOneExists(hasTestTag(SHEET_TAG))

            val first = seen.first()
            val last = seen.last()
            assertNotSame(first, last)
            assertTrue(first.cleared, "the previous presentation's ViewModel is cleared")
        }

    /**
     * Composes a shell whose start route ([FrnkTabRoute.Home]) has a button pushing [FrnkTabRoute.Settings],
     * with one sheet under [SHEET_KEY]. Returns a getter for the navigator the shell handed its entries.
     */
    private fun AndroidComposeUiTest<ComponentActivity>.setShell(
        dismissible: Boolean = true,
        sheetContent: @Composable () -> Unit = {}
    ): () -> FrnkShellNavigator {
        var captured: FrnkShellNavigator? = null
        setContent {
            FrnkTheme {
                FrnkStackShell(
                    startRoute = FrnkTabRoute.Home,
                    hostRoutes = EmptySerializersModule(),
                    sheets =
                        listOf(
                            FrnkShellSheet(key = SHEET_KEY, dismissible = dismissible) {
                                Box(modifier = Modifier.fillMaxWidth().height(320.dp).testTag(SHEET_TAG)) {
                                    sheetContent()
                                }
                            }
                        )
                ) { navigator ->
                    captured = navigator
                    entry<FrnkTabRoute.Home> {
                        Box(modifier = Modifier.testTag(START_TAG)) {
                            BasicText(
                                text = "Push",
                                modifier = Modifier.testTag(PUSH_TAG).clickable { navigator.push(FrnkTabRoute.Settings) }
                            )
                        }
                    }
                    entry<FrnkTabRoute.Settings> {
                        BasicText(text = "Second", modifier = Modifier.testTag(SECOND_TAG))
                    }
                }
            }
        }
        return { checkNotNull(captured) { "the shell never handed its entries a navigator" } }
    }

    private companion object {
        const val SHEET_KEY = "sheet"
        const val START_TAG = "FrnkStackShellTest.start"
        const val PUSH_TAG = "FrnkStackShellTest.push"
        const val SECOND_TAG = "FrnkStackShellTest.second"
        const val SHEET_TAG = "FrnkStackShellTest.sheet"
        const val EXIT_SETTLE_MILLIS = 1_000L
    }
}