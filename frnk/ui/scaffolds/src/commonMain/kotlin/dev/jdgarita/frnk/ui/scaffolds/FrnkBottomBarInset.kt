package dev.jdgarita.frnk.ui.scaffolds

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.unit.dp

/**
 * The bottom inset a screen should reserve so its scrollable content clears a host-owned bottom bar that
 * floats **over** the content (the adaptive bar `FrnkNestedNavScaffold` overlays). Defaults to `0.dp`, so
 * outside a providing scaffold every screen behaves exactly as before.
 *
 * `FrnkScreenScaffold` defaults its `bottomInset` parameter to this local, so a screen rendered inside
 * `FrnkNestedNavScaffold` (which provides the bar's reserved height here) automatically reserves the bar's
 * footprint without the host threading `bottomInset = …` into every destination. An explicit `bottomInset`
 * argument still wins. Declared in `:ui-scaffolds` (where the scaffolds that read it live); `:ui-bottom-nav`'s
 * `FrnkNestedNavScaffold` provides the real value.
 *
 * Non-static (`compositionLocalOf`, **not** `staticCompositionLocalOf`) so that if the provided value
 * changes at runtime only the screens that actually read the inset recompose, not the entire provider subtree.
 */
val LocalFrnkBottomBarInset = compositionLocalOf { 0.dp }