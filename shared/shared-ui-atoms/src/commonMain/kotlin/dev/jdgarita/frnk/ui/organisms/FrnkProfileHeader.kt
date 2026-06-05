package dev.jdgarita.frnk.ui.organisms

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import dev.jdgarita.frnk.ui.atoms.FrnkText
import dev.jdgarita.frnk.ui.atoms.FrnkTextState
import dev.jdgarita.frnk.ui.molecules.FrnkLabeledValue
import dev.jdgarita.frnk.ui.molecules.FrnkLabeledValueOrientation
import dev.jdgarita.frnk.ui.molecules.FrnkLabeledValueState
import dev.jdgarita.frnk.ui.theme.colorOnSurfaceVariant
import dev.jdgarita.frnk.ui.theme.colorPrimaryContainer
import dev.jdgarita.frnk.ui.theme.colorSurface
import dev.jdgarita.frnk.ui.theme.colors
import dev.jdgarita.frnk.ui.theme.shapeCard
import dev.jdgarita.frnk.ui.theme.shapeFull
import dev.jdgarita.frnk.ui.theme.shapes
import dev.jdgarita.frnk.ui.theme.spacing
import dev.jdgarita.frnk.ui.theme.spacingLg
import dev.jdgarita.frnk.ui.theme.spacingMd
import dev.jdgarita.frnk.ui.theme.spacingSm
import dev.jdgarita.frnk.ui.theme.spacingXxs
import dev.jdgarita.frnk.ui.tokens.FrnkIconSize

/**
 * Sealed visual state for [FrnkProfileHeader] — an **organism**: the header block for a profile,
 * account, or detail screen (a leading avatar glyph + name/subtitle, with an optional even row of
 * [FrnkLabeledValueState.Content] stat tiles). [Content] holds the name, avatar (set its
 * `contentDescription` if it conveys identity), optional subtitle, and stats (each forced to
 * [Stacked][FrnkLabeledValueOrientation.Stacked]); [Skeleton] (an `object`) renders the whole card as
 * placeholder blocks (the avatar chip drops its `primaryContainer` fill while loading). Toolkit-standard
 * sealed-state + `Skeleton`-object shape.
 */
sealed interface FrnkProfileHeaderState {
    @Immutable
    data class Content(
        val name: String,
        val avatar: FrnkIconState,
        val subtitle: String? = null,
        val stats: List<FrnkLabeledValueState.Content> = emptyList(),
    ) : FrnkProfileHeaderState

    data object Skeleton : FrnkProfileHeaderState
}

/** Number of placeholder stat tiles rendered in [FrnkProfileHeaderState.Skeleton]. */
private const val SKELETON_STAT_COUNT = 3

/** A profile/account header card: circular avatar + name/subtitle, with an optional even stats row. */
@Composable
fun FrnkProfileHeader(
    state: FrnkProfileHeaderState,
    modifier: Modifier = Modifier,
) {
    val loading = state is FrnkProfileHeaderState.Skeleton

    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .clip(Theme[shapes][shapeCard])
                .background(Theme[colors][colorSurface])
                .padding(Theme[spacing][spacingLg]),
        verticalArrangement = Arrangement.spacedBy(Theme[spacing][spacingMd]),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Theme[spacing][spacingMd]),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier =
                    Modifier
                        .clip(Theme[shapes][shapeFull])
                        // Drop the brand fill while loading so the chip's rim doesn't peek around the
                        // glyph skeleton block (same precedent as FrnkSwitch).
                        .let {
                            if (loading) it else it.background(Theme[colors][colorPrimaryContainer])
                        }.padding(Theme[spacing][spacingSm]),
                contentAlignment = Alignment.Center,
            ) {
                FrnkIcon(
                    state =
                        when (state) {
                            is FrnkProfileHeaderState.Content -> state.avatar
                            // Size the placeholder to the header's conventional avatar (lg) so the
                            // chip doesn't resize when the real avatar loads.
                            FrnkProfileHeaderState.Skeleton -> FrnkIconState.Skeleton(size = FrnkIconSize.lg)
                        },
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(Theme[spacing][spacingXxs]),
            ) {
                when (state) {
                    is FrnkProfileHeaderState.Content -> {
                        FrnkText(state = FrnkTextState.Title(text = state.name))
                        state.subtitle?.let {
                            FrnkText(
                                state = FrnkTextState.BodyMedium(text = it, color = colorOnSurfaceVariant),
                            )
                        }
                    }

                    FrnkProfileHeaderState.Skeleton -> {
                        FrnkText(state = FrnkTextState.Skeleton)
                        FrnkText(state = FrnkTextState.Skeleton)
                    }
                }
            }
        }

        val stats: List<FrnkLabeledValueState> =
            when (state) {
                is FrnkProfileHeaderState.Content -> state.stats.map { it.copy(orientation = FrnkLabeledValueOrientation.Stacked) }
                FrnkProfileHeaderState.Skeleton -> List(SKELETON_STAT_COUNT) { FrnkLabeledValueState.Skeleton }
            }
        if (stats.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Theme[spacing][spacingMd]),
            ) {
                stats.forEach { stat ->
                    FrnkLabeledValue(state = stat, modifier = Modifier.weight(1f))
                }
            }
        }
    }
}
