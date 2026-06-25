package dev.jdgarita.frnk.ui.bottomnav

import androidx.compose.runtime.Immutable
import androidx.navigation3.runtime.NavKey
import dev.jdgarita.frnk.ui.theme.FrnkIconSource

/**
 * Host-provided descriptor for [FrnkNestedNavScaffold]'s **middle tab** — the one tab the toolkit does
 * not fix. Home and Settings are owned by the scaffold (theme icon tokens + `FrnkTabRoute.Home` /
 * `FrnkTabRoute.Settings`); the host supplies this middle tab's:
 *  - [route] — its nested-graph root and the bar-tap target (a [FrnkTabRoute.Custom] or the host's own
 *    `NavKey`); the host registers the destination behind it via `onNestedNavigationModule`,
 *  - [icon] — a theme [FrnkIconSource.Token] or a host-owned [FrnkIconSource.Vector] (Android bar),
 *  - [iosSystemIcon] — the SF-Symbol name for the native iOS bar,
 *  - [label] — the tab label.
 */
@Immutable
data class FrnkCustomTab(
    val route: NavKey,
    val icon: FrnkIconSource,
    val iosSystemIcon: String,
    val label: String
)