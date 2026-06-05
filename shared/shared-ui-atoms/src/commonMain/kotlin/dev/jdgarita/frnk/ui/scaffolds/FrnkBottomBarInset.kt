package dev.jdgarita.frnk.ui.scaffolds

import androidx.compose.runtime.compositionLocalOf
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
 * Non-static (`compositionLocalOf`, **not** `staticCompositionLocalOf`) because the provided value **does
 * change** at runtime: `FrnkTabbedNavScaffold` flips it between the bar's reserved height and `0.dp` when a
 * full-screen route hides the bar. A static local would invalidate the *entire* provider subtree on each
 * flip; the tracked local recomposes only the screens that actually read the inset.
 */
val LocalFrnkBottomBarInset = compositionLocalOf { 0.dp }
