package dev.jdgarita.frnk.ui.scaffolds.sheet

import androidx.compose.ui.unit.dp
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** Pins the two swipe feels: the library's own for every sheet, and the paywall's heavier one. */
class FrnkSheetDismissSwipeTest {
    @Test
    fun defaultIsTheLibrarysOwn() {
        assertEquals(56.dp, FrnkSheetDismissSwipe.Default.positionalThreshold(700.dp))
        assertEquals(125.dp, FrnkSheetDismissSwipe.Default.velocityThreshold)
    }

    @Test
    fun heavyScalesWithTheSheetsTravelAndNeedsADecidedFlick() {
        // A near-full sheet on a 390×844 phone travels roughly 800 dp to Hidden; a fifth of it
        // is the drag that leaves, so a pull that began as the story's overscroll springs back.
        assertEquals(160f, FrnkSheetDismissSwipe.Heavy.positionalThreshold(800.dp).value, 0.01f)
        assertTrue(FrnkSheetDismissSwipe.Heavy.positionalThreshold(800.dp) > FrnkSheetDismissSwipe.Default.positionalThreshold(800.dp))
        assertTrue(FrnkSheetDismissSwipe.Heavy.velocityThreshold > FrnkSheetDismissSwipe.Default.velocityThreshold)
    }
}