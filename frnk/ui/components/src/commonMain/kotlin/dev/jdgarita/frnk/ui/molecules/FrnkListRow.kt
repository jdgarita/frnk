package dev.jdgarita.frnk.ui.molecules

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.composeunstyled.theme.Theme
import com.composeunstyled.theme.ThemeToken
import dev.jdgarita.frnk.ui.atoms.FrnkIcon
import dev.jdgarita.frnk.ui.atoms.FrnkIconState
import dev.jdgarita.frnk.ui.atoms.FrnkSkeletonBox
import dev.jdgarita.frnk.ui.atoms.FrnkText
import dev.jdgarita.frnk.ui.atoms.FrnkTextState
import dev.jdgarita.frnk.ui.haptics.HapticType
import dev.jdgarita.frnk.ui.haptics.LocalFrnkHaptics
import dev.jdgarita.frnk.ui.theme.colorBackground
import dev.jdgarita.frnk.ui.theme.colorOnSurfaceVariant
import dev.jdgarita.frnk.ui.theme.shapeMedium
import dev.jdgarita.frnk.ui.theme.shapes
import dev.jdgarita.frnk.ui.theme.spacing
import dev.jdgarita.frnk.ui.theme.spacingMd
import dev.jdgarita.frnk.ui.theme.spacingSm
import dev.jdgarita.frnk.ui.theme.spacingXxs

/**
 * Sealed visual state for [FrnkListRow] — the workhorse molecule for settings-style lists, menus, and
 * detail rows (a leading icon + title/subtitle column + trailing slot). [Content] holds the
 * title/subtitle/optional leading icon; [Skeleton] (an `object`) collapses the whole row to a single
 * placeholder block. Toolkit-standard sealed-state + `Skeleton`-object shape.
 */
sealed interface FrnkListRowState {
    @Immutable
    data class Content(
        val title: String,
        val subtitle: String? = null,
        val icon: FrnkIconState? = null,
    ) : FrnkListRowState

    data object Skeleton : FrnkListRowState
}

/**
 * A single list row: `[icon]  title / subtitle  [trailing]`. Pass [onClick] to make the whole row
 * tappable — press feedback (ripple) and a [HapticType.Click] fire automatically, both gated off
 * while the skeleton is showing. [trailing] is an optional slot for a chevron, switch, badge, etc.
 *
 * **Optional swipe actions.** Pass a [swipe] state to wrap the row in [FrnkSwipeable], revealing or
 * dismissing to action buttons (delete/archive/…). Swipe is **off by default** (`null`) — when null
 * the row renders exactly as before — and is suppressed while the skeleton shows. Action taps are
 * reported through [onSwipeAction]. While swiping, the sliding row paints an opaque [surfaceColor] so
 * the action panel only shows once dragged aside; set [surfaceColor] to the host's backdrop when the
 * row sits on a non-default surface (e.g. a `colorSurface` card) — it defaults to [colorBackground].
 */
@Composable
fun FrnkListRow(
    state: FrnkListRowState,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    trailing: (@Composable () -> Unit)? = null,
    swipe: FrnkSwipeableState? = null,
    onSwipeAction: (FrnkSwipeAction) -> Unit = {},
    surfaceColor: ThemeToken<Color> = colorBackground,
) {
    if (swipe == null || state is FrnkListRowState.Skeleton) {
        FrnkListRowContent(state = state, onClick = onClick, trailing = trailing, modifier = modifier)
    } else {
        FrnkSwipeable(
            state = swipe,
            onAction = onSwipeAction,
            modifier = modifier,
            contentBackground = surfaceColor,
        ) {
            FrnkListRowContent(state = state, onClick = onClick, trailing = trailing)
        }
    }
}

/** The row body shared by the plain and swipe-wrapped paths. */
@Composable
private fun FrnkListRowContent(
    state: FrnkListRowState,
    onClick: (() -> Unit)?,
    trailing: (@Composable () -> Unit)?,
    modifier: Modifier = Modifier,
) {
    val content =
        when (state) {
            is FrnkListRowState.Content -> state
            FrnkListRowState.Skeleton -> {
                FrnkSkeletonBox(modifier.fillMaxWidth().height(56.dp), shape = shapeMedium)
                return
            }
        }

    val haptics = LocalFrnkHaptics.current
    val interactive = onClick != null

    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .clip(Theme[shapes][shapeMedium])
                .let {
                    if (interactive) {
                        it.clickable {
                            haptics.perform(HapticType.Click)
                            onClick()
                        }
                    } else {
                        it
                    }
                }.padding(horizontal = Theme[spacing][spacingMd], vertical = Theme[spacing][spacingSm]),
        horizontalArrangement = Arrangement.spacedBy(Theme[spacing][spacingMd]),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        content.icon?.let { FrnkIcon(state = it) }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(Theme[spacing][spacingXxs]),
        ) {
            FrnkText(state = FrnkTextState.TitleMedium(text = content.title))
            content.subtitle?.let {
                FrnkText(state = FrnkTextState.BodySmall(text = it, color = colorOnSurfaceVariant))
            }
        }
        trailing?.invoke()
    }
}
