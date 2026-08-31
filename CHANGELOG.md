# Changelog

All notable changes to **frnk** are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/), and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## Pre-1.0 policy

While the public API is in `0.x`:

- `MINOR` (`0.x.0`) may include breaking API changes. Read the release notes before bumping.
- `PATCH` (`0.x.y`) is additive and bug-fix only — safe to take without inspection.

Once a `1.0.0` ships, normal SemVer applies: breaking changes are `MAJOR`-only.

## [Unreleased]

## [0.4.2] - 2026-08-31

### Changed

- **Toolchain and library versions bumped.** AGP 9.3.1 → 9.3.2, Compose Multiplatform 1.11.1 → 1.12.0,
  `compose-unstyled` 2.9.0 → 2.9.2, RevenueCat 3.5.0 → 3.6.0, and `navigation3Runtime` 1.1.6 → 1.1.7
  (the AndroidX runtime only — the JetBrains CMP port `navigation3UI` stays at 1.1.1, its latest
  stable, because the two coordinate groups do not release in lockstep). The hand-pinned
  `androidx-compose-ui` stays at 1.12.0, re-resolved against `androidCompileClasspath` to confirm it
  still matches what CMP 1.12.0 pulls in.
- **Corrected three stale version-catalog comments** that had drifted from the values beside them: the
  navigation3 block still described the pre-split single-ref rule, the SDK block still claimed
  `compileSdk` was 36 (it is 37) "because AGP 9 caps compileSdk at 36" (true of AGP 9.2.1 only), and
  the compose-ui pin referenced CMP 1.11.1 plus a `:shared-ui-atoms` module that no longer exists.
  These comments are load-bearing — the navigation3 one is what documents the two-coordinate-group
  trap. Also recorded that `compose-unstyled` 2.9.2+ declares `minCompileSdk=37`, which is enforced by
  `checkAarMetadata` and therefore **not** caught by `compileAndroidMain`.

### Fixed

- **Demo Home dropped every `DemoHomeEffect` (internal demo harness).** Two ViewModels meet on the Home
  tab, each with its own single-consumer effect channel, and only one was collected: the toolkit's
  pass-through `HomeViewModel` reached `HomeScreen(onEffect)`, while `DemoHomeViewModel` had its
  `state` collected but never its `effects`. Every `Navigate`/`Toast` was buffered and dropped, so
  "Open Paywall" navigated nowhere and no transient message ever appeared (the top-bar crown still
  worked, which masked it). The screen now binds the ViewModel through the toolkit's `FrnkScreen`
  primitive, which consumes the effect channel. No toolkit change — demo only.

## [0.4.1] - 2026-08-31

### Fixed

- **`:monetization-impl` ktlint violations (#70).** Three files added or touched by #69 ended with a
  trailing newline, which `.editorconfig`'s `insert_final_newline = false` makes a
  `standard:final-newline` violation. `v0.4.0` shipped with
  `:monetization-impl:ktlintCommonMainSourceSetCheck` and `ktlintCommonTestSourceSetCheck` failing.

### Changed

- **`foojay-resolver-convention` 0.8.0 → 1.0.0 (#70).** Routine currency: 0.8.0 predates Gradle 9 and
  frnk builds on Gradle 9.5.1.

## [0.4.0] - 2026-08-27

### Changed

- **Paywall offering metadata is now locale-aware (`:monetization-impl`).** `RevenueCatEntitlementProvider`
  resolves the offering's paywall copy through the new pure `resolvePaywallMetadata(metadata, languageTag,
  fallback)`: an optional additive `localizations` object in the offering metadata carries per-language
  overrides (`"localizations": { "es": { "title": …, "subtitle": …, "benefits": [ … ] } }`), matched against
  `platformLanguageTag()` — exact tag first, then primary-language prefix (`es-MX` → `es`). Each field
  resolves locale override → flat key → host fallback; the flat keys stay the canonical copy, so dashboards
  that never add `localizations` (and older clients that never read it) behave exactly as before.
- **BREAKING — `RevenueCatConfig.paywallFallback` and `savingsBadgeTemplate` are now suspend providers**
  (`suspend () -> ProMetadata` / `suspend () -> String`, defaults unchanged in content). Both are resolved
  per call, so hosts can localize them from suspending resource APIs (e.g. compose-resource `getString`)
  against the current locale instead of baking one language in at DI time. Value-style overrides migrate by
  wrapping in a lambda: `paywallFallback = { myMetadata }`.

## [0.3.1] - 2026-08-18

### Added

- **`Modifier.placeholder()` is now public (`:ui-components`).** The placeholder/shimmer trio —
  `Modifier.placeholder(...)`, the `PlaceholderHighlight` interface and its `Shimmer` implementation —
  was `internal` and therefore unusable from a host, even though the components that consume it are
  public. All three are now part of the consumer surface, so hosts can put skeleton loading states on
  their own composables instead of reimplementing the effect.

### Changed

- **iOS non-fatals now carry Kotlin stack frames (`:analytics-impl`).** `CrashReporter.recordException`
  used to go straight to gitlive, whose iOS actual is `FIRCrashlytics.recordError(throwable.asNSError())`
  — the Kotlin throwable landed in `userInfo["KotlinException"]` where Crashlytics cannot read it, and
  the report got the Objective-C stack at the *call* site instead of the throw site, so every iOS
  non-fatal grouped by exception class with an unusable stack. A new
  `internal expect fun recordNativeHandledException(throwable): Boolean` sits beside
  `enableNativeCrashHandler()`: the iOS actual routes to CrashKiOS's
  `CrashlyticsKotlin.sendHandledException(...)`, which builds a real `FIRExceptionModel` from the
  Kotlin stack addresses for the consumer's dSYM to symbolicate. Android returns `false` and keeps
  gitlive, which already hands the Crashlytics Android SDK a full JVM stack. No API change for hosts —
  `recordException` behaves the same, it just reports usefully on iOS now. Consumers still need the
  dSYM-upload build phase from `docs/HOST_INTEGRATION.md` §"Crashlytics setup".

## [0.3.0] - 2026-08-11

### Added

- **`frnk.android.firebase` convention plugin (`build-logic`).** The first convention plugin aimed at
  a **host application** rather than a toolkit module. On a `com.android.application` project it
  applies `google-services` (so `FirebaseInitProvider` auto-initializes Firebase before
  `Application.onCreate`) and `firebase-crashlytics` (so R8-minified release stack traces
  deobfuscate) — both **only when the host's `google-services.json` is present**, so CI and fresh
  clones still configure and build, degrading to the existing logged no-op at runtime. It takes no
  configuration, and frnk's catalog owns the two plugin versions so hosts stop duplicating them.
  Opt in with `pluginManagement { includeBuild("frnk/build-logic") }` — see
  `docs/HOST_INTEGRATION.md` §7.

- **`IdentitySource` (`:identity-api`).** `suspend fun identify(id: String): AppResult<Unit, IdentityError>`
  — one contract for every consumer of a user identity, implemented by `AnalyticsTracker`,
  `CrashReporter`, `EntitlementProvider` and `EntitlementManager`. Crash reports and Analytics now
  carry the host's uid (the reserved Firebase User-ID field, not a high-cardinality user property).

- **`ToolkitEvent.IdentitySynced` / `IdentitySyncFailed`.** A complete identity funnel: exactly one of
  the two is emitted per `SyncAuthUseCase.identify()` call. The failure event carries `stage`
  (`sign_in` | `entitlement`) and `error_type`, both low-cardinality, so a Firebase-auth failure and a
  billing-backend failure stay distinguishable.

- **`FrnkApp(initialAppearance)` (`:ui-app`).** New optional parameter that seeds the app-wide
  `AppearanceController` once, at first composition. A single-appearance host (e.g. a light-only
  app) passes `Appearance.Light` and the theme palette, the Android system-bar icon contrast, and
  the iOS `overrideUserInterfaceStyle` all stop following the OS dark-mode setting — previously the
  controller always started at `Appearance.System`, so such an app showed white status-bar icons
  over light content whenever the device was in dark mode. Default `null` leaves the controller
  untouched (hosts restoring a persisted appearance keep doing so), and the controller stays
  mutable, so runtime toggles keep working.
- **`:core-platform` host-service contracts.** Added an SDK-free KMP module for camera capture,
  image selection/decoding, application settings actions, and maps integrations.
- **Silent receipt sync + already-owned recovery (`:monetization-api`/`-impl`/`-ui`).** New
  `EntitlementProvider.syncPurchases()` → `EntitlementManager.syncPurchases()` →
  `PaywallPurchaseUseCase.sync()`: a silent, no-store-UI receipt sync (RevenueCat
  `awaitSyncPurchasesResult()`), safe to run opportunistically because it only posts what the
  device's store account already owns. The paywall runs it best-effort on attach — a reinstalled
  Pro user is dismissed with Pro restored (`stringPaywallRestored`) instead of being sold to; sync
  failures stay silent. New `MonetizationError.AlreadyOwned` (Play's `ProductAlreadyPurchasedError`
  / `ReceiptAlreadyInUseError`) makes a purchase that the store rejects as "already owned" fall
  through to a restore automatically instead of dead-ending on an error dialog. RC error codes now
  map through the pure, unit-testable `monetizationErrorFor(code, userCancelled)` (cancellation →
  `UserCancelled`, already-owned codes → `AlreadyOwned`, `NetworkError` → `NetworkUnavailable`).
  The paywall's Restore button gains an in-flight state (`isRestoring` disables + relabels it via
  the new `stringPaywallRestoring` token).

### Changed

- **Breaking (`:analytics-api`):** `CrashReporter.setUserId(String?)` → `identify(String)` from
  `IdentitySource`; `AnalyticsTracker` gains the same method. Both interfaces now extend
  `IdentitySource`, so `:analytics-api` gains an `api(projects.identityApi)` dependency — the one
  cross-capability api→api edge in the graph.
- **Breaking (`:monetization-api`):** `EntitlementProvider.identify` / `EntitlementManager.identify`
  move to the inherited `IdentitySource` signature — the parameter is now `id` (was `userId`) and the
  error type is `IdentityError` (was `CommonError`). `DefaultSyncAuthUseCase` gains `CrashReporter`
  and `AnalyticsTracker` constructor parameters.
- **Breaking (`:analytics-api`):** `ToolkitEvent` keys are now lowercase `snake_case`
  (`App_Opened` → `app_opened`, …), and `SignInStarted`/`SignInCompleted` were removed — nothing
  emitted them. Firebase Analytics only accepts alphanumeric-plus-underscore names starting with a
  letter and **silently drops** anything else, so keys that drift are invisible failures.
- `DefaultSyncAuthUseCase` now fans a resolved uid out to all three sinks, but **only the entitlement
  sink gates the result** — the observability results are deliberately discarded (each logs its own
  failure), so an unconfigured Firebase degrades telemetry instead of blocking the host. Pinned by
  `DefaultSyncAuthUseCaseTest`.

- **Breaking: paywall messages are now `FrnkStringSource` (`:shared-monetization-ui`).**
  `PaywallEffect.Message.text` (and the `onMessage` callbacks of `FrnkPaywallDestination` /
  `frnkPaywallNavigation`) changed `String` → `FrnkStringSource`, so toolkit copy stays a theme
  token — host `stringOverrides` + locale re-resolve apply. Hosts hold the source in state and
  render via `resolve()`. New tokens: `stringPaywallRestoring`/`Restored`/`NothingToRestore`/
  `AlreadyOwnedRestoring`/`IdentityError`.
- **Breaking: restore/purchase are identity-gated (`:shared-monetization-ui`).** `PaywallViewModel`
  now requires `SyncAuthUseCase` (resolved by `paywallScaffoldModule` from `monetizationModule`) and
  sequences every store interaction behind `identify()`, so entitlements land on the host's stable
  uid, never RC's transient anonymous id. Custom `EntitlementProvider`/`EntitlementManager`
  implementations must add `syncPurchases()`.

### Fixed

- **`FrnkApp` now hands its `AppearanceController` to `FrnkTheme` (`:ui-app`).** `FrnkApp` provided
  the Koin-resolved controller via `LocalAppearanceController` but called `FrnkTheme` without
  passing it, so the theme's default parameter instantiated a *second* controller (stuck at
  `Appearance.System`) that shadowed the first for everything under the theme. Net effect: the
  palette and the iOS interface-style pin always followed the OS — `initialAppearance` seeding
  (and any host toggle mutating the Koin controller) moved only the system-bar icon contrast,
  while chrome like the Settings footer kept flipping with device dark mode. One controller now
  drives palette, system bars, and the native interface style together.

- **Pro state now hydrates on cold launch (`:monetization-api`).** `DefaultEntitlementManager`
  launches `provider.refresh()` in its injected scope at construction. Previously nothing triggered
  a customer-info fetch on startup — the RevenueCat provider's `isPro` starts `false` and its
  `PurchasesDelegate` installs lazily on the first monetization call — so an entitled user was
  reported Free on every launch until they happened to reopen the paywall. Hosts need no wiring:
  on the validated bootstrap path (`validate = true`, `Koin::validateFrnkBootstrap`) the manager is
  resolved — and therefore hydrated — at `initializeFrnk` time; unvalidated hosts hydrate on the
  first injection. The RevenueCat SDK answers the refresh from its local cache even offline.
  (The manager binding is deliberately *not* `createdAtStart`: eager instantiation would crash
  `startKoin` with a raw Koin error before `validateFrnkBootstrap` could name a missing module.)

### Fixed (RevenueCat provider)

- **`restore()` no longer reports success from stale Pro state (`:monetization-impl`).** Its result
  is computed from the restore's **own** `CustomerInfo` instead of the provider's current `isPro`,
  so a pre-existing Pro state (e.g. god mode) can't masquerade as a successful restore; restore/sync
  failures map through `monetizationErrorFor` instead of collapsing to `Unknown`. SDK wrappers now
  rethrow `CancellationException` (the old `runCatching` swallowed it, handing cancelled callers a
  bogus `StoreUnavailable`).

## [0.2.0-alpha1] - 2026-07-08

### Changed

- **Breaking (nav contract): route catalogues tightened (Tier 3.3, `:core-nav`).** The tab-level catalogue was renamed **`FrnkRoute` → `FrnkTabRoute`** so the level is explicit in the type name (`FrnkRootRoute` keeps its name), and its vestigial `Onboarding`/`Paywall` members were **removed** — full-screen flows live on `FrnkRootRoute`. The paywall helpers in `:shared-monetization-ui` (`frnkPaywallNavigation` + `rememberFrnkSettingsHandler`) now target **`FrnkRootRoute.Paywall`** (the paywall is a `FrnkFullScreenRoute`, so it belongs above the bottom bar; `rememberFrnkSettingsHandler`'s `backStack` must be the **root** stack). The config builders are now **symmetric**: `frnkRootNavConfig` became a function `frnkRootNavConfig(hostRoutes = …)` that registers `FrnkRootRoute.Custom` (the old `val` form silently dropped it) and merges host root routes — closing a real capability gap (the root stack was not host-extensible). **Migration:** `FrnkRoute` → `FrnkTabRoute`; `{ frnkRootNavConfig }` → `{ frnkRootNavConfig() }`. Nested/tab stacks are in-memory only, so the `serialName` change needs no persistence migration; the process-death-persisted root stack (`FrnkRootRoute`) is unchanged.
- **Unified demo entry point on `FrnkAppScaffold`.** Both demo platforms now render through one shared composable, **`FrnkDemoApp`** (`:demo-shared`, renamed from `DemoScreen`), which wraps `:ui-app`'s batteries-included `FrnkAppScaffold` — so `demo-android`'s `MainActivity` (now a thin platform host: `enableEdgeToEdge` + system-bar icon sync + toast) and `iosDemoApp`'s `MainViewController` call the exact same root, and iOS is upgraded from the bare-shell `FrnkTabbedNavScaffold` to the full batteries (auto-mounted paywall + first-launch onboarding + live `EntitlementManager`-driven Settings + Koin fail-fast). `:demo-shared` now depends on `:ui-app` — **cinterop-safe** because `:ui-app` carries no `*-impl`/native cinterop (resolves `EntitlementManager`/`AnalyticsTracker` from Koin at runtime), so `DemoKit.xcframework` stays free of RevenueCat/SQLite/Firebase symbols (verified by a clean `assembleDemoKitDebugXCFramework` link). This corrects the prior (false) "`:demo-shared` can't depend on `:ui-app`" assumption — the boundary was soft architectural-purity, not a hard technical guard. This models the structure the toolkit recommends for real host apps: a `shared` module owning one App composable that both platform hosts call. (Internal demo harness only — no public-API change.)
- **Breaking:** **`FrnkAppShell` was merged into `FrnkTabbedNavScaffold`** (`:ui-bottom-nav`). There is now a single public tabbed-app composable: `FrnkTabbedNavScaffold(appVersion, …, feature, …) { homeContent }` takes over `FrnkAppShell`'s full signature/behaviour (theme wrap + nav config + the fixed `Home · feature · Settings` tabs + per-tab back stacks + built-in Home/Settings/Onboarding destinations + deep-links + `FrnkAppScope` extension points). `FrnkAppShell` is deleted; **migration is a rename** — `FrnkAppShell(...)` → `FrnkTabbedNavScaffold(...)` (identical arguments) with the import moving from `dev.jdgarita.frnk.ui.app.FrnkAppShell` to `dev.jdgarita.frnk.ui.bottomnav.FrnkTabbedNavScaffold`. The previous *generic* host-owned-state `FrnkTabbedNavScaffold(tabbed, tabs, hideBarFor, entryProvider)` form is no longer public — its render core is now a private helper; hosts wanting a custom tab shape or a Material3-free bar wire the lower-level primitives directly (`rememberFrnkTabbedBackStacks` + `FrnkNavDisplay` + `FrnkTabbedBackHandler` + own bar). `FrnkAppScope` + `FrnkFirstLaunchOnboardingEffect` are unchanged in behaviour but **moved to package `…ui.bottomnav`** (was `…ui.app`) so `:ui-bottom-nav` is single-package — update those imports if you referenced them directly. `FrnkAppScaffold` (`:ui-app`) is unaffected for hosts — it still wraps the same behaviour.

### Removed

- **Breaking:** the Material-free **`FrnkBottomNavBar`** pill atom (`:ui-components`) and its index-based **`BottomNavScaffold`** family (`BottomNavScaffold`/`BottomNavScaffoldState`/`BottomNavViewModel`/`BottomNavScaffoldModule`/`BottomNavDefaults`, plus `bottomNavScaffoldModule`) are deleted — no host or demo ever used them. **`FrnkBottomFloatingBar`** (`:ui-bottom-nav`, reached via `FrnkTabbedNavScaffold` / `FrnkAppShell`) is now the toolkit's sole bottom-nav bar. `frnkUiModules()` no longer carries `bottomNavScaffoldModule`. The shared `frnkBottomSystemBarInset()` helper (`:ui-components`) stays — the adaptive bar's `reservedHeight` reads it.

### Added

- **Scaffold system / one-call app root**: **`FrnkAppShell`** (`:shared-ui-nav`, `ui/app/`) stands up a complete tabbed app in one composable — `FrnkTheme` wrap, nav3 saved-state config, the fixed `Home · feature · Settings` adaptive tabs with per-tab back stacks, `FrnkTabbedNavScaffold`, built-in Home/Settings/Onboarding destinations, and deep-links (`FrnkPendingRouteRequest`); host extension points (`homeContent`, `entries`, `effects`, effect handlers) all receive a **`FrnkAppScope`** (`navigateTo`/`back`/`clearAndNavigateTo`). **`FrnkAppScaffold`** (`:shared`) layers the batteries on top: a fail-fast Koin assertion, a Settings tab driven by the **live** `EntitlementManager.isPro` (VM re-keys on flips), the `rememberFrnkSettingsHandler` monetization wiring with appearance/onboarding/feedback fallbacks, and an auto-mounted `ToolkitRoute.Paywall` — a fresh host boots with `initializeFrnk(context = this)` + one `FrnkAppScaffold(...)` call (~15 lines vs ~180 hand-wired). Degrades gracefully under `MonetizationChoice.None`.
- **`HomeScaffold`** (`shared-ui-atoms` `ui/scaffolds/`): `HomeScreen` (VM-backed) + `HomeScreenContent` (stateless) — a pinned `FrnkTopAppBar` over a **scaffold-owned** scrolling column the host fills through a `ColumnScope` slot (the merged bar/inset padding is applied for it). Pass-through `HomeViewModel` re-emits every interaction as a `HomeEffect` (`ActionInvoked`/`NavigationInvoked`); `homeScaffoldModule` is registered by `frnkModules()`. Tested (`HomeViewModelTest`) + previews.
- **Fixed three-tab bottom bar with a host-configurable center "feature" tab** — the adaptive bar (`FrnkBottomFloatingBar`, via `FrnkTabbedNavScaffold` / `FrnkAppShell`) always shows exactly three tabs: `Home · feature · Settings`. The center **`FrnkFeatureItem`** (`route` + `label` + `icon: ImageVector` + `iosSystemIcon`) is the only host-configurable tab — a real navigable tab (own back stack, re-tap-to-root) the host points at its signature surface and registers in `entries`. `rememberFrnkBottomNavState(homeRoot, settingsRoot, feature)` returns the fixed `FrnkBottomNavState` (`internal` ctor; Home/Settings bookends built from theme tokens).
- **Settings extra-section injection** — `rememberDefaultSettingsState(extraSections, extraSectionsPlacement)` (`AfterAppearance` / `BeforeSubscription` / `BeforeLegal` (default) / `End`) injects host sections into the default catalogue without hand-building the whole state; actions keep flowing through `SettingsAction.Custom(id)`. Tested (`SettingsDefaultsTest`).
- **`initializeFrnk(context, ...)`** (`:shared` androidMain, new `koin-android` dep): one-call Android bootstrap that absorbs `DatabaseContext.application` + `androidContext(...)`. `frnkModules()` now installs the SDK-free scaffold VM modules (home/settings/onboarding/bottomNav) unconditionally.
- Demo adoption in all three layers: `:shared-demo`'s `DemoScreen` is rewritten over `FrnkAppShell` (`DemoRoute` shrinks to `Components`/`ComponentDetail`; Home/Settings/Onboarding move to the `ToolkitRoute` defaults; "Components" becomes the center `feature` tab), `androidDemoApp` gains the debug-only `AppScaffoldSmokeActivity` exercising `FrnkAppScaffold` end-to-end on device, and `iosDemoApp` consumes the rebuilt `DemoKit`.

- Firebase Analytics + Crashlytics implementations (`FirebaseAnalyticsTracker` / `FirebaseCrashReporter`) over the gitlive SDKs, with `runCatching` no-op safety when Firebase isn't configured (BACKLOG P1-5).
- `ObservabilityChoice { None, Firebase }` — analytics + crash reporting selectable **independently of `BackendChoice`**, via `frnkModules(backend, observability)` / `initializeFrnk(...)`. `firebaseObservabilityModule` binds the real impls; `noopObservabilityModule` is the `None` default. Lets a local-storage-only app (no backend) ship Firebase telemetry.
- Recording `FakeAnalyticsTracker` / `FakeCrashReporter` (+ `ObservabilityTest`) in `:shared:backend:api` `commonTest`; `DemoViewModelTest` in `:shared-demo`.
- `androidDemoApp` applies the `google-services` + `firebase-crashlytics` Gradle plugins and installs the real `firebaseObservabilityModule` to smoke-test Firebase on a device; the demo gains an "Analytics & Crash" section across all three layers.
- iOS unhandled-crash symbolication via CrashKiOS (`co.touchlab.crashkios:crashlytics`, `:shared:backend:firebase` `iosMain`): selecting `ObservabilityChoice.Firebase` installs a Kotlin/Native unhandled-exception hook (`enableNativeCrashHandler`, no-op on Android) so *uncaught* Kotlin crashes reach Crashlytics symbolicated — not just explicitly-caught `recordException`s (BACKLOG P1-5b). The demo gains a "Force crash (unhandled)" action; `FirebaseObservabilityModuleTest` covers the JVM no-op + resolution. Consumers upload the Kotlin dSYM at their archive step (static framework).
- **Swipe-to-action** (`FrnkSwipeable` + `FrnkSwipeAction` / `FrnkSwipeController` in `shared-ui-atoms` `ui/molecules/`) — a headless **reimplementation** of [stevdza-san/Swipeable-KMP](https://github.com/stevdza-san/Swipeable-KMP) (MIT) with **no Material3** (the upstream artifact hard-depends on `compose.material3`). `FrnkSwipeable(state, onAction, …, content)` wraps any content; `FrnkSwipeBehavior { Dismiss, Reveal }` + `FrnkSwipeDirection { Left, Right, Both }` + token-styled `FrnkSwipeAction`s drive swipe-to-reveal (holds a button row open) or swipe-to-dismiss (fires past `threshold`, snaps back). Pure Compose Foundation gesture engine (`detectHorizontalDragGestures` + `Animatable`); haptics via `LocalFrnkHaptics` (`Selection` on threshold-cross, `Click` on a reveal tap, `Success` on a dismiss commit). `FrnkListRow` gains an **optional, off-by-default** `swipe`/`onSwipeAction` opt-in (+ `surfaceColor` for the sliding backdrop) — `null` renders byte-for-byte the old row. Demoed in all three layers.
- **Typed preferences** over `KeyValueStore` (BACKLOG P4-3): `Preference<T>` (a `ReadWriteProperty`, usable as `pref.value` or `var x by pref`) plus `KeyValueStore.stringPreference`/`booleanPreference`/`intPreference`/`enumPreference(...)` extension factories in `shared-database-api`. Typed accessors with defaults so hosts avoid stringly keys; Int/Enum encode losslessly over the String primitive and fall back to the default on unset/undecodable values (enum decode never throws via `enumValueOf`). Pure stdlib, no new deps, SDK-free; the `KeyValueStore` contract is unchanged. Unit-tested (`PreferenceTest`, 13 cases) and dogfooded by `DefaultEntitlementManager`'s god-mode persistence (same key/representation → no data migration).
- **Design-system tests** (BACKLOG P4-4) — the first Compose UI tests for the toolkit's highest-value atoms: `FrnkSwitch` (5), `FrnkSegmentedControl` (6), and `FrnkTopAppBar` search mode (7), 18 in total. They drive the headless atoms through a real composition (`runComposeUiTest`) and assert against the semantics tree (on/off state, tap→index, query streaming, disabled/skeleton suppression). Run as JVM **host** tests under **Robolectric** (`GraphicsMode.LEGACY`), so they gate in CI with no device/emulator. Live in a new `androidHostTest` source set on `shared-ui-atoms` (the Compose UI-test runtime + Robolectric have no common/iOS variant) and share a `RobolectricComposeTest` base + `setFrnkContent { }` helper. New test-only deps: `org.jetbrains.compose.ui:ui-test`, `androidx.compose.ui:ui-test-manifest`, `org.robolectric:robolectric` — scoped to `androidHostTest`, no production/`commonMain` surface change, XCFrameworks stay clean. Atoms/molecules/organisms otherwise remain previews-only.
- **Cross-platform haptics** (Android + iOS) over `multihaptic` 0.3.2 (BACKLOG P4-5). Compose-free contract in `shared-ui-api` (`HapticType`, `HapticFeedback`, `HapticEngine`, `DefaultHapticFeedback`, `NoOpHapticFeedback`); the `multihaptic` binding (`MultiHapticEngine`) + `LocalFrnkHaptics` live in `shared-ui-atoms`, installed by `FrnkTheme` via `rememberFrnkHaptics()` (no Context plumbing, no native cinterop). Interactive atoms (`FrnkButton`/`FrnkIconButton` → `Click`; `FrnkSwitch`/`FrnkSegmentedControl`/`FrnkBottomNavBar` → `Selection`) auto-fire, gated by the enabled flag the Settings "Haptic feedback" toggle drives through `rememberFrnkSettingsHandler`. Host one-liner: `LocalFrnkHaptics.current.perform(HapticType.Success)`. Unit-tested (`DefaultHapticFeedbackTest`); demoed in all three layers.

### Changed

- **Breaking:** the platform-adaptive bottom bar's default — and now **only** — engine is **`adaptive-nav-bar`** (`io.github.narendraanjana09:adaptive-nav-bar`): a Material3 `NavigationBar` on Android, a native glassy `UITabBar` (iOS 26+) / Material3 bar (older) on iOS, **with a built-in primary-action FAB**. The `engine` parameter is removed from `FrnkTabbedNavScaffold` / `FrnkAppShell` / `FrnkAppScaffold`, and `FrnkAdaptiveNavTab` drops its Calf-only `icon: ImageVector`. **⚠️ Android hosts must bundle the toolkit's nav drawables as raw app assets** — Compose-resource `DrawableResource`s don't package from a KMP library module under AGP 9.2.1 `com.android.kotlin.multiplatform.library` + CMP 1.11.1, so the bar crashes at first render without them (see `docs/HOST_INTEGRATION.md` §8.1). Material3 stays the sole, isolated dependency of `:ui-bottom-nav` (adaptive-nav-bar needs it too).
- **Breaking (restructure Stage 1):** host bootstrap is now an **explicit Koin module list**. `initializeFrnk(modules: List<Module>)` (+ the Android `initializeFrnk(context, modules)` overload) lives in the new **`:core-di`** (`dev.jdgarita.frnk.di`); the choice enums (`BackendChoice` / `ObservabilityChoice` / `MonetizationChoice`) and `frnkModules()` are retired — pass `frnkUiModules() + databaseModule + firebaseObservabilityModule + …` instead (see `docs/HOST_INTEGRATION.md` §4). `FrnkAppScaffold` moved to the new **`:ui-app`** (`dev.jdgarita.frnk.ui.app`, was `dev.jdgarita.frnk.shared`), which also exposes `frnkUiModules()`; `noopObservabilityModule` moved to `:shared:backend:api`. *(Supersedes the `frnkModules`/`ObservabilityChoice` entries below.)*
- **Breaking:** `frnkModules` and `initializeFrnk` gained an `observability` parameter (defaulted to `ObservabilityChoice.None`, so existing source compiles). `firebaseBackendModule` / `supabaseBackendModule` no longer bind `AnalyticsTracker` / `CrashReporter` — they're on the observability axis now.
- `bootstrapFrnkKit` (iOS entry point) gained an `observability` parameter (additive, default `ObservabilityChoice.None`) so iOS hosts can select Firebase observability and trigger the CrashKiOS hook.
- `FrnkTheme` gained a `haptics: HapticFeedback = rememberFrnkHaptics()` parameter (additive, default-provided) and now installs `LocalFrnkHaptics`. The default Settings catalog (`rememberDefaultSettingsState`) groups Notifications + the new "Haptic feedback" toggle under a titled **"Preferences"** section (Notifications was previously an untitled section).
- `FrnkScreenScaffold` gained an additive `containerColor: Color = Theme[colors][colorBackground]` parameter, painted behind the whole screen (overridable to `colorSurface` / `Color.Transparent`) so every screen on the standard template follows the active light/dark palette.
- The default Settings footer is now the minimalist **"Built by JD in 🇨🇷"** (`stringSettingsFooter`). The footer's trailing coffee icon was dropped — `SettingsFooterState` no longer has a `showCoffeeIcon` field (it renders just the text + version).

### Fixed

- Dark mode no longer leaves surfaces light. `FrnkScreenScaffold` now paints a themed background behind its content, and the adaptive bottom bar themes the Android Material3 `NavigationBar` container (`containerColor = colorSurface`) instead of falling back to the unthemed Material baseline — both previously ignored the dark palette, leaving screen content and the bottom nav bar light in dark mode.

### Removed

- **Breaking:** **Calf** (`com.mohamedrejeb.calf:calf-ui`) and the runtime engine A/B are removed entirely. Deleted: the `FrnkAdaptiveNavEngine` enum, the Calf bar `FrnkAdaptiveBottomNavBar`, the Calf-only index scaffold `FrnkAdaptiveBottomNavScaffold`, and `rememberFrnkBottomNavState`. Use `FrnkTabbedNavScaffold` (the adaptive bar) for tabbed apps, or the Material-free `FrnkBottomNavBar` pill in `:ui-components` to avoid Material3 entirely.
- **Breaking (restructure Stage 1):** the aggregators **`:shared`, `:androidApp`, `:iosApp`** — and with them the prebuilt **`FrnkKit.xcframework`** and `bootstrapFrnkKit`. Android hosts depend on the individual `dev.jdgarita.frnk:<module>` coordinates; iOS hosts build their own umbrella XCFramework exporting the modules they use (the demo's `DemoKit` is the worked example; `docs/HOST_INTEGRATION.md` §6). `frnk.kmp.library` no longer declares per-module iOS frameworks (bare targets only).
- **Breaking (restructure Stage 2):** **`AuthService` and the Supabase backend** — `:shared:backend:supabase` is deleted (`BackendChoice.Firebase` was the only remaining value before the enum itself was retired at Stage 1); `Auth.kt`/`FakeAuthService` and `FirebaseAuthService` are gone. Remote data is slated to become Firebase Remote Config (restructure Stage 11).
- **Breaking:** the collapse-on-scroll bars feature — `CollapsibleBarsState`, `rememberCollapsibleBarsState()`, and `Modifier.collapsibleBarOffset(...)` are deleted, and the `collapsibleBars` parameter is removed from `FrnkScreenScaffold`, `FrnkMviScreen`, and `BottomNavScaffoldContent`. The top app bar and the floating bottom nav bar now stay fixed on the viewport while content scrolls underneath them.

- `NoopAnalyticsTracker` / `NoopCrashReporter` removed from `:shared:backend:supabase` and **relocated** to `:shared:backend:api` (they're backend-independent no-op defaults).

## [0.1.0] - 2026-05-15

Initial tagged release of the capability-based KMP toolkit.

### Added

- Capability-based module layout with `api` / `impl` split (`:shared:backend:api` + `:shared:backend:firebase` / `:shared:backend:supabase`; `shared-database-api` + `shared-database-impl`; `shared-monetization-api` + `shared-monetization-revenuecat`).
- `:shared` aggregator module exposing `frnkModules(BackendChoice)` and `initializeFrnk()` for one-shot Koin bootstrap.
- `shared-ui-api` MVI engine (`MviContract` with `UiState`/`UiIntent`/`UiEffect`, `MviViewModel`) and `shared-ui-atoms` headless Compose components built on `compose-unstyled`.
- `AppResult<D, E : AppError>` sealed result type for non-throwing capability APIs.
- `androidApp` (KMP-Android library) and `iosApp` (`FrnkKit` XCFramework) entry points for downstream consumers.
- `:shared-demo` KMP module + `DemoKit.xcframework` powering `androidDemoApp` / `iosDemoApp`. Internal-only — not part of the consumer surface.
- `Frnk.VERSION` constant in `shared-utils` for runtime introspection.

[Unreleased]: https://github.com/jdgarita/frnk/compare/v0.4.2...HEAD
[0.4.2]: https://github.com/jdgarita/frnk/releases/tag/v0.4.2
[0.4.1]: https://github.com/jdgarita/frnk/releases/tag/v0.4.1
[0.4.0]: https://github.com/jdgarita/frnk/releases/tag/v0.4.0
[0.3.1]: https://github.com/jdgarita/frnk/releases/tag/v0.3.1
[0.3.0]: https://github.com/jdgarita/frnk/releases/tag/v0.3.0
[0.2.0-alpha1]: https://github.com/jdgarita/frnk/releases/tag/v0.2.0-alpha1
[0.1.0]: https://github.com/jdgarita/frnk/releases/tag/v0.1.0
