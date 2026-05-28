# shared-backend-api

Pure-interface backend contract. **No Ktor, no Firebase, no Supabase, no Serialization plugin.** Feature code depends on these interfaces; the concrete impls live in `:shared-backend-firebase` and `:shared-backend-supabase`.

## Contents

- `AppResult.kt` — toolkit-wide `sealed interface AppResult<out D, out E : AppError>` with `Success(data)` / `Failure(error)`. `AppError` is an interface with `val message: String`. `CommonError` enum implements it (`Network`, `Unauthorized`, `NotFound`, `Unknown`).
- `Auth.kt` — `AuthService` interface (sign-in / sign-out / current user).
- `RemoteData.kt` — generic CRUD-shaped interface for backed records.
- `Analytics.kt` — analytics + crash-reporting interfaces.

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
