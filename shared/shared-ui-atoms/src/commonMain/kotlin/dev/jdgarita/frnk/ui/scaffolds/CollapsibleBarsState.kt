package dev.jdgarita.frnk.ui.scaffolds

import androidx.compose.animation.core.animate
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.Velocity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * Shared "collapse on scroll" coordinator for a screen's top bar and floating bottom bar. A single
 * instance is observed by **both** bars so they hide and reveal in lock-step: scrolling the content
 * down (finger up) slides the top bar up off-screen and the bottom bar down off-screen; scrolling up
 * brings them both back.
 *
 * Expose [nestedScrollConnection] on an ancestor of the scrolling content (e.g. via
 * [FrnkScreenScaffold]); each bar consumes [collapseFraction] (0 = fully shown, 1 = fully hidden) to
 * translate itself by its own height. The fraction tracks how far the content has **actually
 * scrolled**, so the bars only collapse when the list extends beyond the viewport — a short list that
 * fits on screen consumes no scroll and leaves the bars in place. It snaps to the nearer end when the
 * gesture/fling ends.
 *
 * Obtain one with [rememberCollapsibleBarsState]. When a host swaps the visible screen (e.g. a tab
 * change) it should call [reset] so the new screen always starts with its bars shown.
 */
@Stable
class CollapsibleBarsState internal constructor(
    private val scope: CoroutineScope,
) {
    /** 0 = bars fully shown, 1 = bars fully hidden. Both bars read this to compute their offset. */
    var collapseFraction by mutableFloatStateOf(0f)
        private set

    // Scroll distance (px) over which the bars fully collapse — set by the host scaffold to the top
    // bar's height, so roughly one bar-height of scrolling toggles visibility. Zero until measured,
    // in which case scroll deltas are ignored (bars stay put).
    private var collapseDistancePx = 0f
    private var settleJob: Job? = null

    /** Called by the scaffold once it knows the top bar's pixel height. */
    internal fun setCollapseDistance(px: Float) {
        if (px > 0f) collapseDistancePx = px
    }

    val nestedScrollConnection =
        object : NestedScrollConnection {
            // Drive the collapse from what the content *actually* scrolled (onPostScroll's `consumed`),
            // never from the raw gesture delta. The bars therefore only move when the list can scroll:
            // a short list that fits the viewport consumes nothing, so they stay put. Never consume any
            // scroll ourselves — the content scrolls fully behind the floating bars.
            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource,
            ): Offset {
                if (collapseDistancePx > 0f && consumed.y != 0f) {
                    // A live gesture overrides any in-flight settle animation.
                    settleJob?.cancel()
                    // Content scrolled down → consumed.y < 0 → fraction rises toward 1 (hidden);
                    // scrolled up → consumed.y > 0 → toward 0 (shown).
                    collapseFraction = (collapseFraction - consumed.y / collapseDistancePx).coerceIn(0f, 1f)
                }
                return Offset.Zero
            }

            override suspend fun onPostFling(
                consumed: Velocity,
                available: Velocity,
            ): Velocity {
                settle()
                return Velocity.Zero
            }
        }

    // Snap to whichever end is nearer so the bars never rest half-collapsed.
    private fun settle() {
        val target = if (collapseFraction >= 0.5f) 1f else 0f
        if (collapseFraction == target) return
        settleJob?.cancel()
        settleJob =
            scope.launch {
                animate(collapseFraction, target) { value, _ -> collapseFraction = value }
            }
    }

    /** Snap the bars back to fully shown — call when the host swaps the visible screen. */
    fun reset() {
        settleJob?.cancel()
        collapseFraction = 0f
    }
}

/** Remembers a [CollapsibleBarsState] scoped to the current composition. */
@Composable
fun rememberCollapsibleBarsState(): CollapsibleBarsState {
    val scope = rememberCoroutineScope()
    return remember(scope) { CollapsibleBarsState(scope) }
}

/**
 * Translate a floating bottom bar (height [barHeight]) down off the bottom edge in lock-step with
 * [collapsibleBars]: `collapseFraction` 0 → resting, 1 → fully hidden one bar-height below. The
 * `collapseFraction` read is deferred to layout (lambda-based [offset]), so a scroll that only moves
 * the bar doesn't recompose. The single source of the collapse-offset math, shared by
 * [BottomNavScaffoldContent] and host-rendered bars (e.g. a `NavHost`-driven bottom bar).
 */
fun Modifier.collapsibleBarOffset(
    collapsibleBars: CollapsibleBarsState,
    barHeight: Dp,
): Modifier =
    offset {
        IntOffset(x = 0, y = (collapsibleBars.collapseFraction * barHeight.toPx()).roundToInt())
    }
