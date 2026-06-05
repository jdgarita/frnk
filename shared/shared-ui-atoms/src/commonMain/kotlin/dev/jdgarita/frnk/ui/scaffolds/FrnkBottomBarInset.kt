package dev.jdgarita.frnk.ui.scaffolds

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.dp

/**
 * The bottom inset a screen should reserve so its scrollable content clears a host-owned bottom bar that
 * floats **over** the content (e.g. the adaptive bar `FrnkTabbedNavScaffold` overlays). Defaults to
 * `0.dp`, so outside a providing scaffold every screen behaves exactly as before.
 *
 * `FrnkScreenScaffold` and `FrnkMviScreen` default their `bottomInset` parameter to this local, so a
 * screen rendered inside `FrnkTabbedNavScaffold` (which provides the bar's reserved height here)
 * automatically reserves the bar's footprint without the host threading `bottomInset = …` into every
 * destination. An explicit `bottomInset` argument still wins. Declared in `:shared-ui-atoms` (where the
 * scaffolds that read it live); `:shared-ui-nav`'s `FrnkTabbedNavScaffold` provides the real value.
 *
 * `static` because it changes rarely (only when the bar's reserved height changes); a static local skips
 * tracking reads for cheaper propagation.
 */
val LocalFrnkBottomBarInset = staticCompositionLocalOf { 0.dp }
