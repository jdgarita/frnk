# :data-db-api

Pure-interface SQL persistence SPI (restructure Stage 4 split — the old `shared-database-api` minus key-value, minus NoteStore). **The toolkit owns no schema**: this module is only the seam a host bridges platform driver creation through.

## Contents

- `SqlDriverFactory.kt` — `interface` returning a `SqlDriver` for a host-supplied `SqlSchema` + db name + `SchemaUpgrade`. Bound by `databaseModule` (`:data-db-impl`); consumed by the host's own schema module — `demo/shared`'s `demoNotesModule`/`DemoDB` is the worked example (OQ-2). **Was a `fun interface`** until the wipe hook added the defaulted `upgrade` param (a SAM can't carry it) — construct via `object : SqlDriverFactory` now.
- `SchemaUpgrade.kt` — `sealed interface SchemaUpgrade` (`None` | `WipeOnVersionBump(version)`) + the pure `shouldWipe(persisted, current, dbFileExists)` decision (tested by `ShouldWipeTest`). `WipeOnVersionBump` is the pre-launch delete-and-recreate alternative to `.sqm` migrations; the impl persists the version through the host's `KeyValueStore` (so it needs `prefsModule`).
- `ext/SqlDriverFactoryExt.kt` — `databaseSingle(schema, name, upgrade = SchemaUpgrade.None) { driver -> Db(driver) }` (Tier 2.4), an `inline reified` Koin `Module` extension that registers a `single<T>` resolving the `SqlDriverFactory` and forwarding to `create(...)`, replacing the hand-written `single { Db(get<SqlDriverFactory>().create(Db.Schema, "x.db")) }`. The raw long form stays valid.

What moved out at Stage 4:

- `KeyValueStore` + the typed `Preference<T>` layer → **`:data-prefs-api`** (same Kotlin package, `dev.jdgarita.frnk.database`).
- `NoteStore`/`Note` → **`demo/shared`** (`dev.jdgarita.frnk.demo.notes`) — they were demo scaffolding, not toolkit API (OQ-2).

## Rules

- `sqldelight-runtime` is an `api` dep (the `SqlDriverFactory` signature uses `SqlDriver`/`SqlSchema`), but **no `.sq` files and no generated code here** — schemas belong to hosts (and the demo).
- No Koin *bindings* here — driver wiring lives in `:data-db-impl` (`databaseModule`). The only Koin in this module is the `databaseSingle` **DSL helper** (above), which is why `koin-core` is an `api` dep.

## Dependencies

- `api(libs.sqldelight.runtime)` + `api(libs.koin.core)` (the `Module` receiver of `databaseSingle` is in its public signature). The plugin is `frnk.kmp.library.hosttest` for the `databaseSingle` `commonTest` (`DatabaseSingleTest`; run with `./gradlew :data-db-api:testAndroidHostTest`).
