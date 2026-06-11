# :data-db-api

Pure-interface SQL persistence SPI (restructure Stage 4 split — the old `shared-database-api` minus key-value, minus NoteStore). **The toolkit owns no schema**: this module is only the seam a host bridges platform driver creation through.

## Contents

- `SqlDriverFactory.kt` — `fun interface` returning a `SqlDriver` for a host-supplied `SqlSchema` + db name. Bound by `databaseModule` (`:data-db-impl`); consumed by the host's own schema module (`MyDb(factory.create(MyDb.Schema, "my.db"))`) — `demo/shared`'s `demoNotesModule`/`DemoDB` is the worked example (OQ-2).

What moved out at Stage 4:

- `KeyValueStore` + the typed `Preference<T>` layer → **`:data-prefs-api`** (same Kotlin package, `dev.jdgarita.frnk.database`).
- `NoteStore`/`Note` → **`demo/shared`** (`dev.jdgarita.frnk.demo.notes`) — they were demo scaffolding, not toolkit API (OQ-2).

## Rules

- `sqldelight-runtime` is an `api` dep (the `SqlDriverFactory` signature uses `SqlDriver`/`SqlSchema`), but **no `.sq` files and no generated code here** — schemas belong to hosts (and the demo).
- No Koin bindings here — wiring lives in `:data-db-impl` (`databaseModule`).

## Dependencies

- `api(libs.sqldelight.runtime)`. No tests (the SPI is a single `fun interface`); the plugin is plain `frnk.kmp.library`.
