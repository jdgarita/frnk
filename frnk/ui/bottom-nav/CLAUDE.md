# ui-bottom-nav

The toolkit's **platform-adaptive bottom navigation** module: a Material3 *Expressive*
`HorizontalFloatingToolbar` (floating pill) on Android and a native glassy `UITabBar` (iOS 26+) / Material3
bar (older) on iOS, plus the MVI-backed tabbed scaffold (`FrnkNestedNavScaffold`) that wires it up. Depends
on `:ui-scaffolds` (tokens, theme, `EffectCollector`, `FrnkNavTab`).

## The adaptive bar (`FrnkBottomFloatingBar`)

`FrnkBottomFloatingBar` is an **`expect`/`actual` composable** — each platform renders with its own engine, so
Material3 never leaves Android:
- **Android** (`androidMain/FrnkBottomFloatingBar.android.kt`) — a Material3 *Expressive*
  `HorizontalFloatingToolbar` floating pill, drawing each item's `ImageVector` directly via `IconButton` +
  `Icon`. Renders the items only — no FAB.
- **iOS** (`iosMain/FrnkBottomFloatingBar.ios.kt`) — the toolkit's **vendored** `AdaptiveNavigationBar`
  (under `ui.bottomnav.vendor`, adapted from narendraanjana09's
  [adaptive-nav-bar](https://github.com/narendraanjana09/adaptive-navigation-bar)): a native glassy `UITabBar`
  (iOS 26+) / Material3 Compose bar (older iOS), driven by each item's **SF-Symbol** `iosSystemIcon`. Renders
  the items only — no FAB. Vendored so the toolkit owns the bar's `UIKitView` update block (the upstream
  artifact exposed no Compose-driven hook), which re-applies theme changes in place instead of recreating.

The bar renders **whatever tabs the host supplies** — there is no fixed shape and no FAB. The host hands
`FrnkNestedNavScaffold` (below) a `tabs: List<FrnkNavTab>` (any count, any roots, all destinations
host-supplied), and the scaffold derives one bar item per tab. There are **no toolkit-owned built-in
tabs** — Home/Settings are not baked in; a host that wants them declares them as `FrnkNavTab`s like any
other. Each tab is a **real navigable tab** (selection highlight, re-tap-to-root). The old dynamic
primary-action / FAB / Mode-B mechanism (`FrnkNavPrimaryAction`, `FrnkPrimaryActionRegistry`,
`FrnkPrimaryActionHandler`, `onPrimaryAction`) was removed — see
`mobiai brain search "fixed three-tab bottom bar"`.

> **History.** Originally one of *two* engines: [Calf](https://github.com/MohamedRejeb/Calf) and
> adaptive-nav-bar, A/B-selectable via `FrnkAdaptiveNavEngine`. Calf was removed when adaptive-nav-bar became
> the default; then the **Android** engine was swapped from adaptive-nav-bar's Material3 `NavigationBar` to a
> Material3 Expressive `HorizontalFloatingToolbar` and the common API moved from `DrawableResource` to
> `ImageVector` (adaptive-nav-bar kept on iOS only, then **vendored** into `ui.bottomnav.vendor`); the FAB was
> retired for a centered bar item (Mode B), and the dynamic primary-action mechanism was later dropped
> entirely for the fixed three-tab `Home · feature · Settings` bar. The spike + decision history is in the
> MobiAI brain (`mobiai brain search "adaptive bottom nav"`).

## Icons: `ImageVector` (Android) + SF-Symbol (iOS)

The common bar item carries **two** icon forms because the engines consume different things. The item is
`FrnkNavBarItem` (`key`, `icon: FrnkIconSource`, `iosSystemIcon`, `label`); `FrnkNestedNavScaffold`'s VM
builds one per `FrnkNavTab` (icon via `FrnkIconSource.Vector(tab.icon)`):
- `icon: FrnkIconSource` — resolved to a Compose `ImageVector` on Android (via `.resolve()`), rendered
  directly by the Android floating toolbar. **No `DrawableResource` anywhere on Android** — so there is
  **no host-side asset-packaging step** (the old AGP-9 wart is gone).
- `iosSystemIcon: String` — an SF-Symbol name. The native iOS 26+ `UITabBar` renders a UIKit symbol, not a
  Compose vector, so this identifier stays explicit. The host supplies it per tab (via `FrnkNavTab.iosSystemIcon`).

The vendored bar's `NavigationItem.icon` is a non-null `DrawableResource` (used only on the older-iOS Compose
fallback bar), but the API no longer carries one — so the **iOS** actual feeds the
vendored bar a single bundled placeholder, `commonMain/composeResources/drawable/frnk_nav_placeholder.xml` (in
commonMain because the Compose-resources plugin generates the `Res` accessor there regardless; only the iOS
actual references it). On iOS 26+ it is never shown (the glass bar uses `systemIcon`). `compose.components.resources`
is a commonMain dependency for that generated accessor; no Material3 rides on it. The library adds no native
cinterop, so the XCFrameworks still link under `dynamic_lookup`.

## Why this is its own module

It is the **one place in the toolkit that intentionally takes a Material3 dependency.** The Android bar is a
Material3 Expressive `HorizontalFloatingToolbar`, and the **vendored** iOS bar's older-iOS fallback
(`ComposeNavigationBar`) needs `compose.material3`. Isolating both here keeps Material3 to a single, named module
rather than smeared across the design-system modules `:ui-theme`/`:ui-components`/`:ui-scaffolds` (which stay
Material-free, `compose-unstyled` only). The dependency is **split by source set** — `compose.material3`
(pinned to `1.10.0-alpha05` for `HorizontalFloatingToolbar`) in `androidMain`, `compose.material3` (for the
vendored fallback) + `compose.components.resources` in `iosMain` — but it's all in this one module's `build.gradle.kts`, so the
quarantine holds. `:ui-app` `api`-depends on this module, so **Material3 reaches every consumer of the nav
layer** — a deliberate, host-approved trade.

> **Why `1.10.0-alpha05`?** Compose Multiplatform 1.11.1 resolves `compose.material3` to **1.9.0**, which
> ships only `FloatingToolbarTokens` — the `HorizontalFloatingToolbar` composable (`@ExperimentalMaterial3ExpressiveApi`)
> first appears in `1.10.0-alpha05`. The catalog pins it as `compose-material3-expressive`; bump it (and drop
> the override) once CMP's bundled material3 catches up.

adaptive-nav-bar is pure Kotlin/Compose (no extra native cinterop / SPM package), so the XCFramework still
links under the consumer's existing `-undefined dynamic_lookup`.

## Contents (`ui/bottomnav/`)

- `FrnkNestedNavScaffold.kt` — **the flexible tabbed scaffold** (replaced the removed `FrnkTabbedNavScaffold`);
  the public composable a host calls to get a multiple-back-stack tabbed surface.
  `FrnkNestedNavScaffold(modifier, tabs: List<FrnkNavTab>, onSavedStateConfiguration: () -> SavedStateConfiguration, onNestedNavigationModule: (backStack: NavBackStack<NavKey>) -> Module)`
  takes the host's **own** `tabs` (any count, any roots, **all** destinations host-supplied — no built-in
  Home/Settings, no fixed shape), derives the bar items + routes from them (mapping each `FrnkNavTab` →
  `FrnkNavBarItemModel`, icon via `FrnkIconSource.Vector(tab.icon)`, `route = tab.root`), and renders
  `FrnkNavDisplay(backStack)` + the persistent `FrnkBottomFloatingBar` overlay + a `BackHandler { backStack.back() }`,
  providing `LocalFrnkBottomBarInset`. It is backed by the MVI `FrnkNestedNavViewModel` (below), which **owns
  the bar's view state** (`items` + `selectedIndex`) — selection lives in the VM, **not** in `remember`. A
  tap → `FrnkNestedNavIntent.Tap(index)` → the VM updates `selectedIndex` + emits
  `FrnkNestedNavEffect.Navigate(route)` → the scaffold calls `backStack.navigateTo(route)`. The host supplies
  the saved-state config (`onSavedStateConfiguration`, e.g. `{ frnkNestedNavConfig() }`) and the nested-nav
  Koin module (`onNestedNavigationModule`, handed the created back stack). **Interim:** a single shared
  `rememberNavBackStack` drives every tab; true per-tab back stacks (so each tab keeps its own nested nav,
  and the back-stacks move into the VM too) + the back-from-a-non-home-tab-root→home convention are a
  **planned follow-up**. It does **not** yet implement full-screen bar hiding (`hideBarFor`) or per-tab stacks.
- `FrnkNestedNavViewModel.kt` — the `MviViewModel` backing the scaffold; owns the bar's `items` +
  `selectedIndex` and translates taps into `FrnkNestedNavEffect.Navigate`. Registered by `frnkNestedNavModule`.
- `FrnkNestedNavMviContract.kt` — the MVI contract: `FrnkNestedNavArguments(items)`,
  `FrnkNestedNavModelState(items, selectedIndex)`, `FrnkNestedNavScreenState(items, selectedIndex)`,
  `FrnkNestedNavIntent.Tap(index)`, `FrnkNestedNavEffect.Navigate(route: NavKey)`, and the bar item model
  `FrnkNavBarItemModel(key, icon: FrnkIconSource, iosSystemIcon, label, route: NavKey)`.
- `FrnkNestedNavModule.kt` — `frnkNestedNavModule`, the Koin module registering `FrnkNestedNavViewModel`
  (included in `FrnkAppModule` / the core UI modules).

- `FrnkBottomFloatingBar.kt` (common) — the `expect fun FrnkBottomFloatingBar(items, selectedIndex,
  onItemSelected, modifier)` + `FrnkNavBarItem` (`key`, `icon: FrnkIconSource`, `iosSystemIcon`,
  `label`) + the shared `FrnkNavBarDefaults` (`reservedHeight`, read by `FrnkNestedNavScaffold` to inset
  content behind the overlaid bar). The bar renders items only — no FAB. The two actuals:
  - `FrnkBottomFloatingBar.android.kt` — a Material3 Expressive `HorizontalFloatingToolbar` (floating pill),
    centered at the bottom; each item is an `IconButton` over its `ImageVector` (selected = `colorPrimary`,
    idle = `colorOnSurfaceVariant`). Colors come from
    `FloatingToolbarDefaults.standardFloatingToolbarColors(...)` themed with `FrnkTheme` tokens, not
    `MaterialTheme`. Never touches `DrawableResource`. **Shadow parity:** the plain (no-FAB) overload defaults
    to `ContainerExpandedElevation` (`Level0` = 0.dp, no shadow); it pins `expandedShadowElevation` to
    `ContainerExpandedElevationWithFab` (`Level1`) so the pill still casts the standard floating-toolbar shadow.
  - `FrnkBottomFloatingBar.ios.kt` — the **vendored** `AdaptiveNavigationBar` (`ui.bottomnav.vendor`), themed
    from `FrnkTheme` tokens via `AdaptiveNavigationBarDefaults.colors(...)` (selected = `colorPrimary`,
    unselected = `colorOnSurfaceVariant`, indicator = `colorPrimaryContainer`, surface = `colorSurface`).
    Items render from `iosSystemIcon` + the bundled placeholder drawable. The vendored bar splits its
    `UIKitView` factory/update so selection + theme changes re-apply in place (no recreate/snap) — that
    in-place control is exactly why it was vendored. The bar is full-width and static.
  Most hosts use `FrnkNestedNavScaffold`; call the bar directly only when wiring your own selected-tab state /
  navigation. This is the toolkit's sole bottom-nav bar.

> **Legacy remnants pending removal.** `FrnkBottomNavState.kt`, `FrnkFeatureItem.kt`, `FrnkBottomNavTab.kt`,
> and `FrnkTabbedNavConfig.kt` (+ `FrnkTabbedNavViewState.kt`) physical files still exist but are **orphaned** —
> their only consumer (the removed `FrnkTabbedNavScaffold`) is gone. Don't treat them as live API.

## Override model

Configure the tabs by passing a `tabs: List<FrnkNavTab>` to `FrnkNestedNavScaffold` (any count, any roots).
The host owns the saved-state config (`onSavedStateConfiguration`, e.g. `{ frnkNestedNavConfig() }`) and the
nested-nav Koin module (`onNestedNavigationModule`, handed the created back stack — register destinations
there); frnk owns the `FrnkNavDisplay` + persistent bar + selection state (in `FrnkNestedNavViewModel`) +
bar-inset. The reference integration is `:demo-shared`, which mounts
`FrnkNestedNavScaffold(tabs = demoNestedTabs, onSavedStateConfiguration = { frnkNestedNavConfig() }, onNestedNavigationModule = { nestedBackStack -> nestedNavigationModule(nestedBackStack) { … } })`
inside the `FrnkRootRoute.Tab` destination, under `:ui-app`'s `FrnkApp` root.

## Dependencies

- `api(projects.uiScaffolds)` — tokens, theme, `EffectCollector`, `LocalFrnkBottomBarInset`, `FrnkNavTab`,
  `FrnkNavDisplay`.
- `api(compose.runtime / foundation / ui)`.
- `commonMain`: `implementation(compose.components.resources)` — runtime for the generated `Res` accessor
  (the iOS placeholder drawable). Not Material3.
- `androidMain`: `implementation(libs.compose.material3.expressive)` (material3 `1.10.0-alpha05`) — the
  `HorizontalFloatingToolbar`. **The sole Material3 dependency in the toolkit.** Don't add Material3 to any
  other shared module.
- `iosMain`: `implementation(libs.compose.material3.expressive)` — Material3 for the **vendored** bar's
  older-iOS `ComposeNavigationBar` fallback (previously transitive from the dropped `adaptive-nav-bar`
  artifact). The bar engine itself is vendored under `ui.bottomnav.vendor` — no external bar dependency.

## Rules

- Material3 lives **here only**. Other modules (especially `:ui-theme`/`:ui-components`/`:ui-scaffolds`) stay `compose-unstyled`.
- New adaptive-nav surface goes in `ui/bottomnav/` with the same conventions as atoms/scaffolds (`@Immutable`
  state where applicable, callbacks before `modifier`, tokens-only styling).
