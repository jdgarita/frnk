# :analytics-posthog

PostHog implementation of `:analytics-api`'s `AnalyticsTracker`, over the official
`com.posthog:posthog-kmp` SDK. Installed by assigning
`postHogAnalyticsModule(PostHogAnalyticsConfig(apiKey = …, environment = …))` to
`frnkModules { analytics = … }`. It fills one slot only — crash reporting is `:crash-sentry`'s.

## Contents

- `PostHogAnalyticsConfig.kt` — what the host supplies: the project **API key** (`phc_…`, a public
  client key), `environment` (`debug`/`release`, registered as a super property on every event so
  one project serves both), `host` (`DEFAULT_HOST` = PostHog Cloud US, `EU_HOST`), `debug`, `optOut`,
  `captureApplicationLifecycleEvents`. `isConfigured` is `apiKey.isNotBlank()`.
- `PostHogAnalyticsModule.kt` — `postHogAnalyticsModule(config)`. **Blank key binds
  `NoopAnalyticsTracker` and never touches the SDK.** Otherwise the `AnalyticsTracker` single is
  `createdAtStart`, so `PostHog.setup` runs inside `startKoin` and the SDK's lifecycle events see
  the launch. Setup is once-per-process and `runCatching`-wrapped. Two SDK choices are made here on
  purpose: `captureScreenViews = false` (the SDK's Activity-based capture sees one Activity in a
  Compose app) — screens come through `AnalyticsTracker.screen` — and
  `personProfiles = IDENTIFIED_ONLY` (anonymous events create no person until `identify`, which the
  toolkit calls with the app's anonymous id at bootstrap and which merges them in).
  `Scope.platformPostHogContext()` is the module's only `expect/actual`: Android reads the
  `Application` from Koin's `androidContext()` (the Android `initializeFrnk` overload registers it),
  iOS needs nothing.
- `PostHogAnalyticsTracker.kt` — the binding, behind the `PostHogGateway` seam so
  `PostHogAnalyticsTrackerTest` covers the mapping without an SDK: `track`/`trackCustom` →
  `capture`; `screen` → `PostHog.screen` (the `$screen` event PostHog's navigation insights read);
  `setUserProperty` → `setPersonProperties` (`$set`; a `null` value writes nothing); `identify` →
  `PostHog.identify(distinctId)`. Null parameter values are dropped, nothing is renamed.

## Names

Event and property names go through verbatim. PostHog reserves the `$` prefix for its own
vocabulary; everything else is fine, which is why `ToolkitEvent.key`'s rule (lowercase
`snake_case`) is the intersection with Firebase's, not something PostHog demands.

## Native SDK contract (iOS)

`posthog-kmp` compiles against `posthog-ios` through a bundled cinterop klib; the framework itself is
**not** vendored. The consuming Xcode project links the SPM product `PostHog` (the version this SDK
wraps — see `gradle/libs.versions.toml`), exactly as it links RevenueCat's and Sentry's. Umbrella
frameworks defer the symbols with `linkerOpts("-undefined", "dynamic_lookup")`.

## Rules

- All `com.posthog.*` imports stay here. `:analytics-api` and feature code never see them.
- Public methods return `AppResult` or `Unit`; nothing throws past the binding.

## Dependencies

- `api(projects.analyticsApi)`, `implementation(libs.koin.core)`, `implementation(libs.posthog.kmp)`;
  androidMain adds `libs.koin.android` for `androidApplication()`.
- iOS test linking is disabled (posthog-ios comes from the consuming Xcode target); tests run on
  the JVM host (`./gradlew :analytics-posthog:testAndroidHostTest`).
