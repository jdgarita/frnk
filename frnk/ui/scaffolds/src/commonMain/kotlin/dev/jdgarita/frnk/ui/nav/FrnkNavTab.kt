package dev.jdgarita.frnk.ui.nav

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation3.runtime.NavKey

/**
 * A single bottom-nav tab for `FrnkNestedNavScaffold`: its stable [key], the [root] destination its back
 * stack starts from, and the two icon forms the adaptive bar needs — [icon] (a Compose [ImageVector] for
 * the Android floating toolbar) and [iosSystemIcon] (an SF-Symbol string for the iOS native bar) — plus
 * the [label].
 *
 * Folding the bar's icon/label presentation into the same declaration lets a host describe each tab
 * **once** instead of as two parallel lists. `FrnkNestedNavScaffold` derives both the bar items and their
 * target routes from a `List<FrnkNavTab>`.
 */
@Immutable
data class FrnkNavTab(
    val key: String,
    val root: NavKey,
    val icon: ImageVector,
    val label: String,
    val iosSystemIcon: String
)