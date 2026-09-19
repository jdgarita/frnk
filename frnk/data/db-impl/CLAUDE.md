# :data-db-impl

Platform Room wiring for `:data-db-api` (Room replaced SQLDelight here on 2026-09-18 — `docs/plans/2026-09-18-room-database-seam.md`). The toolkit owns no schema: hosts (and the demo) define their own Room `@Database` and open it through the bound `DatabaseFactory`.

## Contents

- `DatabaseModule.kt` — exports `val databaseModule = module { single<DatabaseFactory> { defaultDatabaseFactory(versionStore = getOrNull<KeyValueStore>()) } }`. The `KeyValueStore` is resolved **leniently** (needed only for `SchemaUpgrade.WipeOnVersionBump`, which persists the schema generation through it — so wipe hosts also install `prefsModule`; `None` hosts don't).
- `Defaults.kt` (`commonMain`) — the common `DefaultDatabaseFactory`: resolves the platform location, honours `SchemaUpgrade` (for `WipeOnVersionBump` it reads the persisted version from the `KeyValueStore` key `frnk.db.<name>.schema_version`, runs `shouldWipe`, deletes the file *before* Room sees it, then records the version *after* a successful build), hands the location to the host's builder lambda, applies the toolkit defaults — `BundledSQLiteDriver()` and `Dispatchers.Default` as the query context — then the host's `configure`, then `build()`. Plus the internal `DbPlatform` SPI (`databaseLocation`/`databaseFileExists`/`deleteDatabaseFiles`) it drives.
- `Defaults.android.kt` / `Defaults.ios.kt` — `actual fun dbPlatform()`: Android `context.getDatabasePath(name)` for all three ops (reads the Android `Context` from `:core-di`'s `DatabaseContext`); iOS `<Application Support>/<name>` for all three ops. One helper resolves the location for the build path **and** exists/delete, so delete-path == create-path by construction. The iOS location is Application Support itself, **not** the `databases/` subfolder the SQLDelight driver used: it is where the blueprint host (Faint) has kept its Room file since launch, so a host moving onto the seam finds its existing data.

## Rules

- Locations and driver defaults only. If you're tempted to add an entity here, it belongs in the consuming host/demo module instead — see `docs/HOST_INTEGRATION.md` §1.
- No tests today: the module is two short platform actuals over a pure decision that `:data-db-api` already tests (`ShouldWipeTest`); the schema round-trip lives in `demo/shared`'s `androidHostTest` (`./gradlew :demo-shared:testAndroidHostTest`).

## Dependencies

- `api(projects.dataDbApi)`; `implementation(libs.koin.core)`; `implementation(libs.kotlinx.coroutines.core)` (`Dispatchers.Default`); `implementation(libs.androidx.sqlite.bundled)` (the driver default — the one native cinterop, which is why the module stays out of `DemoKit`'s common surface); `implementation(projects.dataPrefsApi)` (the `KeyValueStore` the factory persists the schema generation through, for `WipeOnVersionBump`).
- `androidMain`: `implementation(projects.coreDi)` (for `DatabaseContext`).
