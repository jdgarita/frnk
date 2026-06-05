package dev.jdgarita.frnk.ui.theme

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Indication
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.composeunstyled.platformtheme.buildPlatformTheme
import com.composeunstyled.theme.ComponentInteractiveSize
import com.composeunstyled.theme.ThemeProperty
import com.composeunstyled.theme.ThemeToken
import dev.jdgarita.frnk.ui.haptics.HapticFeedback
import dev.jdgarita.frnk.ui.haptics.LocalFrnkHaptics
import dev.jdgarita.frnk.ui.haptics.rememberFrnkHaptics
import dev.jdgarita.frnk.ui.tokens.FrnkColors
import dev.jdgarita.frnk.ui.tokens.FrnkIconSize
import dev.jdgarita.frnk.ui.tokens.FrnkShapes
import dev.jdgarita.frnk.ui.tokens.FrnkSpacing
import dev.jdgarita.frnk.ui.tokens.FrnkTypography

// region ThemeProperty axes
val colors = ThemeProperty<Color>("colors")
val textStyles = ThemeProperty<TextStyle>("textStyles")
val shapes = ThemeProperty<Shape>("shapes")
val strings = ThemeProperty<String>("strings")
val icons = ThemeProperty<ImageVector>("icons")

// Dp axes (P-host-alignment): spacing/padding + icon sizes are host-overridable theme axes, mirroring
// the colors/shapes pattern. Defaults come from the FrnkSpacing / FrnkIconSize objects; hosts override
// per token via FrnkThemeConfig.spacingOverrides / iconSizeOverrides.
val spacing = ThemeProperty<Dp>("spacing")
val iconSizes = ThemeProperty<Dp>("iconSizes")

// Optional axes the toolkit's own atoms don't consume, but a host design language can populate via
// FrnkThemeConfig.shadowOverrides / indicationOverrides and read back as Theme[shadows][...] /
// Theme[indications][...]. Ship empty by default.
val shadows = ThemeProperty<Shadow>("shadows")
val indications = ThemeProperty<Indication>("indications")
// endregion

// region Color tokens.
// Prefixed with `color` (matching the shape*/string*/icon* conventions) so that
// callsites importing these tokens don't shadow stdlib functions (`kotlin.error`)
// or common local variable names (`primary`, `background`, `surface`, `outline`).
val colorPrimary = ThemeToken<Color>("primary")
val colorOnPrimary = ThemeToken<Color>("on_primary")
val colorPrimaryContainer = ThemeToken<Color>("primary_container")
val colorOnPrimaryContainer = ThemeToken<Color>("on_primary_container")

val colorSecondary = ThemeToken<Color>("secondary")
val colorOnSecondary = ThemeToken<Color>("on_secondary")

val colorBackground = ThemeToken<Color>("background")
val colorOnBackground = ThemeToken<Color>("on_background")

val colorSurface = ThemeToken<Color>("surface")
val colorOnSurface = ThemeToken<Color>("on_surface")
val colorSurfaceVariant = ThemeToken<Color>("surface_variant")
val colorOnSurfaceVariant = ThemeToken<Color>("on_surface_variant")

val colorOutline = ThemeToken<Color>("outline")
val colorOutlineVariant = ThemeToken<Color>("outline_variant")

val colorError = ThemeToken<Color>("error")
val colorOnError = ThemeToken<Color>("on_error")
val colorWarning = ThemeToken<Color>("warning")
val colorOnWarning = ThemeToken<Color>("on_warning")
val colorSuccess = ThemeToken<Color>("success")
val colorOnSuccess = ThemeToken<Color>("on_success")

val colorScrim = ThemeToken<Color>("scrim")
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
val shapeNone = ThemeToken<Shape>("shape_none")
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

// region Spacing tokens (Dp). Prefixed `spacing*` so they don't shadow common locals.
val spacingNone = ThemeToken<Dp>("spacing_none")
val spacingXxs = ThemeToken<Dp>("spacing_xxs")
val spacingXs = ThemeToken<Dp>("spacing_xs")
val spacingSm = ThemeToken<Dp>("spacing_sm")
val spacingMd = ThemeToken<Dp>("spacing_md")
val spacingLg = ThemeToken<Dp>("spacing_lg")
val spacingXl = ThemeToken<Dp>("spacing_xl")
val spacingXxl = ThemeToken<Dp>("spacing_xxl")
val spacingScreenPadding = ThemeToken<Dp>("spacing_screen_padding")
val spacingIconPadding = ThemeToken<Dp>("spacing_icon_padding")
val spacingListItemPadding = ThemeToken<Dp>("spacing_list_item_padding")
val spacingSectionSpacing = ThemeToken<Dp>("spacing_section_spacing")
// endregion

// region Icon-size tokens (Dp).
val iconSizeXs = ThemeToken<Dp>("icon_size_xs")
val iconSizeSm = ThemeToken<Dp>("icon_size_sm")
val iconSizeMd = ThemeToken<Dp>("icon_size_md")
val iconSizeLg = ThemeToken<Dp>("icon_size_lg")
val iconSizeXl = ThemeToken<Dp>("icon_size_xl")
val iconSizeXxl = ThemeToken<Dp>("icon_size_xxl")
val iconSizeButton = ThemeToken<Dp>("icon_size_button")
val iconSizeFab = ThemeToken<Dp>("icon_size_fab")
val iconSizeAppBar = ThemeToken<Dp>("icon_size_app_bar")
val iconSizeEmptyState = ThemeToken<Dp>("icon_size_empty_state")
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

internal val LocalFrnkThemeConfig = compositionLocalOf { FrnkThemeConfig.Default }

internal val LightPalette: Map<ThemeToken<Color>, Color> =
    mapOf(
        colorPrimary to FrnkColors.Light.primary,
        colorOnPrimary to FrnkColors.Light.onPrimary,
        colorPrimaryContainer to FrnkColors.Light.primaryContainer,
        colorOnPrimaryContainer to FrnkColors.Light.onPrimaryContainer,
        colorSecondary to FrnkColors.Light.secondary,
        colorOnSecondary to FrnkColors.Light.onSecondary,
        colorBackground to FrnkColors.Light.background,
        colorOnBackground to FrnkColors.Light.onBackground,
        colorSurface to FrnkColors.Light.surface,
        colorOnSurface to FrnkColors.Light.onSurface,
        colorSurfaceVariant to FrnkColors.Light.surfaceVariant,
        colorOnSurfaceVariant to FrnkColors.Light.onSurfaceVariant,
        colorOutline to FrnkColors.Light.outline,
        colorOutlineVariant to FrnkColors.Light.outlineVariant,
        colorError to FrnkColors.Light.error,
        colorOnError to FrnkColors.Light.onError,
        colorWarning to FrnkColors.Light.warning,
        colorOnWarning to FrnkColors.Light.onWarning,
        colorSuccess to FrnkColors.Light.success,
        colorOnSuccess to FrnkColors.Light.onSuccess,
        colorScrim to FrnkColors.Light.scrim,
    )

internal val DarkPalette: Map<ThemeToken<Color>, Color> =
    mapOf(
        colorPrimary to FrnkColors.Dark.primary,
        colorOnPrimary to FrnkColors.Dark.onPrimary,
        colorPrimaryContainer to FrnkColors.Dark.primaryContainer,
        colorOnPrimaryContainer to FrnkColors.Dark.onPrimaryContainer,
        colorSecondary to FrnkColors.Dark.secondary,
        colorOnSecondary to FrnkColors.Dark.onSecondary,
        colorBackground to FrnkColors.Dark.background,
        colorOnBackground to FrnkColors.Dark.onBackground,
        colorSurface to FrnkColors.Dark.surface,
        colorOnSurface to FrnkColors.Dark.onSurface,
        colorSurfaceVariant to FrnkColors.Dark.surfaceVariant,
        colorOnSurfaceVariant to FrnkColors.Dark.onSurfaceVariant,
        colorOutline to FrnkColors.Dark.outline,
        colorOutlineVariant to FrnkColors.Dark.outlineVariant,
        colorError to FrnkColors.Dark.error,
        colorOnError to FrnkColors.Dark.onError,
        colorWarning to FrnkColors.Dark.warning,
        colorOnWarning to FrnkColors.Dark.onWarning,
        colorSuccess to FrnkColors.Dark.success,
        colorOnSuccess to FrnkColors.Dark.onSuccess,
        colorScrim to FrnkColors.Dark.scrim,
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
        shapeNone to FrnkShapes.none,
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

internal val DefaultSpacing: Map<ThemeToken<Dp>, Dp> =
    mapOf(
        spacingNone to FrnkSpacing.none,
        spacingXxs to FrnkSpacing.xxs,
        spacingXs to FrnkSpacing.xs,
        spacingSm to FrnkSpacing.sm,
        spacingMd to FrnkSpacing.md,
        spacingLg to FrnkSpacing.lg,
        spacingXl to FrnkSpacing.xl,
        spacingXxl to FrnkSpacing.xxl,
        spacingScreenPadding to FrnkSpacing.screenPadding,
        spacingIconPadding to FrnkSpacing.iconPadding,
        spacingListItemPadding to FrnkSpacing.listItemPadding,
        spacingSectionSpacing to FrnkSpacing.sectionSpacing,
    )

internal val DefaultIconSizes: Map<ThemeToken<Dp>, Dp> =
    mapOf(
        iconSizeXs to FrnkIconSize.xs,
        iconSizeSm to FrnkIconSize.sm,
        iconSizeMd to FrnkIconSize.md,
        iconSizeLg to FrnkIconSize.lg,
        iconSizeXl to FrnkIconSize.xl,
        iconSizeXxl to FrnkIconSize.xxl,
        iconSizeButton to FrnkIconSize.button,
        iconSizeFab to FrnkIconSize.fab,
        iconSizeAppBar to FrnkIconSize.appBar,
        iconSizeEmptyState to FrnkIconSize.emptyState,
    )

/**
 * Animates [base] colors towards (override-or-base) targets with a 450ms tween. The animation
 * call count is exactly `base.size` on every composition, so `animateColorAsState` slot positions
 * are stable regardless of what the host puts in [overrides].
 *
 * Any [overrides] entry whose key is *not* in [base] (a custom host-defined `ThemeToken<Color>`)
 * passes through unanimated and gets merged into the returned map. The 21 bundled `color*` tokens
 * always animate; custom tokens jump instantly. If we ever want custom tokens to animate too,
 * rewrite this as an explicit per-token list — driving `animateColorAsState` over a runtime-sized
 * key set would re-introduce the slot-count footgun.
 */
@Composable
private fun animateColorPalette(
    base: Map<ThemeToken<Color>, Color>,
    overrides: Map<ThemeToken<Color>, Color>,
): Map<ThemeToken<Color>, Color> {
    val animated =
        base.mapValues { (token, baseValue) ->
            animateColorAsState(
                targetValue = overrides[token] ?: baseValue,
                animationSpec = tween(durationMillis = 450),
            ).value
        }
    val extras = overrides.filterKeys { it !in base }
    return if (extras.isEmpty()) animated else animated + extras
}

/**
 * Module-load-time singleton. `buildPlatformTheme` returns a `@Composable (content) -> Unit` whose
 * body is re-executed on every composition, so reads of `LocalFrnkThemeConfig.current` and
 * `LocalAppearanceController.current` inside this block always reflect the current providers.
 * Verified against compose-unstyled 2.0.0 `buildTheme` (the lambda is captured and invoked per call).
 */
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
        val animatedPalette = animateColorPalette(basePalette, paletteOverrides)

        val fontFamily = config.fontFamily
        val textStylesForTheme =
            if (fontFamily == null) {
                DefaultTextStyles
            } else {
                DefaultTextStyles.mapValues { (_, style) -> style.copy(fontFamily = fontFamily) }
            } + config.textStyleOverrides

        properties[colors] = animatedPalette
        properties[textStyles] = textStylesForTheme
        properties[shapes] = DefaultShapes + config.shapeOverrides
        properties[strings] = DefaultFrnkStrings + config.stringOverrides
        properties[icons] = DefaultFrnkIcons + config.iconOverrides
        properties[spacing] = DefaultSpacing + config.spacingOverrides
        properties[iconSizes] = DefaultIconSizes + config.iconSizeOverrides
        properties[shadows] = config.shadowOverrides
        properties[indications] = config.indicationOverrides

        defaultTextStyle =
            textStylesForTheme[bodyLarge] ?: FrnkTypography.bodyLarge
        defaultContentColor = animatedPalette[colorOnBackground] ?: Color.Unspecified
    }

/**
 * Wraps [content] in the Frnk design system: registers theme tokens, animates between light/dark
 * palettes, and provides the [appearanceController] so callers can read/mutate
 * `LocalAppearanceController.current.appearance` to toggle.
 *
 * Hosts that need the appearance to survive process death should hoist their own controller with
 * `rememberSaveable` (or wire it to a `DataStore`-backed flow) and pass it here.
 *
 * [haptics] is the ambient [HapticFeedback] installed as `LocalFrnkHaptics` — atoms vibrate on
 * interaction through it, gated by its enabled flag (driven by the Settings "Haptic feedback"
 * toggle). It defaults to a `multihaptic`-backed instance; hoist your own (a persisted or
 * initially-disabled [HapticFeedback]) the same way you would the appearance controller.
 */
@Composable
fun FrnkTheme(
    config: FrnkThemeConfig = FrnkThemeConfig.Default,
    appearanceController: AppearanceController = remember { AppearanceController() },
    haptics: HapticFeedback = rememberFrnkHaptics(),
    content: @Composable () -> Unit,
) {
    val indication = config.indication ?: rememberFrnkRipple()
    CompositionLocalProvider(
        LocalFrnkThemeConfig provides config,
        LocalAppearanceController provides appearanceController,
        LocalIndication provides indication,
        LocalFrnkHaptics provides haptics,
    ) {
        FrnkPlatformTheme(content)
    }
}
