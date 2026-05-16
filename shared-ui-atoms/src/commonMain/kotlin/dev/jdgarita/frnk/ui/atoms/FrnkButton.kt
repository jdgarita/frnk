package dev.jdgarita.frnk.ui.atoms

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.composeunstyled.ProvideContentColor
import com.composeunstyled.UnstyledButton
import com.composeunstyled.theme.Theme
import dev.jdgarita.frnk.ui.theme.colorOnPrimary
import dev.jdgarita.frnk.ui.theme.colorPrimary
import dev.jdgarita.frnk.ui.theme.colors
import dev.jdgarita.frnk.ui.theme.labelLarge
import dev.jdgarita.frnk.ui.theme.shapeButton
import dev.jdgarita.frnk.ui.theme.shapes
import dev.jdgarita.frnk.ui.tokens.FrnkSpacing

enum class FrnkButtonVariant {
    Filled,
    Outlined,
    Ghost,
}

@Immutable
data class FrnkButtonState(
    val text: String,
    val enabled: Boolean = true,
    val variant: FrnkButtonVariant = FrnkButtonVariant.Filled,
)

@Composable
fun FrnkButton(
    state: FrnkButtonState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = Theme[shapes][shapeButton]
    val primary = Theme[colors][colorPrimary]
    val onPrimary = Theme[colors][colorOnPrimary]

    val backgroundColor: Color =
        when (state.variant) {
            FrnkButtonVariant.Filled -> if (state.enabled) primary else primary.copy(alpha = 0.4f)
            FrnkButtonVariant.Outlined -> Color.Transparent
            FrnkButtonVariant.Ghost -> Color.Transparent
        }
    val contentColor: Color =
        when (state.variant) {
            FrnkButtonVariant.Filled -> onPrimary
            FrnkButtonVariant.Outlined -> primary
            FrnkButtonVariant.Ghost -> primary
        }.let { if (state.enabled) it else it.copy(alpha = 0.4f) }
    val border: BorderStroke? =
        if (state.variant == FrnkButtonVariant.Outlined) {
            // Mirror the Filled disabled treatment: fade the brand color rather than swapping in a
            // neutral outline, so disabled Filled and disabled Outlined read as the same component.
            BorderStroke(1.dp, if (state.enabled) primary else primary.copy(alpha = 0.4f))
        } else {
            null
        }

    val shapedModifier =
        modifier
            .defaultMinSize(minHeight = 48.dp)
            .clip(shape)
            .background(backgroundColor)
            .let { if (border != null) it.border(border, shape) else it }

    UnstyledButton(
        onClick = onClick,
        enabled = state.enabled,
        modifier = shapedModifier,
        contentPadding = PaddingValues(horizontal = FrnkSpacing.md, vertical = FrnkSpacing.sm),
    ) {
        ProvideContentColor(contentColor) {
            // labelLarge (14sp / Medium) matches the M3 button-label convention. Inherits color
            // from ProvideContentColor above so the disabled alpha applies uniformly.
            FrnkText(state = FrnkTextState.Text(text = state.text, style = labelLarge))
        }
    }
}
