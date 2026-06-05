# shared-ui-nav

The toolkit's **platform-adaptive bottom navigation** module: a genuine native UIKit `UITabBar` on iOS and
a Material3 `NavigationBar` on Android, plus the default scaffold + tab builder that wire it up. Depends on
`shared-ui-atoms` (tokens, theme, `BottomNavScaffoldState`/`BottomNavViewModel`, `FrnkBottomNavItem`).

## Why this is its own module

It is the **one place in the toolkit that intentionally takes a Material3 dependency.** The adaptive bar is
built on [Calf](https://github.com/MohamedRejeb/Calf)'s `AdaptiveNavigationBar`, which renders the native
`UITabBar` on iOS and a Material3 `NavigationBar` elsewhere — and Calf hard-depends on `compose.material3`.
Isolating it here keeps that dependency to a single, named module rather than smeared across `shared-ui-atoms`
(which stays Material-free, `compose-unstyled` only). `:shared` `api`-aggregates this module and `:iosApp`
exports it, so **Material3 + Calf ship in FrnkKit for every consumer** — a deliberate, host-approved trade
(the alternative, a hand-rolled UIKit `UITabBar` interop with zero Material3, was considered and declined in
favour of the maintained component; see `docs/spikes/adaptive-bottom-nav.md`).

Calf is pure Kotlin/Compose (no extra native cinterop / SPM package), so the XCFramework still links under the
consumer's existing `-undefined dynamic_lookup`.

## Contents (`ui/bottomnav/`)

- `FrnkAdaptiveBottomNavBar.kt` — `FrnkAdaptiveBottomNavBar(items, selectedIndex, onItemSelected, modifier)`.
  The low-level adaptive bar over Calf's `AdaptiveNavigationBar` (`@OptIn(ExperimentalCalfUiApi::class)`):
  iOS `UIKitUITabBarItem`s (icons via `UIKitImage.Vector`, rasterised from the `FrnkBottomNavItem` `ImageVector`),
  Android Material3 `NavigationBarItem`s. **Themed from `FrnkTheme` tokens, not the platform defaults** — both
  bars take selected = `colorPrimary`, unselected = `colorOnSurfaceVariant` (Android adds a
  `colorPrimaryContainer` indicator) via Calf's `UIKitTabBarConfiguration` on iOS (→ `UITabBar.tintColor`) and
  `NavigationBarItemDefaults.colors(...)` on Android, so the host's brand flows through instead of iOS system
  blue / the Material baseline. The bar **surface** is themed too: `containerColor = colorSurface` /
  `contentColor = colorOnSurface` are passed to `AdaptiveNavigationBar`, otherwise the Android Material3
  `NavigationBar` would fall back to `NavigationBarDefaults.containerColor` (the unthemed Material baseline,
  i.e. light) and **ignore dark mode**. (Calf's iOS `UITabBar` takes no background token in 0.12.0 — it keeps
  its native translucent material, the desired native look there.) (Caveat: iOS 26 "Liquid Glass" ignores the
  *unselected* tint; the selected brand tint still applies.) Use directly only when wiring your own
  selected-tab state / navigation (as the demo does); most hosts use the scaffold. For the Material-free
  floating pill, use `FrnkBottomNavBar` in `shared-ui-atoms` instead.
- `FrnkAdaptiveBottomNavScaffold.kt` — `FrnkAdaptiveBottomNavScaffold` (VM-backed) + `…Content` (stateless).
  The **default** scaffold: owns which tab is selected (reusing `BottomNavViewModel` + `BottomNavScaffoldState`
  from atoms, so `BottomNavEffect.TabSelected` behaves identically to the pill `BottomNavScaffold`) and pins
  the adaptive bar below the selected destination. Unlike the floating pill the native bar is opaque, so the
  `tabContent` slot is laid **above** it (a `Column`, not a `Box` overlay) and gets a zero bottom inset. The
  host supplies each tab's screen through `tabContent` — wiring its own per-tab navigation there.
- `FrnkTabbedNavScaffold.kt` — `FrnkTabbedNavScaffold(tabbed, tabs, modifier, hideBarFor, entryProvider)`.
  The **nav3 multiple-back-stack** tabbed scaffold: the single composable a host calls to get a standard
  tabbed app. It absorbs the `FrnkNavDisplay` (driven by `tabbed.current`), the persistent
  `FrnkAdaptiveBottomNavBar` overlay (tab switch / re-tap-to-root), `FrnkTabbedBackHandler`
  (back-from-non-home-root→home), full-screen bar hiding (`hideBarFor`), and the bottom-inset bookkeeping
  (provides `LocalFrnkBottomBarInset` = the bar's `reservedHeight` while it shows, so screens on
  `FrnkScreenScaffold`/`FrnkMviScreen` reserve it automatically — no per-screen `bottomInset` threading).
  **The host still owns `tabbed`** (`rememberFrnkTabbedBackStacks(navTabs = …)` in atoms) and the same
  (remembered) `List<FrnkNavTab>`, so it can drive effect-based navigation from its own `EffectCollector` —
  this scaffold structures/renders, the host owns state. `hideBarFor` **defaults to
  `{ it is FrnkFullScreenRoute }`** (the marker in `:shared-ui-api`), so full-screen routes declare the
  intent on themselves rather than the host keeping a predicate in sync with `entryProvider`; override only
  for ad-hoc rules. `entryProvider` defaults to `koinEntryProvider()` (pair with the `navigation<Route>`
  DSL); pass an inline `entryProvider { entry<…> { … } }` when screens share one host-scoped VM (the demo
  does). This is the **Material3 adaptive-bar** scaffold by design (it renders `FrnkAdaptiveBottomNavBar`); a
  host that wants multiple-back-stack nav3 **without** Material3 (e.g. the Material-free `FrnkBottomNavBar`
  pill) hand-wires the primitives instead (`rememberFrnkTabbedBackStacks` + `FrnkNavDisplay` +
  `FrnkTabbedBackHandler` + its own bar). Contrast with `FrnkAdaptiveBottomNavScaffold` below — that one is
  the simpler index-based scaffold (no per-tab back stacks / no pushed detail screens); use this when tabs
  need their own navigation back stacks. `@OptIn(KoinExperimentalAPI::class)` (for the `koinEntryProvider()`
  default — doesn't propagate to callers passing their own provider).
- `FrnkBottomNavDefaults.kt` — `rememberFrnkBottomNavState(middleTabs = emptyList(), …)`. Builds the default
  `BottomNavScaffoldState` enforcing the product rule **every app has at least Home + Settings**: a fixed Home
  tab, the host's optional `middleTabs`, then a fixed Settings tab. Home/Settings resolve icon + label from
  `FrnkIcons`/`FrnkStrings`, so hosts re-skin them via `FrnkThemeConfig`.

## Override model

- **Override the tabs**: pass `middleTabs` to `rememberFrnkBottomNavState` (Home/Settings bookends are always
  present), or build a `BottomNavScaffoldState` by hand for a fully custom shape.
- **Wire the navigation**: two paths. For **single-screen tabs**, use `FrnkAdaptiveBottomNavScaffold` and
  render each tab through the `tabContent` slot. For **tabs that need their own back stacks** (pushed detail
  screens, per-tab navigation), use `FrnkTabbedNavScaffold` with a host-owned `rememberFrnkTabbedBackStacks`
  + an `entryProvider` — frnk owns the display + bar + tab switching + back convention + bar-inset; the host
  owns the back stacks and registers destinations.

## Dependencies

- `api(projects.sharedUiAtoms)` — tokens, theme, `BottomNavScaffoldState`/VM, `FrnkBottomNavItem`, `EffectCollector`.
- `api(compose.runtime / foundation / ui)`.
- `implementation(libs.calf.ui)` + `implementation(compose.material3)` — the adaptive bar's engine. **The sole
  Material3 dependency in the toolkit.** Don't add Material3 to any other shared module.

## Rules

- Material3 lives **here only**. Other modules (especially `shared-ui-atoms`) stay `compose-unstyled`.
- New adaptive-nav surface goes in `ui/bottomnav/` with the same conventions as atoms/scaffolds (`@Immutable`
  state where applicable, callbacks before `modifier`, tokens-only styling).
