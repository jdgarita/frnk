package dev.jdgarita.frnk.ui.atoms

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.isToggleable
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

@OptIn(ExperimentalTestApi::class)
class FrnkSwitchTest : RobolectricComposeTest() {
    @Test
    fun checkedState_reportsOn() =
        runComposeUiTest {
            setFrnkContent { FrnkSwitch(state = FrnkSwitchState.Content(checked = true), onCheckedChange = {}) }
            onNode(isToggleable()).assertIsOn()
        }

    @Test
    fun uncheckedState_reportsOff() =
        runComposeUiTest {
            setFrnkContent { FrnkSwitch(state = FrnkSwitchState.Content(checked = false), onCheckedChange = {}) }
            onNode(isToggleable()).assertIsOff()
        }

    @Test
    fun toggling_emitsTheFlippedValue() =
        runComposeUiTest {
            var received: Boolean? = null
            setFrnkContent {
                FrnkSwitch(state = FrnkSwitchState.Content(checked = false), onCheckedChange = { received = it })
            }

            onNode(isToggleable()).performClick()

            assertEquals(true, received)
        }

    @Test
    fun disabled_isNotInteractiveAndNeverEmits() =
        runComposeUiTest {
            var received: Boolean? = null
            setFrnkContent {
                FrnkSwitch(
                    state = FrnkSwitchState.Content(checked = false, enabled = false),
                    onCheckedChange = { received = it }
                )
            }

            onNode(isToggleable()).assertIsNotEnabled().performClick()
            // The tap is injected but the disabled toggleable swallows it — no emission.
            // (`toggling_emitsTheFlippedValue` is the positive control proving this harness *does* emit.)
            assertNull(received)
        }

    @Test
    fun skeleton_suppressesInteraction() =
        runComposeUiTest {
            var received: Boolean? = null
            setFrnkContent {
                FrnkSwitch(
                    state = FrnkSwitchState.Skeleton,
                    onCheckedChange = { received = it }
                )
            }

            // The skeleton state renders an inert placeholder — no toggleable node exists to tap.
            onNode(isToggleable()).assertDoesNotExist()
            assertNull(received)
        }
}