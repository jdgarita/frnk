package dev.jdgarita.frnk.demo

import androidx.compose.ui.graphics.Color
import dev.jdgarita.frnk.ui.theme.FrnkThemeConfig
import dev.jdgarita.frnk.ui.theme.colorOnPrimary
import dev.jdgarita.frnk.ui.theme.colorPrimary

/**
 * Shared demo theme override so Android and iOS render identically.
 *
 * Uses a red accent palette: `#DC2626` in light, `#F87171` in dark. Light keeps white text on the
 * accent; dark pairs the lighter accent with a deep-red `onPrimary` so button text stays legible.
 */
fun demoRedThemeConfig(): FrnkThemeConfig =
    FrnkThemeConfig(
        lightColorOverrides =
            mapOf(
                colorPrimary to Color(0xFFDC2626),
                colorOnPrimary to Color.White
            ),
        darkColorOverrides =
            mapOf(
                colorPrimary to Color(0xFFF87171),
                colorOnPrimary to Color(0xFF450A0A)
            )
    )