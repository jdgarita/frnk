# shared-database-impl

Concrete persistence bindings for `:shared-database-api`. SQLDelight + `multiplatform-settings` live here; the platform drivers and `.sq` schema files go in this module **only**.

## Contents

- `DatabaseModule.kt` — exports `val databaseModule = module { ... }` with `SqlDriverFactory` + `KeyValueStore` Koin bindings.
- `SettingsKeyValueStore.kt` — `KeyValueStore` over `russhwolf/multiplatform-settings` (with `settings.coroutines`).
- `Defaults.kt` (`commonMain`) + `Defaults.android.kt` / `Defaults.ios.kt` — expect/actual for platform default settings.
- SQLDelight `.sq` files belong under `src/commonMain/sqldelight/...` (per the toolkit-wide convention; SQLDelight generates into `dev.jdgarita.frnk.database.sql` — the package is wired in this module's `build.gradle.kts`).

## Drivers

- `androidMain`: `sqldelight-android-driver`.
- `iosMain`: `sqldelight-native-driver`.
- `commonMain`: no driver — features go through `SqlDriverFactory` from `:shared-database-api`.

The host's downstream app may need to install a `SqlDriver` schema module via `initializeFrnk { modules(sqlDelightSchemaModule) }`; see `docs/ARCHITECTURE.md`.

## Rules

- All SQLDelight imports stay in this module. The api module must not see `app.cash.sqldelight.Query` etc. beyond the surface that's already `api`-exposed from `:shared-database-api`.
- The database class name (`FrnkDB`) is centralized in `buildSrc/src/main/kotlin/ProjectConfiguration.kt`. Read it from there if you wire a new SQLDelight target.
- `expect`/`actual` source-set layout: keep `commonMain` for shared logic; put driver instantiation and platform-default discovery in `androidMain` / `iosMain` actuals.

## Dependencies

- `api(projects.sharedDatabaseApi)`.
- `implementation`: `koin.core`, `settings.core`, `settings.coroutines`.
