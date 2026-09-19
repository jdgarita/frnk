# Room Database Seam Implementation Plan

> **For agentic workers:** Use `mobiai-mobile-executing-plans-with-subagents` (recommended) or `mobiai-mobile-executing-plans` to implement this plan task-by-task. Steps use checkbox syntax for tracking.

**Goal:** Make Room KMP the toolkit's one relational persistence seam, replacing SQLDelight, so the blueprint host (Faint, already on Room) builds its database through frnk instead of carrying its own platform builders.

**Architecture:** The toolkit keeps owning **no schema**. `:data-db-api` becomes a Room-shaped seam: `DatabaseFactory` (opens a host-named database under the platform's database directory, honouring `SchemaUpgrade`, applying the toolkit defaults, then building the host's `RoomDatabase`), the `expect inline reified roomDatabaseBuilder<T>(location)` that hides the platform difference in `Room.databaseBuilder` (Android needs a `Context`, iOS does not), and the `databaseSingle<T>(name)` Koin helper. `:data-db-impl` binds `DefaultDatabaseFactory` over the bundled SQLite driver and the platform file operations. Hosts apply the Room and KSP Gradle plugins in the module that owns their entities and DAOs — the toolkit cannot do that for them (its convention plugins are not consumable through the composite build), exactly as it could not apply the SQLDelight plugin for them.

**File locations:** Android `context.getDatabasePath(name)`; iOS `<Application Support>/<name>`. These match what Faint ships today, so its existing on-device vaults are found unchanged.

**Tech Stack:** Kotlin Multiplatform, Room KMP 2.8.4, androidx.sqlite bundled driver 2.7.0, KSP 2.3.11, Koin, Robolectric (host tests)

**Platform:** KMP / Android / iOS

---

### Task 1: Swap the catalog and root plugins

**Files:**
- Modify: `gradle/libs.versions.toml`
- Modify: `build.gradle.kts`
- Modify: `build-logic/src/main/kotlin/frnk.kmp.library.gradle.kts` (comment only)

- [x] **Step 1:** Remove the `sqldelight` version, the five `sqldelight-*` libraries and the `sqldelight` plugin alias.
- [x] **Step 2:** Add `room = "2.8.4"`, `sqlite = "2.7.0"`, `ksp = "2.3.11"`; libraries `androidx-room-runtime`, `androidx-room-compiler`, `androidx-sqlite-bundled`; plugins `androidx-room`, `ksp`.
- [x] **Step 3:** Root `build.gradle.kts`: replace `alias(libs.plugins.sqldelight) apply false` with the Room and KSP aliases, `apply false`, so schema-owning modules share one plugin classloader.

### Task 2: Rewrite `:data-db-api`

**Files:**
- Delete: `frnk/data/db-api/src/commonMain/kotlin/dev/jdgarita/frnk/database/SqlDriverFactory.kt`
- Delete: `.../ext/SqlDriverFactoryExt.kt`, `.../ext/DatabaseSingleTest.kt`
- Create: `.../database/DatabaseFactory.kt`, `.../database/RoomDatabaseBuilder.kt` (expect) + `androidMain`/`iosMain` actuals, `.../ext/DatabaseSingle.kt`
- Modify: `SchemaUpgrade.kt` (KDoc), `build.gradle.kts`, `CLAUDE.md`
- Create: `commonTest/.../ext/DatabaseSingleTest.kt`

- [x] **Step 1:** `DatabaseFactory.open(name, upgrade, builder: (location) -> RoomDatabase.Builder<T>, configure)` plus the reified `DatabaseFactory.open<T>(name, upgrade, configure)` extension that supplies `roomDatabaseBuilder<T>`.
- [x] **Step 2:** `expect inline fun <reified T : RoomDatabase> roomDatabaseBuilder(location: String): RoomDatabase.Builder<T>`; Android actual reads `DatabaseContext.application` (`:core-di`), iOS actual calls `Room.databaseBuilder<T>(location)`.
- [x] **Step 3:** `Module.databaseSingle<T>(name, upgrade, configure)` registering `single { get<DatabaseFactory>().open<T>(...) }`.
- [x] **Step 4:** Dependencies: `api(libs.androidx.room.runtime)`, `api(libs.koin.core)`, `androidMain implementation(projects.coreDi)`.
- [x] **Step 5:** `DatabaseSingleTest`: a recording `DatabaseFactory` fake; assert name/upgrade forwarded and the builder lambda never invoked before the factory runs.

### Task 3: Rewrite `:data-db-impl`

**Files:**
- Modify: `Defaults.kt`, `Defaults.android.kt`, `Defaults.ios.kt`, `DatabaseModule.kt`, `build.gradle.kts`, `CLAUDE.md`

- [x] **Step 1:** `DbPlatform` = `databaseLocation(name)`, `databaseFileExists(name)`, `deleteDatabaseFiles(name)`.
- [x] **Step 2:** `DefaultDatabaseFactory.open`: wipe per `shouldWipe`, `builder(location)`, `.setDriver(BundledSQLiteDriver()).setQueryCoroutineContext(Dispatchers.IO)`, `configure()`, `.build()`, then persist the generation.
- [x] **Step 3:** `databaseModule` binds `DatabaseFactory`; the `KeyValueStore` stays lenient.
- [x] **Step 4:** Dependencies: drop the SQLDelight drivers, add `implementation(libs.androidx.sqlite.bundled)` in `commonMain`.

### Task 4: Move the demo's schema to Room

**Files:**
- Delete: `demo/shared/src/commonMain/sqldelight/**`, `notes/SqlDelightNoteStore.kt`
- Create: `notes/NoteEntity.kt`, `notes/NoteDao.kt`, `notes/DemoDatabase.kt`, `notes/RoomNoteStore.kt`
- Modify: `notes/DemoNotesModule.kt`, `androidHostTest/.../NoteStoreRoundTripTest.kt`, `demo/shared/build.gradle.kts`, `FrnkAppModule.kt` + `DemoApplication.kt` comments

- [x] **Step 1:** Entity/DAO/database mirroring `Note.sq` (newest first, insert returning the id, delete all), `@ConstructedBy` + the `expect object` constructor, `exportSchema = true` into `demo/shared/schemas/`.
- [x] **Step 2:** `RoomNoteStore` keeps the never-throw `AppResult` contract.
- [x] **Step 3:** `demoNotesModule = module { databaseSingle<DemoDatabase>("demo.db"); single<NoteStore> { RoomNoteStore(get()) } }`.
- [x] **Step 4:** Build file: apply `androidx.room` + `ksp`, add `kspAndroid`/`kspIosArm64`/`kspIosSimulatorArm64` compiler deps, `room { schemaDirectory(...) }`; host test gets Robolectric and builds an in-memory database on `AndroidSQLiteDriver`.

### Task 5: Documentation and the brain

**Files:**
- Modify: `README.md`, `CLAUDE.md`, `REQUIREMENTS.md`, `docs/ARCHITECTURE.md`, `docs/HOST_INTEGRATION.md`, the two module `CLAUDE.md`s, KDoc in `DatabaseContext.kt`, `FrnkInitializer*.kt`, `FrnkUiModules.kt`

- [x] **Step 1:** Every SQLDelight mention becomes the Room equivalent; §1 of `HOST_INTEGRATION.md` shows the host's entity module wiring (plugins, KSP, `databaseSingle`).
- [x] **Step 2:** `mobiai brain save decision` recording why Room replaced SQLDelight (blueprint alignment; Faint's on-disk paths preserved).

### Task 6: Verify

- [x] `./gradlew ktlintFormat`
- [x] `./gradlew compileAndroidMain :demo-android:compileDebugKotlin --parallel --build-cache`
- [x] `./gradlew :data-db-api:compileKotlinIosSimulatorArm64 :data-db-impl:compileKotlinIosSimulatorArm64 :demo-shared:compileKotlinIosSimulatorArm64`
- [x] `./gradlew testAndroidHostTest :demo-android:testDebugUnitTest --parallel --build-cache`

### Task 7: Faint (the host side, in the superproject)

- [x] `:core:data` depends on `frnk-data-db-api` in place of Room runtime + bundled driver directly; the Room compiler and plugins resolve from `frnkLibs`.
- [x] Delete `core/data/.../FaintDatabase.android.kt`, `FaintDatabase.ios.kt`, `buildFaintDatabase`, and the two `shared` wrappers.
- [x] `faintDataModule()` includes frnk's `databaseModule` and `databaseSingle<FaintDatabase>("faint.db")`; `faintFrnkModules` / `initializeFaintFrnkIfNeeded` lose their database parameters; `FaintApplication` and `MainViewController` stop building the database.
- [x] `RoomTastingCardRepositoryHostTest` builds its in-memory database inline.
- [x] `app/scripts/verify.sh`, then `--android` and `--ios` compiles; docs updated (`docs/06`, `docs/07`, `ARCHITECTURE.md`, `CONVENTIONS.md`, `app/CLAUDE.md`, root `CLAUDE.md`).
- [ ] Release: frnk PR → tag → Faint gitlink bump to the tag (never a branch tip).
