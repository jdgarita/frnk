package dev.jdgarita.frnk.demo

import androidx.compose.ui.graphics.Color
import dev.jdgarita.frnk.ui.theme.FrnkThemeConfig
import dev.jdgarita.frnk.ui.theme.colorOnPrimary
import dev.jdgarita.frnk.ui.theme.colorPrimary

/**
 * Shared demo theme override so Android and iOS render identically.
 *
 * Uses the violet accent palette from the frnk landing page (jdgarita.dev `--accent`):
 * `#6E56CF` in light, `#9F85FF` in dark. Light keeps white text on the accent (as the landing's
 * `.btn-primary` does); dark pairs the pastel accent with a deep-violet `onPrimary` so button text
 * stays legible.
 */
fun demoPurpleThemeConfig(): FrnkThemeConfig =
    FrnkThemeConfig(
        lightColorOverrides =
            mapOf(
                colorPrimary to Color(0xFF6E56CF),
                colorOnPrimary to Color.White
            ),
        darkColorOverrides =
            mapOf(
                colorPrimary to Color(0xFF9F85FF),
                colorOnPrimary to Color(0xFF1E1145)
            )
    )