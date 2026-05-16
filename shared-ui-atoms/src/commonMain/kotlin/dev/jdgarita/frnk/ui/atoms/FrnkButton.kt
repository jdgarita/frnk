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
import dev.jdgarita.frnk.ui.theme.colors
import dev.jdgarita.frnk.ui.theme.onPrimary
import dev.jdgarita.frnk.ui.theme.outline
import dev.jdgarita.frnk.ui.theme.primary
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
    val primaryColor = Theme[colors][primary]
    val onPrimaryColor = Theme[colors][onPrimary]
    val outlineColor = Theme[colors][outline]

    val backgroundColor: Color =
        when (state.variant) {
            FrnkButtonVariant.Filled -> if (state.enabled) primaryColor else primaryColor.copy(alpha = 0.4f)
            FrnkButtonVariant.Outlined -> Color.Transparent
            FrnkButtonVariant.Ghost -> Color.Transparent
        }
    val contentColor: Color =
        when (state.variant) {
            FrnkButtonVariant.Filled -> onPrimaryColor
            FrnkButtonVariant.Outlined -> primaryColor
            FrnkButtonVariant.Ghost -> primaryColor
        }.let { if (state.enabled) it else it.copy(alpha = 0.4f) }
    val border: BorderStroke? =
        if (state.variant == FrnkButtonVariant.Outlined) {
            BorderStroke(1.dp, if (state.enabled) primaryColor else outlineColor)
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
            FrnkText(state = FrnkTextState.BodyMedium(text = state.text, color = null))
        }
    }
}
