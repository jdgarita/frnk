# :core-di

Host-facing Koin bootstrap helpers (`dev.jdgarita.frnk.di`). Created at restructure Stage 1 (OQ-5/OQ-7) as the replacement for the deleted `:shared` aggregator's `initializeFrnk` + choice enums. Final home: `frnk/core/di` (Stage 3).

## Public surface

- `initializeFrnk(modules: List<Module>, extraConfig: KoinApplication.() -> Unit = {}): KoinApplication` — one-shot `startKoin`. Hosts pass an **explicit module list** (`frnkUiModules() + databaseModule + prefsModule + firebaseObservabilityModule + …`); there is no backend/observability/monetization switch. See `docs/HOST_INTEGRATION.md` §4 for the canonical snippet.
- **androidMain** `initializeFrnk(context, modules, extraConfig)` — also sets `DatabaseContext.application` and registers `androidContext(...)`. iOS hosts call the common overload.
- **androidMain** `DatabaseContext` — the process-wide Android `Context` seam both data impls read (`:data-db-impl`'s SQLDelight driver factory, `:data-prefs-impl`'s SharedPreferences-backed `KeyValueStore`). Re-homed here at Stage 4 (it lived in the old `shared-database-impl`) so the two split impls share it without depending on each other; they now depend on this module, not the reverse. Hosts that bypass `initializeFrnk` must set it from `Application.onCreate` (the demo does).
- `requireFrnkKoin(): Koin` — fail-fast accessor relied on by `:ui-app`'s `FrnkApp` root (and any host); turns a missing bootstrap into an immediate, explained crash instead of a deep `NoDefinitionFound`.

## Rules

- Keep this module Compose-free and tiny — it's the bottom of the ui column (`ui-app` depends on it; it depends on nothing but Koin).
- Don't reintroduce capability enums or conditional module assembly here; capability selection is the host's module list by design (OQ-7).

## Dependencies

- `commonMain`: `api(libs.koin.core)` (Koin types are in the public signatures).
- `androidMain`: `api(libs.koin.android)`. No project deps — the data impls depend on this module for `DatabaseContext`, not vice versa.
- `commonTest`: `kotlin-test` (+ coroutines-test via the `frnk.kmp.library.hosttest` plugin); run with `./gradlew :core-di:testAndroidHostTest`.
