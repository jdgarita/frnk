# SCAFFOLD_PLAN — frnk Scaffold System

Audit + implementation plan for the host-enablement scaffold system: `FrnkAppScaffold` (root
wrapper), `HomeScaffold`, Settings custom-section injection, and primary-action (FAB) routing to
the active screen's ViewModel. Goal: a host app spins up a fully functioning app in ~15 lines.

## 1. Audit — what already exists (reused, not rebuilt)

| Spec item | Status | Where |
|---|---|---|
| `BaseMviViewModel<State, Intent, Effect>` | **Exists** | `MviViewModel<S, I, E>` in `shared-ui-api` (`ui/mvi/MviViewModel.kt`): StateFlow state, SharedFlow intents, Channel-backed effects, `setState`/`send`/`emit`/`onIntent`. Name kept. |
| `ScreenScaffold` | **Exists** | `FrnkScreenScaffold` + `FrnkMviScreen` (`shared-ui-atoms`): pinned `FrnkTopAppBar`, status-bar insets, `LocalFrnkBottomBarInset`, merged content padding. |
| Nav3 type-safe routing | **Exists** | `ToolkitRoute` (`@Serializable sealed interface : NavKey` — `data object` for argless screens, `data class` for arguments), `frnkNavConfiguration(hostRoutes)`, `FrnkNavDisplay`, `rememberFrnkTabbedBackStacks`, `FrnkTabbedBackHandler`. |
| `SettingsScaffold` + default rows (Restore Purchases, etc.) | **Exists** | `SettingsScreen` + `rememberDefaultSettingsState` (`shared-ui-atoms`): appearance, preferences, subscription (Upgrade / Restore Purchases / Manage), support, legal, developer sections. Custom rows via sealed `SettingsRowState` + `SettingsAction.Custom(id)` + `SettingsEffect.ActionInvoked`. |
| Bottom nav: Home / middle / Settings + platform FAB | **Exists** (PR #45) | `FrnkTabbedNavScaffold` + `rememberFrnkAdaptiveNavTabs` (Home + Settings bookends, host middle tabs). `FrnkNavPrimaryAction` on the `AdaptiveNavBar` engine: Material3 FAB on Android, integrated inline button on iOS. |
| **`AppScaffold` root wrapper** | **Missing** | Hosts hand-wire ~80–200 lines: Koin init, `DatabaseContext.application`, `FrnkTheme`, nav config, tabs, back stacks, settings catalogue + handler, paywall entry. |
| **`HomeScaffold`** | **Missing** | No home template; every host rebuilds top-bar + scrollable column. |
| Settings extra-section injection convenience | **Gap** | Only possible by hand-building the entire `SettingsScreenState`. |
| FAB click → active screen's ViewModel | **Gap** | Host conditionally passes `onPrimaryAction` keyed on `tabbed.currentTabKey` (demo hack). |

## 2. Locked decisions

1. **Navigation: keep AndroidX Navigation3** (`NavKey`/`NavBackStack`/`NavDisplay`, migrated in
   #42–#45). `org.jetbrains.androidx.navigation:navigation-compose` is the Nav2 port and is NOT
   introduced. Type-safe `kotlinx.serialization` sealed routes are already the convention.
2. **Bottom bar + FAB: reuse the `:shared-ui-nav` adaptive engine.** Material3 stays confined to
   that one sanctioned module (Calf default engine; `AdaptiveNavBar` engine for the FAB). No
   Material anywhere else; atoms remain compose-unstyled only.

## 3. Design

### 3.1 Root wrapper — split into shell + batteries

Constraint that forces the split: `:shared-demo` deliberately does **not** depend on `:shared`
(keeps DemoKit's common surface free of RevenueCat/SQLite cinterops), so a `:shared`-only scaffold
would be undemoable cross-platform. Meanwhile `:shared-ui-nav` cannot see `frnkModules` /
monetization-ui (`:shared` api-depends on it — would be circular).

- **`FrnkAppShell`** (new, `:shared-ui-nav`, `dev.jdgarita.frnk.ui.app`): the composable shell.
  `FrnkTheme(themeConfig)` → `frnkNavConfiguration(hostRoutes)` → `rememberFrnkAdaptiveNavTabs`
  → `rememberFrnkTabbedBackStacks` → `FrnkTabbedNavScaffold`, with built-in Home / Settings /
  Onboarding entries, deep-link collection (`FrnkPendingRouteRequest`), and the primary-action
  registry. Assumes Koin is already started. Host extension points: `effects` slot (single
  `EffectCollector` home), `entries` slot (host destinations via instance-keyed `entry(key)`),
  trailing `homeContent: @Composable ColumnScope.() -> Unit`.
- **`FrnkAppScope`** (`@Stable`): `tabbed: FrnkTabbedBackStacks` + `primaryActions` registry +
  `navigateTo(route)` / `back()` — handed to `effects` / `entries` / effect handlers.
- **`FrnkAppScaffold`** (new, `:shared`, `dev.jdgarita.frnk.shared`): thin composition over the
  shell adding what only `:shared` sees — Koin-started assertion (clear error pointing at
  `initializeFrnk`), live `EntitlementManager.isPro` default Settings state (graceful degrade
  under `MonetizationChoice.None`; Settings VM re-keys on entitlement flips),
  `rememberFrnkSettingsHandler`-backed default settings effects, auto-registered
  `ToolkitRoute.Paywall` → `FrnkPaywallDestination`, `paywallFeatures`, `onMessage`.
- **Koin timing:** no self-start in composition (Android needs `Application.onCreate` anyway; a
  composition-scoped container is not the global context `koinViewModel` resolves from). New
  androidMain overload absorbs the SqlDelight context requirement:
  `initializeFrnk(context, ...)` sets `DatabaseContext.application` + `androidContext(...)`
  internally (adds `koin-android` to the catalog + `:shared` androidMain). iOS keeps the existing
  `initializeFrnk(...)`.
- `frnkModules()` now installs the SDK-free scaffold VM modules unconditionally:
  `homeScaffoldModule`, `settingsScaffoldModule`, `onboardingScaffoldModule`,
  `bottomNavScaffoldModule`.

Minimal host integration:

```kotlin
// Application.onCreate (Android)
initializeFrnk(context = this)

// Activity
setContent { FrnkAppScaffold(appVersion = "1.0") { /* home items */ } }
```

### 3.2 Primary-action (FAB) routing

- **`FrnkPrimaryActionRegistry`** (new, `shared-ui-api` `ui/nav/`, Compose-free — mirrors
  `FrnkPendingRouteRequest`): handler stack; `active: StateFlow<(() -> Unit)?>`; last-registered
  wins, unregister restores the previous (covers nav-transition overlap);
  `register(handler): FrnkPrimaryActionRegistration`.
- **`FrnkPrimaryActionHandler`** (new, `shared-ui-atoms` `ui/nav/`):
  `LocalFrnkPrimaryActionRegistry` (`staticCompositionLocalOf`, declared in atoms / provided by
  the nav scaffold — the `LocalFrnkBottomBarInset` pattern) + a `DisposableEffect`-based
  registration composable. Canonical MVI usage:
  `FrnkPrimaryActionHandler { onIntent(HomeIntent.PrimaryActionClicked) }`.
- **`FrnkTabbedNavScaffold`** gains `primaryActionRegistry: FrnkPrimaryActionRegistry? = null`
  (additive). Effective action = screen-registered handler, else host-level `onPrimaryAction`
  fallback; button hidden when both are null. Inert under the Calf engine (no button there).

### 3.3 HomeScaffold (`shared-ui-atoms` `ui/scaffolds/`, SettingsScreen pattern)

- `HomeScreenState(topBar, primaryActionEnabled = false)` — **non-sealed, no Skeleton**
  (recorded decision: screen-template chrome like Settings/Onboarding states; loading visuals
  belong to the host's slot content via the atoms' own Skeleton states).
- `HomeIntent { TopBarActionClicked, NavigationClicked, PrimaryActionClicked }` /
  `HomeEffect { ActionInvoked(key), NavigationInvoked, PrimaryActionInvoked }`;
  `HomeViewModel : MviViewModel` pass-through machine; `homeScaffoldModule`.
- `HomeScreen(initialState, vmKey, onEffect, content: ColumnScope.() -> Unit)` (VM-backed, wires
  `FrnkPrimaryActionHandler` when enabled) + stateless `HomeScreenContent`.
- **Slot decision (recorded): the scaffold owns the vertical scroll** (Column + `verticalScroll`
  + merged padding); the slot supplies items. Guarantees the scroll-behind-bars + bottom-inset
  contract. Escape hatch for `LazyColumn`/custom scroll: drop to `FrnkScreenScaffold` /
  `FrnkMviScreen`. A `lazyContent` variant is deferred.

### 3.4 Settings enhancement (additive)

`rememberDefaultSettingsState` gains `extraSections: List<SettingsSectionState> = emptyList()` +
`extraSectionsPlacement: SettingsExtraSectionsPlacement = BeforeLegal`
(`AfterAppearance | BeforeSubscription | BeforeLegal | End`). Actions keep flowing through
`SettingsAction.Custom(id)` + `SettingsEffect.ActionInvoked`.

### 3.5 Demo adoption (all three layers)

- **`:shared-demo`**: `DemoScreen` rewritten over **`FrnkAppShell`**. `DemoRoute` shrinks to
  `Components` + `ComponentDetail`; Home/Settings/Onboarding move to the `ToolkitRoute` defaults
  + `onboardingPages`. Home tab body → `homeContent`; FAB demo → `homePrimaryActionEnabled = true`
  + effect→toast (the `currentTabKey` conditional is deleted); Components remains the middle-tab
  custom-screen reference; demo keeps custom `settingsState` (god mode via `extraSections`) +
  settings handler + Paywall entry in `entries`; theme via `themeConfig = demoPurpleThemeConfig()`.
- **`androidDemoApp`**: `MainActivity` passes its hoisted `AppearanceController` through. Plus a
  debug-only **`AppScaffoldSmokeActivity`** exercising `initializeFrnk(context)` +
  `FrnkAppScaffold` end-to-end (androidDemoApp → `:androidApp` → `:shared`).
- **`iosDemoApp`**: rebuild `DemoKit.xcframework`, run in simulator (local-only; CI never builds
  iOS).
- Expected boilerplate: DemoScreen integration plumbing ~150 → ~50 lines; a fresh host
  ~180–200 → ~15 lines.

## 4. File inventory

**New**

| File | What |
|---|---|
| `shared/shared-ui-api/src/commonMain/.../ui/nav/FrnkPrimaryActionRegistry.kt` | Compose-free FAB-handler registry |
| `shared/shared-ui-api/src/commonTest/.../ui/nav/FrnkPrimaryActionRegistryTest.kt` | registry semantics |
| `shared/shared-ui-atoms/src/commonMain/.../ui/nav/FrnkPrimaryActionHandler.kt` | composition local + handler composable |
| `shared/shared-ui-atoms/src/commonMain/.../ui/scaffolds/{HomeScreenState,HomeViewModel,HomeScreen,HomeScaffoldModule}.kt` | HomeScaffold |
| `shared/shared-ui-atoms/src/commonDebug/.../previews/HomeScreenPreviews.kt` | previews |
| `shared/shared-ui-atoms/src/androidHostTest/.../scaffolds/{HomeViewModelTest,SettingsDefaultsTest}.kt` | tests |
| `shared/shared-ui-nav/src/commonMain/.../ui/app/{FrnkAppScope,FrnkAppShell}.kt` | composable shell |
| `shared/src/commonMain/.../shared/FrnkAppScaffold.kt` | batteries-included wrapper |
| `shared/src/androidMain/.../shared/FrnkInitializer.android.kt` | `initializeFrnk(context, ...)` |
| `androidDemoApp/src/main/.../AppScaffoldSmokeActivity.kt` | debug smoke for FrnkAppScaffold |

**Modified:** `gradle/libs.versions.toml` (koin-android); `shared/build.gradle.kts`;
`shared/.../FrnkModules.kt` (+ scaffold modules, + test); `shared-ui-nav/.../FrnkTabbedNavScaffold.kt`
(registry param); `shared-ui-atoms/.../SettingsDefaults.kt` (extraSections);
`shared-demo/.../{DemoScreen,DemoRoutes,DemoNavigation,DemoViewModel}.kt` + iosMain
`MainViewController.kt` + commonTest; `androidDemoApp/.../MainActivity.kt` + manifest; docs
(root `CLAUDE.md`, `docs/ARCHITECTURE.md`, `HOST_ALIGNMENT.md`, module CLAUDE.mds, `CHANGELOG.md`).

## 5. Phases

1. **Plan doc** — this file.
2. **Core foundation** — registry + handler + `FrnkTabbedNavScaffold` param; `frnkModules`
   scaffold modules; `koin-android` + `initializeFrnk(context)`. Gate: `compileAndroidMain` +
   `testAndroidHostTest`.
3. **Screens** — HomeScaffold; Settings `extraSections`. Gate: same.
4. **Shell + Scaffold** — `FrnkAppScope`/`FrnkAppShell`; `FrnkAppScaffold`. Gate:
   `compileAndroidMain` + local `:iosApp:assembleFrnkKitReleaseXCFramework`.
5. **Demo adoption** — `:shared-demo` → `androidDemoApp` (+ smoke activity) → `iosDemoApp`.
6. **Docs sweep** + CHANGELOG.

## 6. Verification

1. `./gradlew compileAndroidMain :androidDemoApp:compileDebugKotlin --parallel --build-cache`
2. `./gradlew testAndroidHostTest :androidDemoApp:testDebugUnitTest --parallel --build-cache`
3. `./gradlew :iosApp:assembleFrnkKitReleaseXCFramework` + `:shared-demo:assembleDemoKitDebugXCFramework` (local)
4. `androidDemoApp` on emulator: 3 tabs; Home scrolls behind bars; `AdaptiveNavBar` engine shows
   the FAB only on Home (registry path) and toasts; tab/back conventions intact; Settings extra
   section + god mode; Upgrade → Paywall; Onboarding full-screen hides the bar; smoke activity
   boots `FrnkAppScaffold` over `initializeFrnk(context)`.
5. `iosDemoApp` in simulator against fresh DemoKit: same walkthrough; native `UITabBar` (Calf
   default); inline primary-action button on the `AdaptiveNavBar` engine.

## 7. Risks

- **`:shared-demo` ↛ `:shared`**: the cross-platform demo covers `FrnkAppShell` (~90% of the
  scaffold); `FrnkAppScaffold` itself is verified via compile, FrnkKit assembly, and the
  androidDemoApp smoke activity (Android-only end-to-end).
- **AdaptiveNavBar Android resource-packaging blocker** (see `shared/shared-ui-nav/CLAUDE.md`):
  default engine stays **Calf**; the FAB is functional only when a host opts into
  `AdaptiveNavBar` + the documented asset-copy workaround.
- **nav3 duplicate-entry `require`-throw**: hosts must not re-register `homeRoot` /
  `settingsRoot` / `ToolkitRoute.{Onboarding,Paywall}` inside `entries`.
- **Param-count creep** on the shell: fall back to grouped `@Immutable`
  `FrnkHomeTabConfig`/`FrnkSettingsTabConfig` if review balks.
- **Instance-keyed `entry(key)` + process death**: `data object` roots are safe; avoid
  `data class` roots for tab roots registered this way.
