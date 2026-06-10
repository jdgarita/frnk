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
- `MonetizationChoice` (enum) — `RevenueCat` (default) or `None`. A third axis, independent of backend
  and observability: `RevenueCat` installs `revenueCatModule` + `monetizationModule` +
  `paywallScaffoldModule`; `None` installs **no** monetization bindings (for apps that don't monetize or
  use a different billing provider — they supply their own `EntitlementProvider` via `additionalModules`).
- `frnkModules(backend = Supabase, observability = None, monetization = RevenueCat, additionalModules = []): List<Module>`
  — returns `databaseModule` + chosen-backend module + chosen-observability module + the chosen
  monetization modules + the host's `additionalModules`. Unchosen modules are **not** installed, so their
  bindings never appear in the graph at runtime — even though everything is bundled at compile time.
- `initializeFrnk(backend, observability, monetization, additionalModules, extraConfig)` — one-shot
  `startKoin { modules(frnkModules(…)); extraConfig() }`. Hosts register their own Koin modules via the
  first-class `additionalModules` param, and use `extraConfig` for `androidContext(...)` /
  `allowOverride(true)` / logging. `frnkModules` also installs the SDK-free scaffold VM modules
  (home/settings/onboarding/bottomNav) unconditionally, so the VM-backed scaffolds resolve with no
  per-host `includes(...)`.
- **androidMain** `initializeFrnk(context, …)` — the Android one-call bootstrap: sets
  `DatabaseContext.application` (this module's sanctioned `*-impl` import) and registers
  `androidContext(...)`, then delegates to the common overload. The `koin-android` dep lives in this
  module's androidMain only.
- `FrnkAppScaffold(appName, appVersion, …) { homeContent }` — the **batteries-included app root** over
  `:shared-ui-nav`'s `FrnkAppShell`: fail-fast Koin-started assertion, Settings driven by the live
  `EntitlementManager.isPro` (VM re-keys on flips; degrades to Free under `MonetizationChoice.None`),
  `rememberFrnkSettingsHandler` with appearance/onboarding/feedback fallbacks, auto-mounted
  `ToolkitRoute.Paywall`. Lives here (not `:shared-ui-nav`) because only `:shared` sees the
  monetization wiring — the Compose plugin is already applied to this module.

Keep this surface tiny. Adding a new top-level entry point here is a public-API change for every downstream consumer.

## Aggregation

`commonMain` `api`-depends on:
- All six `*-api` modules (utils, ui-api, ui-atoms, database-api, backend-api, monetization-api).
- `shared-ui-nav` (platform-adaptive bottom navigation; **the toolkit's sole Material3 dependency** — it
  ships in FrnkKit for every consumer, a deliberate trade isolated to that one module). **POC in flight:**
  a second bar engine (`adaptive-nav-bar`, with a built-in primary-action button) is wired alongside Calf for an A/B
  via `FrnkTabbedNavScaffold(engine = …)` — see `shared/shared-ui-nav/CLAUDE.md` (incl. the Android
  resource-packaging caveat) and `docs/spikes/adaptive-bottom-nav.md`.
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
