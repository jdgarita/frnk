# Changelog

All notable changes to **frnk** are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/), and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## Pre-1.0 policy

While the public API is in `0.x`:

- `MINOR` (`0.x.0`) may include breaking API changes. Read the release notes before bumping.
- `PATCH` (`0.x.y`) is additive and bug-fix only — safe to take without inspection.

Once a `1.0.0` ships, normal SemVer applies: breaking changes are `MAJOR`-only.

## [Unreleased]

### Added

- Firebase Analytics + Crashlytics implementations (`FirebaseAnalyticsTracker` / `FirebaseCrashReporter`) over the gitlive SDKs, with `runCatching` no-op safety when Firebase isn't configured (BACKLOG P1-5).
- `ObservabilityChoice { None, Firebase }` — analytics + crash reporting selectable **independently of `BackendChoice`**, via `frnkModules(backend, observability)` / `initializeFrnk(...)`. `firebaseObservabilityModule` binds the real impls; `noopObservabilityModule` is the `None` default. Lets a local-storage-only app (no backend) ship Firebase telemetry.
- Recording `FakeAnalyticsTracker` / `FakeCrashReporter` (+ `ObservabilityTest`) in `shared-backend-api` `commonTest`; `DemoViewModelTest` in `:shared-demo`.
- `androidDemoApp` applies the `google-services` + `firebase-crashlytics` Gradle plugins and installs the real `firebaseObservabilityModule` to smoke-test Firebase on a device; the demo gains an "Analytics & Crash" section across all three layers.
- iOS unhandled-crash symbolication via CrashKiOS (`co.touchlab.crashkios:crashlytics`, `shared-backend-firebase` `iosMain`): selecting `ObservabilityChoice.Firebase` installs a Kotlin/Native unhandled-exception hook (`enableNativeCrashHandler`, no-op on Android) so *uncaught* Kotlin crashes reach Crashlytics symbolicated — not just explicitly-caught `recordException`s (BACKLOG P1-5b). The demo gains a "Force crash (unhandled)" action; `FirebaseObservabilityModuleTest` covers the JVM no-op + resolution. Consumers upload the Kotlin dSYM at their archive step (static framework).
- **Swipe-to-action** (`FrnkSwipeable` + `FrnkSwipeAction` / `FrnkSwipeController` in `shared-ui-atoms` `ui/molecules/`) — a headless **reimplementation** of [stevdza-san/Swipeable-KMP](https://github.com/stevdza-san/Swipeable-KMP) (MIT) with **no Material3** (the upstream artifact hard-depends on `compose.material3`). `FrnkSwipeable(state, onAction, …, content)` wraps any content; `FrnkSwipeBehavior { Dismiss, Reveal }` + `FrnkSwipeDirection { Left, Right, Both }` + token-styled `FrnkSwipeAction`s drive swipe-to-reveal (holds a button row open) or swipe-to-dismiss (fires past `threshold`, snaps back). Pure Compose Foundation gesture engine (`detectHorizontalDragGestures` + `Animatable`); haptics via `LocalFrnkHaptics` (`Selection` on threshold-cross, `Click` on a reveal tap, `Success` on a dismiss commit). `FrnkListRow` gains an **optional, off-by-default** `swipe`/`onSwipeAction` opt-in (+ `surfaceColor` for the sliding backdrop) — `null` renders byte-for-byte the old row. Demoed in all three layers.
- **Typed preferences** over `KeyValueStore` (BACKLOG P4-3): `Preference<T>` (a `ReadWriteProperty`, usable as `pref.value` or `var x by pref`) plus `KeyValueStore.stringPreference`/`booleanPreference`/`intPreference`/`enumPreference(...)` extension factories in `shared-database-api`. Typed accessors with defaults so hosts avoid stringly keys; Int/Enum encode losslessly over the String primitive and fall back to the default on unset/undecodable values (enum decode never throws via `enumValueOf`). Pure stdlib, no new deps, SDK-free; the `KeyValueStore` contract is unchanged. Unit-tested (`PreferenceTest`, 13 cases) and dogfooded by `DefaultEntitlementManager`'s god-mode persistence (same key/representation → no data migration).
- **Cross-platform haptics** (Android + iOS) over `multihaptic` 0.3.2 (BACKLOG P4-5). Compose-free contract in `shared-ui-api` (`HapticType`, `HapticFeedback`, `HapticEngine`, `DefaultHapticFeedback`, `NoOpHapticFeedback`); the `multihaptic` binding (`MultiHapticEngine`) + `LocalFrnkHaptics` live in `shared-ui-atoms`, installed by `FrnkTheme` via `rememberFrnkHaptics()` (no Context plumbing, no native cinterop). Interactive atoms (`FrnkButton`/`FrnkIconButton` → `Click`; `FrnkSwitch`/`FrnkSegmentedControl`/`FrnkBottomNavBar` → `Selection`) auto-fire, gated by the enabled flag the Settings "Haptic feedback" toggle drives through `rememberFrnkSettingsHandler`. Host one-liner: `LocalFrnkHaptics.current.perform(HapticType.Success)`. Unit-tested (`DefaultHapticFeedbackTest`); demoed in all three layers.

### Changed

- **Breaking:** `frnkModules` and `initializeFrnk` gained an `observability` parameter (defaulted to `ObservabilityChoice.None`, so existing source compiles). `firebaseBackendModule` / `supabaseBackendModule` no longer bind `AnalyticsTracker` / `CrashReporter` — they're on the observability axis now.
- `bootstrapFrnkKit` (iOS entry point) gained an `observability` parameter (additive, default `ObservabilityChoice.None`) so iOS hosts can select Firebase observability and trigger the CrashKiOS hook.
- `FrnkTheme` gained a `haptics: HapticFeedback = rememberFrnkHaptics()` parameter (additive, default-provided) and now installs `LocalFrnkHaptics`. The default Settings catalog (`rememberDefaultSettingsState`) groups Notifications + the new "Haptic feedback" toggle under a titled **"Preferences"** section (Notifications was previously an untitled section).
- `FrnkScreenScaffold` gained an additive `containerColor: Color = Theme[colors][colorBackground]` parameter, painted behind the whole screen (overridable to `colorSurface` / `Color.Transparent`) so every screen on the standard template follows the active light/dark palette.
- The default Settings footer is now the minimalist **"Built by JD in 🇨🇷"** (`stringSettingsFooter`), with the trailing coffee icon off by default in the catalog (`SettingsFooterState.showCoffeeIcon` is still opt-in for hosts that want it).

### Fixed

- Dark mode no longer leaves surfaces light. `FrnkScreenScaffold` now paints a themed background behind its content, and `FrnkAdaptiveBottomNavBar` themes the Android Material3 `NavigationBar` container (`containerColor = colorSurface` / `contentColor = colorOnSurface`) instead of falling back to the unthemed Material baseline — both previously ignored the dark palette, leaving screen content and the bottom nav bar light in dark mode.

### Removed

- **Breaking:** the collapse-on-scroll bars feature — `CollapsibleBarsState`, `rememberCollapsibleBarsState()`, and `Modifier.collapsibleBarOffset(...)` are deleted, and the `collapsibleBars` parameter is removed from `FrnkScreenScaffold`, `FrnkMviScreen`, and `BottomNavScaffoldContent`. The top app bar and the floating bottom nav bar now stay fixed on the viewport while content scrolls underneath them.

- `NoopAnalyticsTracker` / `NoopCrashReporter` removed from `shared-backend-supabase` and **relocated** to `shared-backend-api` (they're backend-independent no-op defaults).

## [0.1.0] - 2026-05-15

Initial tagged release of the capability-based KMP toolkit.

### Added

- Capability-based module layout with `api` / `impl` split (`shared-backend-api` + `shared-backend-firebase` / `shared-backend-supabase`; `shared-database-api` + `shared-database-impl`; `shared-monetization-api` + `shared-monetization-revenuecat`).
- `:shared` aggregator module exposing `frnkModules(BackendChoice)` and `initializeFrnk()` for one-shot Koin bootstrap.
- `shared-ui-api` MVI engine (`MviContract` with `UiState`/`UiIntent`/`UiEffect`, `MviViewModel`) and `shared-ui-atoms` headless Compose components built on `compose-unstyled`.
- `AppResult<D, E : AppError>` sealed result type for non-throwing capability APIs.
- `androidApp` (KMP-Android library) and `iosApp` (`FrnkKit` XCFramework) entry points for downstream consumers.
- `:shared-demo` KMP module + `DemoKit.xcframework` powering `androidDemoApp` / `iosDemoApp`. Internal-only — not part of the consumer surface.
- `Frnk.VERSION` constant in `shared-utils` for runtime introspection.

[Unreleased]: https://github.com/jdgarita/frnk/compare/v0.1.0...HEAD
[0.1.0]: https://github.com/jdgarita/frnk/releases/tag/v0.1.0
