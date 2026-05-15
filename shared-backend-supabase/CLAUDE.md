# shared-backend-supabase

Supabase implementation of `:shared-backend-api`. The **default** backend (`BackendChoice.Supabase`) — `:shared/FrnkModules.kt` installs `supabaseBackendModule` unless the host passes `BackendChoice.Firebase`.

## Contents

- `SupabaseAuthService.kt` — `AuthService` over `supabase-auth`.
- `SupabaseRemoteData.kt` — `RemoteData` over `supabase-postgrest` (and `supabase-storage` for blobs).
- `NoopAnalyticsTracker.kt` / `NoopCrashReporter.kt` — Supabase doesn't ship analytics/crash reporting, so these are no-ops to satisfy the `*-api` contract. Hosts wanting real analytics should install a custom Koin binding via `initializeFrnk { ... }`.
- `SupabaseBackendModule.kt` — exports `val supabaseBackendModule = module { ... }`.

## Rules

- Mirror `FirebaseBackendModule.kt`'s bindings 1:1. If the Firebase module exposes an interface, this module must bind something for the same interface — even if it's a no-op (see the Noop trackers).
- Don't reach into anything from `*-firebase`. The two impls must compile independently; that's how parallel Gradle compilation pays off.
- Return `AppResult` everywhere. Map Ktor / Supabase exceptions into `AppResult.Failure(...)` — don't let them escape.

## HTTP client

Ktor is configured here, not in `*-api`:
- `commonMain`: `ktor-client-core`, `content-negotiation`, `serialization`.
- `androidMain`: `ktor-client-android`.
- `iosMain`: `ktor-client-darwin`.

If you need to tweak the Ktor `HttpClient` (interceptors, timeouts, logging), do it inside this module — the configured client is a Koin binding, not part of the `*-api` surface.

## Dependencies

- `api(projects.sharedBackendApi)`.
- Plugin: `kotlin.serialization` (Supabase DTOs use `@Serializable`).
