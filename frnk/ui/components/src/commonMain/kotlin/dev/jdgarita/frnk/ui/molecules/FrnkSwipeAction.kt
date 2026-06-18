package dev.jdgarita.frnk.ui.molecules

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import com.composeunstyled.theme.ThemeToken
import dev.jdgarita.frnk.ui.atoms.FrnkIconState
import dev.jdgarita.frnk.ui.theme.colorError
import dev.jdgarita.frnk.ui.theme.colorOnError

/**
 * How a [FrnkSwipeable] reacts when the drag passes its trigger point.
 *
 * - [Dismiss] — a single action per side that fires the moment the drag is released past the
 *   threshold, then the content snaps back to centre (swipe-to-delete style).
 * - [Reveal] — the content stays held open, exposing a row of tappable action buttons; the panel
 *   closes when an action is tapped (and it opts into [FrnkSwipeAction.autoClose]) or the content is
 *   tapped.
 */
enum class FrnkSwipeBehavior { Dismiss, Reveal }

/**
 * Names the side of the row an action panel sits on — and, equivalently, which way the content is
 * dragged to reveal it.
 *
 * - [Left] — the left-edge panel, revealed by dragging the content **rightward**.
 * - [Right] — the right-edge panel, revealed by dragging the content **leftward**.
 * - [Both] — either side may be opened.
 *
 * These are **physical** directions; the component is not RTL-mirrored today (a future `start`/`end`
 * API is the natural extension — deliberately omitted for now).
 */
enum class FrnkSwipeDirection { Left, Right, Both }

/**
 * One swipe action rendered behind a [FrnkSwipeable]'s content.
 *
 * Following the [dev.jdgarita.frnk.ui.atoms.FrnkTopAppBarAction] precedent, the action carries no
 * callback of its own — it holds a stable [key] and the molecule reports taps through a single
 * `onAction: (FrnkSwipeAction) -> Unit`. Colours are theme tokens (resolved at draw time, like
 * [FrnkIconState.tint]) so they stay dark-mode-correct and host-overridable. The molecule forces the
 * [icon]'s tint to [contentColor] so the glyph always contrasts [containerColor].
 *
 * @property icon the glyph drawn in the action button (its own `tint` is overridden to [contentColor]).
 * @property containerColor the button/background fill token; defaults to [colorError] (destructive).
 * @property contentColor the icon tint token; defaults to [colorOnError].
 * @property autoClose [FrnkSwipeBehavior.Reveal] only — close the panel after this action is tapped.
 * @property label optional accessibility / catalogue label; also the default [key].
 * @property key stable identifier the host switches on in `onAction`. Defaults to [label] then the
 *   icon's content description.
 */
@Immutable
data class FrnkSwipeAction(
    val icon: FrnkIconState.Content,
    val containerColor: ThemeToken<Color> = colorError,
    val contentColor: ThemeToken<Color> = colorOnError,
    val autoClose: Boolean = true,
    val label: String? = null,
    val key: String = label ?: icon.contentDescription.orEmpty()
)