package dev.jdgarita.frnk.demo

import androidx.compose.ui.graphics.Color
import dev.jdgarita.frnk.ui.atoms.ToolkitColors

/** Shared demo palette so Android and iOS render identically. */
fun demoBlueColors(): ToolkitColors =
    ToolkitColors(
        primary = Color(0xFF0A84FF),
        onPrimary = Color.White,
    )
