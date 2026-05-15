# shared

**The only consumer-facing module of the toolkit.** Aggregates every `shared-*-api` + every `shared-*-impl` via `api(...)` so downstream apps depend on `:shared` (or its re-exporters `:androidApp` / `:iosApp`) and nothing else.

## Public surface

`package dev.jdgarita.frnk.shared`:

- `BackendChoice` (enum) — `Supabase` (default) or `Firebase`.
- `frnkModules(backend: BackendChoice = Supabase): List<Module>` — returns `databaseModule` + chosen-backend module + `revenueCatModule`. The unchosen backend's Koin module is **not** installed, so its bindings never appear in the graph at runtime — even though both backends are bundled at compile time.
- `initializeFrnk(backend, extraConfig)` — one-shot `startKoin { modules(frnkModules(backend)); extraConfig() }`. Hosts plug in `androidContext(...)` and their own modules via `extraConfig`.

Keep this surface tiny. Adding a new top-level entry point here is a public-API change for every downstream consumer.

## Aggregation

`commonMain` `api`-depends on:
- All six `*-api` modules (utils, ui-api, ui-atoms, database-api, backend-api, monetization-api).
- All four `*-impl` modules (database-impl, backend-firebase, backend-supabase, monetization-revenuecat).

`koin.core` is `implementation` here because consumers get Koin via the impl modules' `api` exports anyway.

## Rules

- **`:shared` is the only place that may import from an `*-impl` package.** Feature code lives behind `*-api` interfaces and gets resolved by Koin. Any `import dev.jdgarita.frnk.backend.firebase.*` or `...supabase.*` outside this module is a bug.
- Adding a new domain (e.g. `shared-search-api` + `shared-search-elasticsearch`):
  1. Add the modules and `include(...)` them in `settings.gradle.kts`.
  2. Add `api(projects.sharedSearchApi)` and `api(projects.sharedSearchElasticsearch)` to this module's `commonMain.dependencies`.
  3. Add the impl's Koin module to `frnkModules(...)` in `FrnkModules.kt`.
- The package is `dev.jdgarita.frnk.shared` (Android namespace `${GROUP_ID}.shared`). Don't rename — `:androidApp` and `:iosApp` re-export this surface verbatim.

## Dependencies

- `api`-deps as listed above.
- `implementation(libs.koin.core)`.
- Compose Multiplatform plugin is applied because aggregated atoms transitively need it.
