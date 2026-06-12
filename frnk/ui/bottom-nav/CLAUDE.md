# ui-bottom-nav

The toolkit's **platform-adaptive bottom navigation** module: a Material3 `NavigationBar` on Android and a
native glassy `UITabBar` (iOS 26+) / Material3 bar (older) on iOS, plus the tabbed scaffold + tab builder
that wire it up. Depends on `:ui-scaffolds` (tokens, theme, `EffectCollector`).

## The adaptive bar (`adaptive-nav-bar`)

The bar is `FrnkAdaptiveNavBarBottomBar` over
[narendraanjana09/adaptive-nav-bar](https://github.com/narendraanjana09/adaptive-navigation-bar)
(`io.github.narendraanjana09:adaptive-nav-bar`): a Material3 `NavigationBar` on Android, a native glassy
`UITabBar` (iOS 26+) / Material3 Compose bar (older iOS) on iOS, **with a built-in primary-action button** —
a Material3 FAB on Android (docked above the bar by the scaffold) and an inline button beside the items on
iOS (the library's `IosFabItem`). The primary-action button is frnk-owned (`FrnkNavPrimaryAction` +
`stringPrimaryAction` token, same bookend treatment as Home/Settings) and surfaced to hosts via the
scaffold's `onPrimaryAction` callback — the host decides what tapping it does, **per screen** (re-skin per
surface by passing a custom `primaryAction`). It shows only when an action is wired. **Screen routing:** pass
`primaryActionRegistry` (`FrnkPrimaryActionRegistry`, `:core-nav`) and the currently active screen claims the
button via `FrnkPrimaryActionHandler { onIntent(...) }` (`:ui-scaffolds`) — the scaffold provides the
registry through `LocalFrnkPrimaryActionRegistry`, a screen claim wins over `onPrimaryAction` (the host-level
fallback), and the button hides when neither is wired. `FrnkAppShell` wires the registry automatically.

> **History.** This was originally one of *two* engines: [Calf](https://github.com/MohamedRejeb/Calf) (the
> default — genuine native `UITabBar` on iOS, `ImageVector` icons) and adaptive-nav-bar, selectable at
> runtime via a `FrnkAdaptiveNavEngine` A/B. Calf was **removed entirely** when adaptive-nav-bar became the
> default; the enum, the Calf bar (`FrnkAdaptiveBottomNavBar`), and the Calf-only index scaffold
> (`FrnkAdaptiveBottomNavScaffold` + `rememberFrnkBottomNavState`) went with it. The four-way spike
> evaluation + the engine A/B are recorded in the MobiAI brain (`mobiai brain search "adaptive bottom nav"`).

adaptive-nav-bar's icons are **resource-based** (`DrawableResource` on Android + SF-Symbol string on iOS),
not `ImageVector`, so this module also pulls in `compose.components.resources` and bundles the toolkit's
default nav icons (`composeResources/drawable/frnk_nav_{home,settings,primary_action}.xml`). `FrnkAdaptiveNavTab`
carries the resource icons + SF-Symbol names; `rememberFrnkAdaptiveNavTabs(...)` builds the Home + middle +
Settings bookends with the bundled icons. The library adds no native cinterop (it resolves SF Symbols via
built-in `platform.UIKit` interop), so the XCFrameworks still link under `dynamic_lookup`.

### ⚠️ Android icon-packaging requirement (host-side)

adaptive-nav-bar takes `DrawableResource` icons, and those **do not package into the Android APK** from this
KMP **library** module under the current toolchain (AGP 9.2.1 `com.android.kotlin.multiplatform.library` +
Compose Multiplatform 1.11.1). The Compose-resources Gradle plugin can't wire its
`copyAndroidMainComposeResourcesToAndroidAssets` task into the new KMP-Android-library variant —
`prepareComposeResourcesTaskForAndroidMain` is `NO-SOURCE` and the copy task fails with
`outputDirectory doesn't have a configured value`. So the bundled drawables assemble for **iOS** but are
**absent from Android assets**, and a host crashes at runtime with
`MissingResourceException: composeResources/dev.jdgarita.frnk.ui.bottomnav.generated.resources/drawable/frnk_nav_home.xml`.

**Required host step (Android):** the consuming **application** module ships the raw XML at the exact path
the generated `Res` reads, so the runtime `AssetManager` finds them —
`<app>/src/main/assets/composeResources/dev.jdgarita.frnk.ui.bottomnav.generated.resources/drawable/frnk_nav_{home,settings,primary_action}.xml`.
The demo does exactly this (`demo/android-app/src/main/assets/composeResources/` +
`.../README.md`); `docs/HOST_INTEGRATION.md` documents it as a host integration step. This is the accepted
trade for adopting adaptive-nav-bar as the default (the Material-free `FrnkBottomNavBar` pill in
`:ui-components`, which uses `ImageVector`, has no such requirement). A proper fix would need an
AGP/Compose-resources fix for the KMP-Android-library variant, or generating the icons a non-Compose-resources
way.

## Why this is its own module

It is the **one place in the toolkit that intentionally takes a Material3 dependency.** The adaptive bar is
built on adaptive-nav-bar's `AdaptiveNavigationBar`, which renders a native glassy `UITabBar` on iOS 26+ (a
Material3 Compose bar on older iOS) and a Material3 `NavigationBar` on Android — and the library hard-depends
on `compose.material3`. Isolating it here keeps that dependency to a single, named module rather than smeared
across the design-system modules `:ui-theme`/`:ui-components`/`:ui-scaffolds` (which stay Material-free,
`compose-unstyled` only). `:ui-app` `api`-depends on this module, so **Material3 reaches every consumer of the
nav layer** — a deliberate, host-approved trade.

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

- `FrnkAdaptiveNavBarBottomBar.kt` — `FrnkAdaptiveNavBarBottomBar(items, selectedIndex, onItemSelected,
  modifier, primaryAction, onPrimaryAction)` + the Android `FrnkAdaptiveNavBarPrimaryActionFab` + the shared
  `FrnkAdaptiveBottomNavBarDefaults` (`reservedHeight`, read by `FrnkTabbedNavScaffold` to inset content
  behind the overlaid bar). The bar over adaptive-nav-bar's `AdaptiveNavigationBar`: generic over
  `FrnkAdaptiveNavItem` (resource icons — `DrawableResource` + SF-Symbol string). **Themed from `FrnkTheme`
  tokens, not the Material/platform defaults** — selected = `colorPrimary`, unselected =
  `colorOnSurfaceVariant`, indicator = `colorPrimaryContainer`, surface = `colorSurface`, passed through
  `AdaptiveNavigationBarDefaults.colors(...)`. The primary-action button: iOS gets an `IosFabItem` rendered
  inline by the library; Android the library renders no FAB, so the scaffold docks `FrnkAdaptiveNavBarPrimaryActionFab`
  above the bar (guarded to Android via the library's `getPlatform()`). The bar is `key(...)`-ed on the FAB's
  presence + the color tokens because on iOS 26+ the library bakes its FAB handler and brand palette into the
  native `UITabBarAppearance` once in its `UIKitView` factory and never re-applies them (see the file's KDoc
  for the snap-on-recreate caveat). Use directly only when wiring your own selected-tab state / navigation;
  most hosts use the scaffold. For the Material-free floating pill, use `FrnkBottomNavBar` in `:ui-components`.
- `FrnkTabbedNavScaffold.kt` — `FrnkTabbedNavScaffold(tabbed, tabs, modifier, primaryAction, onPrimaryAction, primaryActionRegistry, hideBarFor, entryProvider)`.
  The **nav3 multiple-back-stack** tabbed scaffold: the single composable a host calls to get a standard
  tabbed app. It absorbs the `FrnkNavDisplay` (driven by `tabbed.current`), the persistent
  `FrnkAdaptiveNavBarBottomBar` overlay (tab switch / re-tap-to-root + the built-in primary-action button),
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
- `FrnkAdaptiveNavTab.kt` / `FrnkAdaptiveNavDefaults.kt` — the tab type (`key` + `root` + resource icons +
  SF-Symbol names) and `rememberFrnkAdaptiveNavTabs(homeRoot, settingsRoot, middleTabs, …)`, which enforces
  the product rule **every app has at least Home + Settings**: a fixed Home tab, the host's optional
  `middleTabs`, then a fixed Settings tab (bookend labels from `FrnkStrings`, icons from the bundled drawables).

## Override model

- **Override the tabs**: pass `middleTabs` to `rememberFrnkAdaptiveNavTabs` (Home/Settings bookends are always
  present), or build the `List<FrnkAdaptiveNavTab>` by hand for a fully custom shape.
- **Wire the navigation**: use `FrnkTabbedNavScaffold` with a host-owned `rememberFrnkTabbedBackStacks` + an
  `entryProvider` — frnk owns the display + bar + tab switching + back convention + bar-inset; the host owns
  the back stacks and registers destinations.

## Dependencies

- `api(projects.uiScaffolds)` — tokens, theme, `EffectCollector`, `LocalFrnkBottomBarInset`.
- `api(compose.runtime / foundation / ui)`.
- `implementation(libs.adaptive.nav.bar)` + `implementation(compose.material3)` + `implementation(compose.components.resources)`
  — the adaptive bar's engine + its resource-icon support. **The sole Material3 dependency in the toolkit.**
  Don't add Material3 to any other shared module.

## Rules

- Material3 lives **here only**. Other modules (especially `:ui-theme`/`:ui-components`/`:ui-scaffolds`) stay `compose-unstyled`.
- New adaptive-nav surface goes in `ui/bottomnav/` with the same conventions as atoms/scaffolds (`@Immutable`
  state where applicable, callbacks before `modifier`, tokens-only styling).
