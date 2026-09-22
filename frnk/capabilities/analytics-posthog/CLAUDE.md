# :analytics-posthog

PostHog implementation of `:analytics-api`'s `AnalyticsTracker`, over the official
`com.posthog:posthog-kmp` SDK — the toolkit's **only** analytics provider, mandatory on every host.
Installed by `frnkModules { observability(sentry = …) }` (`:ui-app`, which has an `api` dep on this
module) — the host passes nothing PostHog-specific: **every frnk app reports to one PostHog project,
whose key ships here** (`FrnkPostHogProject`, generated at build time from `POSTHOG_API_KEY` in frnk's
gitignored `local.properties` — never committed), and the builder derives `PostHogAnalyticsConfig(environment
= sentry.environment)`; a host passes `postHog = PostHogAnalyticsConfig(environment = …, debug = …)` only
to tune a flag. `postHogAnalyticsModule(config)` stays public for the raw `initializeFrnk(modules =
listOf(…))` path. Crash reporting is `:crash-sentry`'s (and its DSN *is* per app). There is no no-op and
hosts never bind their own `AnalyticsTracker`; they inject this one for their own events.

## Contents

- `FrnkPostHogProject` — **generated, never committed**: `build.gradle.kts`'s
  `generateFrnkPostHogProject` task writes `build/generated/frnkPostHog/commonMain/kotlin/…/FrnkPostHogProject.kt`
  (wired as a `commonMain` source dir, so it is compiled into the Android AAR and the iOS klib) from
  `POSTHOG_API_KEY` / `POSTHOG_HOST`, resolved `-PPOSTHOG_API_KEY=` › `POSTHOG_API_KEY` env var › the
  frnk checkout's gitignored `local.properties` (in a host: the submodule's). The task **fails** with a
  message naming the three sources when the key is blank, and rejects anything not starting with
  `phc_`. The repo will be public, so the value must never live in source — only in
  `local.properties.example` as an empty slot. It holds `API_KEY` (the project's `phc_…` key — a public
  client key by design, embedded in every shipped binary, granting no read access; the personal/project
  *secret* keys never enter the toolkit) and `HOST` (blank `POSTHOG_HOST` = PostHog Cloud US). Apps are
  told apart by the SDK's `$app_namespace`/`$app_name`/`$app_version` event properties, debug vs release
  by the `environment` super property. Contrast Sentry: one project + DSN per app.
- `PostHogAnalyticsConfig.kt` — `environment` (`debug`/`release`, registered as a super property on
  every event so one project serves both; the only required param), `apiKey` (defaults to
  `FrnkPostHogProject.API_KEY` — hosts leave it), `host` (`DEFAULT_HOST` = `FrnkPostHogProject.HOST`,
  `EU_HOST`), `debug`, `optOut`, `captureApplicationLifecycleEvents`. **A blank `apiKey` throws
  `IllegalArgumentException` in `init`** — never silently dropped events.
- `PostHogAnalyticsModule.kt` — `postHogAnalyticsModule(config)`. The `AnalyticsTracker` single is
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
