package dev.jdgarita.frnk.ui.molecules

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.composeunstyled.theme.Theme
import com.composeunstyled.theme.ThemeToken
import dev.jdgarita.frnk.ui.atoms.FrnkIcon
import dev.jdgarita.frnk.ui.haptics.HapticFeedback
import dev.jdgarita.frnk.ui.haptics.HapticType
import dev.jdgarita.frnk.ui.haptics.LocalFrnkHaptics
import dev.jdgarita.frnk.ui.theme.colorSurfaceVariant
import dev.jdgarita.frnk.ui.theme.colors
import dev.jdgarita.frnk.ui.theme.shapeMedium
import dev.jdgarita.frnk.ui.theme.shapes
import dev.jdgarita.frnk.ui.tokens.FrnkSpacing
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * View state for [FrnkSwipeable].
 *
 * A single action [List] per side covers both behaviours: in [FrnkSwipeBehavior.Dismiss] only the
 * first action of the dragged side is used; in [FrnkSwipeBehavior.Reveal] the whole list renders as a
 * row of tappable buttons. [leftActions] are revealed by dragging the content rightward, [rightActions]
 * by dragging leftward (see [FrnkSwipeDirection]).
 *
 * @property behavior dismiss-and-snap-back vs stay-open-and-reveal.
 * @property direction which side(s) may be opened. [FrnkSwipeDirection.Both] allows either.
 * @property leftActions actions on the left edge (revealed by dragging right).
 * @property rightActions actions on the right edge (revealed by dragging left).
 * @property threshold fraction of the component width the content may be dragged (also the open
 *   distance). 0.5 = up to half the width.
 * @property enabled when false the gesture is inert (the content renders as-is).
 */
@Immutable
data class FrnkSwipeableState(
    val behavior: FrnkSwipeBehavior = FrnkSwipeBehavior.Reveal,
    val direction: FrnkSwipeDirection = FrnkSwipeDirection.Both,
    val leftActions: List<FrnkSwipeAction> = emptyList(),
    val rightActions: List<FrnkSwipeAction> = emptyList(),
    val threshold: Float = 0.5f,
    val enabled: Boolean = true,
)

/** Reveal-mode action button edge length. */
private val ActionButtonSize = 48.dp

/** Past this fraction of the max drag, a REVEAL release stays open; below it snaps back. */
private const val REVEAL_SETTLE_FRACTION = 0.5f

/** Past this fraction of the max drag, a DISMISS release fires the action. */
private const val DISMISS_TRIGGER_FRACTION = 0.9f

/**
 * Wraps [content] in a horizontally swipeable surface that reveals (or dismisses to) token-styled
 * action buttons behind it — the frnk swipe-to-action primitive, usable around any row or card.
 *
 * Behaviour is modelled after [stevdza-san/Swipeable-KMP](https://github.com/stevdza-san/Swipeable-KMP)
 * (MIT). This is a clean-room **headless reimplementation**: no source is copied and there is **no
 * Material3 dependency** — it uses Compose Foundation gestures, [FrnkIcon], frnk tokens, and the
 * ambient [LocalFrnkHaptics] (a [HapticType.Selection] tick as the threshold is crossed, a
 * [HapticType.Click] on a reveal action tap, and a [HapticType.Success] on a dismiss commit, all
 * auto-gated by the global haptics flag).
 *
 * Skeleton: **none, by design** — swipe is interaction chrome; the wrapped [content] owns its own
 * loading skeleton, and callers disable the gesture (via [FrnkSwipeableState.enabled]) while it shows.
 *
 * @param onAction invoked with the tapped/committed [FrnkSwipeAction].
 * @param controller hoist to coordinate a list (one-row-open-at-a-time) or seed an opened state.
 * @param contentBackground optional theme token painted (clipped to the row shape) behind the sliding
 *   [content] so the action panel only shows once dragged aside. `null` leaves [content] to supply its
 *   own opacity — set it to the host's backdrop when wrapping otherwise-transparent content.
 * @param onSwipeProgress optional 0..1 drag-progress callback for custom visual effects.
 */
@Composable
fun FrnkSwipeable(
    state: FrnkSwipeableState,
    onAction: (FrnkSwipeAction) -> Unit,
    modifier: Modifier = Modifier,
    controller: FrnkSwipeController = rememberFrnkSwipeController(),
    contentBackground: ThemeToken<Color>? = null,
    onSwipeProgress: ((Float) -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit,
) {
    val haptics = LocalFrnkHaptics.current
    val scope = rememberCoroutineScope()
    val shape = Theme[shapes][shapeMedium]

    BoxWithConstraints(modifier = modifier) {
        val maxDragPx = constraints.maxWidth * state.threshold

        // Seed / re-anchor an externally-opened side (previews, hoisted controllers, resize). Keyed on
        // maxDragPx only — NOT revealedSide — so an internal settleOpen (which sets revealedSide and runs
        // its own animateTo) is never relaunched and snapped flat; the `!isRunning` guard further skips
        // it whenever a settle animation already owns the offset.
        LaunchedEffect(maxDragPx) {
            val side = controller.revealedSide
            if (side != null && maxDragPx > 0f && !controller.offsetX.isRunning) {
                val target =
                    when (side) {
                        FrnkSwipeDirection.Left -> maxDragPx
                        FrnkSwipeDirection.Right -> -maxDragPx
                        FrnkSwipeDirection.Both -> 0f
                    }
                if (controller.offsetX.value != target) controller.offsetX.snapTo(target)
            }
        }

        Box {
            // Background action layer — its own composable so reading the per-frame offset invalidates
            // only it, not the whole molecule (the foreground reads the offset deferred, in layout).
            FrnkSwipeBackground(
                state = state,
                controller = controller,
                shape = shape,
                haptics = haptics,
                onAction = onAction,
                onAutoClose = { scope.launch { controller.settleClosed() } },
            )

            // Foreground content layer — translated by the drag offset (read deferred to layout).
            Box(
                modifier =
                    Modifier
                        .offset { IntOffset(controller.offsetX.value.roundToInt(), 0) }
                        .let {
                            if (contentBackground != null) {
                                it.clip(shape).background(Theme[colors][contentBackground])
                            } else {
                                it
                            }
                        }.let { base ->
                            if (!state.enabled || maxDragPx <= 0f) {
                                base
                            } else {
                                base.pointerInput(state.direction, maxDragPx, controller.isRevealed) {
                                    // Left actions are revealed by dragging right (+offset), right actions
                                    // by dragging left (-offset). Allow a direction only when its side has
                                    // actions, so the content can't rubber-band over an empty gap.
                                    val allowPositive =
                                        state.direction != FrnkSwipeDirection.Right && state.leftActions.isNotEmpty()
                                    val allowNegative =
                                        state.direction != FrnkSwipeDirection.Left && state.rightActions.isNotEmpty()
                                    val minOffset = if (allowNegative) -maxDragPx else 0f
                                    val maxOffset = if (allowPositive) maxDragPx else 0f
                                    detectHorizontalDragGestures(
                                        onDragStart = {
                                            controller.thresholdCrossed = false
                                            controller.dragTarget = controller.offsetX.value
                                        },
                                        onDragEnd = {
                                            settleAfterDrag(
                                                scope = scope,
                                                controller = controller,
                                                state = state,
                                                maxDragPx = maxDragPx,
                                                haptics = haptics,
                                                onAction = onAction,
                                            )
                                        },
                                    ) { change, dragAmount ->
                                        change.consume()
                                        // Accumulate synchronously into dragTarget (the suspend snapTo
                                        // lags a frame); each launch snaps to the absolute accumulated
                                        // value, so out-of-order completion still lands correctly.
                                        val newOffset =
                                            (controller.dragTarget + dragAmount).coerceIn(minOffset, maxOffset)
                                        controller.dragTarget = newOffset
                                        maybeFireThresholdHaptic(controller, state, maxDragPx, newOffset, haptics)
                                        onSwipeProgress?.invoke((abs(newOffset) / maxDragPx).coerceIn(0f, 1f))
                                        scope.launch { controller.offsetX.snapTo(newOffset) }
                                    }
                                }
                            }
                        },
            ) {
                content()

                // While a reveal panel is open, intercept content taps to close it instead.
                if (state.behavior == FrnkSwipeBehavior.Reveal && controller.isRevealed) {
                    Box(
                        modifier =
                            Modifier
                                .matchParentSize()
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                ) { scope.launch { controller.settleClosed() } },
                    )
                }
            }
        }
    }
}

/**
 * The action layer drawn behind the sliding content. Reads the frame-changing [FrnkSwipeController.offsetX]
 * in its own restart scope so the per-frame invalidation stays isolated here — the parent molecule and
 * the wrapped `content()` don't recompose on every drag tick. Only the dragged side's panel is drawn.
 */
@Composable
private fun BoxScope.FrnkSwipeBackground(
    state: FrnkSwipeableState,
    controller: FrnkSwipeController,
    shape: Shape,
    haptics: HapticFeedback,
    onAction: (FrnkSwipeAction) -> Unit,
    onAutoClose: () -> Unit,
) {
    val offsetValue = controller.offsetX.value
    Box(modifier = Modifier.matchParentSize()) {
        when {
            offsetValue > 0f && state.leftActions.isNotEmpty() ->
                FrnkSwipeActionPanel(
                    actions = state.leftActions,
                    alignment = Alignment.CenterStart,
                    behavior = state.behavior,
                    shape = shape,
                    interactive = controller.revealedSide == FrnkSwipeDirection.Left,
                    haptics = haptics,
                    onAction = onAction,
                    onAutoClose = onAutoClose,
                )

            offsetValue < 0f && state.rightActions.isNotEmpty() ->
                FrnkSwipeActionPanel(
                    actions = state.rightActions,
                    alignment = Alignment.CenterEnd,
                    behavior = state.behavior,
                    shape = shape,
                    interactive = controller.revealedSide == FrnkSwipeDirection.Right,
                    haptics = haptics,
                    onAction = onAction,
                    onAutoClose = onAutoClose,
                )
        }
    }
}

/** Fires a one-shot [HapticType.Selection] the first time a drag passes its commit point. */
private fun maybeFireThresholdHaptic(
    controller: FrnkSwipeController,
    state: FrnkSwipeableState,
    maxDragPx: Float,
    newOffset: Float,
    haptics: HapticFeedback,
) {
    val fraction =
        if (state.behavior == FrnkSwipeBehavior.Dismiss) DISMISS_TRIGGER_FRACTION else REVEAL_SETTLE_FRACTION
    if (!controller.thresholdCrossed && abs(newOffset) >= maxDragPx * fraction) {
        haptics.perform(HapticType.Selection)
        controller.thresholdCrossed = true
    }
}

/**
 * Decides what happens when a drag is released: dismiss-commit, reveal-open, or snap back. Reads the
 * deterministic [FrnkSwipeController.dragTarget] (accumulated synchronously during the drag) so the
 * decision never races the frame-lagged [FrnkSwipeController.offsetX] animation.
 */
private fun settleAfterDrag(
    scope: CoroutineScope,
    controller: FrnkSwipeController,
    state: FrnkSwipeableState,
    maxDragPx: Float,
    haptics: HapticFeedback,
    onAction: (FrnkSwipeAction) -> Unit,
) {
    val offset = controller.dragTarget
    scope.launch {
        when (state.behavior) {
            FrnkSwipeBehavior.Dismiss -> {
                val trigger = maxDragPx * DISMISS_TRIGGER_FRACTION
                when {
                    offset >= trigger && state.leftActions.isNotEmpty() -> {
                        haptics.perform(HapticType.Success)
                        onAction(state.leftActions.first())
                    }

                    offset <= -trigger && state.rightActions.isNotEmpty() -> {
                        haptics.perform(HapticType.Success)
                        onAction(state.rightActions.first())
                    }
                }
                controller.settleClosed()
            }

            FrnkSwipeBehavior.Reveal -> {
                val open = maxDragPx * REVEAL_SETTLE_FRACTION
                when {
                    offset >= open && state.leftActions.isNotEmpty() ->
                        controller.settleOpen(FrnkSwipeDirection.Left, maxDragPx)

                    offset <= -open && state.rightActions.isNotEmpty() ->
                        controller.settleOpen(FrnkSwipeDirection.Right, -maxDragPx)

                    else -> controller.settleClosed()
                }
            }
        }
    }
}

/**
 * The coloured panel behind the content. [FrnkSwipeBehavior.Dismiss] fills with the single action's
 * container colour and aligns its icon to the swiped edge; [FrnkSwipeBehavior.Reveal] lays the actions
 * out as a row of tappable buttons over a neutral tray.
 */
@Composable
private fun FrnkSwipeActionPanel(
    actions: List<FrnkSwipeAction>,
    alignment: Alignment,
    behavior: FrnkSwipeBehavior,
    shape: Shape,
    interactive: Boolean,
    haptics: HapticFeedback,
    onAction: (FrnkSwipeAction) -> Unit,
    onAutoClose: () -> Unit,
) {
    when (behavior) {
        FrnkSwipeBehavior.Dismiss -> {
            val action = actions.first()
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .clip(shape)
                        .background(Theme[colors][action.containerColor]),
                contentAlignment = alignment,
            ) {
                Box(modifier = Modifier.padding(horizontal = FrnkSpacing.xl)) {
                    FrnkIcon(state = action.icon.copy(tint = action.contentColor))
                }
            }
        }

        FrnkSwipeBehavior.Reveal -> {
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .clip(shape)
                        .background(Theme[colors][colorSurfaceVariant]),
            ) {
                Row(
                    modifier =
                        Modifier
                            .fillMaxHeight()
                            .align(alignment)
                            .padding(horizontal = FrnkSpacing.md),
                    horizontalArrangement = Arrangement.spacedBy(FrnkSpacing.sm),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    actions.forEach { action ->
                        Box(
                            modifier =
                                Modifier
                                    .size(ActionButtonSize)
                                    .clip(shape)
                                    .background(Theme[colors][action.containerColor])
                                    .let {
                                        if (interactive) {
                                            it.clickable {
                                                haptics.perform(HapticType.Click)
                                                onAction(action)
                                                if (action.autoClose) onAutoClose()
                                            }
                                        } else {
                                            it
                                        }
                                    },
                            contentAlignment = Alignment.Center,
                        ) {
                            FrnkIcon(state = action.icon.copy(tint = action.contentColor))
                        }
                    }
                }
            }
        }
    }
}
