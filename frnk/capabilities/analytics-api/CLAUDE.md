# :analytics-api

Pure-interface analytics + crash-reporting contract. **No Ktor, no SDK, no Serialization plugin.** Feature code depends on these interfaces; the concrete impls are `:analytics-posthog` (`AnalyticsTracker`) and `:crash-sentry` (`CrashReporter`) — the toolkit's only providers, mandatory on every host, with **no no-op**. (Remote Config was split out as a sibling capability at restructure Stage 11 and retired outright on 2026-09-22; it is not part of this module.)

## Contents

- `AppResult.kt` — **moved to `shared-utils`** (BACKLOG P1-1). The toolkit-wide `sealed interface AppResult<out D, out E : AppError>` (`Success(data)` / `Failure(error)`), the `AppError` interface, the `CommonError` enum (`Network`, `Unauthorized`, `NotFound`, `Unknown`), and `fold(...)` now live in `dev.jdgarita.frnk.utils` so non-backend `*-api` modules (and the demo's `NoteStore`) can return `AppResult` without depending on this module. Import from `dev.jdgarita.frnk.utils`.
- (`RemoteData.kt` was deleted at restructure Stage 11 — Remote Config became its own capability, `:remote-config-api`, itself retired on 2026-09-22. `Auth.kt` was deleted in restructure Stage 2.)
- `Analytics.kt` — analytics + crash-reporting interfaces (`AnalyticsTracker`, `CrashReporter`, `ToolkitEvent`). `AnalyticsTracker.screen(name, params)` is the screen-view primitive: providers map it onto their native one (PostHog `screen`) so hosts never spell a provider's event name. `CrashReporter.log` is the breadcrumb primitive (context for the next report), `recordException(throwable, extras)` the non-fatal, with `extras` scoped to that one report. **Both interfaces extend `IdentitySource`** (`:identity-api`), so each is an identity sink: `suspend fun identify(id: String): AppResult<Unit, IdentityError>` maps to the SDK's *reserved* user-id field (Firebase's `setUserId`, not a custom user property — a uid as a user property or event param is unbounded-cardinality and unusable in reports). This is the sole reason this module depends on `:identity-api`.
  - **`ToolkitEvent.key` is lowercase `snake_case`: letters, digits, underscores, starting with a letter.** That is the lowest common denominator across analytics providers (the retired Firebase tracker silently dropped anything else) and PostHog reserves the `$` prefix; since every tracker wraps its SDK calls in `runCatching`, a malformed key fails with no trace anywhere.
  - The identity funnel is `IdentitySynced` / `IdentitySyncFailed`, emitted by `DefaultSyncAuthUseCase` (`:monetization-api`) — **not** by the tracker binding, which only writes the user id. The event has to follow the step that decides success, or it reports syncs that later failed. (`SignInStarted`/`SignInCompleted` were removed: nothing emitted them.)
- **No `Noop*` here** (removed 2026-09-22): every host — the demo included — ships PostHog + Sentry
  through `frnkModules { observability(…) }`, so a production no-op would only hide a missing key.
  The only stand-ins are the recording `FakeAnalyticsTracker` / `FakeCrashReporter` in `commonTest`.
  This module has no Koin dependency anymore.

## Rules

- **Every interface method returns `AppResult<…, AppError>`. Never throw.** Callers exhaustive-`when` on the result, so introducing a thrown exception silently bypasses the error contract.
- **No SDK dependencies.** If you find yourself reaching for `io.ktor.*` or `com.google.firebase.*`, you're in the wrong module — move it to the matching `*-impl` module.
- DTOs that need `@Serializable` go in the impl module, not here. This module keeps `kotlin.serialization` off its classpath on purpose.
- Adding a new analytics/crash capability:
  1. Define the interface + domain models here.
  2. Implement it in the provider module (`:analytics-posthog` or `:crash-sentry`) and in the `Fake*` here.
  3. Register in each provider's Koin module.

## Dependencies

- `api(projects.sharedUtils)`, `api(libs.kotlinx.coroutines.core)`, `api(libs.koin.core)` (for the no-op modules), `api(projects.identityApi)` (for `IdentitySource`/`IdentityError`, which appear in the public signatures of both interfaces). That's it — still no SDK.
- `commonTest`: `kotlin-test` + `kotlinx-coroutines-test` (host tests opted in via
  `kotlin { android { withHostTest {} } }`; run with `./gradlew :analytics-api:testAndroidHostTest`).

## Testing & the fake pattern

- `AppResult.fold(onSuccess, onFailure)` collapses both arms with a compile-checked
  exhaustive `when`; prefer it over hand-rolled `when` blocks. It lives in `shared-utils`
  now, tested by `AppResultTest` in `:shared-utils` `commonTest`.
- **`FakeAnalyticsTracker` / `FakeCrashReporter` (`commonTest`) are the canonical fake pattern for `*-api` interfaces.**
  When you add a new `*-api` interface (or implement a real `*-impl`), copy their shape to test
  success **and** failure branches without a real SDK:
  back observable state with a `MutableStateFlow`, return a test-controlled `AppResult`
  from every call, and record inputs for assertions. Fakes live in `commonTest`, never
  in `commonMain`.
- `FakeAnalyticsTracker` / `FakeCrashReporter` (`commonTest`) are the recording fakes for the
  observability interfaces (BACKLOG P1-5), exercised by `ObservabilityTest`. Reuse them when
  asserting analytics/crash wiring downstream (e.g. P3 monetization events). The `Noop*` defaults in
  `commonMain` are also covered there (the "never throws" contract).
