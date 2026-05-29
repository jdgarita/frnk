# shared-database-impl

Concrete persistence bindings for `:shared-database-api`. SQLDelight + `multiplatform-settings` live here; the platform drivers and `.sq` schema files go in this module **only**.

## Contents

- `DatabaseModule.kt` — exports `val databaseModule = module { ... }` with `SqlDriverFactory` + `KeyValueStore` bindings, **plus** a `FrnkDB` singleton (built from `SqlDriverFactory.create(FrnkDB.Schema, "frnk.db")`) and the `NoteStore` binding.
- `SqlDelightNoteStore.kt` — the real `NoteStore` impl (BACKLOG P1-1): wraps each `noteQueries` call in `withContext(dispatcher)` + try/catch → `AppResult` (never throws), and maps the generated row to the api `Note`. The generated `FrnkDB` type stays internal to this module.
- `SettingsKeyValueStore.kt` — `KeyValueStore` over `russhwolf/multiplatform-settings` (with `settings.coroutines`).
- `Defaults.kt` (`commonMain`) + `Defaults.android.kt` / `Defaults.ios.kt` — expect/actual for platform default settings.
- `Note.sq` under `src/commonMain/sqldelight/dev/jdgarita/frnk/database/sql/` — the `note` table + `insert` / `lastInsertedId` / `selectAll` / `deleteAll` queries. The SQLDelight Gradle plugin (`alias(libs.plugins.sqldelight)`) is applied here and configured via `sqldelight { databases { create("FrnkDB") { packageName.set("dev.jdgarita.frnk.database.sql") } } }`, both fed from `ProjectConfiguration.DATABASE_NAME` / `DATABASE_PACKAGE`.

## Drivers

- `androidMain`: `sqldelight-android-driver`.
- `iosMain`: `sqldelight-native-driver`.
- `commonMain`: no driver — features go through `SqlDriverFactory` from `:shared-database-api`.

The toolkit owns the `FrnkDB` schema; `databaseModule` builds it through the platform `SqlDriverFactory`. A downstream app can still install its own additional `SqlDriver` schema module via `initializeFrnk { modules(sqlDelightSchemaModule) }`; see `docs/ARCHITECTURE.md`.

## Rules

- All SQLDelight imports stay in this module. The api module must not see `app.cash.sqldelight.Query` etc. beyond the surface that's already `api`-exposed from `:shared-database-api`.
- The database class name (`FrnkDB`) + generated package are centralized in `buildSrc/src/main/kotlin/ProjectConfiguration.kt` (`DATABASE_NAME` / `DATABASE_PACKAGE`). Read them from there if you wire a new SQLDelight target.
- `expect`/`actual` source-set layout: keep `commonMain` for shared logic; put driver instantiation and platform-default discovery in `androidMain` / `iosMain` actuals.

## Testing

- The real round-trip (`NoteStoreRoundTripTest`) lives in **`src/androidHostTest/`** (not `commonTest`) because it uses the JVM JDBC driver `JdbcSqliteDriver.IN_MEMORY` (`libs.sqldelight.sqlite.driver`, added only to the `androidHostTest` source set) — the android/native drivers can't run in a JVM host test. Opt-in is `kotlin { android { withHostTest {} } }`; run with `./gradlew :shared-database-impl:testAndroidHostTest`.

## Dependencies

- `api(projects.sharedDatabaseApi)`.
- `implementation`: `koin.core`, `settings.core`, `settings.coroutines`.
- `androidHostTest`: `kotlin-test`, `kotlinx-coroutines-test`, `sqldelight-sqlite-driver` (JVM).
