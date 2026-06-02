package dev.jdgarita.frnk.ui.molecules

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import com.composeunstyled.theme.Theme
import dev.jdgarita.frnk.ui.atoms.FrnkIcon
import dev.jdgarita.frnk.ui.atoms.FrnkIconState
import dev.jdgarita.frnk.ui.atoms.FrnkSkeleton
import dev.jdgarita.frnk.ui.atoms.FrnkText
import dev.jdgarita.frnk.ui.atoms.FrnkTextState
import dev.jdgarita.frnk.ui.atoms.frnkSkeleton
import dev.jdgarita.frnk.ui.haptics.HapticType
import dev.jdgarita.frnk.ui.haptics.LocalFrnkHaptics
import dev.jdgarita.frnk.ui.theme.colorOnSurfaceVariant
import dev.jdgarita.frnk.ui.theme.shapeMedium
import dev.jdgarita.frnk.ui.theme.shapes
import dev.jdgarita.frnk.ui.tokens.FrnkSpacing

/**
 * View state for [FrnkListRow] — a leading icon + title/subtitle column + trailing slot row, the
 * workhorse molecule for settings-style lists, menus, and detail rows. Composed purely from the
 * [FrnkIcon] and [FrnkText] atoms; the trailing region is a caller-supplied slot.
 *
 * @property title the primary line (rendered as `TitleMedium`).
 * @property subtitle optional secondary line (rendered as muted `BodySmall`); omitted when `null`.
 * @property icon optional leading icon; omitted when `null` so the row works icon-less.
 * @property skeleton loading placeholder. Content-bearing, so the whole row collapses to a single
 *   skeleton block (rounded to [shapeMedium]) and stops responding to taps while `enabled`.
 */
@Immutable
data class FrnkListRowState(
    val title: String,
    val subtitle: String? = null,
    val icon: FrnkIconState? = null,
    val skeleton: FrnkSkeleton = FrnkSkeleton(),
)

/**
 * A single list row: `[icon]  title / subtitle  [trailing]`. Pass [onClick] to make the whole row
 * tappable — press feedback (ripple) and a [HapticType.Click] fire automatically, both gated off
 * while the skeleton is showing. [trailing] is an optional slot for a chevron, switch, badge, etc.
 */
@Composable
fun FrnkListRow(
    state: FrnkListRowState,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    trailing: (@Composable () -> Unit)? = null,
) {
    val haptics = LocalFrnkHaptics.current
    val interactive = onClick != null && !state.skeleton.enabled

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
                }.frnkSkeleton(state.skeleton, shape = shapeMedium)
                .padding(horizontal = FrnkSpacing.md, vertical = FrnkSpacing.sm),
        horizontalArrangement = Arrangement.spacedBy(FrnkSpacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        state.icon?.let { FrnkIcon(state = it) }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(FrnkSpacing.xxs),
        ) {
            FrnkText(state = FrnkTextState.TitleMedium(text = state.title))
            state.subtitle?.let {
                FrnkText(state = FrnkTextState.BodySmall(text = it, color = colorOnSurfaceVariant))
            }
        }
        trailing?.invoke()
    }
}
