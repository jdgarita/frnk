package dev.jdgarita.frnk.ui.tokens

import androidx.compose.ui.graphics.Color

object FrnkPrimitiveColors {
    val neutral0 = Color(0xFFFFFFFF)
    val neutral50 = Color(0xFFFAFAFA)
    val neutral100 = Color(0xFFF5F5F5)
    val neutral200 = Color(0xFFE5E5E5)
    val neutral300 = Color(0xFFD4D4D4)
    val neutral400 = Color(0xFFA3A3A3)
    val neutral500 = Color(0xFF737373)
    val neutral600 = Color(0xFF525252)
    val neutral700 = Color(0xFF404040)
    val neutral800 = Color(0xFF262626)
    val neutral900 = Color(0xFF171717)
    val neutral950 = Color(0xFF0A0A0A)

    val purple100 = Color(0xFFEDE9FE)
    val purple300 = Color(0xFFC4B5FD)
    val purple500 = Color(0xFF8B5CF6)
    val purple600 = Color(0xFF7C3AED)
    val purple700 = Color(0xFF6D28D9)
    val purple900 = Color(0xFF4C1D95)

    val orange400 = Color(0xFFFB923C)
    val orange500 = Color(0xFFF97316)
    val orange700 = Color(0xFFC2410C)

    val red400 = Color(0xFFF87171)
    val red500 = Color(0xFFEF4444)
    val red700 = Color(0xFFB91C1C)

    val green400 = Color(0xFF4ADE80)
    val green500 = Color(0xFF22C55E)
    val green700 = Color(0xFF15803D)
}

object FrnkColors {
    object Light {
        val primary = FrnkPrimitiveColors.purple600
        val onPrimary = FrnkPrimitiveColors.neutral0
        val primaryContainer = FrnkPrimitiveColors.purple100
        val onPrimaryContainer = FrnkPrimitiveColors.purple900

        val secondary = FrnkPrimitiveColors.neutral700
        val onSecondary = FrnkPrimitiveColors.neutral0

        val background = FrnkPrimitiveColors.neutral50
        val onBackground = FrnkPrimitiveColors.neutral900

        val surface = FrnkPrimitiveColors.neutral0
        val onSurface = FrnkPrimitiveColors.neutral900
        val surfaceVariant = FrnkPrimitiveColors.neutral100
        val onSurfaceVariant = FrnkPrimitiveColors.neutral700

        val outline = FrnkPrimitiveColors.neutral300
        val outlineVariant = FrnkPrimitiveColors.neutral200

        val error = FrnkPrimitiveColors.red500
        val onError = FrnkPrimitiveColors.neutral0
        val warning = FrnkPrimitiveColors.orange500
        val onWarning = FrnkPrimitiveColors.neutral900
        val success = FrnkPrimitiveColors.green500
        val onSuccess = FrnkPrimitiveColors.neutral0

        val scrim = Color(0x80000000)
    }

    object Dark {
        val primary = FrnkPrimitiveColors.purple300
        val onPrimary = FrnkPrimitiveColors.purple900
        val primaryContainer = FrnkPrimitiveColors.purple700
        val onPrimaryContainer = FrnkPrimitiveColors.purple100

        val secondary = FrnkPrimitiveColors.neutral300
        val onSecondary = FrnkPrimitiveColors.neutral900

        val background = FrnkPrimitiveColors.neutral950
        val onBackground = FrnkPrimitiveColors.neutral100

        val surface = FrnkPrimitiveColors.neutral900
        val onSurface = FrnkPrimitiveColors.neutral100
        val surfaceVariant = FrnkPrimitiveColors.neutral800
        val onSurfaceVariant = FrnkPrimitiveColors.neutral300

        val outline = FrnkPrimitiveColors.neutral700
        val outlineVariant = FrnkPrimitiveColors.neutral800

        val error = FrnkPrimitiveColors.red400
        val onError = FrnkPrimitiveColors.neutral900
        val warning = FrnkPrimitiveColors.orange400
        val onWarning = FrnkPrimitiveColors.neutral900
        val success = FrnkPrimitiveColors.green400
        val onSuccess = FrnkPrimitiveColors.neutral900

        val scrim = Color(0x80000000)
    }
}
