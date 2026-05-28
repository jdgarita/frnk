# shared-database-api

Pure-interface persistence module. **No SQLDelight runtime usage of generated code, no platform drivers** — just the contracts that feature code depends on.

## Contents

- `KeyValueStore.kt` — interface for simple key/value storage (impl uses `russhwolf/multiplatform-settings`).
- `SqlDriverFactory.kt` — interface returning a `SqlDriver` for the host to bridge platform driver creation.

## Rules

- This module's classpath does include `sqldelight-runtime` and `multiplatform-settings-core` as `api` deps (downstream signatures use them), but **don't put SQLDelight `.sq` files here**. They belong in `:shared-database-impl`, which is where the SQLDelight Gradle plugin runs.
- Don't bind concrete `KeyValueStore` / `SqlDriverFactory` here — Koin wiring lives in `:shared-database-impl` (`databaseModule`).
- Adding a new persisted entity:
  1. Add the `.sq` file under `shared-database-impl/src/commonMain/sqldelight/...`.
  2. Define an interface here that exposes only the surface feature code needs (don't leak the generated `Queries` type if you can help it).
  3. Bind the interface in `:shared-database-impl/DatabaseModule.kt`.

## Dependencies

- `api(projects.sharedUtils)`, `api(libs.sqldelight.runtime)`, `api(libs.settings.core)`.
