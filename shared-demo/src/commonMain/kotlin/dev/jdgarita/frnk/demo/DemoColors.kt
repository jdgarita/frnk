package dev.jdgarita.frnk.demo

import androidx.compose.ui.graphics.Color
import dev.jdgarita.frnk.ui.theme.FrnkThemeConfig
import dev.jdgarita.frnk.ui.theme.colorOnPrimary
import dev.jdgarita.frnk.ui.theme.colorPrimary

/** Shared demo theme override so Android and iOS render identically. */
fun demoBlueThemeConfig(): FrnkThemeConfig =
    FrnkThemeConfig(
        lightColorOverrides =
            mapOf(
                colorPrimary to Color(0xFF0A84FF),
                colorOnPrimary to Color.White,
            ),
        darkColorOverrides =
            mapOf(
                colorPrimary to Color(0xFF409CFF),
                colorOnPrimary to Color(0xFF001A33),
            ),
    )
