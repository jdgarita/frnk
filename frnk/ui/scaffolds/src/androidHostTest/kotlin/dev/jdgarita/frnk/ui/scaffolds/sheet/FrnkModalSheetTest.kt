package dev.jdgarita.frnk.ui.scaffolds.sheet

import androidx.activity.ComponentDialog
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.test.swipeDown
import androidx.compose.ui.test.waitUntilDoesNotExist
import androidx.compose.ui.test.waitUntilExactlyOneExists
import androidx.compose.ui.unit.dp
import dev.jdgarita.frnk.ui.atoms.RobolectricComposeTest
import dev.jdgarita.frnk.ui.atoms.setFrnkContent
import org.robolectric.shadows.ShadowDialog
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * [FrnkModalSheet]'s host-observable contract: every exit the sheet performs itself (a swipe down,
 * system back) reaches [onDismiss][FrnkModalSheet] exactly once, the `canDismiss` veto holds a swipe
 * back, the content's ✕ is drawn by the sheet and runs the content's action, and the content learns
 * when the sheet has come to rest.
 */
@OptIn(ExperimentalTestApi::class)
class FrnkModalSheetTest : RobolectricComposeTest() {
    /**
     * A swipe down is the one exit the host learns about only from the sheet: back and the scrim tap
     * go through the host's own handlers, but the drag settles the sheet at Hidden on its own. Faint's
     * beta 37 lost that report — the modal tore down before compose-unstyled's callback ran, the host's
     * flag stayed true, and the sheet could never be presented again. The observable contract is that
     * a swipe reports exactly once and the sheet can be presented afresh afterwards.
     */
    @Test
    fun swipe_down_reports_one_dismissal_and_the_sheet_presents_again() =
        runComposeUiTest {
            var shown by mutableStateOf(false)
            var dismissals = 0
            setFrnkContent {
                FrnkModalSheet(
                    visible = shown,
                    onDismiss = {
                        dismissals += 1
                        shown = false
                    }
                ) {
                    Box(modifier = Modifier.fillMaxWidth().height(320.dp).testTag(BODY_TAG))
                }
            }

            runOnIdle { shown = true }
            waitUntilExactlyOneExists(hasTestTag(BODY_TAG))

            onNodeWithTag(BODY_TAG).performTouchInput { swipeDown() }
            waitUntilDoesNotExist(hasTestTag(BODY_TAG))
            runOnIdle {
                assertEquals(1, dismissals)
                assertFalse(shown)
            }

            // The regression: with the flag stranded at true, this present would be a no-op.
            runOnIdle { shown = true }
            waitUntilExactlyOneExists(hasTestTag(BODY_TAG))

            onNodeWithTag(BODY_TAG).performTouchInput { swipeDown() }
            waitUntilDoesNotExist(hasTestTag(BODY_TAG))
            runOnIdle { assertEquals(2, dismissals) }
        }

    /**
     * On Android the sheet lives in compose-unstyled's own `ComponentDialog` window, so system back
     * reaches the dialog's dispatcher, never the activity's — which is why the sheet owns its
     * `BackHandler`. The test presses back where a real one lands while the sheet is up: on the
     * dialog's dispatcher.
     */
    @Test
    fun back_press_dismisses_a_visible_sheet() =
        runComposeUiTest {
            var dismissals = 0
            setFrnkContent {
                FrnkModalSheet(visible = true, onDismiss = { dismissals += 1 }) {
                    Box(modifier = Modifier.fillMaxWidth().height(320.dp).testTag(BODY_TAG))
                }
            }
            waitUntilExactlyOneExists(hasTestTag(BODY_TAG))

            runOnIdle {
                val sheetDialog = ShadowDialog.getLatestDialog() as ComponentDialog
                sheetDialog.onBackPressedDispatcher.onBackPressed()
            }
            runOnIdle { assertEquals(1, dismissals) }
        }

    @Test
    fun can_dismiss_false_vetoes_the_swipe() =
        runComposeUiTest {
            var dismissals = 0
            setFrnkContent {
                FrnkModalSheet(
                    visible = true,
                    onDismiss = { dismissals += 1 },
                    canDismiss = { false }
                ) {
                    Box(modifier = Modifier.fillMaxWidth().height(320.dp).testTag(BODY_TAG))
                }
            }
            waitUntilExactlyOneExists(hasTestTag(BODY_TAG))

            onNodeWithTag(BODY_TAG).performTouchInput { swipeDown() }
            waitForIdle()
            runOnIdle { assertEquals(0, dismissals) }
            onNodeWithTag(BODY_TAG).assertIsDisplayed()
        }

    @Test
    fun a_close_handler_draws_the_close_button_and_calls_back() =
        runComposeUiTest {
            var closed = false
            setFrnkContent {
                FrnkModalSheet(visible = true, onDismiss = {}) {
                    FrnkModalSheetCloseHandler("Close sheet") { closed = true }
                    Box(modifier = Modifier.fillMaxWidth().height(320.dp).testTag(BODY_TAG))
                }
            }
            waitUntilExactlyOneExists(hasContentDescription("Close sheet"))

            onNodeWithContentDescription("Close sheet").performClick()
            runOnIdle { assertTrue(closed) }
        }

    @Test
    fun content_is_settled_once_the_sheet_has_risen() =
        runComposeUiTest {
            var settled = false
            setFrnkContent {
                FrnkModalSheet(visible = true, onDismiss = {}) {
                    settled = LocalFrnkModalSheetSettled.current
                    Box(modifier = Modifier.fillMaxWidth().height(320.dp).testTag(BODY_TAG))
                }
            }
            waitUntilExactlyOneExists(hasTestTag(BODY_TAG))
            waitForIdle()
            runOnIdle { assertTrue(settled) }
        }
}

private const val BODY_TAG = "FrnkModalSheetTest.body"