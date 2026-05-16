package dev.jdgarita.frnk.demo

import androidx.compose.ui.graphics.Color
import dev.jdgarita.frnk.ui.theme.FrnkThemeConfig
import dev.jdgarita.frnk.ui.theme.onPrimary
import dev.jdgarita.frnk.ui.theme.primary

/** Shared demo theme override so Android and iOS render identically. */
fun demoBlueThemeConfig(): FrnkThemeConfig =
    FrnkThemeConfig(
        lightColorOverrides =
            mapOf(
                primary to Color(0xFF0A84FF),
                onPrimary to Color.White,
            ),
        darkColorOverrides =
            mapOf(
                primary to Color(0xFF409CFF),
                onPrimary to Color(0xFF001A33),
            ),
    )
