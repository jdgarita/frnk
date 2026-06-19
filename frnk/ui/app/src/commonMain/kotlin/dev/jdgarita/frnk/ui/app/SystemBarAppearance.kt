package dev.jdgarita.frnk.ui.app

import androidx.compose.runtime.Composable

/**
 * Applies the correct system-bar icon contrast for the resolved theme.
 *
 * When [darkTheme] is `true`, status- and navigation-bar icons are rendered light
 * (for a dark background); when `false`, they are rendered dark (for a light background).
 *
 * - **Android**: drives `WindowInsetsControllerCompat.isAppearanceLight{Status,Navigation}Bars`.
 * - **iOS**: no-op — the hosting `UIViewController` manages the status bar.
 */
@Composable
expect fun ApplySystemBarAppearance(darkTheme: Boolean)