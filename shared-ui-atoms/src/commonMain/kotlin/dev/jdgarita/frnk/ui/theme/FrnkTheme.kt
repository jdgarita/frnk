package dev.jdgarita.frnk.ui.theme

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.composeunstyled.platformtheme.buildPlatformTheme
import com.composeunstyled.theme.ComponentInteractiveSize
import com.composeunstyled.theme.ThemeProperty
import com.composeunstyled.theme.ThemeToken
import dev.jdgarita.frnk.ui.tokens.FrnkColors
import dev.jdgarita.frnk.ui.tokens.FrnkShapes
import dev.jdgarita.frnk.ui.tokens.FrnkTypography

// region ThemeProperty axes
val colors = ThemeProperty<Color>("colors")
val textStyles = ThemeProperty<TextStyle>("textStyles")
val shapes = ThemeProperty<Shape>("shapes")
val strings = ThemeProperty<String>("strings")
val icons = ThemeProperty<ImageVector>("icons")
// endregion

// region Color tokens
val primary = ThemeToken<Color>("primary")
val onPrimary = ThemeToken<Color>("on_primary")
val primaryContainer = ThemeToken<Color>("primary_container")
val onPrimaryContainer = ThemeToken<Color>("on_primary_container")

val secondary = ThemeToken<Color>("secondary")
val onSecondary = ThemeToken<Color>("on_secondary")

val background = ThemeToken<Color>("background")
val onBackground = ThemeToken<Color>("on_background")

val surface = ThemeToken<Color>("surface")
val onSurface = ThemeToken<Color>("on_surface")
val surfaceVariant = ThemeToken<Color>("surface_variant")
val onSurfaceVariant = ThemeToken<Color>("on_surface_variant")

val outline = ThemeToken<Color>("outline")
val outlineVariant = ThemeToken<Color>("outline_variant")

val error = ThemeToken<Color>("error")
val onError = ThemeToken<Color>("on_error")
val warning = ThemeToken<Color>("warning")
val onWarning = ThemeToken<Color>("on_warning")
val success = ThemeToken<Color>("success")
val onSuccess = ThemeToken<Color>("on_success")

val scrim = ThemeToken<Color>("scrim")
// endregion

// region TextStyle tokens
val displayLarge = ThemeToken<TextStyle>("display_large")
val displayMedium = ThemeToken<TextStyle>("display_medium")
val displaySmall = ThemeToken<TextStyle>("display_small")

val headlineLarge = ThemeToken<TextStyle>("headline_large")
val headlineMedium = ThemeToken<TextStyle>("headline_medium")
val headlineSmall = ThemeToken<TextStyle>("headline_small")

val titleLarge = ThemeToken<TextStyle>("title_large")
val titleMedium = ThemeToken<TextStyle>("title_medium")
val titleSmall = ThemeToken<TextStyle>("title_small")

val bodyLarge = ThemeToken<TextStyle>("body_large")
val bodyMedium = ThemeToken<TextStyle>("body_medium")
val bodySmall = ThemeToken<TextStyle>("body_small")

val labelLarge = ThemeToken<TextStyle>("label_large")
val labelMedium = ThemeToken<TextStyle>("label_medium")
val labelSmall = ThemeToken<TextStyle>("label_small")
// endregion

// region Shape tokens
val shapeExtraSmall = ThemeToken<Shape>("shape_extra_small")
val shapeSmall = ThemeToken<Shape>("shape_small")
val shapeMedium = ThemeToken<Shape>("shape_medium")
val shapeLarge = ThemeToken<Shape>("shape_large")
val shapeExtraLarge = ThemeToken<Shape>("shape_extra_large")
val shapeFull = ThemeToken<Shape>("shape_full")
val shapeButton = ThemeToken<Shape>("shape_button")
val shapeCard = ThemeToken<Shape>("shape_card")
val shapeTextField = ThemeToken<Shape>("shape_text_field")
val shapeBottomSheet = ThemeToken<Shape>("shape_bottom_sheet")
// endregion

enum class Appearance {
    Light,
    Dark,
    System,
}

class AppearanceController {
    var appearance: Appearance by mutableStateOf(Appearance.System)
}

val LocalAppearanceController = compositionLocalOf { AppearanceController() }

internal val LocalFrnkThemeConfig = compositionLocalOf { FrnkThemeConfig() }

internal val LightPalette: Map<ThemeToken<Color>, Color> =
    mapOf(
        primary to FrnkColors.Light.primary,
        onPrimary to FrnkColors.Light.onPrimary,
        primaryContainer to FrnkColors.Light.primaryContainer,
        onPrimaryContainer to FrnkColors.Light.onPrimaryContainer,
        secondary to FrnkColors.Light.secondary,
        onSecondary to FrnkColors.Light.onSecondary,
        background to FrnkColors.Light.background,
        onBackground to FrnkColors.Light.onBackground,
        surface to FrnkColors.Light.surface,
        onSurface to FrnkColors.Light.onSurface,
        surfaceVariant to FrnkColors.Light.surfaceVariant,
        onSurfaceVariant to FrnkColors.Light.onSurfaceVariant,
        outline to FrnkColors.Light.outline,
        outlineVariant to FrnkColors.Light.outlineVariant,
        error to FrnkColors.Light.error,
        onError to FrnkColors.Light.onError,
        warning to FrnkColors.Light.warning,
        onWarning to FrnkColors.Light.onWarning,
        success to FrnkColors.Light.success,
        onSuccess to FrnkColors.Light.onSuccess,
        scrim to FrnkColors.Light.scrim,
    )

internal val DarkPalette: Map<ThemeToken<Color>, Color> =
    mapOf(
        primary to FrnkColors.Dark.primary,
        onPrimary to FrnkColors.Dark.onPrimary,
        primaryContainer to FrnkColors.Dark.primaryContainer,
        onPrimaryContainer to FrnkColors.Dark.onPrimaryContainer,
        secondary to FrnkColors.Dark.secondary,
        onSecondary to FrnkColors.Dark.onSecondary,
        background to FrnkColors.Dark.background,
        onBackground to FrnkColors.Dark.onBackground,
        surface to FrnkColors.Dark.surface,
        onSurface to FrnkColors.Dark.onSurface,
        surfaceVariant to FrnkColors.Dark.surfaceVariant,
        onSurfaceVariant to FrnkColors.Dark.onSurfaceVariant,
        outline to FrnkColors.Dark.outline,
        outlineVariant to FrnkColors.Dark.outlineVariant,
        error to FrnkColors.Dark.error,
        onError to FrnkColors.Dark.onError,
        warning to FrnkColors.Dark.warning,
        onWarning to FrnkColors.Dark.onWarning,
        success to FrnkColors.Dark.success,
        onSuccess to FrnkColors.Dark.onSuccess,
        scrim to FrnkColors.Dark.scrim,
    )

internal val DefaultTextStyles: Map<ThemeToken<TextStyle>, TextStyle> =
    mapOf(
        displayLarge to FrnkTypography.displayLarge,
        displayMedium to FrnkTypography.displayMedium,
        displaySmall to FrnkTypography.displaySmall,
        headlineLarge to FrnkTypography.headlineLarge,
        headlineMedium to FrnkTypography.headlineMedium,
        headlineSmall to FrnkTypography.headlineSmall,
        titleLarge to FrnkTypography.titleLarge,
        titleMedium to FrnkTypography.titleMedium,
        titleSmall to FrnkTypography.titleSmall,
        bodyLarge to FrnkTypography.bodyLarge,
        bodyMedium to FrnkTypography.bodyMedium,
        bodySmall to FrnkTypography.bodySmall,
        labelLarge to FrnkTypography.labelLarge,
        labelMedium to FrnkTypography.labelMedium,
        labelSmall to FrnkTypography.labelSmall,
    )

internal val DefaultShapes: Map<ThemeToken<Shape>, Shape> =
    mapOf(
        shapeExtraSmall to FrnkShapes.extraSmall,
        shapeSmall to FrnkShapes.small,
        shapeMedium to FrnkShapes.medium,
        shapeLarge to FrnkShapes.large,
        shapeExtraLarge to FrnkShapes.extraLarge,
        shapeFull to FrnkShapes.full,
        shapeButton to FrnkShapes.button,
        shapeCard to FrnkShapes.card,
        shapeTextField to FrnkShapes.textField,
        shapeBottomSheet to FrnkShapes.bottomSheet,
    )

@Composable
private fun animateColorPalette(target: Map<ThemeToken<Color>, Color>): Map<ThemeToken<Color>, Color> =
    target.mapValues { (_, value) ->
        animateColorAsState(targetValue = value, animationSpec = tween(durationMillis = 450)).value
    }

private val FrnkPlatformTheme =
    buildPlatformTheme {
        defaultComponentInteractiveSize =
            ComponentInteractiveSize(
                nonTouchInteractionSize = 32.dp,
                touchInteractionSize = 48.dp,
            )

        val config = LocalFrnkThemeConfig.current
        val isDark =
            when (LocalAppearanceController.current.appearance) {
                Appearance.Light -> false
                Appearance.Dark -> true
                Appearance.System -> isSystemInDarkTheme()
            }
        val basePalette = if (isDark) DarkPalette else LightPalette
        val paletteOverrides = if (isDark) config.darkColorOverrides else config.lightColorOverrides
        val palette = basePalette + paletteOverrides
        val animatedPalette = animateColorPalette(palette)

        properties[colors] = animatedPalette
        properties[textStyles] = DefaultTextStyles + config.textStyleOverrides
        properties[shapes] = DefaultShapes + config.shapeOverrides
        properties[strings] = DefaultFrnkStrings + config.stringOverrides
        properties[icons] = DefaultFrnkIcons + config.iconOverrides

        defaultTextStyle = FrnkTypography.bodyLarge
        defaultContentColor = animatedPalette[onBackground] ?: Color.Unspecified
    }

@Composable
fun FrnkTheme(
    config: FrnkThemeConfig = FrnkThemeConfig(),
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(LocalFrnkThemeConfig provides config) {
        FrnkPlatformTheme(content)
    }
}
