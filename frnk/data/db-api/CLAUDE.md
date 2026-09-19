# :data-db-api

The Room persistence seam (Room replaced SQLDelight here on 2026-09-18 — `docs/plans/2026-09-18-room-database-seam.md`). **The toolkit owns no schema**: this module is only what a host cannot write in `commonMain` to open its own `@Database`.

## Contents

- `DatabaseFactory.kt` — `interface DatabaseFactory { fun <T : RoomDatabase> open(name, upgrade, builder: (location) -> RoomDatabase.Builder<T>, configure) : T }` plus the reified `DatabaseFactory.open<T>(name, upgrade, configure)` extension that supplies the platform builder. Bound by `databaseModule` (`:data-db-impl`); consumed by the host's own entity module — `demo/shared`'s `demoNotesModule`/`DemoDatabase` is the worked example (OQ-2). The member overload takes the builder as a lambda so a test fake never has to produce a real `RoomDatabase`.
- `RoomDatabaseBuilder.kt` (+ `androidMain`/`iosMain` actuals) — `expect inline fun <reified T : RoomDatabase> roomDatabaseBuilder(location): RoomDatabase.Builder<T>`. The one line of Room a host cannot write in common code: `Room.databaseBuilder` takes a `Context` on Android (read from `:core-di`'s `DatabaseContext`) and only a name on iOS. Room's builder constructor is internal, so this has to be reified — a `KClass` seam cannot reach it.
- `SchemaUpgrade.kt` — `sealed interface SchemaUpgrade` (`None` | `WipeOnVersionBump(version)`) + the pure `shouldWipe(persisted, current, dbFileExists)` decision (tested by `ShouldWipeTest`). `WipeOnVersionBump` is the pre-launch delete-and-recreate alternative to writing migrations; the impl persists the version through the host's `KeyValueStore` (so it needs `prefsModule`). `None` leaves the schema to Room's own migrations (or `fallbackToDestructiveMigration`, passed through `configure`).
- `ext/DatabaseSingle.kt` — `databaseSingle<T>(name, upgrade = SchemaUpgrade.None, configure = {})`, an `inline reified` Koin `Module` extension that registers a `single<T>` resolving the `DatabaseFactory` and forwarding to `open(...)`, replacing the hand-written `single { get<DatabaseFactory>().open<Db>("x.db") }`. The raw long form stays valid.

What moved out at Stage 4:

- `KeyValueStore` + the typed `Preference<T>` layer → **`:data-prefs-api`** (same Kotlin package, `dev.jdgarita.frnk.database`).
- `NoteStore`/`Note` → **`demo/shared`** (`dev.jdgarita.frnk.demo.notes`) — they were demo scaffolding, not toolkit API (OQ-2).

## Rules

- `room-runtime` is an `api` dep (the `DatabaseFactory` signature uses `RoomDatabase`/`RoomDatabase.Builder`), but **no entities, no DAOs, no `@Database` and no KSP here** — schemas belong to hosts (and the demo), which apply the Room + KSP plugins themselves. Room's runtime is pure Kotlin on every target, so exporting this module into a host's umbrella framework adds no native cinterop; the bundled SQLite driver stays in `:data-db-impl`.
- No Koin *bindings* here — factory wiring lives in `:data-db-impl` (`databaseModule`). The only Koin in this module is the `databaseSingle` **DSL helper** (above), which is why `koin-core` is an `api` dep.

## Dependencies

- `api(libs.androidx.room.runtime)` + `api(libs.koin.core)` (the `Module` receiver of `databaseSingle` is in its public signature); `androidMain` `implementation(projects.coreDi)` for `DatabaseContext`. The plugin is `frnk.kmp.library.hosttest` for the `commonTest`s (`DatabaseSingleTest`, `ShouldWipeTest`; run with `./gradlew :data-db-api:testAndroidHostTest`).
