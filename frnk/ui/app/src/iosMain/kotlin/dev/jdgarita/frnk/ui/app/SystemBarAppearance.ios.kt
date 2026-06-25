package dev.jdgarita.frnk.ui.app

import androidx.compose.runtime.Composable

@Composable
actual fun ApplySystemBarAppearance(darkTheme: Boolean) {
    // No-op: Compose Multiplatform on iOS manages the status bar via the hosting
    // UIViewController, whose appearance follows the system trait collection.
}