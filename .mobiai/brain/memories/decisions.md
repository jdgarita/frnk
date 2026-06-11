# Decisions

<!--
Architecture decisions specific to this project.
Append entries with: mobiai brain save decision (coming in Phase 2).
Each entry should record: title, status (active|deprecated), platform,
area, date, decision, reason, files.
-->

## Restructure Stage 4: data split (db vs prefs) + NoteStore demoted to demo scaffolding

- id: restructure-stage-4-data-split-db-vs-prefs-notestore-demoted-20260611-190217
- type: architecture_decision
- status: active
- platform: kmp
- area: persistence
- date: 2026-06-11

### Decision
The old `shared-database-api`/`shared-database-impl` pair split into four modules at their final names (landed 2026-06-11, branch chore/data-split):

- `:data-db-api` — only the `SqlDriverFactory` SPI. **The toolkit owns no SQLDelight schema.**
- `:data-db-impl` — `databaseModule` binds the platform driver factory (AndroidSqliteDriver / NativeSqliteDriver). No SQLDelight Gradle plugin here anymore.
- `:data-prefs-api` — `KeyValueStore` + typed `Preference<T>` (pure stdlib, dependency-free).
- `:data-prefs-impl` — `prefsModule` binds `SettingsKeyValueStore` (multiplatform-settings).

Kotlin packages unchanged (`dev.jdgarita.frnk.database(.impl)`) per restructure decision D8.

### Reason
- OQ-2: `NoteStore`/`Note.sq`/`SqlDelightNoteStore` were demo scaffolding, not toolkit API → moved to `demo/shared` as `dev.jdgarita.frnk.demo.notes` with a demo-owned `DemoDB` schema (`dev.jdgarita.frnk.demo.sql`, configured inline in demo/shared/build.gradle.kts), consuming `SqlDriverFactory` exactly like a real host. `androidDemoApp` overrides the in-memory `FakeNoteStore` with the real path (`databaseModule` + `demoNotesModule`); DemoKit/iOS keeps the fake so the framework stays SQLite-driver-free.
- `DatabaseContext` (Android Context seam) re-homed to `:core-di` androidMain (`dev.jdgarita.frnk.di`) because BOTH split impls need it and they must not depend on each other; the old `core-di → shared-database-impl` dep inverted to `data-*-impl → core-di`.
- `monetization-api` depends only on `:data-prefs-api` (god-mode persistence is key-value only).
- Hosts install `databaseModule` + `prefsModule` independently; what isn't passed never enters the Koin graph.
- The `FrnkDB` constants were deleted from buildSrc/ProjectConfiguration.kt.

### Files
- frnk/data/db-api
- frnk/data/db-impl
- frnk/data/prefs-api
- frnk/data/prefs-impl
- demo/shared
- frnk/core/di

## Restructure Stage 5: :shared:backend:* re-flattened to :analytics-api/:analytics-impl (OQ-6)

- id: restructure-stage-5-shared-backend-re-flattened-to-analytics-20260611-192015
- type: architecture_decision
- status: active
- platform: kmp
- area: restructure
- date: 2026-06-11

Stage 5 of docs/RESTRUCTURE_PLAN.md landed on main @ 6bff47d (2026-06-11).

- Renamed :shared:backend:api → :analytics-api and :shared:backend:firebase → :analytics-impl. Settings-only change: include() entries + projectDir remaps folded into the main map; the :shared / :shared:backend build-file-less hull projects and their parked projectDirs are gone. Directories were already final since Stage 3 (frnk/capabilities/analytics-{api,impl}).
- OQ-6 fold: the frnk.backend.{api,impl,firebase} convention plugins in build-logic were deleted. Both modules now apply the standard frnk.kmp.library.hosttest (+ alias(libs.plugins.kotlin.serialization) on the impl) and declare inline: namespaces dev.jdgarita.frnk.backend.api / .backend.firebase (unchanged), api deps (shared-utils + coroutines + koin-core on the api; analytics-api + firebase firestore/analytics/crashlytics on the impl), the androidMain Firebase BOM platform dep, and the iosMain CrashKiOS dep.
- Typesafe accessors re-pointed: projects.shared.backend.api → projects.analyticsApi (monetization-api, ui-app, demo/shared incl. the DemoKit export list), projects.shared.backend.firebase → projects.analyticsImpl (demo/android-app).
- Kotlin packages did NOT change (plan D8): dev.jdgarita.frnk.backend(.firebase) stay; Koin module names unchanged (firebaseBackendModule, firebaseObservabilityModule, noopObservabilityModule). New artifact coordinates: dev.jdgarita.frnk:analytics-api / :analytics-impl.
- still (host) impact: none — its 5 consumed coordinates never included the backend pair (plan §4).
- Verified: clean, compileAndroidMain + demo compileDebugKotlin, testAndroidHostTest + demo testDebugUnitTest, compileKotlinIosSimulatorArm64 (covers the CrashKiOS iosMain code Linux CI never compiles), DemoKit XCFramework, manual phone smoke test by JD.

### Files
- settings.gradle.kts
- frnk/capabilities/analytics-api/build.gradle.kts
- frnk/capabilities/analytics-impl/build.gradle.kts
