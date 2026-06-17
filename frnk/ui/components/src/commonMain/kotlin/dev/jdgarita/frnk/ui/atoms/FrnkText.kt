package dev.jdgarita.frnk.ui.atoms

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import com.composeunstyled.LocalContentColor
import com.composeunstyled.LocalTextStyle
import com.composeunstyled.Text
import com.composeunstyled.theme.Theme
import com.composeunstyled.theme.ThemeToken
import dev.jdgarita.frnk.ui.theme.FrnkStringSource
import dev.jdgarita.frnk.ui.theme.bodyLarge
import dev.jdgarita.frnk.ui.theme.bodyMedium
import dev.jdgarita.frnk.ui.theme.bodySmall
import dev.jdgarita.frnk.ui.theme.colors
import dev.jdgarita.frnk.ui.theme.ext.resolve
import dev.jdgarita.frnk.ui.theme.headlineSmall
import dev.jdgarita.frnk.ui.theme.shapeSmall
import dev.jdgarita.frnk.ui.theme.textStyles
import dev.jdgarita.frnk.ui.theme.titleLarge
import dev.jdgarita.frnk.ui.theme.titleMedium

/**
 * The default loading-skeleton configuration shared by every content [FrnkTextState] subtype's
 * `skeleton` parameter (and the base class). Exposed publicly (not private) so hosts and tests can
 * reference the exact same instance when constructing or asserting on text state, and so the default
 * lives in one place rather than being re-spelled at every constructor.
 */
val FrnkTextDefaultSkeleton: FrnkSkeleton = FrnkSkeleton()

/**
 * View state for [FrnkText]. Each subtype picks a sensible default text style; pass a non-null
 * [style] token to override.
 *
 * Variant selection:
 * - [Raw] — **inherit** everything from ambient `LocalTextStyle` / `LocalContentColor` unless you
 *   pass tokens explicitly. Reach for this when you're already inside a `ProvideTextStyle` or a
 *   parent like `FrnkButton`/`FrnkIconButton` that supplies the style — using a semantic variant
 *   like [Body] would clobber the ambient style. (Named `Raw` rather than `Text` to avoid colliding
 *   with `com.composeunstyled.Text` at any call site that wildcard-imports the sealed subtypes.)
 * - [Title] / [TitleMedium] / [HeadlineSmall] / [Body] / [BodyMedium] / [BodySmall] — semantic
 *   variants that default to the matching [dev.jdgarita.frnk.ui.theme.titleLarge],
 *   [dev.jdgarita.frnk.ui.theme.bodyLarge], etc. token. Use these at top-level callsites.
 * - [AppName] — renders an [AnnotatedString] (lets the brand name have per-character styling).
 */

@Immutable
sealed class FrnkTextState(
    open val color: ThemeToken<Color>? = null,
    open val colorAlpha: Float = 1f,
    open val fontSize: TextUnit = TextUnit.Unspecified,
    open val textAlign: TextAlign = TextAlign.Unspecified,
    open val singleLine: Boolean = false,
    open val fontWeight: FontWeight? = null,
    open val style: ThemeToken<TextStyle>? = null,
    open val skeleton: FrnkSkeleton = FrnkTextDefaultSkeleton,
) {
    /**
     * Text whose copy comes from a [FrnkStringSource] (token / raw / composite), resolved against the
     * theme at render time — so a ViewModel can author text without composition. Every semantic variant
     * below is a [Resolvable]; [AppName] is the one exception (it carries an [AnnotatedString] for
     * per-character brand styling, which a plain `String` can't represent). Each variant also offers a
     * `String` secondary constructor that wraps the literal in [FrnkStringSource.Raw], so the common
     * `FrnkTextState.Title(text = "…")` call site stays unchanged.
     */
    @Immutable
    sealed class Resolvable(
        open val content: FrnkStringSource,
        color: ThemeToken<Color>?,
        colorAlpha: Float,
        fontSize: TextUnit,
        textAlign: TextAlign,
        singleLine: Boolean,
        fontWeight: FontWeight?,
        style: ThemeToken<TextStyle>?,
        skeleton: FrnkSkeleton,
    ) : FrnkTextState(color, colorAlpha, fontSize, textAlign, singleLine, fontWeight, style, skeleton)

    /** Inherits from ambient `LocalTextStyle` / `LocalContentColor` unless overridden. */
    data class Raw(
        override val content: FrnkStringSource,
        override val color: ThemeToken<Color>? = null,
        override val colorAlpha: Float = 1f,
        override val fontSize: TextUnit = TextUnit.Unspecified,
        override val textAlign: TextAlign = TextAlign.Unspecified,
        override val singleLine: Boolean = false,
        override val fontWeight: FontWeight? = null,
        override val style: ThemeToken<TextStyle>? = null,
        override val skeleton: FrnkSkeleton = FrnkTextDefaultSkeleton,
    ) : Resolvable(content, color, colorAlpha, fontSize, textAlign, singleLine, fontWeight, style, skeleton) {
        constructor(
            text: String,
            color: ThemeToken<Color>? = null,
            colorAlpha: Float = 1f,
            fontSize: TextUnit = TextUnit.Unspecified,
            textAlign: TextAlign = TextAlign.Unspecified,
            singleLine: Boolean = false,
            fontWeight: FontWeight? = null,
            style: ThemeToken<TextStyle>? = null,
            skeleton: FrnkSkeleton = FrnkTextDefaultSkeleton,
        ) : this(FrnkStringSource.Raw(text), color, colorAlpha, fontSize, textAlign, singleLine, fontWeight, style, skeleton)
    }

    data class Title(
        override val content: FrnkStringSource,
        override val textAlign: TextAlign = TextAlign.Start,
        override val style: ThemeToken<TextStyle>? = titleLarge,
        override val color: ThemeToken<Color>? = null,
        override val colorAlpha: Float = 1f,
        override val fontSize: TextUnit = TextUnit.Unspecified,
        override val singleLine: Boolean = false,
        override val fontWeight: FontWeight = FontWeight.SemiBold,
        override val skeleton: FrnkSkeleton = FrnkTextDefaultSkeleton,
    ) : Resolvable(content, color, colorAlpha, fontSize, textAlign, singleLine, fontWeight, style, skeleton) {
        constructor(
            text: String,
            textAlign: TextAlign = TextAlign.Start,
            style: ThemeToken<TextStyle>? = titleLarge,
            color: ThemeToken<Color>? = null,
            colorAlpha: Float = 1f,
            fontSize: TextUnit = TextUnit.Unspecified,
            singleLine: Boolean = false,
            fontWeight: FontWeight = FontWeight.SemiBold,
            skeleton: FrnkSkeleton = FrnkTextDefaultSkeleton,
        ) : this(FrnkStringSource.Raw(text), textAlign, style, color, colorAlpha, fontSize, singleLine, fontWeight, skeleton)
    }

    data class TitleMedium(
        override val content: FrnkStringSource,
        override val textAlign: TextAlign = TextAlign.Start,
        override val style: ThemeToken<TextStyle>? = titleMedium,
        override val color: ThemeToken<Color>? = null,
        override val colorAlpha: Float = 1f,
        override val fontSize: TextUnit = TextUnit.Unspecified,
        override val singleLine: Boolean = false,
        override val fontWeight: FontWeight = FontWeight.Medium,
        override val skeleton: FrnkSkeleton = FrnkTextDefaultSkeleton,
    ) : Resolvable(content, color, colorAlpha, fontSize, textAlign, singleLine, fontWeight, style, skeleton) {
        constructor(
            text: String,
            textAlign: TextAlign = TextAlign.Start,
            style: ThemeToken<TextStyle>? = titleMedium,
            color: ThemeToken<Color>? = null,
            colorAlpha: Float = 1f,
            fontSize: TextUnit = TextUnit.Unspecified,
            singleLine: Boolean = false,
            fontWeight: FontWeight = FontWeight.Medium,
            skeleton: FrnkSkeleton = FrnkTextDefaultSkeleton,
        ) : this(FrnkStringSource.Raw(text), textAlign, style, color, colorAlpha, fontSize, singleLine, fontWeight, skeleton)
    }

    data class HeadlineSmall(
        override val content: FrnkStringSource,
        override val textAlign: TextAlign = TextAlign.Start,
        override val style: ThemeToken<TextStyle>? = headlineSmall,
        override val color: ThemeToken<Color>? = null,
        override val colorAlpha: Float = 1f,
        override val fontSize: TextUnit = TextUnit.Unspecified,
        override val singleLine: Boolean = false,
        override val fontWeight: FontWeight = FontWeight.SemiBold,
        override val skeleton: FrnkSkeleton = FrnkTextDefaultSkeleton,
    ) : Resolvable(content, color, colorAlpha, fontSize, textAlign, singleLine, fontWeight, style, skeleton) {
        constructor(
            text: String,
            textAlign: TextAlign = TextAlign.Start,
            style: ThemeToken<TextStyle>? = headlineSmall,
            color: ThemeToken<Color>? = null,
            colorAlpha: Float = 1f,
            fontSize: TextUnit = TextUnit.Unspecified,
            singleLine: Boolean = false,
            fontWeight: FontWeight = FontWeight.SemiBold,
            skeleton: FrnkSkeleton = FrnkTextDefaultSkeleton,
        ) : this(FrnkStringSource.Raw(text), textAlign, style, color, colorAlpha, fontSize, singleLine, fontWeight, skeleton)
    }

    data class Body(
        override val content: FrnkStringSource,
        override val textAlign: TextAlign = TextAlign.Start,
        override val style: ThemeToken<TextStyle>? = bodyLarge,
        override val color: ThemeToken<Color>? = null,
        override val colorAlpha: Float = 1f,
        override val fontSize: TextUnit = TextUnit.Unspecified,
        override val singleLine: Boolean = false,
        override val fontWeight: FontWeight = FontWeight.Normal,
        override val skeleton: FrnkSkeleton = FrnkTextDefaultSkeleton,
    ) : Resolvable(content, color, colorAlpha, fontSize, textAlign, singleLine, fontWeight, style, skeleton) {
        constructor(
            text: String,
            textAlign: TextAlign = TextAlign.Start,
            style: ThemeToken<TextStyle>? = bodyLarge,
            color: ThemeToken<Color>? = null,
            colorAlpha: Float = 1f,
            fontSize: TextUnit = TextUnit.Unspecified,
            singleLine: Boolean = false,
            fontWeight: FontWeight = FontWeight.Normal,
            skeleton: FrnkSkeleton = FrnkTextDefaultSkeleton,
        ) : this(FrnkStringSource.Raw(text), textAlign, style, color, colorAlpha, fontSize, singleLine, fontWeight, skeleton)
    }

    data class BodyMedium(
        override val content: FrnkStringSource,
        override val textAlign: TextAlign = TextAlign.Start,
        override val style: ThemeToken<TextStyle>? = bodyMedium,
        override val color: ThemeToken<Color>? = null,
        override val colorAlpha: Float = 1f,
        override val fontSize: TextUnit = TextUnit.Unspecified,
        override val singleLine: Boolean = false,
        override val fontWeight: FontWeight = FontWeight.Normal,
        override val skeleton: FrnkSkeleton = FrnkTextDefaultSkeleton,
    ) : Resolvable(content, color, colorAlpha, fontSize, textAlign, singleLine, fontWeight, style, skeleton) {
        constructor(
            text: String,
            textAlign: TextAlign = TextAlign.Start,
            style: ThemeToken<TextStyle>? = bodyMedium,
            color: ThemeToken<Color>? = null,
            colorAlpha: Float = 1f,
            fontSize: TextUnit = TextUnit.Unspecified,
            singleLine: Boolean = false,
            fontWeight: FontWeight = FontWeight.Normal,
            skeleton: FrnkSkeleton = FrnkTextDefaultSkeleton,
        ) : this(FrnkStringSource.Raw(text), textAlign, style, color, colorAlpha, fontSize, singleLine, fontWeight, skeleton)
    }

    data class BodySmall(
        override val content: FrnkStringSource,
        override val textAlign: TextAlign = TextAlign.Start,
        override val style: ThemeToken<TextStyle>? = bodySmall,
        override val color: ThemeToken<Color>? = null,
        override val colorAlpha: Float = 1f,
        override val fontSize: TextUnit = TextUnit.Unspecified,
        override val singleLine: Boolean = false,
        override val fontWeight: FontWeight = FontWeight.Normal,
        override val skeleton: FrnkSkeleton = FrnkTextDefaultSkeleton,
    ) : Resolvable(content, color, colorAlpha, fontSize, textAlign, singleLine, fontWeight, style, skeleton) {
        constructor(
            text: String,
            textAlign: TextAlign = TextAlign.Start,
            style: ThemeToken<TextStyle>? = bodySmall,
            color: ThemeToken<Color>? = null,
            colorAlpha: Float = 1f,
            fontSize: TextUnit = TextUnit.Unspecified,
            singleLine: Boolean = false,
            fontWeight: FontWeight = FontWeight.Normal,
            skeleton: FrnkSkeleton = FrnkTextDefaultSkeleton,
        ) : this(FrnkStringSource.Raw(text), textAlign, style, color, colorAlpha, fontSize, singleLine, fontWeight, skeleton)
    }

    data class AppName(
        val annotated: AnnotatedString,
        override val textAlign: TextAlign = TextAlign.Center,
        override val style: ThemeToken<TextStyle>? = titleLarge,
        override val color: ThemeToken<Color>? = null,
        override val colorAlpha: Float = 1f,
        override val fontSize: TextUnit = TextUnit.Unspecified,
        override val singleLine: Boolean = true,
        override val fontWeight: FontWeight = FontWeight.Bold,
        override val skeleton: FrnkSkeleton = FrnkTextDefaultSkeleton,
    ) : FrnkTextState(color, colorAlpha, fontSize, textAlign, singleLine, fontWeight, style, skeleton)

    /**
     * Standalone loading placeholder (a single bar). The content subtypes above also carry a
     * [skeleton] field for **content-sized** text skeletons (a block sized to the real string, used
     * when a parent knows the text) — this `object` is the generic, content-agnostic case mandated by
     * the toolkit's sealed-state + `Skeleton`-object convention.
     */
    data object Skeleton : FrnkTextState()
}

@Composable
fun FrnkText(
    state: FrnkTextState,
    modifier: Modifier = Modifier,
) {
    // Skeleton is the loading/initial state — handle it first, before any token resolution.
    when (state) {
        is FrnkTextState.Skeleton ->
            FrnkSkeletonBox(modifier.width(120.dp).height(16.dp), shape = shapeSmall)

        is FrnkTextState.AppName ->
            FrnkStyledText(state = state, text = state.annotated, modifier = modifier)

        is FrnkTextState.Resolvable ->
            FrnkStyledText(state = state, text = AnnotatedString(state.content.resolve()), modifier = modifier)
    }
}

/** Resolves the shared style/color/weight from [state]'s tokens and draws [text]. */
@Composable
private fun FrnkStyledText(
    state: FrnkTextState,
    text: AnnotatedString,
    modifier: Modifier,
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

    Text(
        modifier = modifier.frnkSkeleton(state.skeleton, shape = shapeSmall),
        text = text,
        color = resolvedColor.copy(alpha = state.colorAlpha),
        style = resolvedStyle,
        fontSize = resolvedFontSize,
        textAlign = state.textAlign,
        singleLine = state.singleLine,
        fontWeight = resolvedFontWeight,
    )
}
