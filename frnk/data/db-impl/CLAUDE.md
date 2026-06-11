# :data-db-impl

Platform SQLDelight **driver** wiring for `:data-db-api` (restructure Stage 4 split — the old `shared-database-impl` minus key-value, minus the NoteStore/FrnkDB schema). The toolkit owns no schema: hosts (and the demo) define their own SQLDelight database and build it through the bound `SqlDriverFactory`.

## Contents

- `DatabaseModule.kt` — exports `val databaseModule = module { single<SqlDriverFactory> { defaultSqlDriverFactory() } }`. That's the whole module: drivers only. Key-value is a separate axis (`prefsModule`, `:data-prefs-impl`).
- `Defaults.kt` (`commonMain`) + `Defaults.android.kt` / `Defaults.ios.kt` — `expect/actual` for the platform driver factory: `AndroidSqliteDriver` (reads the Android `Context` from `:core-di`'s `DatabaseContext`) / `NativeSqliteDriver`.

What moved out at Stage 4:

- `SettingsKeyValueStore` + `defaultKeyValueStore` → **`:data-prefs-impl`**.
- `Note.sq` + `SqlDelightNoteStore` + the FrnkDB schema + `NoteStoreRoundTripTest` → **`demo/shared`** (demo-owned `DemoDB`, OQ-2). This module no longer applies the SQLDelight Gradle plugin — there is nothing to generate.
- `DatabaseContext` → **`:core-di`** androidMain (`dev.jdgarita.frnk.di`), so `:data-prefs-impl` can read it too without depending on this module. The dependency direction inverted: this module's androidMain now depends on `:core-di`.

## Rules

- Drivers only. If you're tempted to add a `.sq` file here, it belongs in the consuming host/demo module instead — see `docs/HOST_INTEGRATION.md` §1.
- No tests today: the module is two one-line driver actuals (the schema round-trip test moved to `demo/shared`'s `androidHostTest`, run via `./gradlew :shared-demo:testAndroidHostTest`).

## Dependencies

- `api(projects.dataDbApi)`; `implementation(libs.koin.core)`.
- `androidMain`: `sqldelight-android-driver` + `implementation(projects.coreDi)` (for `DatabaseContext`).
- `iosMain`: `sqldelight-native-driver`.
