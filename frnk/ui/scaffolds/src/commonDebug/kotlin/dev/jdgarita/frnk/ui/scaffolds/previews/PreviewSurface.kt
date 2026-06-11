package dev.jdgarita.frnk.ui.scaffolds.previews

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.composeunstyled.theme.Theme
import dev.jdgarita.frnk.ui.theme.Appearance
import dev.jdgarita.frnk.ui.theme.AppearanceController
import dev.jdgarita.frnk.ui.theme.FrnkTheme
import dev.jdgarita.frnk.ui.theme.colorBackground
import dev.jdgarita.frnk.ui.theme.colors
import dev.jdgarita.frnk.ui.tokens.FrnkSpacing

/**
 * Per-module copy of the preview surface helper. The original lives in `:ui-components`'s `commonDebug`
 * under `…ui.atoms.previews`; `commonDebug` is a custom intermediate source set that isn't shared across
 * modules, so `:ui-scaffolds`' scaffold previews carry their own copy (restructure Stage 7b).
 *
 * It lives in **this** module's own `…ui.scaffolds.previews` package — NOT the original `…ui.atoms.previews`
 * — deliberately: `commonDebug` feeds `iosMain`, so two identical `PreviewSurface` signatures across the two
 * klibs would collide at the Kotlin/Native link step ("symbol already bound") when DemoKit links both. A
 * distinct package gives it a distinct IR signature. Keep the two copies in sync.
 *
 * Wraps preview content in [FrnkTheme] under a given [Appearance], so each preview renders against the
 * correct palette. The controller is passed directly to [FrnkTheme] — `FrnkTheme` provides its own
 * `LocalAppearanceController`, so an outer `CompositionLocalProvider` would be silently shadowed.
 */
@Composable
internal fun PreviewSurface(
    appearance: Appearance = Appearance.Light,
    content: @Composable () -> Unit,
) {
    val controller = remember(appearance) { AppearanceController().apply { this.appearance = appearance } }
    FrnkTheme(appearanceController = controller) {
        Column(
            modifier = Modifier.background(Theme[colors][colorBackground]).padding(FrnkSpacing.md),
            verticalArrangement = Arrangement.spacedBy(FrnkSpacing.sm),
        ) {
            content()
        }
    }
}
