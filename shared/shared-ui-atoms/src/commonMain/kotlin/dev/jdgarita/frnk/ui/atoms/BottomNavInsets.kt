package dev.jdgarita.frnk.ui.atoms

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp

/**
 * The bottom system navigation-bar inset (the space a bottom-pinned bar must float above so it never
 * sits over the system nav buttons / home indicator). `0.dp` when the host isn't edge-to-edge.
 *
 * Single source for the inset read shared by every bottom-nav `reservedHeight` (the floating
 * `FrnkBottomNavBar` here and the adaptive bar in `shared-ui-nav`, which `api`-depends on this module),
 * so the `WindowInsets.navigationBars` lookup isn't duplicated per bar.
 */
@Composable
fun frnkBottomSystemBarInset(): Dp = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
