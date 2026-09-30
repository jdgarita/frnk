package dev.jdgarita.frnk.ui.atoms

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.click
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.runComposeUiTest
import dev.jdgarita.frnk.ui.haptics.DefaultHapticFeedback
import dev.jdgarita.frnk.ui.haptics.HapticType
import dev.jdgarita.frnk.ui.theme.FrnkStringSource
import dev.jdgarita.frnk.ui.theme.FrnkTheme
import dev.jdgarita.frnk.ui.theme.stringCancel
import org.robolectric.annotation.Config
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * [FrnkDialog]'s contract, ported from Faint's `PoAlertTest` plus the copy and layout promises (modality,
 * focus and back are in `FrnkDialogModalTest`): its scrim
 * swallows what would otherwise reach the screen beneath, its actions still take a real finger, and each
 * action reports itself by key. The scrim and the buttons pull in opposite directions — a scrim that
 * consumes every change of every gesture also cancels the tap on its own buttons — so both are pinned.
 */
@OptIn(ExperimentalTestApi::class)
@Config(qualifiers = "w390dp-h844dp")
class FrnkDialogTest : RobolectricComposeTest() {
    private val cancel = FrnkDialogAction(key = "cancel", label = FrnkStringSource.Token(stringCancel), kind = FrnkDialogActionKind.Cancel)
    private val confirm = FrnkDialogAction(key = "confirm", label = FrnkStringSource.Raw("Confirm"), kind = FrnkDialogActionKind.Primary)

    private fun state(
        variant: FrnkDialogVariant = FrnkDialogVariant.Accent,
        layout: FrnkDialogActionLayout = FrnkDialogActionLayout.Row,
        actions: List<FrnkDialogAction> = listOf(cancel, confirm)
    ) = FrnkDialogState(
        title = FrnkStringSource.Raw("Title"),
        eyebrow = FrnkStringSource.Raw("Archive"),
        body = FrnkStringSource.Raw("Body copy"),
        variant = variant,
        actions = actions,
        actionLayout = layout
    )

    /**
     * A finger never lands perfectly still: a real tap carries a few sub-slop move events between down
     * and up, where `performClick` sends none. Compose cancels a press the moment an ancestor consumes a
     * move, so this is the tap that has to survive.
     */
    @Test
    fun a_tap_with_finger_movement_fires_the_action() =
        runComposeUiTest {
            val fired = mutableListOf<String>()
            setFrnkContent { FrnkDialog(state = state(), onAction = { fired += it.key }) }

            onNodeWithText("Cancel").performTouchInput {
                down(center)
                moveBy(Offset(2f, 1f))
                moveBy(Offset(1f, 1f))
                up()
            }
            onNodeWithText("Confirm").performTouchInput {
                down(center)
                moveBy(Offset(-1f, 2f))
                up()
            }

            assertEquals(listOf("cancel", "confirm"), fired)
        }

    /**
     * Drawn with the in-window [FrnkDialogOverlay]: the test harness injects touches straight into a
     * node's own window, so it can't show the dialog window covering the activity. The scrim still has
     * to swallow them inside that window, and inside a `ModalHost` portal, where it shares one.
     */
    @Test
    fun the_scrim_keeps_taps_from_what_lies_beneath() =
        runComposeUiTest {
            var beneathClicks = 0
            setFrnkContent {
                Box(Modifier.fillMaxSize()) {
                    Box(
                        Modifier
                            .fillMaxSize()
                            .testTag("beneath")
                            .clickable { beneathClicks += 1 }
                    )
                    FrnkDialogOverlay(state = state(), onAction = {})
                }
            }

            // Off the card, on the scrim — and on the card's own non-interactive title.
            onNodeWithTag("beneath").performTouchInput { click(Offset(10f, 10f)) }
            onNodeWithText("Title").performTouchInput { click(center) }

            assertEquals(0, beneathClicks)
        }

    @Test
    fun renders_the_copy_with_the_eyebrow_upper_cased_and_token_labels_resolved() =
        runComposeUiTest {
            setFrnkContent { FrnkDialog(state = state(), onAction = {}) }

            onNodeWithText("Title").assertExists()
            onNodeWithText("ARCHIVE").assertExists()
            onNodeWithText("Body copy").assertExists()
            // stringCancel resolved through the theme's string registry.
            onNodeWithText("Cancel").assertExists()
        }

    @Test
    fun the_card_announces_its_title_as_the_pane_title() =
        runComposeUiTest {
            setFrnkContent { FrnkDialog(state = state(), onAction = {}) }

            onNode(SemanticsMatcher.expectValue(SemanticsProperties.PaneTitle, "Title")).assertExists()
        }

    @Test
    fun a_stacked_destructive_dialog_reports_every_action_in_order() =
        runComposeUiTest {
            val fired = mutableListOf<FrnkDialogActionKind>()
            val delete = FrnkDialogAction(key = "delete", label = FrnkStringSource.Raw("Delete"), kind = FrnkDialogActionKind.Destructive)
            val keep = FrnkDialogAction(key = "keep", label = FrnkStringSource.Raw("Keep"), kind = FrnkDialogActionKind.Cancel)
            setFrnkContent {
                FrnkDialog(
                    state =
                        state(
                            variant = FrnkDialogVariant.Destructive,
                            layout = FrnkDialogActionLayout.Stacked,
                            actions = listOf(delete, keep)
                        ),
                    onAction = { fired += it.kind }
                )
            }

            onNodeWithText("Delete").performClick()
            onNodeWithText("Keep").performClick()

            assertEquals(listOf(FrnkDialogActionKind.Destructive, FrnkDialogActionKind.Cancel), fired)
        }

    @Test
    fun the_extra_slot_renders_between_body_and_actions() =
        runComposeUiTest {
            setFrnkContent {
                FrnkDialog(
                    state = state(variant = FrnkDialogVariant.Warning),
                    onAction = {},
                    extra = { FrnkText(FrnkTextState.Raw(text = "ERR · 42")) }
                )
            }

            onNodeWithText("ERR · 42").assertExists()
        }

    @Test
    fun an_action_fires_a_click_haptic() =
        runComposeUiTest {
            val emitted = mutableListOf<HapticType>()
            val haptics = DefaultHapticFeedback(engine = { emitted += it })
            setContent { FrnkTheme(haptics = haptics) { FrnkDialog(state = state(), onAction = {}) } }

            onNodeWithText("Confirm").performClick()

            assertEquals(listOf(HapticType.Click), emitted)
        }
}