package dev.jdgarita.frnk.ui.molecules

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

internal const val FRNK_SWIPE_SETTLE_DURATION_MS = 300

/**
 * State holder for [FrnkSwipeable]: owns the foreground translation and which side is held open.
 *
 * Created via [rememberFrnkSwipeController] and kept alive across recompositions (the engine reads
 * and animates [offsetX] in response to drag gestures). Hoist your own instance to coordinate a list
 * — e.g. to enforce "only one row open at a time" — or to seed a preview/test in an opened state via
 * [initialRevealed].
 *
 * @property offsetX foreground horizontal translation in px. Positive = dragged right (the left-side
 *   actions show); negative = dragged left (the right-side actions show).
 * @property revealedSide which side is currently held open, or `null` when closed/resting.
 */
@Stable
class FrnkSwipeController internal constructor(
    initialRevealed: FrnkSwipeDirection? = null
) {
    val offsetX: Animatable<Float, AnimationVector1D> = Animatable(0f)

    var revealedSide: FrnkSwipeDirection? by mutableStateOf(initialRevealed)
        internal set

    val isRevealed: Boolean get() = revealedSide != null

    /**
     * The drag offset accumulated **synchronously** during a gesture (px). [offsetX] is an [Animatable]
     * whose `snapTo` is suspend, so it lags the finger by a frame; this plain field is the deterministic
     * source of truth the drag handler reads/writes and the release logic settles against.
     */
    internal var dragTarget: Float = 0f

    /** Per-gesture latch so the threshold-cross haptic fires at most once per drag. */
    internal var thresholdCrossed: Boolean = false

    /** Animate the content back to centre and clear [revealedSide]. */
    suspend fun settleClosed(animationSpec: AnimationSpec<Float> = tween(FRNK_SWIPE_SETTLE_DURATION_MS)) {
        revealedSide = null
        offsetX.animateTo(targetValue = 0f, animationSpec = animationSpec)
    }

    /** Animate the content to [targetPx] and hold the [side] open. */
    internal suspend fun settleOpen(
        side: FrnkSwipeDirection,
        targetPx: Float,
        animationSpec: AnimationSpec<Float> = tween(FRNK_SWIPE_SETTLE_DURATION_MS)
    ) {
        revealedSide = side
        offsetX.animateTo(targetValue = targetPx, animationSpec = animationSpec)
    }
}

/**
 * Remembers a [FrnkSwipeController] for a [FrnkSwipeable]. Pass [initialRevealed] to start a
 * preview/test with a side already open (the molecule snaps the offset once it has measured a width).
 */
@Composable
fun rememberFrnkSwipeController(initialRevealed: FrnkSwipeDirection? = null): FrnkSwipeController =
    remember { FrnkSwipeController(initialRevealed) }