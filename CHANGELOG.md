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

### Changed

- **Breaking:** `frnkModules` and `initializeFrnk` gained an `observability` parameter (defaulted to `ObservabilityChoice.None`, so existing source compiles). `firebaseBackendModule` / `supabaseBackendModule` no longer bind `AnalyticsTracker` / `CrashReporter` — they're on the observability axis now.
- `bootstrapFrnkKit` (iOS entry point) gained an `observability` parameter (additive, default `ObservabilityChoice.None`) so iOS hosts can select Firebase observability and trigger the CrashKiOS hook.

### Fixed

### Removed

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
