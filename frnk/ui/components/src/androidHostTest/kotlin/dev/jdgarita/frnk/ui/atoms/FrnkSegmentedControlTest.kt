package dev.jdgarita.frnk.ui.atoms

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

@OptIn(ExperimentalTestApi::class)
class FrnkSegmentedControlTest : RobolectricComposeTest() {
    private val options = listOf("System", "Light", "Dark")

    @Test
    fun rendersEveryOptionLabel() =
        runComposeUiTest {
            setFrnkContent {
                FrnkSegmentedControl(
                    state = FrnkSegmentedControlState.Content(options = options, selectedIndex = 0),
                    onOptionSelected = {},
                )
            }
            options.forEach { onNodeWithText(it).assertIsDisplayed() }
        }

    @Test
    fun tappingAnOption_emitsItsIndex() =
        runComposeUiTest {
            var selected: Int? = null
            setFrnkContent {
                FrnkSegmentedControl(
                    state = FrnkSegmentedControlState.Content(options = options, selectedIndex = 0),
                    onOptionSelected = { selected = it },
                )
            }

            onNodeWithText("Dark").performClick()

            assertEquals(2, selected)
        }

    @Test
    fun tappingTheAlreadySelectedOption_stillEmits() =
        runComposeUiTest {
            var selected: Int? = null
            setFrnkContent {
                FrnkSegmentedControl(
                    state = FrnkSegmentedControlState.Content(options = options, selectedIndex = 1),
                    onOptionSelected = { selected = it },
                )
            }

            // The reducer-free atom reports every tap (only the haptic is gated on an actual change).
            onNodeWithText("Light").performClick()

            assertEquals(1, selected)
        }

    @Test
    fun outOfRangeSelectedIndex_staysInteractive() =
        runComposeUiTest {
            var selected: Int? = null
            setFrnkContent {
                FrnkSegmentedControl(
                    // selectedIndex is coerced into bounds; the control must still render + respond.
                    state = FrnkSegmentedControlState.Content(options = options, selectedIndex = 99),
                    onOptionSelected = { selected = it },
                )
            }

            options.forEach { onNodeWithText(it).assertIsDisplayed() }
            onNodeWithText("System").performClick()
            assertEquals(0, selected)
        }

    @Test
    fun disabled_doesNotEmitOnTap() =
        runComposeUiTest {
            var selected: Int? = null
            setFrnkContent {
                FrnkSegmentedControl(
                    state = FrnkSegmentedControlState.Content(options = options, selectedIndex = 0, enabled = false),
                    onOptionSelected = { selected = it },
                )
            }

            onNodeWithText("Dark").performClick()

            assertNull(selected)
        }

    @Test
    fun skeleton_doesNotEmitOnTap() =
        runComposeUiTest {
            var selected: Int? = null
            setFrnkContent {
                FrnkSegmentedControl(
                    state = FrnkSegmentedControlState.Skeleton,
                    onOptionSelected = { selected = it },
                )
            }

            // The skeleton state renders no option labels, so there is nothing to tap.
            onNodeWithText("Dark").assertDoesNotExist()
            assertNull(selected)
        }
}
