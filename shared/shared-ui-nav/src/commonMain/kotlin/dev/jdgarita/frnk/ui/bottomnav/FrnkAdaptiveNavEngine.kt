package dev.jdgarita.frnk.ui.bottomnav

/**
 * Selects which adaptive bottom-bar implementation [FrnkTabbedNavScaffold] renders. The two engines are
 * bundled side-by-side for a POC so a host can A/B their UX/performance at runtime.
 *
 * - [Calf] — the original bar over [Calf](https://github.com/MohamedRejeb/Calf): a genuine native UIKit
 *   `UITabBar` on iOS and a Material3 `NavigationBar` on Android. No built-in "add" button. **Default.**
 * - [AdaptiveNavBar] — the bar over
 *   [narendraanjana09/adaptive-nav-bar](https://github.com/narendraanjana09/adaptive-navigation-bar):
 *   a Material3 `NavigationBar` on Android and a native glassy `UITabBar` (iOS 26+) / Material3 bar
 *   (older) on iOS, **with a built-in "add" button** (a FAB on Android, an inline button on iOS). Its
 *   icons are resource-based (`DrawableResource` + SF-Symbol string), not `ImageVector`.
 */
enum class FrnkAdaptiveNavEngine {
    Calf,
    AdaptiveNavBar,
}
