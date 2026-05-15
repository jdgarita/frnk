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

### Changed

### Fixed

### Removed

## [0.1.0] - 2026-05-15

Initial tagged release of the capability-based KMP toolkit.

### Added

- Capability-based module layout with `api` / `impl` split (`shared-backend-api` + `shared-backend-firebase` / `shared-backend-supabase`; `shared-database-api` + `shared-database-impl`; `shared-monetization-api` + `shared-monetization-revenuecat`).
- `:shared` aggregator module exposing `frnkModules(BackendChoice)` and `initializeFrnk()` for one-shot Koin bootstrap.
- `shared-ui-atoms` MVI engine (`MviContract`, `MviViewModel`, `ObserveAsEvents`) and headless Compose components built on `compose-unstyled`.
- `AppResult<D, E : AppError>` sealed result type for non-throwing capability APIs.
- `androidApp` (KMP-Android library) and `iosApp` (`FrnkKit` XCFramework) entry points for downstream consumers.
- `:shared-demo` KMP module + `DemoKit.xcframework` powering `androidDemoApp` / `iosDemoApp`. Internal-only — not part of the consumer surface.
- `Frnk.VERSION` constant in `shared-utils` for runtime introspection.

[Unreleased]: https://github.com/jdgarita/frnk/compare/v0.1.0...HEAD
[0.1.0]: https://github.com/jdgarita/frnk/releases/tag/v0.1.0
