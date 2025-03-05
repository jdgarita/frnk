package dev.garita.frnk.ui.componentLibrary

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf

internal val LocalSwiftlyColor = staticCompositionLocalOf { ComposeFrnkColor.default }
internal val LocalSwiftlyShapes = staticCompositionLocalOf { ComposeFrnkShape.default }

object FrnkTheme {
    val colors: ComposeFrnkColor
        @Composable
        @ReadOnlyComposable
        get() = LocalSwiftlyColor.current

    val shapes: ComposeFrnkShape
        @ReadOnlyComposable
        @Composable
        get() = LocalSwiftlyShapes.current

    var isDarkModeSupported: Boolean = false
}

@Composable
fun FrnkTheme(
    color: ComposeFrnkColor = if (isSystemInDarkTheme() && FrnkTheme.isDarkModeSupported) {
        ComposeFrnkColor.dark()
    } else {
        ComposeFrnkColor.light()
    },
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(
        LocalSwiftlyColor provides color
    ) {
        MaterialTheme(
            content = content
        )
    }
}