# shared-ui-nav

The toolkit's **platform-adaptive bottom navigation** module: a genuine native UIKit `UITabBar` on iOS and
a Material3 `NavigationBar` on Android, plus the default scaffold + tab builder that wire it up. Depends on
`shared-ui-atoms` (tokens, theme, `BottomNavScaffoldState`/`BottomNavViewModel`, `FrnkBottomNavItem`).

## POC: two bar engines (A/B), selectable at runtime

`FrnkTabbedNavScaffold` carries an `engine: FrnkAdaptiveNavEngine` so a host can A/B two bar
implementations live (compare UX/performance before committing to one):

- **`FrnkAdaptiveNavEngine.Calf`** (**default**) — the original `FrnkAdaptiveBottomNavBar` over
  [Calf](https://github.com/MohamedRejeb/Calf): a genuine native UIKit `UITabBar` on iOS, Material3
  `NavigationBar` on Android. No built-in "add" button.
- **`FrnkAdaptiveNavEngine.AdaptiveNavBar`** — `FrnkAdaptiveNavBarBottomBar` over
  [narendraanjana09/adaptive-nav-bar](https://github.com/narendraanjana09/adaptive-navigation-bar)
  (`io.github.narendraanjana09:adaptive-nav-bar`): Material3 `NavigationBar` on Android, a native glassy
  `UITabBar` (iOS 26+) / Material3 Compose bar (older iOS) on iOS, **with a built-in primary-action
  button** — a Material3 FAB on Android (docked above the bar by the scaffold) and an inline button beside
  the items on iOS (the library's `IosFabItem`). The primary-action button is frnk-owned
  (`FrnkNavPrimaryAction` + `stringPrimaryAction` token, same bookend treatment as Home/Settings) and
  surfaced to hosts via the scaffold's `onPrimaryAction` callback — the host decides what tapping it does,
  **per screen** (re-skin per surface by passing a custom `primaryAction`). It shows only on this engine
  and only when an action is wired. **Screen routing:** pass `primaryActionRegistry`
  (`FrnkPrimaryActionRegistry`, `shared-ui-api`) and the currently active screen claims the button via
  `FrnkPrimaryActionHandler { onIntent(...) }` (`shared-ui-atoms`) — the scaffold provides the registry
  through `LocalFrnkPrimaryActionRegistry`, a screen claim wins over `onPrimaryAction` (the host-level
  fallback), and the button hides when neither is wired. `FrnkAppShell` wires the registry automatically.

Both engines keep Material3 confined to this module (no rule change). `adaptive-nav-bar`'s icons are
**resource-based** (`DrawableResource` on Android + SF-Symbol string on iOS), not `ImageVector`, so this
module also pulls in `compose.components.resources` and bundles the toolkit's default nav icons
(`composeResources/drawable/frnk_nav_{home,settings,primary_action}.xml`). The new tab type `FrnkAdaptiveNavTab`
carries **both** icon forms (the `ImageVector` for Calf + the resource icons for adaptive-nav-bar) so one
tab list feeds either engine; `rememberFrnkAdaptiveNavTabs(...)` builds the Home + middle + Settings
bookends with the bundled icons. Neither library adds native cinterop (adaptive-nav-bar resolves SF
Symbols via built-in `platform.UIKit` interop), so the XCFrameworks still link under `dynamic_lookup`.

**iOS note:** Calf gives a genuine native `UITabBar`; adaptive-nav-bar gives a native glassy `UITabBar`
only on iOS 26+, falling back to a Material3 Compose bar on older iOS — the headline thing the A/B exists
to evaluate.

### ⚠️ Android packaging blocker (key POC finding)

The adaptive-nav-bar engine's `DrawableResource` icons **do not package into the Android APK** from a
`shared-*` KMP **library** module under the current toolchain (AGP 9.2.1 `com.android.kotlin.multiplatform.library`
+ Compose Multiplatform 1.11.1). The Compose-resources Gradle plugin can't wire its
`copyAndroidMainComposeResourcesToAndroidAssets` task into the new KMP-Android-library variant —
`prepareComposeResourcesTaskForAndroidMain` is `NO-SOURCE` and the copy task fails with
`outputDirectory doesn't have a configured value`. So the bundled drawables assemble for **iOS** but are
**absent from Android assets**, and switching to this engine throws
`MissingResourceException: composeResources/dev.jdgarita.frnk.ui.bottomnav.generated.resources/drawable/frnk_nav_home.xml`
at runtime. Since the library hard-requires `DrawableResource` (no `ImageVector`/composable-slot path),
this is a genuine adoption blocker for shipping the engine from a toolkit library — not a wiring mistake.

**POC workaround (demo only):** the consuming **application** module
(`demo/android-app/src/main/assets/composeResources/<resourcePackage>/drawable/…`) ships the raw XML at the
exact path the generated `Res` reads, so the runtime `AssetManager` finds it. See
`demo/android-app/src/main/assets/composeResources/README.md`. This is a hack — a real host would have to
copy these assets too, which is why it counts against adopting this library in the toolkit as-is. The Calf
engine (and `:shared-ui-atoms`' `ImageVector` pill) have no such issue. Proper resolution would need an
AGP/Compose-resources fix for the KMP-Android-library variant, or generating the icons a non-Compose-resources
way.

## Why this is its own module

It is the **one place in the toolkit that intentionally takes a Material3 dependency.** The adaptive bar is
built on [Calf](https://github.com/MohamedRejeb/Calf)'s `AdaptiveNavigationBar`, which renders the native
`UITabBar` on iOS and a Material3 `NavigationBar` elsewhere — and Calf hard-depends on `compose.material3`.
Isolating it here keeps that dependency to a single, named module rather than smeared across `shared-ui-atoms`
(which stays Material-free, `compose-unstyled` only). `:ui-app` `api`-depends on this module, so **Material3 + Calf reach every consumer of the nav layer** — a deliberate, host-approved trade
(the alternative, a hand-rolled UIKit `UITabBar` interop with zero Material3, was considered and declined in
favour of the maintained component; see `docs/spikes/adaptive-bottom-nav.md`).

Calf is pure Kotlin/Compose (no extra native cinterop / SPM package), so the XCFramework still links under the
consumer's existing `-undefined dynamic_lookup`.

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
  `FrnkAppScaffold` layers the monetization batteries over this; `:shared-demo` uses the shell
  directly (it can't see `:ui-app`) and is the reference integration.
- `FrnkAppScope.kt` — `@Stable` handle (`tabbed: FrnkTabbedBackStacks` + `primaryActions` registry +
  `navigateTo`/`back`/`clearAndNavigateTo`) handed to every shell extension point.

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
