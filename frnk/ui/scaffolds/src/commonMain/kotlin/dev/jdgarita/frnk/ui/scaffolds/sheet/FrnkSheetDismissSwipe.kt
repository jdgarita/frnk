package dev.jdgarita.frnk.ui.scaffolds.sheet

import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * How much of a swipe it takes to dismiss a modal sheet: a release settles at Hidden once the
 * drag has covered [positionalThreshold] of the sheet's travel **or** is moving faster than
 * [velocityThreshold]; anything less springs back. The two are compose-unstyled's own knobs,
 * named here so a sheet asks for a feel rather than for numbers.
 */
@Immutable
class FrnkSheetDismissSwipe(
    /** The drag, as a function of the sheet's whole travel from its detent to Hidden. */
    val positionalThreshold: (totalDistance: Dp) -> Dp,
    /** The flick, in dp per second. */
    val velocityThreshold: Dp
) {
    companion object {
        /** The library's defaults — 56 dp of drag, or 125 dp/s — and every sheet's unless it says otherwise. */
        val Default: FrnkSheetDismissSwipe = FrnkSheetDismissSwipe(positionalThreshold = { 56.dp }, velocityThreshold = 125.dp)

        /**
         * For a sheet whose body scrolls under the finger: a pull that begins as the scroll's
         * overscroll at its top is the same gesture as a swipe down, so it takes a drag of a
         * fifth of the sheet's travel, or a hard flick, to leave — a swipe the user means is
         * still one swipe, only a longer or a faster one. Pair it with the sheet-body scroll
         * connection on the scroller: the flick half is for the sheet's own handle, and a
         * release over the body is judged on the drag alone.
         */
        val Heavy: FrnkSheetDismissSwipe =
            FrnkSheetDismissSwipe(
                positionalThreshold = { totalDistance -> totalDistance * HEAVY_SWIPE_FRACTION },
                velocityThreshold = HEAVY_SWIPE_VELOCITY
            )
    }
}

private const val HEAVY_SWIPE_FRACTION = 0.2f
private val HEAVY_SWIPE_VELOCITY = 1000.dp