# :shared:backend:supabase

Supabase implementation of `:shared:backend:api`. The **default** backend (`BackendChoice.Supabase`) — `:shared/FrnkModules.kt` installs `supabaseBackendModule` unless the host passes `BackendChoice.Firebase`.

## Contents

- `SupabaseAuthService.kt` — `AuthService` over `supabase-auth`.
- `SupabaseRemoteData.kt` — `RemoteData` over `supabase-postgrest` (and `supabase-storage` for blobs).
- `SupabaseBackendModule.kt` — exports `val supabaseBackendModule = module { ... }` (**auth + remote data only**).

Analytics/crash are **no longer bound here** (BACKLOG P1-5). They moved to the backend-independent
`ObservabilityChoice` axis in `:shared` — the no-op defaults (`Noop{Analytics,Crash}`, now in
`:shared:backend:api`) are bound by `noopObservabilityModule`. A Supabase-backed app that wants real
analytics picks `ObservabilityChoice.Firebase` (or installs its own binding).

## Rules

- Mirror `FirebaseBackendModule.kt`'s bindings 1:1 — both now bind `AuthService` + `RemoteData` only (analytics/crash live on the separate observability axis).
- Don't reach into anything from `*-firebase`. The two impls must compile independently; that's how parallel Gradle compilation pays off.
- Return `AppResult` everywhere. Map Ktor / Supabase exceptions into `AppResult.Failure(...)` — don't let them escape.

## HTTP client

Ktor is configured here, not in `*-api`:
- `commonMain`: `ktor-client-core`, `content-negotiation`, `serialization`.
- `androidMain`: `ktor-client-android`.
- `iosMain`: `ktor-client-darwin`.

If you need to tweak the Ktor `HttpClient` (interceptors, timeouts, logging), do it inside this module — the configured client is a Koin binding, not part of the `*-api` surface.

## Dependencies

- `api(projects.shared.backend.api)`.
- Plugin: `kotlin.serialization` (Supabase DTOs use `@Serializable`).
