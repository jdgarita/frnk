# shared

**The only consumer-facing module of the toolkit.** Aggregates every `shared-*-api` + every `shared-*-impl` via `api(...)` so downstream apps depend on `:shared` (or its re-exporters `:androidApp` / `:iosApp`) and nothing else.

## Public surface

`package dev.jdgarita.frnk.shared`:

- `BackendChoice` (enum) — `Supabase` (default) or `Firebase`. Selects the **auth + remote data** backend only.
- `ObservabilityChoice` (enum) — `None` (default, no-op) or `Firebase`. Selects analytics + crash
  reporting **independently of `BackendChoice`** (BACKLOG P1-5): a local-only app (no backend) or a
  Supabase-backed app can still pick `ObservabilityChoice.Firebase`. `noopObservabilityModule` (here)
  is the `None` default over `shared-backend-api`'s `Noop{Analytics,Crash}`; `firebaseObservabilityModule`
  (in `shared-backend-firebase`) is the Firebase one.
- `frnkModules(backend = Supabase, observability = None): List<Module>` — returns `databaseModule` +
  chosen-backend module + chosen-observability module + `revenueCatModule`. Unchosen modules are **not**
  installed, so their bindings never appear in the graph at runtime — even though everything is bundled
  at compile time.
- `initializeFrnk(backend, observability, extraConfig)` — one-shot
  `startKoin { modules(frnkModules(backend, observability)); extraConfig() }`. Hosts plug in
  `androidContext(...)` and their own modules via `extraConfig`.

Keep this surface tiny. Adding a new top-level entry point here is a public-API change for every downstream consumer.

## Aggregation

`commonMain` `api`-depends on:
- All six `*-api` modules (utils, ui-api, ui-atoms, database-api, backend-api, monetization-api).
- `shared-ui-nav` (platform-adaptive bottom navigation; **the toolkit's sole Material3 dependency** — it
  ships in FrnkKit for every consumer, a deliberate trade isolated to that one module).
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
