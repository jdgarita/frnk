# ui-bottom-nav

The toolkit's **platform-adaptive bottom navigation** module: a Material3 *Expressive*
`HorizontalFloatingToolbar` (floating pill) on Android and a native glassy `UITabBar` (iOS 26+) / Material3
bar (older) on iOS, plus the tabbed scaffold + tab builder that wire it up. Depends on `:ui-scaffolds`
(tokens, theme, `EffectCollector`).

## The adaptive bar (`FrnkBottomNavBar`)

`FrnkBottomNavBar` is an **`expect`/`actual` composable** — each platform renders with its own engine, so
Material3 never leaves Android:
- **Android** (`androidMain/FrnkBottomNavBar.android.kt`) — a Material3 *Expressive*
  `HorizontalFloatingToolbar` floating pill, drawing each item's `ImageVector` directly via `IconButton` +
  `Icon`. The primary-action button is the toolbar's built-in docked FAB (`floatingActionButton` slot).
- **iOS** (`iosMain/FrnkBottomNavBar.ios.kt`) — narendraanjana09's
  [adaptive-nav-bar](https://github.com/narendraanjana09/adaptive-navigation-bar)
  (`io.github.narendraanjana09:adaptive-nav-bar`) `AdaptiveNavigationBar`: a native glassy `UITabBar`
  (iOS 26+) / Material3 Compose bar (older iOS), driven by each item's **SF-Symbol** `iosSystemIcon`. The
  primary-action button is the library's inline `IosFabItem`.

The primary-action button is frnk-owned (`FrnkNavPrimaryAction` + `stringPrimaryAction` token + the
`iconNavAdd` theme icon, same bookend treatment as Home/Settings) and surfaced to hosts via the scaffold's
`onPrimaryAction` callback — the host decides what tapping it does, **per screen** (re-skin per surface by
passing a custom `primaryAction`). It shows only when an action is wired. **Screen routing:** pass
`primaryActionRegistry` (`FrnkPrimaryActionRegistry`, `:core-nav`) and the currently active screen claims the
button via `FrnkPrimaryActionHandler { onIntent(...) }` (`:ui-scaffolds`) — the scaffold provides the
registry through `LocalFrnkPrimaryActionRegistry`, a screen claim wins over `onPrimaryAction` (the host-level
fallback), and the button hides when neither is wired. `FrnkAppShell` wires the registry automatically.

> **History.** Originally one of *two* engines: [Calf](https://github.com/MohamedRejeb/Calf) and
> adaptive-nav-bar, A/B-selectable via `FrnkAdaptiveNavEngine`. Calf was removed when adaptive-nav-bar became
> the default; then the **Android** engine was swapped from adaptive-nav-bar's Material3 `NavigationBar` to a
> Material3 Expressive `HorizontalFloatingToolbar` and the common API moved from `DrawableResource` to
> `ImageVector` (adaptive-nav-bar kept on iOS only). The spike history is in the MobiAI brain
> (`mobiai brain search "adaptive bottom nav"`).

## Icons: `ImageVector` (Android) + SF-Symbol (iOS)

The common item (`FrnkNavBarItem`) and tab (`FrnkAdaptiveNavTab`) carry **two** icon forms because the
engines consume different things:
- `icon: ImageVector` — a clean shared Compose vector, rendered directly by the Android floating toolbar.
  Defaults come from theme icon tokens (`iconNavHome`/`iconSettings`/`iconNavAdd`), host-overridable via
  `FrnkThemeConfig.iconOverrides`. **No `DrawableResource` anywhere on Android** — so there is **no host-side
  asset-packaging step** (the old AGP-9 wart is gone).
- `iosSystemIcon: String` — an SF-Symbol name. The native iOS 26+ `UITabBar` renders a UIKit symbol, not a
  Compose vector, so this identifier stays explicit. `rememberFrnkAdaptiveNavTabs(...)` supplies `"house"` /
  `"gearshape"`; hosts give middle tabs their own.

adaptive-nav-bar's `NavigationItem.icon` / `IosFabItem.icon` are non-null `DrawableResource`s (used only on
the older-iOS Compose fallback bar), but the API no longer carries one — so the **iOS** actual feeds the
library a single bundled placeholder, `commonMain/composeResources/drawable/frnk_nav_placeholder.xml` (in
commonMain because the Compose-resources plugin generates the `Res` accessor there regardless; only the iOS
actual references it). On iOS 26+ it is never shown (the glass bar uses `systemIcon`). `compose.components.resources`
is a commonMain dependency for that generated accessor; no Material3 rides on it. The library adds no native
cinterop, so the XCFrameworks still link under `dynamic_lookup`.

## Why this is its own module

It is the **one place in the toolkit that intentionally takes a Material3 dependency.** The Android bar is a
Material3 Expressive `HorizontalFloatingToolbar`, and adaptive-nav-bar (iOS) hard-depends on
`compose.material3` for its older-iOS fallback. Isolating both here keeps Material3 to a single, named module
rather than smeared across the design-system modules `:ui-theme`/`:ui-components`/`:ui-scaffolds` (which stay
Material-free, `compose-unstyled` only). The dependency is **split by source set** — `compose.material3`
(pinned to `1.10.0-alpha05` for `HorizontalFloatingToolbar`) in `androidMain`, adaptive-nav-bar +
`compose.components.resources` in `iosMain` — but it's all in this one module's `build.gradle.kts`, so the
quarantine holds. `:ui-app` `api`-depends on this module, so **Material3 reaches every consumer of the nav
layer** — a deliberate, host-approved trade.

> **Why `1.10.0-alpha05`?** Compose Multiplatform 1.11.1 resolves `compose.material3` to **1.9.0**, which
> ships only `FloatingToolbarTokens` — the `HorizontalFloatingToolbar` composable (`@ExperimentalMaterial3ExpressiveApi`)
> first appears in `1.10.0-alpha05`. The catalog pins it as `compose-material3-expressive`; bump it (and drop
> the override) once CMP's bundled material3 catches up.

adaptive-nav-bar is pure Kotlin/Compose (no extra native cinterop / SPM package), so the XCFramework still
links under the consumer's existing `-undefined dynamic_lookup`.

## Contents (`ui/app/`)

- `FrnkAppShell.kt` — **the one-call app shell** (host-enablement). Stands up a complete tabbed app:
  `FrnkTheme(themeConfig)` wrap, `frnkNavConfiguration(hostRoutes)`, `rememberFrnkAdaptiveNavTabs`
  (Home + `middleTabs` + Settings), `rememberFrnkTabbedBackStacks`, `FrnkTabbedNavScaffold`, built-in
  **Home** (`HomeScreen` with the host's `homeContent` `ColumnScope` slot; `homeTopBar`/`homeVmKey`/
  `homePrimaryActionEnabled`/`onHomeEffect`), **Settings** (default catalogue + `settingsExtraSections`,
  overridable via the `settingsState`/`settingsEffects` composable factories + `settingsVmKey`),
  optional **Onboarding** (`onboardingPages` → registers `ToolkitRoute.Onboarding`; back/close pops),
  deep-links (`pendingRoutes: FrnkPendingRouteRequest?`), and a shell-owned `FrnkPrimaryActionRegistry`.
  Host extension points — `effects` (the single `EffectCollector` home), `entries`
  (`EntryProviderScope<NavKey>.(FrnkAppScope) -> Unit`; **never** re-register the built-in routes,
  nav3 `require`-throws on duplicates), and the effect handlers — all receive the `FrnkAppScope`.
  Assumes **Koin is already started** (`frnkUiModules()` in `:ui-app` carries all scaffold VM modules). `:ui-app`'s
  `FrnkAppScaffold` layers the monetization batteries over this; `:demo-shared` uses the shell
  directly (it can't see `:ui-app`) and is the reference integration.
- `FrnkAppScope.kt` — `@Stable` handle (`tabbed: FrnkTabbedBackStacks` + `primaryActions` registry +
  `navigateTo`/`back`/`clearAndNavigateTo`) handed to every shell extension point.

## Contents (`ui/bottomnav/`)

- `FrnkBottomNavBar.kt` (common) — the `expect fun FrnkBottomNavBar(items, selectedIndex, onItemSelected,
  modifier, primaryAction, onPrimaryAction)` + `FrnkNavBarItem` (`key`, `icon: ImageVector`, `iosSystemIcon`,
  `label`) + the shared `FrnkNavBarDefaults` (`reservedHeight`, read by `FrnkTabbedNavScaffold` to inset
  content behind the overlaid bar). The two actuals:
  - `FrnkBottomNavBar.android.kt` — a Material3 Expressive `HorizontalFloatingToolbar` (floating pill),
    centered at the bottom; each item is an `IconButton` over its `ImageVector` (selected = `colorPrimary`,
    idle = `colorOnSurfaceVariant`); the primary action is the toolbar's docked FAB slot. Colors come from
    `FloatingToolbarDefaults.standardFloatingToolbarColors(...)` themed with `FrnkTheme` tokens, not
    `MaterialTheme`. Never touches `DrawableResource`. **Shadow parity:** the no-FAB overload defaults to
    `ContainerExpandedElevation` (`Level0` = 0.dp, no shadow) while the WithFab overload defaults to
    `ContainerExpandedElevationWithFab` (`Level1`), so the no-FAB call pins `expandedShadowElevation` to the
    WithFab value — the pill casts the same shadow on every screen, FAB or not.
  - `FrnkBottomNavBar.ios.kt` — adaptive-nav-bar's `AdaptiveNavigationBar`, themed from `FrnkTheme` tokens
    via `AdaptiveNavigationBarDefaults.colors(...)` (selected = `colorPrimary`, unselected =
    `colorOnSurfaceVariant`, indicator = `colorPrimaryContainer`, surface = `colorSurface`). Items render
    from `iosSystemIcon` + the bundled placeholder drawable; the primary action is an inline `IosFabItem`.
    `key(...)`-ed on the FAB's presence + color tokens because on iOS 26+ the library bakes its FAB handler
    and brand palette into the native `UITabBarAppearance` once in its `UIKitView` factory and never
    re-applies them (see the file's KDoc for the snap-on-recreate caveat).
  Most hosts use the scaffold; call the bar directly only when wiring your own selected-tab state /
  navigation. For a Material-free floating pill in the design-system tier, use the distinct `FrnkBottomNavBar`
  in `:ui-components` (different package).
- `FrnkTabbedNavScaffold.kt` — `FrnkTabbedNavScaffold(tabbed, tabs, modifier, primaryAction, onPrimaryAction, primaryActionRegistry, hideBarFor, entryProvider)`.
  The **nav3 multiple-back-stack** tabbed scaffold: the single composable a host calls to get a standard
  tabbed app. It absorbs the `FrnkNavDisplay` (driven by `tabbed.current`), the persistent
  `FrnkBottomNavBar` overlay (tab switch / re-tap-to-root + the built-in primary-action button),
  `FrnkTabbedBackHandler` (back-from-non-home-root→home), full-screen bar hiding (`hideBarFor`), and the
  bottom-inset bookkeeping (provides `LocalFrnkBottomBarInset` = the bar's `reservedHeight` while it shows, so
  screens on `FrnkScreenScaffold`/`FrnkMviScreen` reserve it automatically — no per-screen `bottomInset`
  threading). **The host still owns `tabbed`** (`rememberFrnkTabbedBackStacks(tabs = …)`) and the same
  (remembered) `List<FrnkAdaptiveNavTab>`, so it can drive effect-based navigation from its own
  `EffectCollector` — this scaffold structures/renders, the host owns state. `hideBarFor` **defaults to
  `{ it is FrnkFullScreenRoute }`** (the marker in `:core-nav`), so full-screen routes declare the
  intent on themselves rather than the host keeping a predicate in sync with `entryProvider`; override only
  for ad-hoc rules. `entryProvider` defaults to `koinEntryProvider()` (pair with the `navigation<Route>`
  DSL); pass an inline `entryProvider { entry<…> { … } }` when screens share one host-scoped VM (the demo
  does). This is the **Material3 adaptive-bar** scaffold by design; a host that wants multiple-back-stack
  nav3 **without** Material3 (e.g. the Material-free `FrnkBottomNavBar` pill) hand-wires the primitives
  instead (`rememberFrnkTabbedBackStacks` + `FrnkNavDisplay` + `FrnkTabbedBackHandler` + its own bar).
  `@OptIn(KoinExperimentalAPI::class)` (for the `koinEntryProvider()` default — doesn't propagate to callers
  passing their own provider).
- `FrnkAdaptiveNavTab.kt` / `FrnkAdaptiveNavDefaults.kt` — the tab type (`key` + `root` + `icon: ImageVector`
  + `iosSystemIcon` SF-Symbol) and `rememberFrnkAdaptiveNavTabs(homeRoot, settingsRoot, middleTabs, …)`, which
  enforces the product rule **every app has at least Home + Settings**: a fixed Home tab, the host's optional
  `middleTabs`, then a fixed Settings tab (bookend labels from `FrnkStrings`, icons from the `iconNavHome` /
  `iconSettings` theme tokens + `"house"` / `"gearshape"` SF-Symbols).
- `FrnkNavPrimaryAction.kt` — the primary-action descriptor (`icon: ImageVector` + `iosSystemIcon` + `label`)
  and `rememberFrnkNavPrimaryAction(...)` (defaults to the `iconNavAdd` token + `"plus"`).

## Override model

- **Override the tabs**: pass `middleTabs` to `rememberFrnkAdaptiveNavTabs` (Home/Settings bookends are always
  present), or build the `List<FrnkAdaptiveNavTab>` by hand for a fully custom shape.
- **Wire the navigation**: use `FrnkTabbedNavScaffold` with a host-owned `rememberFrnkTabbedBackStacks` + an
  `entryProvider` — frnk owns the display + bar + tab switching + back convention + bar-inset; the host owns
  the back stacks and registers destinations.

## Dependencies

- `api(projects.uiScaffolds)` — tokens, theme, `EffectCollector`, `LocalFrnkBottomBarInset`.
- `api(compose.runtime / foundation / ui)`.
- `commonMain`: `implementation(compose.components.resources)` — runtime for the generated `Res` accessor
  (the iOS placeholder drawable). Not Material3.
- `androidMain`: `implementation(libs.compose.material3.expressive)` (material3 `1.10.0-alpha05`) — the
  `HorizontalFloatingToolbar`. **The sole Material3 dependency in the toolkit.** Don't add Material3 to any
  other shared module.
- `iosMain`: `implementation(libs.adaptive.nav.bar)` — the native glassy bar engine.

## Rules

- Material3 lives **here only**. Other modules (especially `:ui-theme`/`:ui-components`/`:ui-scaffolds`) stay `compose-unstyled`.
- New adaptive-nav surface goes in `ui/bottomnav/` with the same conventions as atoms/scaffolds (`@Immutable`
  state where applicable, callbacks before `modifier`, tokens-only styling).
