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

## Restructure Stage 6: shared-ui-api split into :core-mvi/:core-nav/:haptics (facade)

- id: restructure-stage-6-shared-ui-api-split-into-core-mvi-core-n-20260611-194525
- type: architecture_decision
- status: active
- platform: kmp
- area: restructure
- date: 2026-06-11

Stage 6 of docs/RESTRUCTURE_PLAN.md landed on main @ 2c90163 (2026-06-11).

- Split :shared-ui-api into three focused modules via git-mv (14 renames, zero Kotlin source edits, packages unchanged per D8):
  - :core-mvi — kept the frnk/core/mvi directory; owns ui/mvi/* (MviContract, MviViewModel) + ui/UiText.kt + MviViewModelTest. deps: api(shared-utils) + coroutines + lifecycle-viewmodel. namespace dev.jdgarita.frnk.core.mvi. Dropped the serialization plugin (no routes here anymore).
  - :core-nav — new frnk/core/nav; owns ui/nav/* (ToolkitRoute, NavBackStackExt, FrnkNavConfig, FrnkPendingRouteRequest, FrnkPrimaryActionRegistry, FrnkFullScreenRoute) + 3 tests. deps: coroutines + serialization-core (+ kotlin-serialization plugin) + navigation3-runtime. Compose-free. namespace dev.jdgarita.frnk.core.nav.
  - :haptics — new frnk/capabilities/haptics; CONTRACT ONLY (HapticType, HapticFeedback, HapticEngine SPI, DefaultHapticFeedback, NoOpHapticFeedback + test). dep: coroutines only. namespace dev.jdgarita.frnk.haptics. The multihaptic engine binding stays in shared-ui-atoms until Stage 7a (explicit guard against pulling it early).
- Facade: :shared-ui-api is now src-less, parked at frnk/core/ui-api-facade (NOT frnk/core/mvi — two Gradle projects can't share a projectDir; this was the key wrinkle). build = api(coreMvi)+api(coreNav)+api(haptics), applying frnk.kmp.library only. Preserves the EXACT transitive classpath shared-ui-api advertised — including the shared-utils export, which now rides through core-mvi's api(shared-utils). Deleted at Stage 9.
- Consumer wiring: shared-ui-atoms KEPT its api(projects.sharedUiApi) on the facade (still-invisibility proof). demo/shared was REQUIRED to re-point both its iOS export(...) and api(...) from sharedUiApi to the three successors — Kotlin/Native export is non-transitive, so exporting the empty facade would carry no MVI/nav/haptics Swift symbols into DemoKit.
- still (host) impact: none — the frnk-shared-ui-api coordinate keeps resolving via the facade (plan §4). still re-points to frnk-core-mvi + frnk-core-nav at Stage 9.
- Verified: clean, compileAndroidMain + demo compileDebugKotlin, testAndroidHostTest + demo testDebugUnitTest (5 new test XMLs across the three modules), compileKotlinIosSimulatorArm64, DemoKit XCFramework, manual phone smoke test by JD (MainActivity + AppScaffoldSmokeActivity boot, tab nav, haptics toggle).

### Files
- settings.gradle.kts
- frnk/core/mvi/build.gradle.kts
- frnk/core/nav/build.gradle.kts
- frnk/capabilities/haptics/build.gradle.kts
- frnk/core/ui-api-facade/build.gradle.kts
- demo/shared/build.gradle.kts

## Restructure Stage 7: shared-ui-atoms split into :ui-theme/:ui-components/:ui-scaffolds (facade)

- id: restructure-stage-7-shared-ui-atoms-split-into-ui-theme-ui-c-20260611-232231
- type: architecture_decision
- status: active
- platform: kmp
- area: restructure
- date: 2026-06-11

Stage 7 of docs/RESTRUCTURE_PLAN.md landed on main @ aa1b592 (2026-06-11), as 3 commits: 7a (3b9a774), 7b (aa90d0f), docs (aa1b592). The largest, highest-risk module split, done via git-mv (packages unchanged per D8) behind the still-invisible facade pattern.

- 7a — extract :ui-theme + move multihaptic engine into :haptics:
  - :ui-theme (frnk/ui/theme) <- ui/theme/* + ui/tokens/* (FrnkTheme/FrnkThemeConfig/FrnkStrings/FrnkIcons/FrnkRipple + tokens). deps: api(haptics) + implementation(shared-utils for applyNativeInterfaceStyle) + api(compose-unstyled-theming) + impl(platformtheme/ripple-indication/icons-lucide). namespace dev.jdgarita.frnk.ui.theme. Applies frnk.kmp.library.compose.
  - :haptics gained the engine binding (FrnkHaptics.kt + MultiHapticEngine.kt) moved down from shared-ui-atoms; now applies frnk.kmp.library.compose + frnk.kmp.library.hosttest and adds impl(lifecycle-runtime-compose + multihaptic-core/-compose). Was contract-only after Stage 6.
  - shared-ui-atoms api()-facaded :ui-theme; dropped theming(now transitive)/platformtheme/ripple/multihaptic + the unused kotlin-serialization plugin.

- 7b — split into :ui-components + :ui-scaffolds:
  - New build-logic convention plugin frnk.kmp.library.composehosttest = frnk.kmp.library.compose + withHostTest{isIncludeAndroidResources=true} + androidHostTest bundle (kotlin-test/coroutines-test/compose-ui-test/ui-test-manifest/robolectric) + commonDebug @Preview source set (dependsOn androidMain + both iOS) with compose-ui-tooling(-preview). Extracted from shared-ui-atoms' hand-written build; applied by both :ui-components and :ui-scaffolds.
  - :ui-components (frnk/ui/components — the dir shared-ui-atoms occupied) <- ui/atoms + ui/molecules + ui/organisms + ui/placeholder. deps: api(uiTheme) + impl(compose-unstyled primitives/button/icon/separators + icons-lucide). namespace dev.jdgarita.frnk.ui.components.
  - :ui-scaffolds (frnk/ui/scaffolds) <- ui/scaffolds + Compose ui/mvi (FrnkMviScreen, EffectCollector) + Compose ui/nav (FrnkNavDisplay, FrnkNavTab, FrnkTabbedBackStacks/-Handler, animations, FrnkPrimaryActionHandler). deps: api(uiComponents + coreMvi + coreNav) + impl(shared-utils) + api(koin-compose/-viewmodel/-navigation3 + lifecycle-runtime-compose + nav3 ui/runtime/viewmodel) + impl(compose-ui-backhandler) + test-only impl(icons-lucide). namespace dev.jdgarita.frnk.ui.scaffolds.
  - shared-ui-atoms reduced to src-less facade re-parked at frnk/ui/atoms-facade (two projects can't share frnk/ui/components), api()-ing uiTheme+uiComponents+uiScaffolds+haptics. Deleted at Stage 9.

Landmines (as built):
- FrnkSectionCard (was internal, shared by organisms in :ui-components AND the Settings scaffold now in :ui-scaffolds) -> PROMOTED TO PUBLIC in :ui-components (not the wrapper option; no duplication).
- PreviewSurface KOTLIN/NATIVE LINK COLLISION: the per-module duplicate preview helper in :ui-scaffolds had to live in its own ...ui.scaffolds.previews package, NOT the original ...ui.atoms.previews — commonDebug feeds iosMain, so an identical signature in two klibs fails the DemoKit link with 'IrSimpleFunctionSymbolImpl is already bound'. The 3 scaffold preview files dropped their now-same-package import. RobolectricComposeTest/setFrnkContent are androidHostTest-only (JVM), so their per-module copy keeps the original ...ui.atoms package with no collision.
- ripple-indication + icons-lucide landed in :ui-theme/:ui-components per ACTUAL usage (theme uses both; components keeps lucide only for the FrnkBottomNavBar @Preview), not the plan's tentative components-only split.

Re-pointing: demo/shared export(...) + api(...) -> the three successors (Kotlin/Native export is non-transitive). shared-monetization-ui -> :ui-scaffolds, so its DIRECT deps are now exactly {ui-scaffolds, monetization-api} (the Stage 8 precondition, verified via gradle dependencies). :shared-ui-nav STAYS on the shared-ui-atoms facade as the still-invisibility proof.

still (host) impact: none — the frnk-shared-ui-atoms coordinate keeps resolving via the facade (plan §4). still re-points to frnk-ui-theme + frnk-ui-components + frnk-ui-scaffolds at Stage 9.

Verified: clean, compileAndroidMain + demo compileDebugKotlin, testAndroidHostTest + demo testDebugUnitTest, compileKotlinIosSimulatorArm64, DemoKit XCFramework, install + launch on Pixel 7a (MainActivity resumed, no crash, full design system renders).

Stages 8 (monetization tidy) and 9 (coordinate flip) NOT started.

### Files
- settings.gradle.kts
- build-logic/src/main/kotlin/frnk.kmp.library.composehosttest.gradle.kts
- frnk/ui/theme/build.gradle.kts
- frnk/ui/components/build.gradle.kts
- frnk/ui/scaffolds/build.gradle.kts
- frnk/ui/atoms-facade/build.gradle.kts
- frnk/capabilities/haptics/build.gradle.kts
- demo/shared/build.gradle.kts
- frnk/capabilities/monetization-ui/build.gradle.kts

## Restructure Stage 8: deleted vestigial ToolkitDiModule; :core-di is sole DI-bootstrap home

- id: restructure-stage-8-deleted-vestigial-toolkitdimodule-core-d-20260611-233920
- type: architecture_decision
- status: active
- platform: kmp
- area: restructure/di
- date: 2026-06-11

Stage 8 (Monetization tidy) landed on main 2026-06-11 (commit 621be26).

Decision: removed the dead `toolkitCoreModules()` expect/actual (`dev.jdgarita.frnk.di.ToolkitDiModule` + .android/.ios actuals) from `:shared-monetization-api`. Both actuals returned `emptyList()`; nothing referenced the symbol outside the 3 deleted files (verified in frnk AND still — still's own host code never called it; the only still hits were inside its stale embedded frnk submodule, which refreshes at Stage 9).

`:core-di` (created Stage 1, OQ-5/OQ-7) is the SOLE host-facing Koin-bootstrap home: `initializeFrnk(modules)` + androidMain `initializeFrnk(context, modules)` (sets `DatabaseContext` + `androidContext`) + fail-fast `requireFrnkKoin()`. No orphan assembly helpers surfaced in Stages 4–7, so nothing was folded in (none invented).

Retained: `api(libs.koin.core)` on `:shared-monetization-api` — still used by `MonetizationModule.kt` (`org.koin.dsl.module`), it was NOT a ToolkitDiModule-only dep.

Re-confirmed: `:shared-monetization-ui` direct deps are exactly `{ui-scaffolds, monetization-api}` (Stage 7 re-point holds).

Note: `dev.jdgarita.frnk.di` is ALSO `:core-di`'s package (`FrnkInitializer`/`DatabaseContext`) — two modules sharing the package is legal; only the monetization-api half was vestigial.

Verified: clean + compileAndroidMain + testAndroidHostTest + compileKotlinIosSimulatorArm64 + DemoKit XCFramework all green; demo installed + launched on Pixel 7a (lynx), MainActivity resumed, no crash.

Next: Stage 9 = the coordinate flip (facade deletes + module renames) — the only still-churn stage, its own session.

### Files
- frnk/capabilities/monetization-api/CLAUDE.md
- frnk/core/di/CLAUDE.md
- docs/RESTRUCTURE_PLAN.md

## Stage 9 — the coordinate flip (frnk side)

- id: stage-9-the-coordinate-flip-frnk-side-20260612-001944
- type: architecture_decision
- status: active
- platform: kmp
- area: restructure
- date: 2026-06-12

-

### Files
- settings.gradle.kts
- frnk/ui/bottom-nav/build.gradle.kts
- frnk/ui/app/build.gradle.kts
- frnk/capabilities/monetization-ui/build.gradle.kts
- frnk/capabilities/monetization-impl/build.gradle.kts
- demo/shared/build.gradle.kts
- demo/android-app/build.gradle.kts
- docs/RESTRUCTURE_PLAN.md
