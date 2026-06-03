# shared-database-api

Pure-interface persistence module. **No SQLDelight runtime usage of generated code, no platform drivers** — just the contracts that feature code depends on.

## Contents

- `KeyValueStore.kt` — interface for simple key/value storage (impl uses `russhwolf/multiplatform-settings`).
- `Preference.kt` — typed convenience layer over `KeyValueStore` (BACKLOG P4-3). `Preference<T> : ReadWriteProperty` plus `KeyValueStore.stringPreference/booleanPreference/intPreference/enumPreference(...)` factories, so hosts get typed accessors with defaults and `var x by pref` delegation instead of stringly keys. Pure stdlib; the `KeyValueStore` contract is unchanged — Int/Enum encode losslessly over the String primitive (corrupt/unknown values fall back to the default; enum decode never throws). `Long`/`Double`/nullable-string are deliberately deferred (no consumer; would need extra encoding). Dogfooded by `DefaultEntitlementManager`'s god-mode persistence (`shared-monetization-api`).
- `SqlDriverFactory.kt` — interface returning a `SqlDriver` for the host to bridge platform driver creation. Reused by `databaseModule` to build the toolkit's own `FrnkDB` from `FrnkDB.Schema`.
- `NoteStore.kt` — first relational entity (BACKLOG P1-1): the `Note(id, content, createdAt)` domain model + the `NoteStore` interface (`add` / `all` / `clear`), all returning `AppResult<…, CommonError>`. The generated `FrnkDB` row type stays in `:shared-database-impl` and never crosses this boundary — the impl maps rows to this `Note`. `AppResult`/`CommonError` come from `shared-utils`.

## Rules

- This module's classpath does include `sqldelight-runtime` and `multiplatform-settings-core` as `api` deps (downstream signatures use them), but **don't put SQLDelight `.sq` files here**. They belong in `:shared-database-impl`, which is where the SQLDelight Gradle plugin runs.
- Don't bind concrete `KeyValueStore` / `SqlDriverFactory` here — Koin wiring lives in `:shared-database-impl` (`databaseModule`).
- Adding a new persisted entity:
  1. Add the `.sq` file under `shared-database-impl/src/commonMain/sqldelight/...`.
  2. Define an interface here that exposes only the surface feature code needs (don't leak the generated `Queries` type if you can help it).
  3. Bind the interface in `:shared-database-impl/DatabaseModule.kt`.

## Dependencies

- `api(projects.sharedUtils)`, `api(libs.sqldelight.runtime)`, `api(libs.settings.core)`.
