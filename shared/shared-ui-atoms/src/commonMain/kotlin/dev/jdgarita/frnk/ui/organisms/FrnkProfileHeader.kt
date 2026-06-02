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
import dev.jdgarita.frnk.ui.atoms.FrnkSkeleton
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
import dev.jdgarita.frnk.ui.tokens.FrnkSpacing

/**
 * View state for [FrnkProfileHeader] — an **organism**: a leading avatar glyph + a name/subtitle block,
 * with an optional row of [FrnkLabeledValueState] stat tiles beneath. The header block for a profile,
 * account, or detail screen, composed from the [FrnkIcon]/[FrnkText] atoms + the [FrnkLabeledValue]
 * molecule.
 *
 * @property name the primary line (rendered as `Title`).
 * @property avatar the leading glyph, shown inside a circular `primaryContainer` chip. Decorative by
 *   default (pass `contentDescription = null`); if the avatar conveys identity for the caller, set a
 *   meaningful `contentDescription` on it so screen readers announce it — the organism passes it through
 *   verbatim and never synthesises one from [name].
 * @property subtitle optional secondary line (muted `BodyMedium`); omitted when `null`.
 * @property stats optional stat tiles laid out evenly in a row below the identity block (each
 *   [FrnkLabeledValueState] is forced to [Stacked][FrnkLabeledValueOrientation.Stacked]); the stats
 *   row and its top divider are omitted when empty. Keep stat **values** short/compact (e.g. `"48d"`,
 *   `"Pro"`): each tile gets an equal `weight(1f)` slice, so a wide value wraps to multiple lines and
 *   leaves its column taller than its siblings.
 * @property skeleton loading placeholder. Content-bearing, so the flag is **passed through** to every
 *   child (avatar, name, subtitle, each stat value collapses to a block) and the avatar chip drops its
 *   `primaryContainer` fill while loading so its rim doesn't peek around the glyph skeleton.
 */
@Immutable
data class FrnkProfileHeaderState(
    val name: String,
    val avatar: FrnkIconState,
    val subtitle: String? = null,
    val stats: List<FrnkLabeledValueState> = emptyList(),
    val skeleton: FrnkSkeleton = FrnkSkeleton(),
)

/** A profile/account header card: circular avatar + name/subtitle, with an optional even stats row. */
@Composable
fun FrnkProfileHeader(
    state: FrnkProfileHeaderState,
    modifier: Modifier = Modifier,
) {
    val loading = state.skeleton.enabled

    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .clip(Theme[shapes][shapeCard])
                .background(Theme[colors][colorSurface])
                .padding(FrnkSpacing.lg),
        verticalArrangement = Arrangement.spacedBy(FrnkSpacing.md),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(FrnkSpacing.md),
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
                        }.padding(FrnkSpacing.sm),
                contentAlignment = Alignment.Center,
            ) {
                FrnkIcon(state = state.avatar.copy(skeleton = state.skeleton))
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(FrnkSpacing.xxs),
            ) {
                FrnkText(state = FrnkTextState.Title(text = state.name, skeleton = state.skeleton))
                state.subtitle?.let {
                    FrnkText(
                        state =
                            FrnkTextState.BodyMedium(
                                text = it,
                                color = colorOnSurfaceVariant,
                                skeleton = state.skeleton,
                            ),
                    )
                }
            }
        }

        if (state.stats.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(FrnkSpacing.md),
            ) {
                state.stats.forEach { stat ->
                    FrnkLabeledValue(
                        state =
                            stat.copy(
                                orientation = FrnkLabeledValueOrientation.Stacked,
                                skeleton = state.skeleton,
                            ),
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}
