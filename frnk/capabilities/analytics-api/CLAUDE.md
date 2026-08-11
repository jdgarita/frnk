# :analytics-api

Pure-interface analytics + crash-reporting contract. **No Ktor, no Firebase, no Serialization plugin.** Feature code depends on these interfaces; the concrete impls live in `:analytics-impl`. (Remote Config is its own sibling capability pair — `:remote-config-api`/`:remote-config-impl` — since restructure Stage 11, not part of this module.)

## Contents

- `AppResult.kt` — **moved to `shared-utils`** (BACKLOG P1-1). The toolkit-wide `sealed interface AppResult<out D, out E : AppError>` (`Success(data)` / `Failure(error)`), the `AppError` interface, the `CommonError` enum (`Network`, `Unauthorized`, `NotFound`, `Unknown`), and `fold(...)` now live in `dev.jdgarita.frnk.utils` so non-backend `*-api` modules (and the demo's `NoteStore`) can return `AppResult` without depending on this module. Import from `dev.jdgarita.frnk.utils`.
- (`RemoteData.kt` was deleted at restructure Stage 11 — Remote Config became its own capability pair, `:remote-config-api`/`:remote-config-impl`, sibling of analytics. `Auth.kt` was deleted in restructure Stage 2.)
- `Analytics.kt` — analytics + crash-reporting interfaces (`AnalyticsTracker`, `CrashReporter`, `ToolkitEvent`). **Both interfaces extend `IdentitySource`** (`:identity-api`), so each is an identity sink: `suspend fun identify(id: String): AppResult<Unit, IdentityError>` maps to the SDK's *reserved* user-id field (Firebase's `setUserId`, not a custom user property — a uid as a user property or event param is unbounded-cardinality and unusable in reports). This is the sole reason this module depends on `:identity-api`.
  - **`ToolkitEvent.key` must be alphanumeric-plus-underscore and start with a letter.** Firebase Analytics rejects anything else — hyphens included — and silently drops the event; since `FirebaseAnalyticsTracker` wraps every SDK call in `runCatching`, a malformed key fails with no trace anywhere. The vocabulary is lowercase `snake_case`.
  - The identity funnel is `IdentitySynced` / `IdentitySyncFailed`, emitted by `DefaultSyncAuthUseCase` (`:monetization-api`) — **not** by the tracker binding, which only writes the user id. The event has to follow the step that decides success, or it reports syncs that later failed. (`SignInStarted`/`SignInCompleted` were removed: nothing emitted them.)
- `NoopObservability.kt` — `NoopAnalyticsTracker` / `NoopCrashReporter`, the SDK-free no-op defaults
  (BACKLOG P1-5). They live here rather than in a backend impl because observability is a
  **backend-independent axis**.
- `NoopObservabilityModule.kt` — `val noopObservabilityModule`, the Koin binding of the `Noop*`
  defaults (moved here from the deleted `:shared` aggregator at restructure Stage 1). Hosts install
  it XOR `firebaseObservabilityModule`.

## Rules

- **Every interface method returns `AppResult<…, AppError>`. Never throw.** Callers exhaustive-`when` on the result, so introducing a thrown exception silently bypasses the error contract.
- **No SDK dependencies.** If you find yourself reaching for `io.ktor.*` or `com.google.firebase.*`, you're in the wrong module — move it to the matching `*-impl` module.
- DTOs that need `@Serializable` go in the impl module, not here. This module keeps `kotlin.serialization` off its classpath on purpose.
- Adding a new analytics/crash capability:
  1. Define the interface + domain models here.
  2. Implement it in `:analytics-impl`.
  3. Register in `FirebaseObservabilityModule.kt`.

## Dependencies

- `api(projects.sharedUtils)`, `api(libs.kotlinx.coroutines.core)`, `api(libs.koin.core)` (for `noopObservabilityModule`), `api(projects.identityApi)` (for `IdentitySource`/`IdentityError`, which appear in the public signatures of both interfaces). That's it — still no SDK.
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
