package dev.jdgarita.frnk.ui.scaffolds.steps

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.composeunstyled.theme.Theme
import dev.jdgarita.frnk.ui.theme.colorOnSurface
import dev.jdgarita.frnk.ui.theme.colorOutline
import dev.jdgarita.frnk.ui.theme.colorPrimary
import dev.jdgarita.frnk.ui.theme.colors

/**
 * Where a [FrnkStepFlow] stands: one short bar per page, [FrnkStepMeterDefaults.SegmentWidth] ×
 * [FrnkStepMeterDefaults.SegmentHeight] dp, [FrnkStepMeterDefaults.Gap] apart. Pages behind the
 * [current] one are [done] (the theme's `colorOnSurface`), the current one is [active]
 * (`colorPrimary`), the ones ahead are [ahead] (`colorOutline`).
 *
 * [current] is 0-based. The meter is one node for a screen reader, read as [contentDescription]
 * — the host's localized "Step 2 of 3" — never as [count] separate shapes.
 */
@Composable
fun FrnkStepMeter(
    current: Int,
    count: Int,
    contentDescription: String,
    modifier: Modifier = Modifier,
    done: Color = Theme[colors][colorOnSurface],
    active: Color = Theme[colors][colorPrimary],
    ahead: Color = Theme[colors][colorOutline]
) {
    Row(
        modifier = modifier.clearAndSetSemantics { this.contentDescription = contentDescription },
        horizontalArrangement = Arrangement.spacedBy(FrnkStepMeterDefaults.Gap)
    ) {
        repeat(count) { index ->
            Box(
                Modifier
                    .size(width = FrnkStepMeterDefaults.SegmentWidth, height = FrnkStepMeterDefaults.SegmentHeight)
                    .background(
                        color = frnkStepSegmentColor(index, current, done, active, ahead),
                        shape = RoundedCornerShape(FrnkStepMeterDefaults.SegmentHeight / 2)
                    )
            )
        }
    }
}

/** The colour of segment [index] when [current] is the page shown. */
internal fun frnkStepSegmentColor(
    index: Int,
    current: Int,
    done: Color,
    active: Color,
    ahead: Color
): Color =
    when {
        index < current -> done
        index == current -> active
        else -> ahead
    }

/** The meter's geometry. */
object FrnkStepMeterDefaults {
    val SegmentWidth: Dp = 22.dp
    val SegmentHeight: Dp = 4.dp
    val Gap: Dp = 4.dp
}