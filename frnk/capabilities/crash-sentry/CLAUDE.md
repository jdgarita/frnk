# :crash-sentry

Sentry implementation of `:analytics-api`'s `CrashReporter`, over the official
`io.sentry:sentry-kotlin-multiplatform` SDK. Installed by assigning
`sentryCrashReportingModule(SentryCrashReportingConfig(dsn = …, environment = …))` to
`frnkModules { crashReporting = … }`. It fills one slot only — analytics is `:analytics-posthog`'s.

## Contents

- `SentryCrashReportingConfig.kt` — what the host supplies: the project **DSN** (a public client key,
  Settings → Client Keys; not the auth token, which uploads symbols and never enters the app),
  `environment` (`debug`/`release` — one project serves both, an issue filter separates them),
  optional `release` (`<bundleId>@<version>+<build>`), `debug`, `sampleRate`, and a `configure`
  escape hatch over the raw `SentryOptions`. `isConfigured` is `dsn.isNotBlank()`.
- `SentryCrashReportingModule.kt` — `sentryCrashReportingModule(config)`. **Blank DSN binds
  `NoopCrashReporter` and never touches the SDK** (a clone without keys, CI). Otherwise the
  `CrashReporter` single is `createdAtStart`, so `Sentry.init` runs inside `startKoin` on every
  bootstrap path, `validate` or not — a crash in the first frames is still reported. Init is
  once-per-process (`started` flag), `runCatching`-wrapped, and sets `sendDefaultPii = false` and
  `enableUnhandledCppExceptionMonitoring = false` (Sentry's own recommendation for Compose
  Multiplatform on Apple) before the host's `configure` runs.
- `SentryCrashReporter.kt` — the binding, behind the `SentryGateway` seam so
  `SentryCrashReporterTest` covers the mapping without an SDK: `recordException(throwable, extras)`
  → `Sentry.captureException(throwable) { scope.setExtra(…) }` (**scoped to that one event** — the
  Crashlytics-era caveat that extras leak into the next report is gone); `log(message)` →
  `Breadcrumb.info(message)`; `identify(id)` → `Sentry.setUser(User(id))`. Every SDK call is
  `runCatching`-wrapped and logs on failure.

## Why there is no CrashKiOS here

On Apple targets `Sentry.init` installs the SDK's own `setUnhandledExceptionHook` (a vendored
NSExceptionKt port), and since KMP 0.23 `captureException(kotlinThrowable)` carries the throwable's
real Kotlin frames. So there is no native hook to install and no `expect/actual` in this module —
it is commonMain only. **Never install CrashKiOS's hook next to Sentry's**: two hooks double-report.

## Symbolication is the host's job

- **Android:** apply `frnk.android.sentry` (build-logic) in the application module. It applies
  Sentry's Android Gradle plugin, keeps `autoInstallation` off (it would raise `sentry-android`
  above the version this SDK bundles — KMP issue #450), and uploads the R8 mapping only when
  `SENTRY_AUTH_TOKEN` is in the environment (the release machine's, never CI's).
- **iOS:** link `sentry-cocoa` (SPM product `Sentry`, the version this SDK's compat table names — see
  `gradle/libs.versions.toml`) in the Xcode project, keep `DEBUG_INFORMATION_FORMAT = dwarf-with-dsym`,
  and upload dSYMs with `sentry-cli debug-files upload` from a run-script phase (`docs/HOST_INTEGRATION.md`
  §6). The static umbrella framework means the app's own dSYM already carries the Kotlin frames.

## Rules

- All `io.sentry.*` imports stay here. `:analytics-api` and feature code never see them.
- Public methods return `AppResult` or `Unit`; nothing throws past the binding.
- Version bumps go **together**: `sentry-kmp` ↔ the `sentry-cocoa` version hosts link ↔ the
  `sentry-android` version the KMP SDK bundles. The catalog comment records the pairing.

## Dependencies

- `api(projects.analyticsApi)`, `implementation(libs.koin.core)`, `implementation(libs.sentry.kmp)`.
- iOS test linking is disabled (the Cocoa SDK comes from the consuming Xcode target); tests run on
  the JVM host (`./gradlew :crash-sentry:testAndroidHostTest`).
