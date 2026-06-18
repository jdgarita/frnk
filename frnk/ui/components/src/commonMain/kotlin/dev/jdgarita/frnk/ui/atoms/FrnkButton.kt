package dev.jdgarita.frnk.ui.atoms

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.composeunstyled.ProvideContentColor
import com.composeunstyled.UnstyledButton
import com.composeunstyled.theme.Theme
import dev.jdgarita.frnk.ui.haptics.HapticType
import dev.jdgarita.frnk.ui.haptics.LocalFrnkHaptics
import dev.jdgarita.frnk.ui.theme.colorOnPrimary
import dev.jdgarita.frnk.ui.theme.colorPrimary
import dev.jdgarita.frnk.ui.theme.colors
import dev.jdgarita.frnk.ui.theme.iconSizeSm
import dev.jdgarita.frnk.ui.theme.iconSizes
import dev.jdgarita.frnk.ui.theme.labelLarge
import dev.jdgarita.frnk.ui.theme.shapeButton
import dev.jdgarita.frnk.ui.theme.shapes
import dev.jdgarita.frnk.ui.theme.spacing
import dev.jdgarita.frnk.ui.theme.spacingMd
import dev.jdgarita.frnk.ui.theme.spacingSm

enum class FrnkButtonVariant {
    Filled,
    Outlined,
    Ghost
}

/**
 * Sealed visual state for [FrnkButton]. [Content] is the interactive button; [Skeleton] is the loading
 * placeholder. Toolkit-standard sealed-state + `Skeleton` shape.
 */
sealed interface FrnkButtonState {
    @Immutable
    data class Content(
        val text: String,
        val enabled: Boolean = true,
        val variant: FrnkButtonVariant = FrnkButtonVariant.Filled,
        /** Optional glyph rendered before the label at the small icon-size token, inheriting the
         *  button's content color. `null` (default) = label only. */
        val leadingIcon: ImageVector? = null
    ) : FrnkButtonState

    data object Skeleton : FrnkButtonState
}

@Composable
fun FrnkButton(
    state: FrnkButtonState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val content =
        when (state) {
            is FrnkButtonState.Content -> state
            FrnkButtonState.Skeleton -> {
                FrnkSkeletonBox(
                    modifier.defaultMinSize(minWidth = 96.dp, minHeight = 48.dp),
                    shape = shapeButton
                )
                return
            }
        }

    val shape = Theme[shapes][shapeButton]
    val haptics = LocalFrnkHaptics.current
    val primary = Theme[colors][colorPrimary]
    val onPrimary = Theme[colors][colorOnPrimary]

    val backgroundColor: Color =
        when (content.variant) {
            FrnkButtonVariant.Filled -> if (content.enabled) primary else primary.copy(alpha = 0.4f)
            FrnkButtonVariant.Outlined -> Color.Transparent
            FrnkButtonVariant.Ghost -> Color.Transparent
        }
    val contentColor: Color =
        when (content.variant) {
            FrnkButtonVariant.Filled -> onPrimary
            FrnkButtonVariant.Outlined -> primary
            FrnkButtonVariant.Ghost -> primary
        }.let { if (content.enabled) it else it.copy(alpha = 0.4f) }
    val border: BorderStroke? =
        if (content.variant == FrnkButtonVariant.Outlined) {
            // Mirror the Filled disabled treatment: fade the brand color rather than swapping in a
            // neutral outline, so disabled Filled and disabled Outlined read as the same component.
            BorderStroke(1.dp, if (content.enabled) primary else primary.copy(alpha = 0.4f))
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
        onClick = {
            haptics.perform(HapticType.Click)
            onClick()
        },
        enabled = content.enabled,
        modifier = shapedModifier,
        contentPadding = PaddingValues(horizontal = Theme[spacing][spacingMd], vertical = Theme[spacing][spacingSm])
    ) {
        ProvideContentColor(contentColor) {
            // labelLarge (14sp / Medium) matches the M3 button-label convention. Inherits color
            // from ProvideContentColor above so the disabled alpha applies uniformly.
            val label = @Composable { FrnkText(state = FrnkTextState.Raw(text = content.text, style = labelLarge)) }
            val leading = content.leadingIcon
            if (leading == null) {
                label()
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Theme[spacing][spacingSm])
                ) {
                    // tint = null → inherits LocalContentColor (the ProvideContentColor above).
                    FrnkIcon(
                        state =
                            FrnkIconState.Content(
                                imageVector = leading,
                                contentDescription = null,
                                size = Theme[iconSizes][iconSizeSm]
                            )
                    )
                    label()
                }
            }
        }
    }
}