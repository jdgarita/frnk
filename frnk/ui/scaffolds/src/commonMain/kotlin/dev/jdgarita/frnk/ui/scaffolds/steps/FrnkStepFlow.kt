package dev.jdgarita.frnk.ui.scaffolds.steps

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import dev.jdgarita.frnk.ui.theme.frnkReducedMotion

/**
 * A short run of pages that turn in place: a paywall's "what it is → how it works → choose a plan",
 * a multi-page offer. Only [content] turns; whatever the caller draws around this (a top row with
 * Back and a [FrnkStepMeter], a pinned footer) stays put.
 *
 * [step] is the page shown and [stepIndex] its position in the run, which decides the direction:
 * a page with a higher index slides in from the end edge (forward), a lower one from the start
 * edge (back), so in a right-to-left layout forward comes in from the left, in step with the
 * [FrnkStepMeter]. The turn is [frnkStepTransition]: a quarter-width slide plus a fade, ~380 ms, or a
 * 150 ms crossfade when [reducedMotion] is on.
 *
 * [onBack], when not null, takes system back (Android's back, the iOS back swipe where the host
 * routes it) while this is composed: the caller turns one page back, or swallows it on the first
 * page of a flow that must not close. Leave it null to let back fall through to whatever handler
 * encloses this. The handler is the deepest one in a screen that composes this last, so it
 * outranks the screen's own.
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun <T> FrnkStepFlow(
    step: T,
    stepIndex: (T) -> Int,
    modifier: Modifier = Modifier,
    reducedMotion: Boolean = frnkReducedMotion(),
    onBack: (() -> Unit)? = null,
    label: String = "frnk-step-flow",
    content: @Composable AnimatedContentScope.(T) -> Unit
) {
    BackHandler(enabled = onBack != null) { onBack?.invoke() }
    val layoutDirection = LocalLayoutDirection.current
    AnimatedContent(
        targetState = step,
        modifier = modifier,
        transitionSpec = {
            frnkStepTransition(
                forward = stepIndex(targetState) > stepIndex(initialState),
                reducedMotion = reducedMotion,
                layoutDirection = layoutDirection
            )
        },
        label = label,
        content = content
    )
}

/**
 * The page turn [FrnkStepFlow] plays, for a host that drives its own `AnimatedContent`: the new
 * page slides in from the side it lies on (the end edge going [forward], the start edge going
 * back) and fades up while the old one slides the other way and fades out, [FrnkStepFlowDefaults.TurnMillis]
 * on [FrnkStepFlowDefaults.Easing]. The edges follow [layoutDirection]: the end edge is the left one
 * right-to-left. Under [reducedMotion] it is a plain [FrnkStepFlowDefaults.ReducedFadeMillis]
 * crossfade with no size animation either, so pages of different heights swap without the
 * container growing or shrinking.
 */
fun frnkStepTransition(
    forward: Boolean,
    reducedMotion: Boolean,
    layoutDirection: LayoutDirection = LayoutDirection.Ltr
): ContentTransform {
    if (reducedMotion) {
        val fade = tween<Float>(FrnkStepFlowDefaults.ReducedFadeMillis)
        return (fadeIn(fade) togetherWith fadeOut(fade)).using(null)
    }
    val sign = frnkStepSlideSign(forward, layoutDirection)
    val slide = tween<IntOffset>(FrnkStepFlowDefaults.TurnMillis, easing = FrnkStepFlowDefaults.Easing)
    val fade = tween<Float>(FrnkStepFlowDefaults.TurnMillis, easing = FrnkStepFlowDefaults.Easing)
    return (slideInHorizontally(slide) { width -> sign * width / FrnkStepFlowDefaults.SlideDivisor } + fadeIn(fade)) togetherWith
        (slideOutHorizontally(slide) { width -> -sign * width / FrnkStepFlowDefaults.SlideDivisor } + fadeOut(fade))
}

/**
 * Which way the incoming page travels on the x axis: `1` from the right, `-1` from the left. Forward
 * comes from the end edge, which is the right one left-to-right and the left one right-to-left.
 */
internal fun frnkStepSlideSign(
    forward: Boolean,
    layoutDirection: LayoutDirection
): Int {
    val fromEnd = if (forward) 1 else -1
    return if (layoutDirection == LayoutDirection.Rtl) -fromEnd else fromEnd
}

/** The page turn's timing, shared by [frnkStepTransition] and hosts that match it elsewhere. */
object FrnkStepFlowDefaults {
    /** How long a page takes to turn. */
    const val TurnMillis: Int = 380

    /** The crossfade that replaces the turn under reduced motion. */
    const val ReducedFadeMillis: Int = 150

    /** `cubic-bezier(.2, .8, .2, 1)`: quick out of the gate, a long soft landing. */
    val Easing: Easing = CubicBezierEasing(0.2f, 0.8f, 0.2f, 1f)

    /** How far a page travels as it turns: a quarter of the width, enough to read as a step sideways. */
    const val SlideDivisor: Int = 4
}