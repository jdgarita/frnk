package dev.jdgarita.frnk.ui.bottomnav

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation3.runtime.NavKey

/**
 * The host-facing config for the bottom bar's **center "feature" tab** — the one tab a host configures
 * ([FrnkBottomNavState]'s Home and Settings bookends are toolkit-owned and fixed). It is a real
 * navigable tab: tapping it switches to its own back stack (re-tapping resets that stack to [route]),
 * exactly like the Home and Settings tabs.
 *
 * Point it at the app's signature surface — a "New X" flow, a capture/camera screen, the app's main
 * library — by supplying that screen's [route] (which the host registers in its `entryProvider`) plus
 * the two icon forms the adaptive bar needs: [icon] (a Compose [ImageVector] for the Android floating
 * toolbar) and [iosSystemIcon] (an SF-Symbol name for the iOS native bar).
 *
 * @param route the tab's back-stack root destination (host-defined; register an `entry(route) { … }`).
 * @param label the tab label, doubling as the bar item's accessibility description.
 * @param icon the Android `ImageVector` icon.
 * @param iosSystemIcon the iOS SF-Symbol name.
 * @param key the stable tab key (kept stable across recompositions; rarely needs overriding).
 */
@Immutable
data class FrnkFeatureItem(
    val route: NavKey,
    val label: String,
    val icon: ImageVector,
    val iosSystemIcon: String,
    val key: String = "feature",
)
