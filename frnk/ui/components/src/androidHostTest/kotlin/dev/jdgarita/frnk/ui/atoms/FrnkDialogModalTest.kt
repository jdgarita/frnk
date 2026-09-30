package dev.jdgarita.frnk.ui.atoms

import androidx.activity.ComponentActivity
import androidx.activity.ComponentDialog
import androidx.activity.OnBackPressedCallback
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.isHeading
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.runAndroidComposeUiTest
import dev.jdgarita.frnk.ui.theme.FrnkStringSource
import dev.jdgarita.frnk.ui.theme.FrnkTheme
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowDialog
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * [FrnkDialog] is modal for every input, not just touch: it is presented in its own window (the
 * platform [ComponentDialog] behind compose-unstyled's `Modal`, as `FrnkModalSheet` is), marked as a
 * dialog, so accessibility services and keyboard focus stay inside it; focus starts on its first
 * action; and system back never reaches the screen beneath — it goes to `onDismissRequest`, or nowhere.
 */
@OptIn(ExperimentalTestApi::class)
@Config(qualifiers = "w390dp-h844dp")
class FrnkDialogModalTest : RobolectricComposeTest() {
    private val cancel = FrnkDialogAction(key = "cancel", label = FrnkStringSource.Raw("Cancel"), kind = FrnkDialogActionKind.Cancel)
    private val confirm = FrnkDialogAction(key = "confirm", label = FrnkStringSource.Raw("Confirm"), kind = FrnkDialogActionKind.Primary)
    private val state = FrnkDialogState(title = FrnkStringSource.Raw("Title"), actions = listOf(cancel, confirm))

    private fun latestDialog(): ComponentDialog = ShadowDialog.getLatestDialog() as ComponentDialog

    @Test
    fun the_dialog_lives_in_its_own_modal_window_apart_from_the_background() =
        runAndroidComposeUiTest<ComponentActivity> {
            setContent {
                FrnkTheme {
                    Box(Modifier.fillMaxSize()) {
                        Box(Modifier.fillMaxSize().testTag("beneath").clickable {})
                        FrnkDialog(state = state, onAction = {})
                    }
                }
            }
            waitForIdle()

            assertTrue(runOnIdle { latestDialog().isShowing }, "the dialog is presented in a window of its own")
            onNode(hasText("Title") and hasAnyAncestor(isDialog())).assertExists()
            onNode(hasText("Confirm") and hasAnyAncestor(isDialog())).assertExists()
            // The background's controls are not part of the dialog's (modal) window.
            onNode(hasTestTag("beneath") and hasAnyAncestor(isDialog())).assertDoesNotExist()
        }

    @Test
    fun the_title_is_a_heading_and_focus_starts_on_the_first_action() =
        runAndroidComposeUiTest<ComponentActivity> {
            setContent { FrnkTheme { FrnkDialog(state = state, onAction = {}) } }
            waitForIdle()

            onNode(hasText("Title") and isHeading()).assertExists()
            onNodeWithText("Cancel").assertIsFocused()
        }

    @Test
    fun back_goes_to_onDismissRequest() =
        runAndroidComposeUiTest<ComponentActivity> {
            var dismissals = 0
            setContent { FrnkTheme { FrnkDialog(state = state, onAction = {}, onDismissRequest = { dismissals += 1 }) } }
            waitForIdle()

            runOnUiThread { latestDialog().onBackPressedDispatcher.onBackPressed() }
            waitForIdle()

            assertEquals(1, dismissals)
        }

    @Test
    fun without_onDismissRequest_back_is_swallowed_and_the_dialog_stays() =
        runAndroidComposeUiTest<ComponentActivity> {
            var screenBacks = 0
            runOnUiThread {
                activity!!.onBackPressedDispatcher.addCallback(
                    object : OnBackPressedCallback(true) {
                        override fun handleOnBackPressed() {
                            screenBacks += 1
                        }
                    }
                )
            }
            setContent { FrnkTheme { FrnkDialog(state = state, onAction = {}) } }
            waitForIdle()
            val dialog = runOnIdle { latestDialog() }

            runOnUiThread { dialog.onBackPressedDispatcher.onBackPressed() }
            waitForIdle()

            assertTrue(runOnIdle { dialog.isShowing }, "back does not close a dialog that has no dismiss")
            assertEquals(0, screenBacks, "back never reaches the screen beneath")
        }
}