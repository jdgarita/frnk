package dev.jdgarita.frnk.ui.atoms

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.composeunstyled.UnstyledHorizontalSeparator
import com.composeunstyled.UnstyledVerticalSeparator
import com.composeunstyled.theme.Theme
import com.composeunstyled.theme.ThemeToken
import dev.jdgarita.frnk.ui.theme.colorOutline
import dev.jdgarita.frnk.ui.theme.colors

@Immutable
sealed class FrnkDividerState(
    open val thickness: Dp,
    open val color: ThemeToken<Color>
) {
    data class Horizontal(
        override val thickness: Dp = 1.dp,
        override val color: ThemeToken<Color> = colorOutline
    ) : FrnkDividerState(thickness, color)

    data class Vertical(
        override val thickness: Dp = 1.dp,
        override val color: ThemeToken<Color> = colorOutline
    ) : FrnkDividerState(thickness, color)
}

@Composable
fun FrnkDivider(
    state: FrnkDividerState = FrnkDividerState.Horizontal(),
    modifier: Modifier = Modifier
) {
    val resolvedColor = Theme[colors][state.color]
    when (state) {
        is FrnkDividerState.Horizontal ->
            UnstyledHorizontalSeparator(
                color = resolvedColor,
                modifier = modifier,
                thickness = state.thickness
            )
        is FrnkDividerState.Vertical ->
            UnstyledVerticalSeparator(
                color = resolvedColor,
                modifier = modifier,
                thickness = state.thickness
            )
    }
}