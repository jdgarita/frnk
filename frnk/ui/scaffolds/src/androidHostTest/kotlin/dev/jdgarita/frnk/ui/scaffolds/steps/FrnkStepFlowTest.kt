package dev.jdgarita.frnk.ui.scaffolds.steps

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.test.waitUntilDoesNotExist
import androidx.compose.ui.test.waitUntilExactlyOneExists
import dev.jdgarita.frnk.ui.atoms.RobolectricComposeTest
import dev.jdgarita.frnk.ui.atoms.setFrnkContent
import dev.jdgarita.frnk.ui.theme.LocalFrnkReducedMotion
import kotlin.test.Test
import kotlin.test.assertEquals

/** [FrnkStepFlow] turns to the page it is given; [FrnkStepMeter] colours and reads one node. */
@OptIn(ExperimentalTestApi::class)
class FrnkStepFlowTest : RobolectricComposeTest() {
    @Test
    fun the_flow_shows_the_page_it_is_given_and_turns_to_the_next() =
        runComposeUiTest {
            var step by mutableIntStateOf(0)
            setFrnkContent {
                CompositionLocalProvider(LocalFrnkReducedMotion provides true) {
                    FrnkStepFlow(step = step, stepIndex = { it }) { page ->
                        Box(Modifier.testTag("page-$page"))
                    }
                }
            }
            waitUntilExactlyOneExists(hasTestTag("page-0"))

            runOnIdle { step = 1 }
            waitUntilExactlyOneExists(hasTestTag("page-1"))
            waitUntilDoesNotExist(hasTestTag("page-0"))
        }

    @Test
    fun the_meter_is_one_node_read_as_its_description() =
        runComposeUiTest {
            setFrnkContent { FrnkStepMeter(current = 1, count = 3, contentDescription = "Step 2 of 3") }
            onAllNodesWithContentDescription("Step 2 of 3").assertCountEquals(1)
        }

    @Test
    fun segments_behind_are_done_the_current_is_active_and_the_rest_ahead() {
        val colors = (0 until 3).map { frnkStepSegmentColor(it, current = 1, done = Color.Black, active = Color.Green, ahead = Color.Gray) }
        assertEquals(listOf(Color.Black, Color.Green, Color.Gray), colors)
    }
}