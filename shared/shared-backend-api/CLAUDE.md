# shared-backend-api

Pure-interface backend contract. **No Ktor, no Firebase, no Supabase, no Serialization plugin.** Feature code depends on these interfaces; the concrete impls live in `:shared-backend-firebase` and `:shared-backend-supabase`.

## Contents

- `AppResult.kt` — **moved to `shared-utils`** (BACKLOG P1-1). The toolkit-wide `sealed interface AppResult<out D, out E : AppError>` (`Success(data)` / `Failure(error)`), the `AppError` interface, the `CommonError` enum (`Network`, `Unauthorized`, `NotFound`, `Unknown`), and `fold(...)` now live in `dev.jdgarita.frnk.utils` so non-backend `*-api` modules (e.g. `shared-database-api`'s `NoteStore`) can return `AppResult` without depending on this module. Import from `dev.jdgarita.frnk.utils`.
- `Auth.kt` — `AuthService` interface (sign-in / sign-out / current user).
- `RemoteData.kt` — generic CRUD-shaped interface for backed records.
- `Analytics.kt` — analytics + crash-reporting interfaces (`AnalyticsTracker`, `CrashReporter`, `ToolkitEvent`).
- `NoopObservability.kt` — `NoopAnalyticsTracker` / `NoopCrashReporter`, the SDK-free no-op defaults
  (BACKLOG P1-5). They live here rather than in a backend impl because observability is a
  **backend-independent axis** — `:shared`'s `noopObservabilityModule` binds them for
  `ObservabilityChoice.None`. (Moved here from `shared-backend-supabase`.)

## Rules

- **Every interface method returns `AppResult<…, AppError>`. Never throw.** Callers exhaustive-`when` on the result, so introducing a thrown exception silently bypasses the error contract.
- **No SDK dependencies.** If you find yourself reaching for `io.ktor.*`, `com.google.firebase.*`, or `io.github.jan.tennert.supabase.*`, you're in the wrong module — move it to the matching `*-impl` module.
- DTOs that need `@Serializable` go in the impl module, not here. This module keeps `kotlin.serialization` off its classpath on purpose.
- Adding a new backend capability:
  1. Define the interface + domain models here.
  2. Implement it in **both** `shared-backend-firebase` and `shared-backend-supabase` — both backends must satisfy the contract.
  3. Register in `FirebaseBackendModule.kt` and `SupabaseBackendModule.kt`.

## Dependencies

- `api(projects.sharedUtils)`, `api(libs.kotlinx.coroutines.core)`. That's it.
- `commonTest`: `kotlin-test` + `kotlinx-coroutines-test` (host tests opted in via
  `kotlin { android { withHostTest {} } }`; run with `./gradlew :shared-backend-api:testAndroidHostTest`).

## Testing & the fake pattern

- `AppResult.fold(onSuccess, onFailure)` collapses both arms with a compile-checked
  exhaustive `when`; prefer it over hand-rolled `when` blocks. It lives in `shared-utils`
  now, tested by `AppResultTest` in `:shared-utils` `commonTest`.
- **`FakeAuthService` (`commonTest`) is the canonical fake pattern for `*-api` interfaces.**
  When you add a new `*-api` interface (or implement a real `*-impl`, e.g. BACKLOG
  P1-2/P1-3), copy its shape to test success **and** failure branches without a real SDK:
  back observable state with a `MutableStateFlow`, return a test-controlled `AppResult`
  from every call, and record inputs for assertions. Fakes live in `commonTest`, never
  in `commonMain`.
- `FakeAnalyticsTracker` / `FakeCrashReporter` (`commonTest`) are the recording fakes for the
  observability interfaces (BACKLOG P1-5), exercised by `ObservabilityTest`. Reuse them when
  asserting analytics/crash wiring downstream (e.g. P3 monetization events). The `Noop*` defaults in
  `commonMain` are also covered there (the "never throws" contract).
