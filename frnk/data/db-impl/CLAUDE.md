# :data-db-impl

Platform SQLDelight **driver** wiring for `:data-db-api` (restructure Stage 4 split — the old `shared-database-impl` minus key-value, minus the NoteStore/FrnkDB schema). The toolkit owns no schema: hosts (and the demo) define their own SQLDelight database and build it through the bound `SqlDriverFactory`.

## Contents

- `DatabaseModule.kt` — exports `val databaseModule = module { single<SqlDriverFactory> { defaultSqlDriverFactory(versionStore = getOrNull<KeyValueStore>()) } }`. Drivers only; the `KeyValueStore` is resolved **leniently** (needed only for `SchemaUpgrade.WipeOnVersionBump`, which persists the schema generation through it — so wipe hosts also install `prefsModule`; `None` hosts don't).
- `Defaults.kt` (`commonMain`) — the common `DefaultSqlDriverFactory` (honors `SchemaUpgrade`: for `WipeOnVersionBump` it reads the persisted version from the `KeyValueStore` key `frnk.db.<name>.schema_version`, runs `shouldWipe`, deletes the file *before* opening, then records the version *after* a successful open) + the internal `DbPlatform` SPI (`createDriver`/`databaseFileExists`/`deleteDatabaseFiles`) it drives.
- `Defaults.android.kt` / `Defaults.ios.kt` — `actual fun dbPlatform()`: Android `AndroidSqliteDriver` + `context.getDatabasePath(name)` exists/delete (reads the Android `Context` from `:core-di`'s `DatabaseContext`); iOS `NativeSqliteDriver` + `NSApplicationSupportDirectory/databases` exists/delete. The iOS actual pins that one base dir for **all three** ops — it injects it into `NativeSqliteDriver` via `onConfiguration` (`extendedConfig.basePath`) and resolves exists/delete against the same `databasesDir()` helper, so **delete-path == create-path by construction** (the wipe is deterministic; closes the former m2 follow-up). The dir mirrors SQLiter's own default so an existing on-disk DB is still found, and is created in-helper because an explicit `basePath` bypasses SQLiter's auto-create.

What moved out at Stage 4:

- `SettingsKeyValueStore` + `defaultKeyValueStore` → **`:data-prefs-impl`**.
- `Note.sq` + `SqlDelightNoteStore` + the FrnkDB schema + `NoteStoreRoundTripTest` → **`demo/shared`** (demo-owned `DemoDB`, OQ-2). This module no longer applies the SQLDelight Gradle plugin — there is nothing to generate.
- `DatabaseContext` → **`:core-di`** androidMain (`dev.jdgarita.frnk.di`), so `:data-prefs-impl` can read it too without depending on this module. The dependency direction inverted: this module's androidMain now depends on `:core-di`.

## Rules

- Drivers only. If you're tempted to add a `.sq` file here, it belongs in the consuming host/demo module instead — see `docs/HOST_INTEGRATION.md` §1.
- No tests today: the module is two one-line driver actuals (the schema round-trip test moved to `demo/shared`'s `androidHostTest`, run via `./gradlew :demo-shared:testAndroidHostTest`).

## Dependencies

- `api(projects.dataDbApi)`; `implementation(libs.koin.core)`; `implementation(projects.dataPrefsApi)` (the `KeyValueStore` the factory persists the schema generation through, for `WipeOnVersionBump`).
- `androidMain`: `sqldelight-android-driver` + `implementation(projects.coreDi)` (for `DatabaseContext`).
- `iosMain`: `sqldelight-native-driver`.
