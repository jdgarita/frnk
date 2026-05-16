package dev.jdgarita.frnk.ui.atoms.previews

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.composeunstyled.theme.Theme
import dev.jdgarita.frnk.ui.theme.Appearance
import dev.jdgarita.frnk.ui.theme.AppearanceController
import dev.jdgarita.frnk.ui.theme.FrnkTheme
import dev.jdgarita.frnk.ui.theme.LocalAppearanceController
import dev.jdgarita.frnk.ui.theme.colorBackground
import dev.jdgarita.frnk.ui.theme.colors
import dev.jdgarita.frnk.ui.tokens.FrnkSpacing

/**
 * Wraps preview content in [FrnkTheme] under a given [Appearance], so each preview
 * renders against the correct palette in Android Studio / Compose Multiplatform previews.
 */
@Composable
internal fun PreviewSurface(
    appearance: Appearance = Appearance.Light,
    content: @Composable () -> Unit,
) {
    val controller = remember(appearance) { AppearanceController().apply { this.appearance = appearance } }
    CompositionLocalProvider(LocalAppearanceController provides controller) {
        FrnkTheme {
            Column(
                modifier = Modifier.background(Theme[colors][colorBackground]).padding(FrnkSpacing.md),
                verticalArrangement = Arrangement.spacedBy(FrnkSpacing.sm),
            ) {
                content()
            }
        }
    }
}
