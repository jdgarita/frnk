package dev.jdgarita.frnk.ui.theme

import androidx.compose.foundation.IndicationNodeFactory
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import com.composables.compose.ripple.rememberRippleIndication

/**
 * The Frnk press-feedback **ripple**, built on `com.composables:ripple-indication` (the compose-unstyled
 * vendor's theme-agnostic ripple — no Material3).
 *
 * [FrnkTheme] installs this as the ambient `LocalIndication`, so every interactive atom and every host
 * `Modifier.clickable` / `UnstyledButton` rendered under the theme ripples automatically. Host apps that
 * want a differently-styled ripple on their own components pass the result of this factory explicitly:
 *
 * ```
 * val interaction = remember { MutableInteractionSource() }
 * Box(
 *     Modifier
 *         .clip(Theme[shapes][shapeCard])
 *         .clickable(interaction, indication = rememberFrnkRipple(color = Theme[colors][colorPrimary])) { … },
 * )
 * ```
 *
 * @param color the ripple color. [Color.Unspecified] (the default) follows the ambient content color at the
 *   call site, so the ripple adapts per surface (e.g. light on a filled button, dark on a light row).
 * @param bounded when `true` the ripple is clipped to the component's bounds/shape; `false` draws an
 *   unbounded circular ripple (typical for icon-only targets).
 * @param radius the ripple radius; [Dp.Unspecified] (the default) lets it derive from the component size.
 */
@Composable
fun rememberFrnkRipple(
    color: Color = Color.Unspecified,
    bounded: Boolean = true,
    radius: Dp = Dp.Unspecified,
): IndicationNodeFactory =
    rememberRippleIndication(
        color = color,
        bounded = bounded,
        radius = radius,
    )
