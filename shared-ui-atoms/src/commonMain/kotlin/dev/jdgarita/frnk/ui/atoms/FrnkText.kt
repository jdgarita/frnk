package dev.jdgarita.frnk.ui.atoms

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import com.composeunstyled.LocalContentColor
import com.composeunstyled.LocalTextStyle
import com.composeunstyled.Text
import com.composeunstyled.theme.Theme
import com.composeunstyled.theme.ThemeToken
import dev.jdgarita.frnk.ui.theme.bodyLarge
import dev.jdgarita.frnk.ui.theme.bodyMedium
import dev.jdgarita.frnk.ui.theme.bodySmall
import dev.jdgarita.frnk.ui.theme.colors
import dev.jdgarita.frnk.ui.theme.headlineSmall
import dev.jdgarita.frnk.ui.theme.textStyles
import dev.jdgarita.frnk.ui.theme.titleLarge
import dev.jdgarita.frnk.ui.theme.titleMedium

@Immutable
sealed class FrnkTextState(
    open val text: String,
    open val color: ThemeToken<Color>? = null,
    open val colorAlpha: Float = 1f,
    open val fontSize: TextUnit = TextUnit.Unspecified,
    open val textAlign: TextAlign = TextAlign.Unspecified,
    open val singleLine: Boolean = false,
    open val fontWeight: FontWeight? = null,
    open val style: ThemeToken<TextStyle>? = null,
) {
    data class Text(
        override val text: String,
        override val color: ThemeToken<Color>? = null,
        override val colorAlpha: Float = 1f,
        override val fontSize: TextUnit = TextUnit.Unspecified,
        override val textAlign: TextAlign = TextAlign.Unspecified,
        override val singleLine: Boolean = false,
        override val fontWeight: FontWeight? = null,
        override val style: ThemeToken<TextStyle>? = null,
    ) : FrnkTextState(text, color, colorAlpha, fontSize, textAlign, singleLine, fontWeight, style)

    data class Title(
        override val text: String,
        override val textAlign: TextAlign = TextAlign.Start,
        override val style: ThemeToken<TextStyle>? = titleLarge,
        override val color: ThemeToken<Color>? = null,
        override val colorAlpha: Float = 1f,
        override val fontSize: TextUnit = TextUnit.Unspecified,
        override val singleLine: Boolean = false,
        override val fontWeight: FontWeight = FontWeight.SemiBold,
    ) : FrnkTextState(text, color, colorAlpha, fontSize, textAlign, singleLine, fontWeight, style)

    data class TitleMedium(
        override val text: String,
        override val textAlign: TextAlign = TextAlign.Start,
        override val style: ThemeToken<TextStyle>? = titleMedium,
        override val color: ThemeToken<Color>? = null,
        override val colorAlpha: Float = 1f,
        override val fontSize: TextUnit = TextUnit.Unspecified,
        override val singleLine: Boolean = false,
        override val fontWeight: FontWeight = FontWeight.Medium,
    ) : FrnkTextState(text, color, colorAlpha, fontSize, textAlign, singleLine, fontWeight, style)

    data class HeadlineSmall(
        override val text: String,
        override val textAlign: TextAlign = TextAlign.Start,
        override val style: ThemeToken<TextStyle>? = headlineSmall,
        override val color: ThemeToken<Color>? = null,
        override val colorAlpha: Float = 1f,
        override val fontSize: TextUnit = TextUnit.Unspecified,
        override val singleLine: Boolean = false,
        override val fontWeight: FontWeight = FontWeight.SemiBold,
    ) : FrnkTextState(text, color, colorAlpha, fontSize, textAlign, singleLine, fontWeight, style)

    data class Body(
        override val text: String,
        override val textAlign: TextAlign = TextAlign.Start,
        override val style: ThemeToken<TextStyle>? = bodyLarge,
        override val color: ThemeToken<Color>? = null,
        override val colorAlpha: Float = 1f,
        override val fontSize: TextUnit = TextUnit.Unspecified,
        override val singleLine: Boolean = false,
        override val fontWeight: FontWeight = FontWeight.Normal,
    ) : FrnkTextState(text, color, colorAlpha, fontSize, textAlign, singleLine, fontWeight, style)

    data class BodyMedium(
        override val text: String,
        override val textAlign: TextAlign = TextAlign.Start,
        override val style: ThemeToken<TextStyle>? = bodyMedium,
        override val color: ThemeToken<Color>? = null,
        override val colorAlpha: Float = 1f,
        override val fontSize: TextUnit = TextUnit.Unspecified,
        override val singleLine: Boolean = false,
        override val fontWeight: FontWeight = FontWeight.Normal,
    ) : FrnkTextState(text, color, colorAlpha, fontSize, textAlign, singleLine, fontWeight, style)

    data class BodySmall(
        override val text: String,
        override val textAlign: TextAlign = TextAlign.Start,
        override val style: ThemeToken<TextStyle>? = bodySmall,
        override val color: ThemeToken<Color>? = null,
        override val colorAlpha: Float = 1f,
        override val fontSize: TextUnit = TextUnit.Unspecified,
        override val singleLine: Boolean = false,
        override val fontWeight: FontWeight = FontWeight.Normal,
    ) : FrnkTextState(text, color, colorAlpha, fontSize, textAlign, singleLine, fontWeight, style)

    data class AppName(
        val annotated: AnnotatedString,
        override val text: String = annotated.text,
        override val textAlign: TextAlign = TextAlign.Center,
        override val style: ThemeToken<TextStyle>? = titleLarge,
        override val color: ThemeToken<Color>? = null,
        override val colorAlpha: Float = 1f,
        override val fontSize: TextUnit = TextUnit.Unspecified,
        override val singleLine: Boolean = true,
        override val fontWeight: FontWeight = FontWeight.Bold,
    ) : FrnkTextState(text, color, colorAlpha, fontSize, textAlign, singleLine, fontWeight, style)
}

@Composable
fun FrnkText(
    modifier: Modifier = Modifier,
    state: FrnkTextState,
) {
    val styleToken = state.style
    val colorToken = state.color
    val resolvedStyle =
        if (styleToken == null) LocalTextStyle.current else Theme[textStyles][styleToken]
    val resolvedFontSize =
        if (state.fontSize == TextUnit.Unspecified) resolvedStyle.fontSize else state.fontSize
    val resolvedColor =
        if (colorToken == null) LocalContentColor.current else Theme[colors][colorToken]
    val resolvedFontWeight =
        if (state.fontWeight == null) resolvedStyle.fontWeight else state.fontWeight

    if (state is FrnkTextState.AppName) {
        Text(
            modifier = modifier,
            text = state.annotated,
            color = resolvedColor.copy(alpha = state.colorAlpha),
            style = resolvedStyle,
            fontSize = resolvedFontSize,
            textAlign = state.textAlign,
            singleLine = state.singleLine,
            fontWeight = resolvedFontWeight,
        )
    } else {
        Text(
            modifier = modifier,
            text = state.text,
            color = resolvedColor.copy(alpha = state.colorAlpha),
            style = resolvedStyle,
            fontSize = resolvedFontSize,
            textAlign = state.textAlign,
            singleLine = state.singleLine,
            fontWeight = resolvedFontWeight,
        )
    }
}
