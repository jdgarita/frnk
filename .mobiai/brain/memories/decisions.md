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

## Stage 10 — demo project renames (:shared-demo→:demo-shared, :androidDemoApp→:demo-android)

- id: stage-10-demo-project-renames-shared-demo-demo-shared-androi-20260612-012657
- type: architecture_decision
- status: active
- platform: shared
- area: restructure
- date: 2026-06-12

Final rename stage of the restructure. Pure Gradle project-name / typesafe-accessor / task-prefix churn — zero Kotlin logic changes (dirs already moved at Stage 3).

Renamed the two demo Gradle projects to their final flat names:
- :shared-demo → :demo-shared (dir demo/shared, unchanged); accessor projects.sharedDemo → projects.demoShared (one consumer: demo/android-app).
- :androidDemoApp → :demo-android (dir demo/android-app, unchanged); no accessor consumers.

Touch points: settings.gradle.kts include + projectDir remap; the Xcode Run Script build phase (demo/ios-app, :shared-demo:assembleDemoKit{Debug,Release}XCFramework → :demo-shared:...); CI task prefixes (:androidDemoApp: → :demo-android:, task names compileDebugKotlin/testDebugUnitTest unchanged since it stays a com.android.application). DemoKit baseName unchanged → framework stays DemoKit.xcframework at demo/shared/build/XCFrameworks/...; Xcode fileRefs + FRAMEWORK_SEARCH_PATHS are dir-based on demo/shared (unmoved) so untouched.

NOT touched: Android applicationId (dev.jdgarita.frnk.demo), module namespaces (…demo.shared), iOS bundle id. Device launch stays adb … dev.jdgarita.frnk.demo/.MainActivity.

still impact: NONE — the demo projects are internal smoke harnesses, not consumed coordinates; no still change, no submodule bump.

Verified: frnk standalone (clean + compileAndroidMain :demo-android: + testAndroidHostTest :demo-android: + compileKotlinIosSimulatorArm64 + :demo-shared:assembleDemoKitDebugXCFramework, all green); Android device Pixel 7a (MainActivity DemoScreen + AppScaffoldSmokeActivity :ui-app chain render clean, no FATAL/Koin); iOS simulator build runs the renamed Run Script + embeds DemoKit.framework (user verified manually). Landed on main as c3cc2b9 on 2026-06-11.

Remaining: Stage 11 (additive :camera/:permissions + Remote Config pair) and Stage 12 (docs consistency sweep — finishes the broad prose rename Stage 10 deferred).

### Files
- settings.gradle.kts
- demo/android-app/build.gradle.kts
- demo/ios-app/iosDemoApp.xcodeproj/project.pbxproj
- .github/workflows/main.yml

## CI paused on private repo — only release tag job + on-demand @claude

- id: ci-paused-on-private-repo-only-release-tag-job-on-demand-cla-20260612-014745
- type: architecture_decision
- status: active
- platform: shared
- area: ci_cd
- date: 2026-06-12

### Decision
While frnk is private (foundation phase), build/test CI is **off** to stop burning free-tier GitHub Actions minutes (the per-push `compile & test` job was hitting the spending limit on the private repo).

Removed: `.github/workflows/main.yml` (compile & test on every push/PR) and `.github/workflows/claude-code-review.yml` (auto PR review).

Kept (cheap, rare triggers):
- `release.yml` — fires only on a `v*` tag push; verifies `Frnk.VERSION` matches the tag, extracts the CHANGELOG section, creates the GitHub Release. This is the one intentionally-kept CI job for tagging releases (0.1.0, 1.0.0, ...).
- `claude.yml` — on-demand `@claude` assistant; only consumes minutes when explicitly mentioned.

### Reason
Free GitHub accounts have limited Actions minutes on private repos. frnk stays private during foundation work, so the heavy per-push pipeline is not worth the spend. Validation is done locally instead (pre-push gradle gate + pre-commit ktlint hook).

### Reverting (when foundation is done + repo goes public)
Public repos get unlimited Actions minutes. At that point:
1. Make the repo public.
2. Re-add branch protection on `main`.
3. Enable PRs.
4. Restore the `compile & test` job (old `main.yml`) and optionally `claude-code-review.yml`.

### Local validation (replaces the dropped CI gate)
- `./gradlew compileAndroidMain :demo-android:compileDebugKotlin --parallel --build-cache`
- `./gradlew testAndroidHostTest :demo-android:testDebugUnitTest --parallel --build-cache`

### Files
- .github/workflows/release.yml
- .github/workflows/claude.yml
- CLAUDE.md
- README.md
- docs/ARCHITECTURE.md
- docs/RELEASING.md

## Stage 11 — Remote Config capability pair + :camera/:permissions scaffolds

- id: stage-11-remote-config-capability-pair-camera-permissions-sc-20260612-022500
- type: architecture_decision
- status: active
- platform: shared
- area: capabilities
- date: 2026-06-12

Stage 11 of the frnk restructure (additive, low risk — landed 2026-06-11). still impact: NONE (still is a *future* candidate consumer of Remote Config, not wired here).

### What
Four new capability modules under frnk/capabilities/, all on the frnk.kmp.library.hosttest convention plugin:
- :remote-config-api — RemoteConfigService: read-only typed key→value (getString/Boolean/Long/Double(key, default)) + suspend fetchAndActivate(): AppResult<Unit, CommonError>. NoopRemoteConfig default + noopRemoteConfigModule. Sibling of :analytics-*, kept SEPARATE from it (OQ-1).
- :remote-config-impl — FirebaseRemoteConfigService over gitlive dev.gitlive:firebase-config (Firebase.remoteConfig). Honours the per-call default when a key resolves to ValueSource.Static; blank strings fall back too. fetchAndActivate maps failures to AppResult.Failure(CommonError.Unknown), rethrows CancellationException. Exposed as remoteConfigModule. Catalog: added firebase-config (gitlive 2.4.0 ref), removed now-dead firebase-firestore.
- :camera — api-only SCAFFOLD: CameraController.capturePhoto(): AppResult<CameraImage, CommonError> + NoopCameraController (returns Failure) + cameraModule. No impl, no native cinterop (future feature work).
- :permissions — api-only SCAFFOLD: PermissionController.status/request(Permission): PermissionStatus + Noop (NotDetermined/Denied) + permissionsModule.

### Reshape / deletions (no real consumers — confirmed)
Deleted the three Firestore-shaped stubs: RemoteData.kt (:analytics-api), FirestoreRemoteData.kt + firebaseBackendModule (:analytics-impl). Removed the orphaned firestore dep + kotlin-serialization plugin from :analytics-impl. The only repo-wide mention was a DemoModule.kt doc comment (fixed).

### api/impl discipline (the one risk on an additive stage)
:remote-config-impl pulls a native Firebase SDK → installed by the HOST via initializeFrnk(modules=…), resolved through Koin; kept OUT of :demo-shared common (DemoKit exports only the *-api). RemoteConfigService returns AppResult from :shared-utils, so no sibling *-api→*-api dep.

### Demo (all three layers)
:demo-shared installs the three no-op modules + a "Capabilities (Stage 11)" Home section (Remote welcome value + camera/permission rows + Fetch/Capture/Request buttons), DemoViewModel injects the three; :demo-android overrides remoteConfigModule with the REAL Firebase impl (verified on Pixel 7a — logcat shows FirebaseRemoteConfig queried; falls back to bundled default since no demo_welcome_message param is set in the frnk-demo console).

### Shape decision
Confirmed against still's live Android Remote Config usage (RemoteConfig.getString(RemoteConfigKey), key→string, fetch-and-activate). The recommended typed shape is a clean superset — still can adapt its RemoteConfigKey enum onto getString(key.key, key.default).

### Verified
frnk standalone from clean: compileAndroidMain + :demo-android:compileDebugKotlin; full testAndroidHostTest + the three new-module tests; compileKotlinIosSimulatorArm64; :demo-shared:assembleDemoKitDebugXCFramework (proves no firebase-config cinterop leak into DemoKit common). Android device: MainActivity + AppScaffoldSmokeActivity, no Koin/FATAL errors. iOS simulator not run (no pbxproj change; XCFramework build is the substitute gate).

Remaining: Stage 12 (docs/CI consistency sweep — full ARCHITECTURE module-graph rewrite + remaining stale-name scrub).

### Files
- frnk/capabilities/remote-config-api
- frnk/capabilities/remote-config-impl
- frnk/capabilities/camera
- frnk/capabilities/permissions
- settings.gradle.kts
- gradle/libs.versions.toml

## Restructure Stage 12 — CI + docs consistency sweep (FINAL stage; restructure complete)

- id: restructure-stage-12-ci-docs-consistency-sweep-final-stage-r-20260612-025115
- type: architecture_decision
- status: active
- platform: kmp
- area: docs/restructure
- date: 2026-06-12

-

### Files
- docs/ARCHITECTURE.md
- docs/HOST_INTEGRATION.md
- docs/RESTRUCTURE_PLAN.md
- CLAUDE.md
- README.md
- REQUIREMENTS.md

## Foundational technical decisions (the 'why' behind frnk's stack)

- id: foundational-technical-decisions-the-why-behind-frnk-s-stack-20260612-030643
- type: architecture_decision
- status: active
- platform: kmp
- area: architecture
- date: 2026-06-12

The load-bearing rationale for frnk's foundational choices (migrated out of REQUIREMENTS.md §6 so the spec stays lean; REQUIREMENTS.md now points here). Each is a deliberate, defensible call — reversible ones are flagged.

### compose-unstyled over Material3
Material3 ships an opinionated Google look that fights customization; its `MaterialTheme` color/type system is the contract you must deviate from. `compose-unstyled` is headless — the design system IS ours from line one, our own token axes (`colors`/`textStyles`/`shapes`/`strings`/`icons`) are the contract, and granular artifacts keep binary weight down. Strict rule: zero Material in any module except `:ui-bottom-nav` (see the adaptive-bottom-nav decision).

### KMP + Compose Multiplatform
One shared codebase for logic AND UI across Android + iOS. The toolkit's whole value is amortizing cross-cutting concerns once; KMP/CMP is the only stack that shares architecture + rendered UI while still allowing per-platform escapes via expect/actual.

### api/impl module split
Each SDK-backed domain splits into `*-api` (pure interface, no SDK) + `*-impl` (concrete Koin-bound binding). Benefits: parallel Gradle compilation (api builds before any impl), faster incremental builds (touching an impl doesn't invalidate api consumers), swap-ability (change the installed Koin module, not a recompile), test isolation (fakes live in api consumers' test sources, never import a real SDK).

### No aggregator (restructure Stage 1, OQ-7)
The old `:shared` aggregator bundled every api+impl behind one coordinate and selected capabilities with enums. Deleted: hosts now depend on exactly the modules they use and pass an explicit Koin module list to `initializeFrnk(...)` — what isn't passed never enters the graph. "One call" ergonomics survive in `initializeFrnk(modules)` + `FrnkAppScaffold`.

### Koin (not compile-time DI)
KMP-friendly, no annotation processing across targets, and runtime module composition is exactly what the explicit-module-list bootstrap needs (install only what the host passes).

### Runtime capability selection (module list, not enum)
Capabilities are installed by passing their Koin module to `initializeFrnk(...)`; domain code resolves only `*-api` interfaces, so providers swap without recompiling features, and what a host doesn't install never ships in its graph.

### Composite build over published artifacts
Live edits (change toolkit source, rebuild consumer, no publish cycle), atomic cross-repo refactors in one commit, no registry overhead while private. Reversible: Maven coordinates are kept stable so flipping to published artifacts later is non-breaking.

### MVI engine with no Compose in :core-mvi
Keeps the presentation contract (`UiState`/`UiIntent`/`UiEffect`, `MviViewModel`) compilable + unit-testable without `compose.runtime`, so the engine is reusable and testable in isolation. Same reasoning splits `:core-nav` Compose-free.

### iOS dynamic_lookup linker option
`:monetization-impl` (RevenueCat) and `:analytics-impl` (Firebase) cinterop native iOS SDKs the toolkit does NOT ship. An umbrella XCFramework bundling them uses `linkerOpts("-undefined","dynamic_lookup")` to defer symbol resolution so it links locally; the consumer's Xcode project resolves the native SDKs at integration time (DemoKit does this for its CrashKiOS + RevenueCat cinterops). See [[adaptive-bottom-nav-calf-material3-decision]].

### Files
- REQUIREMENTS.md
- docs/ARCHITECTURE.md

## Adaptive bottom nav: Calf + Material3 accepted toolkit-wide, isolated to :ui-bottom-nav

- id: adaptive-bottom-nav-calf-material3-accepted-toolkit-wide-iso-20260612-030713
- type: architecture_decision
- status: deprecated
- platform: kmp
- area: ui_navigation
- date: 2026-06-12

Outcome of the spike/adaptive-bottom-nav evaluation (distilled from the now-deleted docs/spikes/adaptive-bottom-nav.md). Decided & shipped 2026-06-02.

### Decision
After A/B-ing four candidates live in the demo (floating Pill / Haze frosted bar / hand-rolled native UITabBar interop / Calf), the maintainer chose **Calf's AdaptiveNavigationBar for both platforms** — a genuine native UIKit `UITabBar` on iOS and a **Material3 NavigationBar on Android** (themed from `FrnkTheme` tokens, not Material defaults).

**Material3 is accepted as a toolkit-wide dependency for this one feature** (the maintainer's explicit call), traded for a single maintained adaptive component. It is **isolated to `:ui-bottom-nav`** (the SOLE Material3 module); `:ui-app` inherits it transitively as the accepted batteries-included trade. Every other module — `:ui-theme`/`:ui-components`/`:ui-scaffolds` and below — stays `compose-unstyled`-only. For a Material-free bar, use `FrnkBottomNavBar` (the pill) in `:ui-components`.

### Rejected
- **Haze** (frosted-glass modifier, no Material3): a faithful Compose imitation, not a real UITabBar; alpha dependency at the time; HazeState crosses the atom-scaffold-host boundary. Dropped once Calf-for-both was chosen.
- **Hand-rolled native UITabBar interop** (the no-Material3 answer to Calf): real UITabBar without Material3, but UIKit-drawn (ignores FrnkTheme/haptics), and collapse/haptics would need bridging. Declined in favour of Calf's maintained component.
- **adaptive-nav-bar (narendraanjana09)** as a Calf replacement with a built-in primary-action FAB: evaluated in a later POC, rejected — see the bugfix entry on the AGP9 Compose-resources packaging blocker, plus the iOS-26 FAB snap (unanimatable native overlay).

### Reason
One maintained library covers both platforms with a genuine iOS feel; the cost (Material3 on Android) is bounded to a single named module and does not leak into the design system. Calf is pure Kotlin/Compose (no extra native cinterop), so the XCFramework still links under dynamic_lookup.

### Files
- frnk/ui/bottom-nav/build.gradle.kts

## Open work: PostHog analytics tracker (planned, not yet implemented)

- id: open-work-posthog-analytics-tracker-planned-not-yet-implemen-20260612-031749
- type: architecture_decision
- status: active
- platform: kmp
- area: analytics
- date: 2026-06-12

OPEN / PLANNED — not yet built. Folded in from the deleted BACKLOG.md so the toolkit's remaining open work stays in the brain (search "open work"). The only other open item, the OnboardingScreen nav3 button bug, is recorded as a bugfix entry.

### What
Add a **PostHog** `AnalyticsTracker` implementation as the provider-neutral analytics option named in REQUIREMENTS.md §3.6. Firebase analytics/crash already ship; PostHog rounds out analytics without coupling product analytics to a backend choice.

### Scope (decide + record the decision when built)
Likely a small new impl module (e.g. `:analytics-posthog`) or an addition under the backend-agnostic analytics path. Keep the `*-api` SDK-free; bind PostHog via its own Koin module, installable independently of the data backend.

### Acceptance criteria
- [ ] `AnalyticsTracker` implemented against PostHog; bound via its own Koin module; installable independently of the data backend.
- [ ] `noopObservabilityModule` remains the safe default.
- [ ] Demoed (an event fired from `DemoScreen`, visible in logs/fake) across the three demo layers.

### Convention reminders
Respect REQUIREMENTS §2 invariants + the strict UI rules §4; must pass `compileAndroidMain` + `testAndroidHostTest` (KMP) / the demo app's `testDebugUnitTest`; pre-commit `ktlintFormat` leaves the tree clean. A feature isn't done until exercised in `:demo-shared`, `demo-android`, and `iosDemoApp` (or a written justification).

## Adaptive bottom nav: adaptive-nav-bar is the default; Calf removed entirely

- id: adaptive-bottom-nav-adaptive-nav-bar-is-the-default-calf-rem-20260612-033949
- type: architecture_decision
- status: active
- platform: kmp
- area: ui-bottom-nav
- date: 2026-06-12

**Decision.** The platform-adaptive bottom bar is now `narendraanjana09/adaptive-nav-bar`
(`io.github.narendraanjana09:adaptive-nav-bar` 1.0.1) as the **sole** engine. **Calf
(`com.mohamedrejeb.calf:calf-ui` 0.12.0) was removed entirely** — the `FrnkAdaptiveNavEngine`
runtime A/B enum, the Calf bar (`FrnkAdaptiveBottomNavBar`), and the Calf-only index scaffold
(`FrnkAdaptiveBottomNavScaffold` + `rememberFrnkBottomNavState`) were deleted. `engine` params
dropped from `FrnkTabbedNavScaffold`/`FrnkAppShell`/`FrnkAppScaffold`; `FrnkAdaptiveNavTab` lost
its Calf-only `icon: ImageVector`. Material3 stays — adaptive-nav-bar needs it too — so the
"sole Material3 module = :ui-bottom-nav" rule is unchanged.

**Why.** User directive (acting as Principal KMP). adaptive-nav-bar gives a native glassy
iOS-26 `UITabBar` + a built-in primary-action FAB, which the toolkit had already wired
(screen-routed via `FrnkPrimaryActionRegistry`). Keeping two engines + Calf was dead weight
once adaptive-nav-bar won.

**⚠️ Accepted trade — Android icon packaging (still live, verified 2026-06-11).** adaptive-nav-bar
takes `DrawableResource` icons; under AGP 9.2.1 `com.android.kotlin.multiplatform.library` + CMP
1.11.1 those **do NOT package into the Android APK from a KMP library module**
(`prepareComposeResourcesTaskForAndroidMain` is NO-SOURCE; copy task fails). Empirically confirmed:
removed the demo's asset workaround, built `:demo-android` (success), unzipped APK → zero
`frnk_nav_*` drawables → runtime `MissingResourceException`. This is exactly why Calf (ImageVector,
no compose-resources) had been the default. **Chosen resolution (user picked "ship host-asset
requirement"):** every Android host must copy the 3 nav drawables into its *application* module at
`src/main/assets/composeResources/dev.jdgarita.frnk.ui.bottomnav.generated.resources/drawable/frnk_nav_{home,settings,primary_action}.xml`.
Documented as a required step in `docs/HOST_INTEGRATION.md §8.1`; demo keeps the workaround in
`demo/android-app/src/main/assets/composeResources/`. Escape hatch: the Material-free
`FrnkBottomNavBar` pill (`:ui-components`, ImageVector) has no such requirement.

**Validated:** `compileAndroidMain` + `:demo-android:compileDebugKotlin` + `testAndroidHostTest`
+ `:demo-android:testDebugUnitTest` + `ktlintCheck` all green.

Supersedes [[adaptive-bottom-nav-calf-material3-accepted-toolkit-wide-iso-20260612-030713]].

### Files
- frnk/ui/bottom-nav/build.gradle.kts
- frnk/ui/bottom-nav/src/commonMain/kotlin/dev/jdgarita/frnk/ui/bottomnav/FrnkAdaptiveNavBarBottomBar.kt
- frnk/ui/bottom-nav/src/commonMain/kotlin/dev/jdgarita/frnk/ui/bottomnav/FrnkTabbedNavScaffold.kt
- docs/HOST_INTEGRATION.md

## Deleted buildSrc; group id moved to libs.versions.toml (frnk-groupId)

- id: deleted-buildsrc-group-id-moved-to-libs-versions-toml-frnk-g-20260612-041547
- type: architecture_decision
- status: active
- platform: kmp
- area: build/gradle
- date: 2026-06-12

**What:** `buildSrc` held a single constant — `ProjectConfiguration.GROUP_ID = "dev.jdgarita.frnk"` — used by ~25 module build scripts for their `namespace`/`applicationId`. The `build-logic` included build (convention plugins) **cannot see buildSrc**, so `frnk.kmp.base.gradle.kts` hard-coded the same group string with a "keep in sync" comment — a real duplication.

**Decision:** Moved the group id into `gradle/libs.versions.toml` as the `[versions]` entry **`frnk-groupId`** and **deleted `buildSrc` entirely**.
- Module build scripts now read `libs.versions.frnk.groupId.get()`.
- `frnk.kmp.base` reads `versionCatalog.findVersion("frnk-groupId").get().requiredVersion`.

**Why:** The version catalog is the ONE source visible to **both** the `build-logic` included build and the module build scripts; buildSrc was visible only to the latter — exactly why the group id had to be duplicated. This finishes the migration already done for the SDK levels (`android-min/compile/targetSdk` live only in the catalog for the same reason). `build-logic` is now the single home for shared build logic.

**Direction matters:** We collapsed `buildSrc → catalog/build-logic`, NOT `plugins → buildSrc`. Moving the convention plugins into buildSrc would re-introduce the documented `alias(...) apply false` classpath-leak clash and was explicitly rejected.

**Validation:** `./gradlew compileAndroidMain --parallel --build-cache` → BUILD SUCCESSFUL. The resolved group string is byte-identical to before (pure sourcing change). Docs updated: CLAUDE.md, README.md, REQUIREMENTS.md.

**Gotcha for future edits:** do the build-script swap with `sed`/literal tools or a perl pattern that escapes `$` (`\$\{...\}`) — an unescaped perl `s/${ProjectConfiguration.GROUP_ID}/.../` interpolates the `${...}` as an empty var and corrupts every file.

### Files
- gradle/libs.versions.toml
- build-logic/src/main/kotlin/frnk.kmp.base.gradle.kts

## ui-bottom-nav: FrnkBottomNavBar expect/actual — Material3 Expressive on Android, adaptive-nav-bar on iOS, ImageVector API

- id: ui-bottom-nav-frnkbottomnavbar-expect-actual-material3-expre-20260612-171806
- type: architecture_decision
- status: active
- platform: kmp
- area: ui-bottom-nav
- date: 2026-06-12

Renamed FrnkAdaptiveNavBarBottomBar -> FrnkBottomNavBar and split it into an expect/actual composable so each platform renders with its own engine and Material3 never leaves Android.

WHY / decisions (all confirmed with the user):
- Android engine swapped from adaptive-nav-bar's Material3 NavigationBar to a Material3 *Expressive* HorizontalFloatingToolbar (floating pill), drawing ImageVector icons directly via IconButton+Icon; the primary action is the toolbar's docked FAB slot (deleted the old separately-docked FrnkAdaptiveNavBarPrimaryActionFab + the getPlatform() branch in FrnkTabbedNavScaffold).
- iOS keeps narendraanjana09 adaptive-nav-bar (native glassy UITabBar). The native bar renders SF-Symbols and CANNOT consume a Compose ImageVector — that is why the common item keeps an iosSystemIcon: String alongside icon: ImageVector. DrawableResource was removed from the public API.
- Common item is FrnkNavBarItem (key, icon: ImageVector, iosSystemIcon, label); tab is FrnkAdaptiveNavTab (now icon: ImageVector + iosSystemIcon); FrnkNavPrimaryAction now icon: ImageVector (default = new iconNavAdd theme token = Lucide.Plus). Named FrnkNavBarItem/FrnkNavBarDefaults (not FrnkBottomNavItem) to avoid clashing with the existing Material-free pill atom FrnkBottomNavBar/FrnkBottomNavItem in :ui-components.

VERIFIED GOTCHAS:
- HorizontalFloatingToolbar is NOT in the material3 that CMP 1.11.1 resolves (1.9.0 ships only FloatingToolbar TOKENS). It first appears in material3 1.10.0-alpha05 (@ExperimentalMaterial3ExpressiveApi). Pinned as catalog 'compose-material3-expressive = 1.10.0-alpha05', applied ONLY in :ui-bottom-nav androidMain (overrides the plugin's 1.9.0). Drop the override when CMP's bundled material3 catches up.
- adaptive-nav-bar's NavigationItem.icon / IosFabItem.icon are non-null DrawableResource. Since the API no longer carries one, the iOS actual feeds the library a single bundled placeholder commonMain/composeResources/drawable/frnk_nav_placeholder.xml (only shown on the older-iOS Compose fallback). The Compose-resources plugin generates the Res accessor into commonMain regardless of the file's source set, so compose.components.resources MUST be a commonMain dependency (putting it in iosMain breaks the generated commonMain Res.kt -> 'Unresolved reference Res' on iOS).

FREE WIN: Android no longer reads any DrawableResource, so the old AGP-9 host requirement (ship the toolkit nav drawables as raw assets under demo/android-app/.../assets/composeResources) is GONE — that shim + its README were deleted, and HOST_INTEGRATION §8.1 rewritten.

Material3 quarantine preserved: split by source set (material3 expressive in androidMain, adaptive-nav-bar in iosMain) but all in :ui-bottom-nav/build.gradle.kts. Gates: compileAndroidMain + :demo-android:compileDebugKotlin, :demo-shared:assembleDemoKitDebugXCFramework, testAndroidHostTest — all green.

### Files
- frnk/ui/bottom-nav/src/commonMain/kotlin/dev/jdgarita/frnk/ui/bottomnav/FrnkBottomNavBar.kt
- frnk/ui/bottom-nav/src/androidMain/kotlin/dev/jdgarita/frnk/ui/bottomnav/FrnkBottomNavBar.android.kt
- frnk/ui/bottom-nav/src/iosMain/kotlin/dev/jdgarita/frnk/ui/bottomnav/FrnkBottomNavBar.ios.kt
- frnk/ui/bottom-nav/build.gradle.kts

## Adaptive bottom bar = FrnkBottomFloatingBar (atom pill keeps FrnkBottomNavBar)

- id: adaptive-bottom-bar-frnkbottomfloatingbar-atom-pill-keeps-fr-20260612-233651
- type: architecture_decision
- status: active
- platform: kmp
- area: ui-bottom-nav
- date: 2026-06-12

The platform-adaptive bottom bar in :ui-bottom-nav (expect/actual: Material3 *Expressive* HorizontalFloatingToolbar on Android, native glassy UITabBar on iOS) was renamed **FrnkBottomNavBar -> FrnkBottomFloatingBar** (composable + the 3 files). The separate Material-free pill atom in :ui-components (package ui.atoms) **keeps** the name FrnkBottomNavBar.

**Why:** the two components previously shared the name FrnkBottomNavBar, disambiguated only by package — confusing. Renaming the adaptive bar disambiguates them by name: adaptive bar = FrnkBottomFloatingBar, design-system pill = FrnkBottomNavBar.

**How to apply:** the demo Components catalog entry 'FrnkBottomFloatingBar' now renders the *real* adaptive bar (the same expect/actual component shown at the foot of every screen via FrnkTabbedNavScaffold/FrnkAppShell) using FrnkNavBarItem, NOT the atom pill — so the showcase matches the live bar on both Android & iOS. Supporting types (FrnkNavBarItem, FrnkNavBarDefaults, FrnkNavPrimaryAction) kept their names. The atom pill is still consumed by the index-based BottomNavScaffold.

### Files
- frnk/ui/bottom-nav/src/commonMain/kotlin/dev/jdgarita/frnk/ui/bottomnav/FrnkBottomFloatingBar.kt
- demo/shared/src/commonMain/kotlin/dev/jdgarita/frnk/demo/DemoScreen.kt

## Bottom-nav primary action is a centered bar item (Mode B), not a FAB

- id: bottom-nav-primary-action-is-a-centered-bar-item-mode-b-not-20260612-233702
- type: architecture_decision
- status: active
- platform: kmp
- area: ui-bottom-nav
- date: 2026-06-12

FrnkTabbedNavScaffold injects the primary action as a **permanent centered '+' item** in FrnkBottomFloatingBar (internal PRIMARY_ACTION_KEY, inserted at the middle of the tabs), rather than a docked FAB (Android) / inline IosFabItem (iOS). The bar therefore stays full-width/centered and identical on both platforms — no FAB, no narrowing slide.

**Why:** with the FAB approach, the native iOS UITabBar **snapped with zero animation** when the FAB appeared/disappeared and the bar shifted narrow<->full-width; the public UITabBar API can't be driven to animate that slide or to control the Home-vs-other-screen safe-area reservation (proven on-sim). Mode B sidesteps it entirely. Chosen over Mode A (animated FAB) after an on-device A/B in the demo's Bottom Nav Lab.

**How to apply:** the bar's primaryAction/onPrimaryAction params still exist (NavLab Mode A compares the FAB path; direct callers may opt in), but the scaffold + FrnkAppShell use Mode B everywhere. hideBarFor (defaults to FrnkFullScreenRoute) + FrnkNavBarDefaults.reservedHeight are retained so content clears the bar. On Android the no-FAB toolbar overload is used (plain HorizontalFloatingToolbar) so the pill stays horizontally centered, with expandedShadowElevation pinned to the WithFab value for shadow parity; animateContentSize() was removed because it clipped the pill's bottom-edge shadow.

### Files
- frnk/ui/bottom-nav/src/commonMain/kotlin/dev/jdgarita/frnk/ui/bottomnav/FrnkTabbedNavScaffold.kt

## iOS adaptive bottom bar vendored into ui.bottomnav.vendor

- id: ios-adaptive-bottom-bar-vendored-into-ui-bottomnav-vendor-20260612-233712
- type: architecture_decision
- status: active
- platform: ios
- area: ui-bottom-nav
- date: 2026-06-12

The iOS FrnkBottomFloatingBar no longer depends on the io.github.narendraanjana09:adaptive-nav-bar artifact. Its source is **vendored** under frnk/ui/bottom-nav/src/iosMain/.../vendor/ (AdaptiveNavigationBar + AdaptiveNavBarModels), adapted by the toolkit.

**Why:** the published library builds the whole UITabBar in its UIKitView *factory* and exposes no Compose-animatable hook, so frnk couldn't own the bar's slide/fade transitions (it had to wrap the view in key(...), destroying/recreating it on every FAB/theme change -> snap). Vendoring gives full control: the vendored bar splits factory/update so FAB presence + theme re-apply in place (no recreate). (Mode B means the app flow no longer shows a FAB anyway, but the vendored bar animates for direct/NavLab use.)

**How to apply (build):** dropped libs.adaptive.nav.bar + its version-catalog entry; iosMain now implements libs.compose.material3.expressive (Material3 is needed ONLY by the vendored older-iOS ComposeNavigationBar fallback — previously transitive from the library). Still quarantined to this one module; pure Kotlin/Compose (no extra native cinterop), so umbrella XCFrameworks still link under dynamic_lookup. The vendored fallback still reads the bundled frnk_nav_placeholder drawable. Upstream repo has no LICENSE — attribution headers were added to the vendored files.

### Files
- frnk/ui/bottom-nav/src/iosMain/kotlin/dev/jdgarita/frnk/ui/bottomnav/vendor/AdaptiveNavigationBar.kt
- frnk/ui/bottom-nav/build.gradle.kts

## First-launch onboarding + FrnkFullScreenScaffold

- id: first-launch-onboarding-frnkfullscreenscaffold-20260615-032640
- type: architecture_decision
- status: active
- platform: shared
- area: ui/scaffolds + app-root
- date: 2026-06-15

A pasted spec asked for a parallel app-root (AppRoute / FrnkRootRouter / FrnkTabNavScaffold / first-launch). Most of it already existed (ToolkitRoute, FrnkScreenScaffold, FrnkTabbedNavScaffold, FrnkAppShell/FrnkAppScaffold). Decision: **extend the existing system**, do NOT build a parallel route/router/tab-scaffold (would duplicate + drift + name-collide). Skipped a Material-free nav3 tab scaffold for now.

Two genuinely-new pieces shipped:
1. **FrnkFullScreenScaffold** (:ui-scaffolds) — immersive chrome template (close ✕ via WindowInsets.safeDrawing + container + BoxScope content), mirroring FrnkScreenScaffold. **No sealed Skeleton/Error state** despite the original plan: scaffold chrome owns no skeleton (HOST_INTEGRATION §9; FrnkScreenScaffold has no state either) — host content owns loading visuals. OnboardingScreen + PaywallScreen were refactored onto it (dropped their hand-rolled close buttons).
2. **First-launch onboarding gating** — OnboardingGate (KeyValueStore-backed Preference<Boolean>, key "frnk.onboarding.seen") + rememberOnboardingGate() in :ui-scaffolds (needed api(projects.dataPrefsApi) — pure stdlib, XCFramework-clean), and FrnkFirstLaunchOnboardingEffect in :ui-bottom-nav (where FrnkAppScope lives). FrnkAppScaffold wires it automatically (new showOnboardingOnFirstLaunch=true param, lenient gate resolution like EntitlementManager); the demo (on the bare FrnkAppShell, cant see :ui-app) opts into the same helper in its effects slot.

**Mechanics that made it small:** FrnkAppShell already pops Onboarding-close back to its pusher, so pushing ToolkitRoute.Onboarding over the Home tab on first launch gives close→Home for free; Settings→Onboarding→close→Settings unchanged. **Semantics:** mark-seen ON PRESENT (not on completion) so a mid-onboarding kill donot re-show; flippable by calling gate.markSeen() from a completion handler instead.

**Demo caveat:** demo uses FakeKeyValueStore (in-memory, per-session), so every COLD start replays first-launch — intended. Cross-launch persistence is covered by OnboardingGateTest + real disk-backed prefsModule on hosts. Verified on Android emulator: first-launch onboarding, close→Home, paywall-from-Home full-screen chrome, paywall close→Home; iOS validated at DemoKit XCFramework link level.

### Files
- frnk/ui/scaffolds/src/commonMain/kotlin/dev/jdgarita/frnk/ui/scaffolds/FrnkFullScreenScaffold.kt
- frnk/ui/scaffolds/src/commonMain/kotlin/dev/jdgarita/frnk/ui/scaffolds/OnboardingGate.kt
- frnk/ui/bottom-nav/src/commonMain/kotlin/dev/jdgarita/frnk/ui/app/FrnkFirstLaunchOnboardingEffect.kt
- frnk/ui/app/src/commonMain/kotlin/dev/jdgarita/frnk/ui/app/FrnkAppScaffold.kt

## FrnkFullScreenScaffold owns merged content padding (code-review hardening)

- id: frnkfullscreenscaffold-owns-merged-content-padding-code-revi-20260615-034600
- type: architecture_decision
- status: active
- platform: shared
- area: ui/scaffolds
- date: 2026-06-15

Follow-up to [first-launch-onboarding]. A high-effort code review surfaced 6 findings; all addressed:

1. (confirmed) Paywall HeadlineSmall title could render under the overlaid close ✕ (no reserved top band, unconstrained title width). FIX + altitude generalization: FrnkFullScreenScaffold now takes `contentPadding: PaddingValues` and its `content` slot is `BoxScope.(PaddingValues) -> Unit` — the scaffold folds safe-drawing insets + a reserved close-button band (FrnkFullScreenScaffoldDefaults.CloseButtonHeight = 48.dp) + the caller padding into ONE merged PaddingValues handed to content (mirrors how FrnkScreenScaffold hands merged padding). Onboarding + Paywall apply that padding; the ✕ can never collide with content and a scroll list scrolls UNDER the pinned ✕.
2. (#5) Paywall bottom inset was outside verticalScroll (fixed dead strip). FIX: padding is now applied INSIDE the scroll via PaywallScreenContent(contentPadding=padding) — scroll-with-content restored.
3. (#2) markSeen() persisted BEFORE navigate. FIX: navigate first, then mark.
4. (#3) First-launch silently never showed when host had onboardingPages but no KeyValueStore. FIX: FrnkFirstLaunchOnboardingEffect falls back to a rememberSaveable session flag (shows once per app run) when gate==null.
5. (#4) OnboardingGate exposed a public ctor naming KeyValueStore. FIX: ctor is now `internal` → KeyValueStore no longer in :ui-scaffolds public API → downgraded api(dataPrefsApi) to implementation(dataPrefsApi).
6. (#6, latent) FrnkFirstLaunchOnboardingEffect enabled flag is decoupled from whether ToolkitRoute.Onboarding is registered — navigating to an unregistered route throws from nav3 NavDisplay. No in-tree crash (callers tie enabled to onboardingPages.isNotEmpty()); KDoc now states the precondition loudly.

Verified on Android emulator: paywall header now sits below the ✕; onboarding still centered/clears bars. Full compileAndroidMain + ktlintCheck + testAndroidHostTest + DemoKit XCFramework all green.

### Files
- frnk/ui/scaffolds/src/commonMain/kotlin/dev/jdgarita/frnk/ui/scaffolds/FrnkFullScreenScaffold.kt
- frnk/ui/bottom-nav/src/commonMain/kotlin/dev/jdgarita/frnk/ui/app/FrnkFirstLaunchOnboardingEffect.kt

## FrnkBottomFloatingBar is the sole bottom-nav bar; FrnkBottomNavBar + BottomNavScaffold removed

- id: frnkbottomfloatingbar-is-the-sole-bottom-nav-bar-frnkbottomn-20260615-162823
- type: architecture_decision
- status: active
- platform: shared
- area: ui-bottom-nav
- date: 2026-06-15

Decision (2026-06-15): The toolkit now has ONE bottom-nav bar — `FrnkBottomFloatingBar` (`:ui-bottom-nav`, the expect/actual Material3 HorizontalFloatingToolbar on Android / vendored glassy UITabBar on iOS), reached via `FrnkTabbedNavScaffold` / `FrnkAppShell` / `FrnkAppScaffold`.

We DELETED the duplicate Material-free `FrnkBottomNavBar` pill atom (`:ui-components` `ui/atoms/`) and its entire index-based `BottomNavScaffold` family (`BottomNavScaffold`, `BottomNavScaffoldState`, `BottomNavViewModel`, `BottomNavScaffoldModule`, `BottomNavDefaults`, `bottomNavScaffoldModule`) plus the `FrnkBottomNavBarPreviews`.

Why: an audit (asked to evaluate the two bars) found the pill + BottomNavScaffold had ZERO host/demo usage — all three demo layers (`:demo-shared` via `FrnkAppShell`, `:demo-android` MainActivity + AppScaffoldSmokeActivity, `demo/ios-app` via DemoKit) render `FrnkBottomFloatingBar` only. They were unvalidated public surface shipping in the XCFramework. User chose removal over adding a demo showcase.

Wiring changes: removed `bottomNavScaffoldModule` from `frnkUiModules()` (`:ui-app`), its assertion in `FrnkUiModulesTest`, and the `includes(bottomNavScaffoldModule)` in the demo's `DemoModule`.

KEPT: `frnkBottomSystemBarInset()` in `:ui-components` `BottomNavInsets.kt` — `FrnkBottomFloatingBar` imports it for its `FrnkNavBarDefaults.reservedHeight`. Also kept the `icons-lucide` dep in `:ui-components` (still used by `FrnkTopAppBarTest`).

Verified: `./gradlew compileAndroidMain :demo-android:compileDebugKotlin` + `:ui-app:testAndroidHostTest` all green. Logged as a Breaking entry under CHANGELOG [Unreleased] > Removed. This supersedes any earlier 'two bars (adaptive vs Material-free pill)' framing in the docs.

### Files
- frnk/ui/bottom-nav/src/commonMain/kotlin/dev/jdgarita/frnk/ui/bottomnav/FrnkBottomFloatingBar.kt
- frnk/ui/components/src/commonMain/kotlin/dev/jdgarita/frnk/ui/atoms/BottomNavInsets.kt

## Bottom bar: animated-FAB mode removed; Mode B centered item is the sole behaviour

- id: bottom-bar-animated-fab-mode-removed-mode-b-centered-item-is-20260615-165043
- type: architecture_decision
- status: active
- platform: shared
- area: ui-bottom-nav
- date: 2026-06-15

The adaptive bar carried two primary-action behaviours, A/B-compared in a demo NavLab screen: Mode (1) an animated FAB (Material3 docked FAB on Android; iOS glass UIVisualEffectView FAB fading/scaling in while the native UITabBar slid narrow<->full-width), and Mode (2) a permanent centered "+" item injected into the bar.

Decision (user, 2026-06-15): drop the animated-FAB mode entirely and ship Mode B (centered item) only. Production already used only Mode B — FrnkTabbedNavScaffold/FrnkAppShell/FrnkAppScaffold inject the centered item and call the bar WITHOUT primaryAction/onPrimaryAction; the FAB path had exactly one caller (the NavLab harness).

Removed end-to-end:
- FrnkBottomFloatingBar expect/actual lost the primaryAction + onPrimaryAction params (signature is now items/selectedIndex/onItemSelected/modifier).
- Android: deleted the docked-FAB HorizontalFloatingToolbar(floatingActionButton=...) branch; kept only the plain overload, still pinning expandedShadowElevation = ContainerExpandedElevationWithFab for shadow parity.
- iOS vendored AdaptiveNavigationBar: deleted the glass FAB (buildFab/FabHandler/existingFab), the narrow<->full slide geometry (targetTabBarFrame, narrowWidth, fab/tabItem metrics), BAR_ANIM_DURATION, IosBarState.lastFabPresent, and the IosFabItem model. Bar is now full-width + static; the UIKitView split factory/update is kept ONLY for in-place selection sync + theme-colour re-apply (still the reason it stays vendored).
- Demo NavLab removed wholesale: NavLabScreen.kt, DemoRoute.NavLab, NAV_LAB_ROUTE_KEY + dispatch, all navLab* VM state/intents/reducers, route registration + entry + the 'Open Bottom Nav Lab' button.

FrnkNavPrimaryAction / rememberFrnkNavPrimaryAction are KEPT — they describe the centered '+' item (icon/label/iosSystemIcon), not a FAB.

Verified: compileAndroidMain + iosSimulatorArm64 for ui-bottom-nav & demo-shared, demo-android compile, testAndroidHostTest + demo-android unit tests, ktlintFormat all green; ran demo-android on device — centered '+' shows on Home (4 items), drops to 3 items on tabs with no claim, tab switching works.

### Files
- frnk/ui/bottom-nav/src/commonMain/kotlin/dev/jdgarita/frnk/ui/bottomnav/FrnkBottomFloatingBar.kt
- frnk/ui/bottom-nav/src/androidMain/kotlin/dev/jdgarita/frnk/ui/bottomnav/FrnkBottomFloatingBar.android.kt
- frnk/ui/bottom-nav/src/iosMain/kotlin/dev/jdgarita/frnk/ui/bottomnav/FrnkBottomFloatingBar.ios.kt
- frnk/ui/bottom-nav/src/iosMain/kotlin/dev/jdgarita/frnk/ui/bottomnav/vendor/AdaptiveNavigationBar.kt

## Bottom bar: fixed 3-tab Home·feature·Settings (primary-action machinery removed)

- id: bottom-bar-fixed-3-tab-home-feature-settings-primary-action-20260615-172658
- type: architecture_decision
- status: active
- platform: kmp
- area: ui-bottom-nav
- date: 2026-06-15

**Decision.** `FrnkBottomFloatingBar` (via `FrnkTabbedNavScaffold` / `FrnkAppShell`) is now a **fixed three-tab** bar — `Home · feature · Settings` — shown on every screen. The center **"feature"** tab is the only host-configurable slot (`FrnkFeatureItem`: route + label + icon:ImageVector + iosSystemIcon). It is a real navigable tab (own back stack, selection highlight, re-tap-to-root). Home/Settings are toolkit-owned bookends built from theme tokens via `rememberFrnkBottomNavState(homeRoot, settingsRoot, feature)` → `FrnkBottomNavState` (internal ctor; only `feature` settable).

**Why.** Previously the bar showed a variable item count: tabs (Home + middleTabs + Settings) plus a dynamically-injected centered primary-action "+" item (Mode B) that only appeared when a screen claimed it — so Home had 4 items but other tabs had 3. The user wanted a stable, always-3-item bar where the middle slot is the host's app-specific entry point (typically an "add"/New-X/camera surface). Modeling it as a navigable tab (not a transient action) matched the existing multiple-back-stack scaffold and the demo's Components tab with the least disruption.

**What was removed (reverses the earlier "Mode B / keep FrnkNavPrimaryAction" decisions).** The entire dynamic primary-action mechanism: `FrnkPrimaryActionRegistry` (:core-nav), `FrnkPrimaryActionHandler` + `LocalFrnkPrimaryActionRegistry` (:ui-scaffolds), `FrnkNavPrimaryAction`/`rememberFrnkNavPrimaryAction` + the Mode-B injection/`PRIMARY_ACTION_KEY` in `FrnkTabbedNavScaffold`, `FrnkTabbedNavScaffold`'s `primaryAction`/`onPrimaryAction`/`primaryActionRegistry` params, `FrnkAppScope.primaryActions`, `FrnkAppShell`/`FrnkAppScaffold`'s `primaryAction`/`onPrimaryAction`/`homePrimaryActionEnabled`, `HomeScreenState.primaryActionEnabled` + `HomeIntent.PrimaryActionClicked` + `HomeEffect.PrimaryActionInvoked`, and the now-unused `iconNavAdd`/`stringPrimaryAction` theme tokens. `rememberFrnkAdaptiveNavTabs(middleTabs=…)` → replaced by `rememberFrnkBottomNavState(feature=…)`.

**Host contract.** The feature tab's root is host content — register `entry(feature.route) { … }` in `entries` (the shell owns only Home/Settings/Onboarding). `feature` is a required, non-defaulted param (a default route would have no registered destination and crash nav3). `FrnkBottomFloatingBar`'s expect/actual signature was unchanged (already list-based: items/selectedIndex/onItemSelected). Demo wires feature → DemoRoute.Components.

**Review refinements (same change).** (1) `rememberFrnkBottomNavState` now `require(...)`s `feature.key != homeKey && != settingsKey` — colliding keys would otherwise give duplicate back-stack keys + wrong `indexOfFirst` selection silently. (2) `demo-android`'s `AppScaffoldSmokeActivity` declares its **own** harness-local `@Serializable data object SmokeFeatureRoute : NavKey` instead of borrowing `:demo-shared`'s `DemoRoute.Components` — this required applying `alias(libs.plugins.kotlin.serialization)` to `:demo-android`, which **works fine alongside AGP 9's built-in Kotlin** (same mechanism as the already-applied `kotlin.compose` compiler plugin; built-in Kotlin does accept Kotlin compiler plugins). (3) Kept `FrnkFeatureItem` (host-facing config, `route`) separate from `FrnkAdaptiveNavTab` (internal tab, `root`) — deliberate, mirrors the existing `FrnkNavTab`/`FrnkAdaptiveNavTab` "presentation sibling" precedent.

### Files
- frnk/ui/bottom-nav/src/commonMain/kotlin/dev/jdgarita/frnk/ui/bottomnav/FrnkBottomNavState.kt
- frnk/ui/bottom-nav/src/commonMain/kotlin/dev/jdgarita/frnk/ui/bottomnav/FrnkFeatureItem.kt
- frnk/ui/bottom-nav/src/commonMain/kotlin/dev/jdgarita/frnk/ui/bottomnav/FrnkAdaptiveNavDefaults.kt
- frnk/ui/bottom-nav/src/commonMain/kotlin/dev/jdgarita/frnk/ui/bottomnav/FrnkTabbedNavScaffold.kt

## Merged FrnkAppShell into FrnkTabbedNavScaffold (one public tabbed-app composable)

- id: merged-frnkappshell-into-frnktabbednavscaffold-one-public-ta-20260616-152854
- type: architecture_decision
- status: active
- platform: shared
- area: ui-bottom-nav / app shell
- date: 2026-06-16

Decision (user, 2026-06-16): collapse the FrnkAppShell + FrnkTabbedNavScaffold two-layer split into ONE public composable. FrnkAppShell (the opinionated app shell in :ui-bottom-nav ui/app/) is DELETED; FrnkTabbedNavScaffold (ui/bottomnav/) now carries FrnkAppShell's exact signature/body (FrnkTheme wrap + frnkNavConfiguration + rememberFrnkBottomNavState fixed Home·feature·Settings + rememberFrnkTabbedBackStacks + FrnkAppScope + deep-link + built-in Home/Settings/Onboarding + the FrnkAppSettingsTab/rememberShellSettingsHandler private helpers).

This REVERSES the prior 'keep separate' evaluation. Why reverse: FrnkAppShell was the ONLY in-repo caller of FrnkTabbedNavScaffold and the generic host-owned-state form had ZERO external callers, so the public split earned nothing day-to-day; user wanted one component in charge.

How it stays clean: the old generic FrnkTabbedNavScaffold(tabbed, tabs, hideBarFor, entryProvider) body is preserved VERBATIM as a private 'TabbedNavHost' helper in the same file (bar overlay + LocalFrnkBottomBarInset + FrnkTabbedBackHandler + FrnkNavDisplay). So zero behaviour change — pure surface/relocation refactor. Dropped the koinEntryProvider() default + @OptIn(KoinExperimentalAPI) since the private core is always handed an explicit provider.

What was traded (accepted): the generic, theme-agnostic, host-owns-state PUBLIC scaffold is gone. A host wanting a custom tab shape or a Material3-free bar now drops to the raw primitives (rememberFrnkTabbedBackStacks + FrnkNavDisplay + FrnkTabbedBackHandler + own bar). The surviving name FrnkTabbedNavScaffold now over-promises (it's really an app shell) — rename + de-publishing FrnkAppScope were explicitly left as OPTIONAL follow-ups, not done.

Migration for callers: rename FrnkAppShell(...) -> FrnkTabbedNavScaffold(...) (identical args); import moves dev.jdgarita.frnk.ui.app.FrnkAppShell -> dev.jdgarita.frnk.ui.bottomnav.FrnkTabbedNavScaffold. Callers rewired: FrnkAppScaffold (:ui-app), DemoScreen (:demo-shared). :ui-app compile surface unchanged (already api-deps :ui-bottom-nav).

Follow-up (same change, addressing code-review feedback): (1) the relocated private helpers were renamed off the dead 'shell' vocabulary — rememberShellSettingsHandler -> rememberDefaultSettingsHandler, FrnkAppSettingsTab -> TabbedNavSettingsTab. (2) FrnkAppScope + FrnkFirstLaunchOnboardingEffect MOVED from package dev.jdgarita.frnk.ui.app -> dev.jdgarita.frnk.ui.bottomnav (and physically from ui/app/ into ui/bottomnav/), deleting the module's now-empty separate ui/app package so :ui-bottom-nav is single-package. FrnkAppScaffold (:ui-app, package …ui.app) lost same-package access and now imports both from …ui.bottomnav; DemoScreen's imports updated likewise. Behaviour unchanged.

### Files
- frnk/ui/bottom-nav/src/commonMain/kotlin/dev/jdgarita/frnk/ui/bottomnav/FrnkTabbedNavScaffold.kt
- frnk/ui/bottom-nav/src/commonMain/kotlin/dev/jdgarita/frnk/ui/bottomnav/FrnkAppScope.kt (moved from ui/app/)
- frnk/ui/bottom-nav/src/commonMain/kotlin/dev/jdgarita/frnk/ui/bottomnav/FrnkFirstLaunchOnboardingEffect.kt (moved from ui/app/)
- frnk/ui/bottom-nav/src/commonMain/kotlin/dev/jdgarita/frnk/ui/app/FrnkAppShell.kt (deleted)
- frnk/ui/app/src/commonMain/kotlin/dev/jdgarita/frnk/ui/app/FrnkAppScaffold.kt
- demo/shared/src/commonMain/kotlin/dev/jdgarita/frnk/demo/DemoScreen.kt

## FrnkAppScaffold/FrnkTabbedNavScaffold take a FrnkAppConfig/FrnkTabbedNavConfig bundle

- id: frnkappscaffold-frnktabbednavscaffold-take-a-frnkappconfig-f-20260616-171324
- type: architecture_decision
- status: active
- platform: shared
- area: ui-app / ui-bottom-nav public API
- date: 2026-06-16

The two public app-root composables had grown to 21 (FrnkAppScaffold) / 24 (FrnkTabbedNavScaffold) flat parameters. We bundled the **data-shaped** params into one `@Immutable` config, grouped into a `*Config` sub-bundle per feature area.

**Naming convention (the rule to keep):** `*Config` = host-supplied input declared once and rarely changing (FrnkAppConfig, FrnkThemeConfig). `*State`/`*ViewState` = state the toolkit owns and mutates at runtime (MVI UiState, FrnkTabbedNavViewState). Don't name host config `*State` even though "it's Compose" — `*State` carries runtime-mutable/observable connotations and collides with MVI. Chose `Config` over `State` after the user (correctly) noted host config doesn't change at runtime.

**Types.** `:ui-bottom-nav`: `FrnkTabbedNavConfig(app, nav, theme, home, settings, onboarding)` over sub-configs `FrnkAppInfo(name,version)`, `FrnkNavConfig(feature, hostRoutes, hideBarFor, homeRoot, settingsRoot)`, `FrnkHomeConfig(topBar, vmKey)`, `FrnkSettingsConfig(extraSections, vmKey)`, `FrnkOnboardingConfig(pages, showOnFirstLaunch)` (reuses `FrnkThemeConfig`). `:ui-app`: `FrnkAppConfig(...same six... + monetization: FrnkMonetizationConfig(paywallFeatures))` — the batteries superset; `monetization` can't live in `:ui-bottom-nav`. `FrnkAppConfig.toTabbedNavConfig()` (internal) projects the shared six down; FrnkAppScaffold then `.copy()`s in the app-name Home top bar + the `frnk-settings-$isPro` Settings VM key.

**What stayed parameters (deliberate):** `@Composable` slots (homeContent, entries, effects, settingsState/settingsEffects factories) and event callbacks (onMessage, onHomeEffect) — storing `@Composable` lambdas in an `@Immutable` data class breaks its stability contract and fights the toolkit's slot-API convention. Runtime controllers `appearanceController` (@Stable, mutable) and `pendingRoutes` (FrnkPendingRouteRequest, StateFlow-backed signal) also stayed params — they're not immutable config. Net: FrnkAppScaffold 21→9 params, FrnkTabbedNavScaffold 24→10.

**`showOnboardingOnFirstLaunch`** folded into `FrnkOnboardingConfig.showOnFirstLaunch` (shared) — still meaningful at the bare-scaffold layer: a direct FrnkTabbedNavScaffold host (the demo) reads it to gate its own FrnkFirstLaunchOnboardingEffect.

**Internal bar/tab model (same change):** the WIP `FrnkBottomNavTab` (sealed, Home/Feature/Settings subtypes) **superseded** the `FrnkAdaptiveNavTab` data class (deleted, hard-replace, no alias) so the fixed three-tab shape is typed; `FrnkBottomNavState.tabs` is now `List<FrnkBottomNavTab>`. The WIP `FrnkTabbedNavViewState(navBarItems, navBarItemIndexSelected)` now holds the bar's derived render state inside the private TabbedNavHost (was loose locals).

Verified: ktlintFormat + compileAndroidMain + demo-android compile, testAndroidHostTest + demo-android tests, DemoKit iOS XCFramework link, and ran demo-android on device — FrnkAppScaffold smoke + DemoScreen (onboarding/home/three-tab bar) both render.

### Files
- frnk/ui/app/src/commonMain/kotlin/dev/jdgarita/frnk/ui/app/FrnkAppConfig.kt
- frnk/ui/bottom-nav/src/commonMain/kotlin/dev/jdgarita/frnk/ui/bottomnav/FrnkTabbedNavConfig.kt
- frnk/ui/app/src/commonMain/kotlin/dev/jdgarita/frnk/ui/app/FrnkAppScaffold.kt
- frnk/ui/bottom-nav/src/commonMain/kotlin/dev/jdgarita/frnk/ui/bottomnav/FrnkTabbedNavScaffold.kt

## Extension functions live in their own ext/<Type>Ext.kt file

- id: extension-functions-live-in-their-own-ext-type-ext-kt-file-20260616-180244
- type: architecture_decision
- status: active
- platform: kmp
- area: code organization convention
- date: 2026-06-16

**Convention (forward-looking):** extension functions go in their **own file** under an `ext/` subpackage of the owning module — never appended to the target type's declaration file.

- Package: `dev.jdgarita.frnk.<module>.ext` (e.g. `dev.jdgarita.frnk.ui.app.ext`).
- File name: `<Type>Ext.kt` — extensions on `FrnkAppConfig` → `ext/FrnkAppConfigExt.kt`.
- Worked example: `FrnkAppConfig.toTabbedNavConfig()` (the FrnkAppConfig → FrnkTabbedNavConfig projection) was moved out of `FrnkAppConfig.kt` into `frnk/ui/app/src/commonMain/kotlin/dev/jdgarita/frnk/ui/app/ext/FrnkAppConfigExt.kt`.

**Why:** keeps data-class / type files declaration-only, and makes extensions discoverable in one predictable place per type.

**Scope:** applies to every NEW extension function. Not a mandate to mass-migrate existing inline extensions. Also recorded as a bullet in the repo root `CLAUDE.md` "Conventions to follow when adding code".

### Files
- frnk/ui/app/src/commonMain/kotlin/dev/jdgarita/frnk/ui/app/ext/FrnkAppConfigExt.kt

## Prioritize Hoisted State over Internal State for ViewStates/AppStates

- id: prioritize-hoisted-state-over-internal-state-for-viewstates-20260616-191659
- type: architecture_decision
- status: active
- platform: kmp
- area: state_management
- date: 2026-06-16

### Decision
For all ViewStates and AppStates, prioritize **Hoisted State** over Internal State.

Screen / navigation / business state lives in the feature's `MviViewModel` (`*State` + `*Intent`), exposed via StateFlow and hoisted up. Composables stay stateless: they read from the collected `state` and emit `onIntent(...)`. AppState/config is likewise declared once by the host as an `@Immutable` bundle (`*Config`) and passed down.

Do **not** hold screen/nav/business state as `var ... by remember { mutableStateOf(...) }` inside a composable. Reserve `remember`/`rememberSaveable` only for genuinely-local, transient UI holders (scroll, pager, focus, animation state, and `remember`-derived builders).

### Reason
- Testability: reducers and state transitions are unit-testable in `commonMain` without a composition.
- Predictability & single source of truth: state changes flow through the MVI reducer (`setState { copy(...) }`), not scattered local mutations.
- KMP/Compose correctness: stateless composables recompose deterministically; `@Immutable` config bundles keep params stable.
- Consistency: the toolkit's atoms, molecules, organisms, and scaffolds already follow this; new code must match them.

### Scope
Applies project-wide to ViewStates and AppStates going forward — Principal-level default for this KMP/Compose toolkit.

## Reactive Settings/Home VM config-sync (SyncMviConfig + *Intent.ConfigChanged); drop isPro vmKey re-keying

- id: reactive-settings-home-vm-config-sync-syncmviconfig-intent-c-20260616-204432
- type: architecture_decision
- status: active
- platform: kmp
- area: state_management
- date: 2026-06-16

The Settings and Home scaffolds keep a single persistent `MviViewModel` and **react** to host-recomputed state via a `ConfigChanged` intent, instead of re-creating the VM by folding `isPro` into its Koin `vmKey`.

### How
- `SyncMviConfig(viewModel, config, asIntent)` (`:ui-scaffolds` `ui/mvi/`) is the one helper that feeds a recomputed `config` into a persistent VM. Seeded once via `parametersOf`; it skips the seed pass (`state.value === config`) and dispatches `asIntent(config)` on every later structurally-distinct config. Keys structurally — the toolkit state types are `@Immutable data class`es, so a freshly-built-but-equal config is a no-op.
- `SettingsIntent.ConfigChanged` -> `mergedWith(incoming)`: adopts the incoming structure (Free/Pro subscription rows, titles, footer, and the **theme from the appearance controller**) while preserving VM-owned interaction state (toggle `checked` matched by row id; version-tap dev-reveal). `HomeIntent.ConfigChanged` -> wholesale replace, since `HomeScreenState` is pure chrome.
- `FrnkAppScaffold` no longer re-keys the Settings VM (`"frnk-settings-$isPro"` removed); the demo dropped `home-$isPro` and `settings-$isPro-$isGodMode`. `vmKey` stays a host escape hatch (force a fresh VM), kept symmetric across all four scaffolds (Settings/Home/Onboarding/Paywall).

### Reason
Re-keying threw away the entire VM on every `isPro` flip — resetting in-VM state seeded by value (notifications toggle, god-mode reveal progress) and accumulating stale VM instances in the nav entry's `ViewModelStore` (retained until the entry is disposed; `onCleared` deferred). The reactive merge keeps one VM, preserves interaction state, and removes the instance accumulation. The `state.value !== config` reference guard is correct because the state types are data classes and `parametersOf` seeds the exact object on first composition.

### Tradeoffs / notes
- **Theme is taken from `incoming`, not preserved.** Appearance has a single source of truth (the host's appearance controller, which `incoming` is built from), so an appearance change from any path is reflected; the toggle's optimistic feedback comes from the `ThemeSelected` reducer, not the merge.
- **1-frame stale render** on a *live, in-place* `isPro` flip is accepted (inherent to `collectAsStateWithLifecycle` + an async intent channel); a screen (re)entered after the flip seeds correctly and never shows the old config. Documented in `SyncMviConfig` KDoc.
- `mergedWith` hard-codes which fields are VM-owned; a contract comment at the `incoming.copy(...)` site guards future additions (a new user-mutated field must be preserved there or it resets on every `ConfigChanged`).

See also [[prioritize-hoisted-state-over-internal-state-for-viewstates]].

### Files
- frnk/ui/scaffolds/src/commonMain/kotlin/dev/jdgarita/frnk/ui/mvi/SyncMviConfig.kt
- frnk/ui/scaffolds/src/commonMain/kotlin/dev/jdgarita/frnk/ui/scaffolds/SettingsViewModel.kt
- frnk/ui/scaffolds/src/commonMain/kotlin/dev/jdgarita/frnk/ui/scaffolds/SettingsScreen.kt
- frnk/ui/scaffolds/src/commonMain/kotlin/dev/jdgarita/frnk/ui/scaffolds/HomeScreen.kt
- frnk/ui/app/src/commonMain/kotlin/dev/jdgarita/frnk/ui/app/FrnkAppScaffold.kt

## Scaffolds split into per-feature subpackages; *Content renderers kept internal

- id: scaffolds-split-into-per-feature-subpackages-content-rendere-20260616-210920
- type: architecture_decision
- status: active
- platform: kmp
- area: ui-scaffolds
- date: 2026-06-16

The flat `dev.jdgarita.frnk.ui.scaffolds` package was reorganized into per-feature subpackages — `.home`, `.onboarding`, `.settings` — to group each scaffold's Screen/State/ViewModel/ScaffoldModule together as the set grows. Cross-cutting templates (FrnkScreenScaffold, FrnkFullScreenScaffold, FrnkBottomBarInset, FeedbackEmailLauncher) and the shared Onboarding* types stay in the root `ui.scaffolds` package. Settings reducer helpers (mergedWith/withTheme/withToggle/mapRows) were extracted out of SettingsViewModel.kt into `settings/ext/SettingsScreenStateExt.kt` (ext-function-own-file convention), leaving SettingsViewModel.kt as just the MVI state machine; the helpers went private->internal so the VM in the sibling `settings` package can call them.

Decision: the stateless `*Content` renderers (HomeScreenContent/OnboardingScreenContent/SettingsScreenContent) are `internal`, not public. They are consumed only by in-module previews + the VM-backed wrapper; nothing out-of-module calls them. This implements the standing preference 'composables internal when possible' and overrides the older docs wording that exposed `*Content` for 'advanced hosts'. Promote to public only if a real cross-module/host call site appears.

Trap avoided: moving Onboarding*/Home*/Settings* types into subpackages requires repointing every consumer import (DemoScreen, FrnkTabbedNavConfig, FrnkTabbedNavScaffold, previews) — a half-done move leaves unresolved references that only surface at compile (`:ui-scaffolds`/`:ui-bottom-nav`/`:demo-shared`). Also watch for IDE `_root_ide_package_.` auto-complete artifacts leaking in when a moved file references a type still in the parent package; add a real import instead.

### Files
- frnk/ui/scaffolds/src/commonMain/kotlin/dev/jdgarita/frnk/ui/scaffolds/home
- frnk/ui/scaffolds/src/commonMain/kotlin/dev/jdgarita/frnk/ui/scaffolds/onboarding
- frnk/ui/scaffolds/src/commonMain/kotlin/dev/jdgarita/frnk/ui/scaffolds/settings

## Use-case concept: ObserveProStatusUseCase injected into SettingsViewModel

- id: use-case-concept-observeprostatususecase-injected-into-setti-20260617-154211
- type: architecture_decision
- status: active
- platform: kmp
- date: 2026-06-17

Introduced the use-case pattern in the frnk toolkit (domain use cases injected into ViewModels via Koin instead of threading domain state down through Compose).

First use case: ObserveProStatusUseCase (fun interface -> StateFlow<Boolean>) + DefaultObserveProStatusUseCase, in :monetization-api (frnk/capabilities/monetization-api, package dev.jdgarita.frnk.monetization.usecase), next to EntitlementManager/FeatureGate; bound in monetizationModule (single<ObserveProStatusUseCase>{...}). Wraps EntitlementManager.isPro.

Final wiring (user-directed, after rejecting both a nullable param and a no-op default): the use case is a REQUIRED Koin dependency — no NoOp, no nullable, no default value. SettingsViewModel (:ui-scaffolds): ctor param observeProStatus: ObserveProStatusUseCase (non-null, no default), exposed as val isPro: StateFlow<Boolean> = observeProStatus.invoke(). :ui-scaffolds gained implementation(projects.monetizationApi) (pure-interface, no SDK/cinterop).

settingsScaffoldModule resolves it via get(): SettingsViewModel(initial = params.get(), observeProStatus = get()). The DefaultObserveProStatusUseCase binding lives in monetizationModule (it needs EntitlementManager).

CONSEQUENCE: this makes monetizationModule REQUIRED for the VM-backed Settings scaffold — a host that installs frnkUiModules() (settingsScaffoldModule) WITHOUT monetizationModule will fail Koin resolution when the Settings VM is created (previously the design degraded to Free). The demo installs monetizationModule so it's fine. User explicitly chose this over the optional/degrade approaches.

STEP 1 = wire+expose only. FrnkAppScaffold's isPro pass-down and rememberDefaultSettingsState catalogue are UNTOUCHED. Step 2 (later) moves catalogue logic into SettingsViewModel and retires SettingsDefaults.kt.

Why :monetization-api not a ui-scaffolds port: user wanted domain use cases injected into VMs. Tests: SettingsViewModelTest (androidHostTest) covers Pro path + degrade-to-Free path.

## Token-in-state for view text/icons (FrnkStringSource / FrnkIconSource)

- id: token-in-state-for-view-text-icons-frnkstringsource-frnkicon-20260617-181751
- type: architecture_decision
- status: active
- platform: kmp
- area: ui/design-system
- date: 2026-06-17

## What & why
View states now hold **theme-token references** resolved at the **leaf atom**, instead of resolved Strings/ImageVectors baked in by a `@Composable remember*State` builder. This lets ViewModels author/update state without composition (honours the hoisted-state rule) and makes state locale-/override-independent (re-resolves automatically when the theme changes).

## The types (in :ui-theme, next to the tokens)
- `FrnkStringSource` = `Token(ThemeToken<String>) | Raw(String) | Composite(parts, separator)`.
- `FrnkIconSource` = `Token(ThemeToken<ImageVector>) | Vector(ImageVector)`.
- `@Composable resolve()` extensions in `ui/theme/ext/` (Composite uses a plain for-loop because @Composable cant be called inside joinToString {}). Must run under `FrnkTheme{}`.
- `ThemeToken` is a Compose-free key, but the artifact pulls compose.foundation, so these refs live in :ui-theme (a Compose module), NOT :core-mvi.

## Atom shape (single field, no dual representation)
- `FrnkText`: a `sealed class Resolvable(content: FrnkStringSource, ...)` intermediate; the 7 style-preset leaves (Raw/Title/.../BodySmall) hold one `content` field + a **String secondary constructor** that wraps in `FrnkStringSource.Raw` so the ~114 `Title(text="x")` call sites stay unchanged. `AppName` extends the base directly with `annotated: AnnotatedString` (the one non-Resolvable; a String cant carry per-char styling). Render is one `when` with Skeleton first; AppName/Resolvable share a private `FrnkStyledText` helper (resolved content wrapped as AnnotatedString).
- `FrnkIcon.Content`: single non-null `icon: FrnkIconSource` + an `ImageVector` secondary constructor. Removed the old nullable `imageVector`+`source`+`?: return` (which allowed a silent invisible icon).
- `FrnkTextDefaultSkeleton` is a **public** top-level val reused as the default for every subtype `skeleton` param (one source of truth; intentionally not private).

## Why D2 (single stored field + secondary ctor) over alternatives
Considered: (D1) drop String, wrap everywhere = ~160 edits + permanent verbosity; (D3) collapse the 8 style subtypes into one data class + factories = breaks every `when(state)` reducer, bigger. D2 fixes the dual-field/precedence/both-null smells with near-zero call-site churn; cost is per-subtype ctor boilerplate (default-drift hazard — keep primary & secondary defaults in sync).

## Settings pilot
`rememberDefaultSettingsState` is now a thin `@Composable` wrapper (reads live LocalFrnkHaptics, memoizes) over a **composition-free `defaultSettingsState(...)`** the VM/Koin can call. Settings row/section/footer state hold `FrnkStringSource`/`FrnkIconSource`. `SettingsFooterState.version` is `FrnkStringSource.Raw` (deliberately the concrete subtype — version is always a literal). `SyncMviConfig`/`ConfigChanged` still carries the genuinely dynamic host inputs (isPro, version, extraSections). Reducers are structural (match by id) so unaffected. `SettingsDefaultsTest` dropped Robolectric/Compose → plain reducer test.

## Constraints / notes
- `FrnkStringSource.Composite` is **titles-only, never list/row content** (per-recomposition ArrayList alloc in resolve()). 
- Dormant `UiText` in :core-mvi left as-is (future Compose-free string ref for :core-mvi-only feature VMs; would need an id->token registry).
- Verified on-device: onboarding (String path), Settings (token path + Free<->Pro ConfigChanged), Components gallery, FrnkText variant detail (all presets + AppName + Skeleton).

### Files
- frnk/ui/theme/src/commonMain/kotlin/dev/jdgarita/frnk/ui/theme/FrnkStringSource.kt
- frnk/ui/theme/src/commonMain/kotlin/dev/jdgarita/frnk/ui/theme/FrnkIconSource.kt
- frnk/ui/components/src/commonMain/kotlin/dev/jdgarita/frnk/ui/atoms/FrnkText.kt
- frnk/ui/components/src/commonMain/kotlin/dev/jdgarita/frnk/ui/atoms/FrnkIcon.kt
- frnk/ui/scaffolds/src/commonMain/kotlin/dev/jdgarita/frnk/ui/scaffolds/settings/SettingsDefaults.kt

## Deferred: migrate FrnkSectionCard/FrnkSegmentedControl (+ FrnkListRow etc.) to FrnkStringSource

- id: deferred-migrate-frnksectioncard-frnksegmentedcontrol-frnkli-20260617-181807
- type: architecture_decision
- status: temporary
- platform: kmp
- area: ui/design-system
- date: 2026-06-17
- review_after: 2026-09-17

## Open follow-up (code-review finding #4)
`FrnkSectionCard` (title/footnote) and `FrnkSegmentedControl` (options) render text **through FrnkText** but expose plain `String`/`List<String>`, so the Settings `*Content` resolves tokens one level up (`section.title?.resolve()`, `optionLabels.map { it.resolve() }`) instead of handing refs to the leaf. Same gap exists in `FrnkListRow`/`FrnkLabeledValue`/`FrnkEmptyState`/`FrnkProfileHeader`/`FrnkTopAppBar`.

## Decision: do Option A later (chosen, not yet implemented)
Migrate these components to accept `FrnkStringSource` (forward to the internal FrnkText) so resolution happens uniformly at the leaf. Plan:
- `FrnkSegmentedControlState.Content.options: List<FrnkStringSource>`; `FrnkSectionCard` + `FrnkListSectionState` title/footnote: `FrnkStringSource?`; Settings drops its `.resolve()`/`.map{resolve()}` calls.
- Add an ergonomic bridge in :ui-theme: `String.asTextSource` / `List<String>.asTextSources()` (wrap in Raw) — because JVM generic erasure blocks a `List<String>` vs `List<FrnkStringSource>` overload, and data-class fields cant carry a String secondary-ctor shim.
- ~16 literal call sites (demo/tests/previews) get `.asTextSource(s)`.
- Likely a small library-wide sweep (the other text-bearing components too) for full consistency.

## Why deferred
The token-in-state win (VM-authored, composition-free Settings *state*) is already achieved; this is internal leaf-resolution consistency, not correctness. Scoped as its own task to avoid bundling a library-wide sweep into the pilot. See the active "Token-in-state" decision for the shipped part.

### Files
- frnk/ui/components/src/commonMain/kotlin/dev/jdgarita/frnk/ui/organisms/FrnkSectionCard.kt
- frnk/ui/components/src/commonMain/kotlin/dev/jdgarita/frnk/ui/atoms/FrnkSegmentedControl.kt
- frnk/ui/scaffolds/src/commonMain/kotlin/dev/jdgarita/frnk/ui/scaffolds/settings/SettingsScreen.kt

## Model-first MVI: Arguments + onAttached + FrnkScreen lifecycle wrapper

- id: model-first-mvi-arguments-onattached-frnkscreen-lifecycle-wr-20260618-143809
- type: architecture_decision
- status: active
- platform: kmp
- area: core-mvi / ui-scaffolds
- date: 2026-06-18

## Decision

Runtime inputs for a `ModelMviViewModel` arrive as a data-only `Arguments` bundle supplied at **attach time**, not via the constructor. A generic type param `A : Arguments` was added as the leading param of `ModelMviViewModel<A, M, S, I, E>`. A public `attach(arguments)` (guarded to run once) retains `arguments` (lateinit, `private set`) and invokes the overridable `onAttached(arguments)` hook, where the VM seeds its `ModelState` and starts loads. Service dependencies (managers, trackers) stay as ordinary constructor params — kept out of `Arguments`.

The Compose driver is a wrapper pair in `:ui-scaffolds` (`ui/mvi/`, so binding can use `compose.runtime` while `:core-mvi` stays Compose-free):
- `RememberMviLifecycle(vm, arguments)` — a `DisposableEffect` calls `vm.attach(arguments)` on first composition.
- `FrnkScreen(vm, arguments) { content }` — thin wrapper that calls `RememberMviLifecycle` then renders `content()`, so screens don't repeat the attach call. Lives in `ui/mvi/` (next to `FrnkMviScreen`/`EffectCollector`), NOT a separate `ui/screen/` package.

## Why

- Side effects (analytics "viewed", offerings/data load) now fire when the screen is actually presented, not at construction.
- Mirrors a proven `RememberLifecycle`/`MviViewModelWrapper` pattern from a prior project; chosen over VM-implements-`DefaultLifecycleObserver` because it needs ZERO new deps (`lifecycle-runtime` is already transitive in `:core-mvi`) and makes engine tests trivial (`vm.attach(args)` with no `LifecycleOwner`).
- Replaces the old `koinViewModel { parametersOf(initialState) }` seeding — Koin VM registrations drop `parametersOf`; config arrives via `Arguments`.

## Tradeoffs / gotchas

- `arguments` is `lateinit` (reading before attach throws) — acceptable: intents only flow from a shown (already-attached) screen.
- Model-first VMs map an EMPTY initial model to a UiState once at construction (before `onAttached` seeds it). Any UiState invariant must tolerate empty. For Onboarding the `require(pages.isNotEmpty())` invariant MOVED from `OnboardingScreenState` (the UiState) to `OnboardingArguments` (the input); reducers using `coerceIn(0, pages.lastIndex)` need `lastIndex.coerceAtLeast(0)` to survive the pre-attach empty frame. The empty frame is hidden behind the nav slide transition.
- `attach` is guarded once, so a retained VM (same `vmKey`) keeps its state across re-open; a new `vmKey` gives a fresh attach.

## Scope / status

Proof migrations done: `PaywallViewModel`, `OnboardingViewModel`. Deferred (NOT done): renaming `ModelMviViewModel` -> `MviViewModel` + deleting the old `MviViewModel<S,I,E>` base; migrating `SettingsViewModel`/`HomeViewModel`/`DemoViewModel`; `onDetached`/`activate`/`deactivate` (ON_RESUME/ON_PAUSE) hooks (the `onDispose` in `RememberMviLifecycle` is structured for them). Verified on a Pixel 7a: onboarding full flow + paywall load/close.

### Files
- frnk/core/mvi/src/commonMain/kotlin/dev/jdgarita/frnk/ui/mvi/ModelMviViewModel.kt
- frnk/core/mvi/src/commonMain/kotlin/dev/jdgarita/frnk/ui/mvi/MviContract.kt
- frnk/ui/scaffolds/src/commonMain/kotlin/dev/jdgarita/frnk/ui/mvi/RememberMviLifecycle.kt
- frnk/ui/scaffolds/src/commonMain/kotlin/dev/jdgarita/frnk/ui/mvi/FrnkScreen.kt

## ViewModels consume use cases, not EntitlementManager directly

- id: viewmodels-consume-use-cases-not-entitlementmanager-directly-20260618-150250
- type: architecture_decision
- status: active
- platform: kmp
- area: monetization
- date: 2026-06-18

Extended the use-case seam established by ObserveProStatusUseCase: PaywallViewModel no longer depends on EntitlementManager directly — it consumes the new PaywallPurchaseUseCase (interface in :monetization-api/usecase, internal DefaultPaywallPurchaseUseCase delegating to EntitlementManager, bound in monetizationModule). This keeps ViewModels agnostic of the concrete domain manager / billing SDK.

WHY: EntitlementManager is meant to stay an internal-ish domain detail of :monetization-api; ViewModels resolving it directly leaks that coupling. Use cases are the sanctioned injection point.

DESIGN CHOICE: one grouped use case (offerings/purchase/restore on a single interface) over three granular fun-interfaces — the three ops are cohesive (all the paywall's billing). The analytics funnel stays in DefaultEntitlementManager.purchase()/the VM; the use case is a pure pass-through (no double-tracking).

NOT YET DONE (follow-up): EntitlementManager is still NOT internal — it remains public API consumed by FrnkSettingsHandler (:monetization-ui: restorePurchases/managementUrl/setGodMode), FrnkAppScaffold (:ui-app: isPro + getOrNull detection), and the demo (DemoViewModel/DemoScreen + DefaultEntitlementManager in DemoViewModelTest; FeatureGate's public ctor also exposes it). Making it internal requires extracting use cases for those consumers (restore / manage-subscription / set-god-mode / observe-status) and routing them through, plus making DefaultEntitlementManager internal and reworking the demo test. Scoped but multi-module.

GOTCHA: when renaming the VM ctor param, propagate to the Koin named-arg call in PaywallScaffoldModule — tests use positional args so they compile even when commonMain is broken (the named-arg mismatch only surfaced in :shared-monetization-ui:compileAndroidMain, not testAndroidHostTest).

### Files
- frnk/capabilities/monetization-api/src/commonMain/kotlin/dev/jdgarita/frnk/monetization/usecase/PaywallPurchaseUseCase.kt
- frnk/capabilities/monetization-api/src/commonMain/kotlin/dev/jdgarita/frnk/monetization/usecase/DefaultPaywallPurchaseUseCase.kt
- frnk/capabilities/monetization-ui/src/commonMain/kotlin/dev/jdgarita/frnk/monetization/ui/PaywallViewModel.kt

## Onboarding Arguments: drop dead layout knobs, keep the non-empty pages guard

- id: onboarding-arguments-drop-dead-layout-knobs-keep-the-non-emp-20260618-155158
- type: architecture_decision
- status: active
- platform: kmp
- area: ui-scaffolds/onboarding
- date: 2026-06-18

Removed `pagerHeight: Dp?` and `userScrollEnabled: Boolean` from OnboardingArguments/OnboardingModelState/OnboardingScreenState plus the conditional pager-modifier branch in OnboardingScreenContent. WHY: both were fully plumbed (Arguments -> ModelState -> ScreenState -> renderer) but unreachable dead config surface — the only production construction site (FrnkTabbedNavScaffold) and the public FrnkOnboardingConfig never exposed a setter, so pagerHeight was permanently null (pager always used Modifier.weight(1f)) and userScrollEnabled permanently true. Only a preview ever set pagerHeight, by bypassing Arguments.

GOTCHA / kept invariant: the `init { require(pages.isNotEmpty()) }` guard on OnboardingArguments is NOT a layout knob and MUST stay — it is the fail-fast for the public OnboardingScreen(arguments=...) boundary that advanced hosts compose directly. The first cleanup pass accidentally deleted it along with the knobs while the kdoc still claimed 'the guard lives here'; restored, and locked in with OnboardingViewModelTest.empty_pages_fails_fast_at_construction. FrnkTabbedNavScaffold separately gates the Onboarding route + Arguments construction behind config.onboarding.pages.isNotEmpty(), so the toolkit's own path never hits the guard — but the direct-API path relies on it.

### Files
- frnk/ui/scaffolds/src/commonMain/kotlin/dev/jdgarita/frnk/ui/scaffolds/onboarding/OnboardingScreenState.kt
- frnk/ui/scaffolds/src/commonMain/kotlin/dev/jdgarita/frnk/ui/scaffolds/onboarding/OnboardingViewModel.kt
- frnk/ui/scaffolds/src/commonMain/kotlin/dev/jdgarita/frnk/ui/scaffolds/onboarding/OnboardingScreen.kt

## Demo entry point unified on FrnkAppScaffold; :ui-app boundary was soft

- id: demo-entry-point-unified-on-frnkappscaffold-ui-app-boundary-20260618-175614
- type: architecture_decision
- status: active
- platform: kmp
- date: 2026-06-18

Unified both demo platforms onto a single shared composable (:demo-shared's FrnkDemoApp — renamed from DemoScreen, file FrnkDemoApp.kt) that wraps :ui-app's batteries-included FrnkAppScaffold. demo-android's MainActivity and iosDemoApp's MainViewController both call FrnkDemoApp now; MainActivity is a thin platform host (enableEdgeToEdge + system-bar icon sync + toast only).

KEY CORRECTION: the long-standing claim that ':demo-shared can't depend on :ui-app or it taints DemoKit.xcframework' was FALSE. :ui-app (frnk/ui/app/build.gradle.kts) depends only on :ui-bottom-nav + :shared-monetization-ui + :analytics-api + :core-di — NO *-impl, NO native cinterop (it resolves EntitlementManager/AnalyticsTracker from Koin at runtime). :demo-shared already depended on three of those four. Adding api(projects.uiApp) added zero RevenueCat/SQLite/Firebase symbols — verified by a clean ':demo-shared:assembleDemoKitDebugXCFramework' link. The boundary was soft architectural-purity, not a hard technical guard. The 'no *-impl in :demo-shared common surface' guarantee STILL holds.

Why: previously Android used FrnkAppScaffold directly while iOS used the bare-shell FrnkTabbedNavScaffold (hand-wiring paywall + onboarding). That split meant no single shared App composable and the recommended real-host path (FrnkAppScaffold) was not demoed on iOS. Goal: model the structure real host apps should follow (android-app + shared + ios-app, with shared owning one App composable).

Tradeoffs: (1) the bare FrnkTabbedNavScaffold shell is no longer hosted on-device by the demo — still exercised internally (FrnkAppScaffold composes it) and by its own module tests. (2) iOS paywall analytics 'source' flips demo->settings (now aligns with Android). Both acceptable.

Follow-up (same session): renamed the unified entry composable DemoScreen -> FrnkDemoApp (file DemoScreen.kt -> FrnkDemoApp.kt via git mv) to match the 'shared owns one App composable' framing; updated all call sites + comments + docs.

Docs updated: CHANGELOG.md, ARCHITECTURE.md, HOST_INTEGRATION.md section 8, root CLAUDE.md, frnk/ui/app/CLAUDE.md, demo/android-app/CLAUDE.md, README.md, demo/ios-app/README.md, ContentView.swift.

### Files
- demo/shared/src/commonMain/kotlin/dev/jdgarita/frnk/demo/FrnkDemoApp.kt
- demo/android-app/src/main/kotlin/dev/jdgarita/frnk/demo/MainActivity.kt
- demo/shared/src/iosMain/kotlin/dev/jdgarita/frnk/demo/MainViewController.kt
- demo/shared/build.gradle.kts

## demo-android Context.toast extension is the single toast helper

- id: demo-android-context-toast-extension-is-the-single-toast-hel-20260618-181433
- type: architecture_decision
- status: active
- platform: android
- date: 2026-06-18

The demo Android host's transient-feedback one-liner is the internal Context.toast(message) extension in demo/android-app/src/main/kotlin/dev/jdgarita/frnk/demo/ext/ContextExt.kt (short Toast.makeText(...).show()). MainActivity.handleEffect uses it to surface DemoEffect.Toast. **Convention:** reuse Context.toast for any transient demo feedback instead of inlining Toast.makeText; keep toast plumbing in this single extension so call sites stay terse and consistent. Follows the ext/<Type>Ext.kt extension-function convention.

### Files
- demo/android-app/src/main/kotlin/dev/jdgarita/frnk/demo/ext/ContextExt.kt

## Bottom-nav nav state moved into MVI ViewModel (FrnkNestedNavScaffold)

- id: bottom-nav-nav-state-moved-into-mvi-viewmodel-frnknestednavs-20260624-183537
- type: architecture_decision
- status: active
- platform: shared
- area: navigation
- date: 2026-06-24

FrnkNestedNavScaffold (backed by FrnkNestedNavViewModel) replaced the removed FrnkTabbedNavScaffold/FrnkAppScaffold as the toolkit's tabbed-nav path. The bar's view state (items + selectedIndex) lives in the MviViewModel, NOT in rememberXYZ() — this is the standard going forward: hoist UI/selection/nav state into the VM. onIntent(Tap) updates selectedIndex via updateModel AND emits FrnkNestedNavEffect.Navigate(route: NavKey) (FrnkNavBarItemModel gained route: NavKey), which the scaffold applies via backStack.navigateTo(route). FINAL SHAPE (current): FrnkNestedNavScaffold is a FIXED three-tab (Home · Components · Settings) scaffold — the three bar items (theme icon tokens iconNavHome/iconNavComponent/iconNavSettings via FrnkIconSource.Token; SF-Symbols house/square.grid.2x2/gearshape; routes FrnkRoute.Home / FrnkRoute.Custom(\"Components\") / FrnkRoute.Settings) are hardcoded INSIDE the scaffold. Signature: FrnkNestedNavScaffold(modifier, onSavedStateConfiguration, onNestedNavigationModule) — NO tabs param. The host supplies only the saved-state config + the nested-nav Koin module registering the destinations behind those three routes. (An intermediate version briefly took tabs: List<FrnkNavTab> and derived items from it; that was reverted same-day, and FrnkNavTab is now DELETED — do not document host-supplied tabs.) Interim: a single shared rememberNavBackStack drives every tab; true per-tab back stacks (so a tab keeps its nested nav) + the back-from-a-non-home-tab-root->home convention are a planned follow-up, where the back stacks also move into the VM. Deleted: FrnkAppScope, FrnkTabbedBackStacks/rememberFrnkTabbedBackStacks/FrnkTabbedBackHandler/FrnkTab, and FrnkNavTab. App apex is now :ui-app's FrnkApp(onSavedStateConfiguration, onNavigationModule); demo FrnkDemoApp calls it and wires its own root nav module (FrnkRootRoute Onboarding/Tab/Paywall), mounting FrnkNestedNavScaffold at Tab. Orphaned/pending removal: FrnkBottomNavState, FrnkFeatureItem, FrnkBottomNavTab, FrnkTabbedNavConfig/FrnkTabbedNavViewState, FrnkAppConfig/FrnkAppConfigExt.

### Files
- frnk/ui/bottom-nav/src/commonMain/kotlin/dev/jdgarita/frnk/ui/bottomnav/FrnkNestedNavScaffold.kt
- frnk/ui/bottom-nav/src/commonMain/kotlin/dev/jdgarita/frnk/ui/bottomnav/FrnkNestedNavViewModel.kt
- frnk/ui/bottom-nav/src/commonMain/kotlin/dev/jdgarita/frnk/ui/bottomnav/FrnkNestedNavMviContract.kt

## Per-tab back stacks moved into FrnkNestedNavViewModel + host-provided Custom tab

- id: per-tab-back-stacks-moved-into-frnknestednavviewmodel-host-p-20260624-204831
- type: architecture_decision
- status: active
- platform: shared
- area: navigation
- date: 2026-06-24

FOLLOW-UP LANDED (replaces the interim single-shared-stack note in 'bottom-nav-nav-state-moved-into-mvi-viewmodel-...'). FrnkNestedNavViewModel now OWNS per-tab back stacks (true multiple-back-stacks). Design: the VM holds one live NavBackStack<NavKey> (val backStack, seeded with FrnkRoute.Home) that the scaffold renders via FrnkNavDisplay AND hands to the host's onNestedNavigationModule; plus savedStacks: MutableList<MutableList<NavKey>> snapshots per tab. The active tab is mirrored live in backStack; on tab switch the VM snapshots the current stack into savedStacks[from], then clears+addAll savedStacks[to] (canonical nav3 multiple-back-stack pattern). NavBackStack is constructed OUTSIDE composition via its public ctor NavBackStack(vararg) (a SnapshotStateList) — confirmed in navigation3-runtime 1.1.1. Conventions implemented: re-tapping the active tab pops it to root (clearAndNavigateTo); system/predictive back at a tab root from a non-Home tab returns to Home (HOME_INDEX=0). Intents: FrnkNestedNavIntent.Tap(index) + NEW data object Back. FrnkNestedNavEffect.Navigate REMOVED (nav is VM-internal now; the empty sealed FrnkNestedNavEffect only satisfies the MVI generic; FrnkNestedNavArguments is now an empty data object). Scaffold stopped using FrnkScreen — it uses koinViewModel { parametersOf(customTab) } + collectAsStateWithLifecycle + a CONDITIONAL BackHandler(enabled = backStack.size <= 1 && selectedIndex != 0) so within-tab pops are left to FrnkNavDisplay/NavDisplay's own back and only the tab-root->Home case is handled by the scaffold. HOST-PROVIDED CUSTOM TAB: new public type FrnkCustomTab(route: NavKey, icon: FrnkIconSource, iosSystemIcon: String, label: String). Home + Settings stay toolkit-fixed (iconNavHome/iconNavSettings, house/gearshape, FrnkRoute.Home/FrnkRoute.Settings); the host supplies ONLY the middle tab via FrnkCustomTab (it can use FrnkIconSource.Vector for a fully host-owned icon). New scaffold signature: FrnkNestedNavScaffold(customTab: FrnkCustomTab, modifier, onNestedNavigationModule) — REMOVED nestedNavArguments AND onSavedStateConfiguration. frnkNestedNavModule now binds viewModel { params -> FrnkNestedNavViewModel(customTab = params.get()) }. PERSISTENCE TRADEOFF: stacks are in-memory (survive recomposition + config change via the VM, NOT full process death). The dropped onSavedStateConfiguration / frnkNestedNavConfig path means process-death restore of the nested stacks is deferred (a future SavedStateHandle option). Verified end-to-end on a physical Pixel 7a: per-tab persistence (component detail retained across a Settings round-trip), within-tab back pop, back-at-non-home-root->Home. Reducer tests added in :ui-bottom-nav commonTest (FrnkNestedNavViewModelTest). NOTE: NOT committed yet (user reviews first). Demo caller: demo/shared/.../navigation/modules/RootNavigationModule.kt.

### Files
- frnk/ui/bottom-nav/src/commonMain/kotlin/dev/jdgarita/frnk/ui/bottomnav/FrnkNestedNavViewModel.kt
- frnk/ui/bottom-nav/src/commonMain/kotlin/dev/jdgarita/frnk/ui/bottomnav/FrnkNestedNavScaffold.kt
- frnk/ui/bottom-nav/src/commonMain/kotlin/dev/jdgarita/frnk/ui/bottomnav/FrnkNestedNavMviContract.kt
- frnk/ui/bottom-nav/src/commonMain/kotlin/dev/jdgarita/frnk/ui/bottomnav/FrnkCustomTab.kt
- frnk/ui/bottom-nav/src/commonMain/kotlin/dev/jdgarita/frnk/ui/bottomnav/FrnkNestedNavModule.kt

## NavDisplay is the single owner of system back (LocalFrnkBackHandledByHost)

- id: navdisplay-is-the-single-owner-of-system-back-localfrnkbackh-20260624-215822
- type: architecture_decision
- status: active
- platform: shared
- area: navigation
- date: 2026-06-24

Fixed: back from a non-Home tab root (e.g. Settings) exited the app instead of returning to Home, because the tab-root screen's own FrnkScreen installed an always-on BackHandler (DidPressBack -> backStack.back()) that popped the shared nested stack before FrnkNestedNavScaffold's tab-root->Home handler could run. Components worked only because ComponentsListScreen has no back handler. ROOT CAUSE / INVARIANT: nav3's NavDisplay already owns system back (within-stack pops) — a FrnkScreen rendered as a NavDisplay entry must NOT also install its own. SOLUTION (Option B3): new CompositionLocal LocalFrnkBackHandledByHost (declared in :ui-scaffolds FrnkScreen.kt, compositionLocalOf{false}); FrnkScreen's new param handleBackPressed defaults to !LocalFrnkBackHandledByHost.current (so standalone screens keep the handler, NavDisplay entries drop it; escape hatch handleBackPressed=true). FrnkNavDisplay provides LocalFrnkBackHandledByHost=true around its entries. So leaf FrnkScreens defer system back to NavDisplay (pops) + the enclosing tabbed scaffold (tab-root->Home). SCOPE: :ui-app's FrnkApp root uses a RAW NavDisplay (not FrnkNavDisplay), so root screens (onboarding/paywall) are unaffected — handleBackPressed resolves to !false=true there, unchanged. So B3 today only affects the nested tab nav (FrnkNestedNavScaffold's FrnkNavDisplay), but is principled for any future FrnkNavDisplay. Demo Home/Settings DidPressBack->backStack.back() branches were dead under B3 (no back handler -> no DidPressBack) and have since been REMOVED; the demo SettingsScreen also dropped its now-unused onNavigateAway param. Verified on device: Settings root->Home, Components root->Home, Home root->exit, within-tab detail->list pop, onboarding completes. Files: FrnkScreen.kt, FrnkNavDisplay.kt, demo NestedNavigationModule.kt, demo SettingsScreen.kt.

### Files
- frnk/ui/scaffolds/src/commonMain/kotlin/dev/jdgarita/frnk/ui/mvi/FrnkScreen.kt
- frnk/ui/scaffolds/src/commonMain/kotlin/dev/jdgarita/frnk/ui/nav/FrnkNavDisplay.kt

## Public-API improvement backlog (docs/api-improvements/) + Tier 1 landed

- id: public-api-improvement-backlog-docs-api-improvements-tier-1-20260624-224123
- type: architecture_decision
- status: active
- platform: shared
- area: architecture
- date: 2026-06-24

Tiered backlog of frnk public-API improvements (host ergonomics) lives at docs/api-improvements/ (README.md has a reusable evaluation prompt + tier-1.md..tier-4.md). It's a backlog, NOT canonical API reference (canonical stays in per-module CLAUDE.md + docs/). Tiers: 1 hygiene; 2 host ergonomics (nav-wiring simplification, bootstrap presets/XOR validation, Settings<->monetization decoupling, db Koin helper, iOS/Crashlytics consolidation, implement EffectCollector/SyncMviConfig); 3 consistency/type-safety; 4 capability maturity. TIER 1 LANDED: (a) removed phantom MVI-binding symbols from all live docs/KDoc — FrnkMviScreen/EffectCollector/SyncMviConfig/RememberMviLifecycle NEVER existed in code (only FrnkScreen does; the SyncMviConfig mechanism survives as SettingsIntent/HomeIntent.ConfigChanged). (b) git rm'd the dead legacy config cluster (7 unused files): :ui-app FrnkAppConfig.kt + ext/FrnkAppConfigExt.kt; :ui-bottom-nav FrnkTabbedNavConfig.kt/FrnkTabbedNavViewState.kt/FrnkBottomNavState.kt/FrnkBottomNavTab.kt/FrnkFeatureItem.kt. (c) fixed requireFrnkKoin error message FrnkAppScaffold->FrnkApp + removed two stale 'pending removal' doc notes. Compile gate green. Remaining tracked (tier-1.md item 1.4): FrnkAppScaffold/FrnkTabbedNavScaffold still referenced in some KDoc (REQUIREMENTS.md, demo KDoc, OnboardingGate, FrnkUiModules(+Test), FrnkRootRoute). NOT committed yet (user reviews).

### Files
- docs/api-improvements/README.md
- docs/api-improvements/tier-1.md

## Tier 2.1: frnkTabbedRootModule batteries-included tabbed app root

- id: tier-2-1-frnktabbedrootmodule-batteries-included-tabbed-app-20260624-231308
- type: architecture_decision
- status: active
- platform: shared
- area: navigation
- date: 2026-06-24

Added a batteries-included convenience over FrnkApp for the common onboarding->tab shell->paywall shape, collapsing the host's hand-written root+nested Koin modules + cross-level threading. New public API in :ui-app (FrnkTabbedRootModule.kt): frnkTabbedRootModule(customTab) { home{nav->}; custom{route,nav->}; settings{nav->} (required) + optional onboarding{onComplete->}; paywall{onClose->} } returns the (rootBackStack)->Module lambda FrnkApp's onNavigationModule expects. Tab slots receive FrnkTabNavigator(open/back = active tab stack; openPaywall/showOnboarding = root stack). rememberFrnkRootStartRoute(showOnboarding=true) returns Onboarding until OnboardingGate seen then Tab; the helper marks the gate seen when onboarding is presented. FrnkApp gained startRoute: FrnkRootRoute = FrnkRootRoute.Onboarding (additive, non-breaking; restored saved stacks override it). The DSL hides the navigation<> DSL + KoinExperimentalAPI opt-in. SHAPE/GATING were user-chosen (DSL-helper over content-slot composable; auto-gating via OnboardingGate). Demo migrated: FrnkDemoApp is now one FrnkApp(... frnkTabbedRootModule ...) call; deleted demo RootNavigationModule.kt + NestedNavigationModule.kt. Implementation note: function-type slots must be called positionally (named args on function types are prohibited). VERIFIED: Android compile+test+device run (onboarding gating on first launch -> Home; Components tab+detail+back via nav.open/back; Home crown -> paywall via nav.openPaywall). iOS: both Kotlin targets compile. INCIDENTAL PRE-EXISTING iOS fixes (unrelated to 2.1) needed to build iOS: moved :ui-app ApplySystemBarAppearance actual from iosArm64Main (device-only) to iosMain (so iosSimulatorArm64 builds); removed MainViewController's onEffect param (referenced the long-deleted DemoEffect). STILL BROKEN separately: demo Swift iosDemoApp (ComposeViewController.swift/ContentView.swift) still references deleted DemoEffect/toast -> needs a Swift-side cleanup. NOT committed (user reviews). Backlog: docs/api-improvements/tier-2.md (2.1 -> Done).

### Files
- frnk/ui/app/src/commonMain/kotlin/dev/jdgarita/frnk/ui/app/FrnkTabbedRootModule.kt
- frnk/ui/app/src/commonMain/kotlin/dev/jdgarita/frnk/ui/app/FrnkApp.kt
- demo/shared/src/commonMain/kotlin/dev/jdgarita/frnk/demo/FrnkDemoApp.kt

## Settings↔monetization coupling kept by design (Tier 2.3 Won't do)

- id: settings-monetization-coupling-kept-by-design-tier-2-3-won-t-20260625-152246
- type: architecture_decision
- status: active
- platform: shared
- area: architecture
- date: 2026-06-25

Decision (2026-06-25): do NOT decouple the VM-backed Settings scaffold (:ui-scaffolds) from monetization. SettingsViewModel takes a REQUIRED ObserveProStatusUseCase (get()), bound by monetizationModule, so monetizationModule is a baseline dependency of the Settings scaffold. The user confirmed it's fine to ASSUME a monetization module is always installed; this is the intended design, not a temporary compromise (supersedes the earlier 'refactor planned later' framing from 2026-06-17). Public-API backlog item docs/api-improvements/tier-2.md §2.3 ('Decouple Settings from monetization') is marked Won't do. DO NOT reintroduce nullable / no-op / getOrNull fallbacks for monetization in Settings, and don't propose decoupling. Updated: tier-2.md §2.3, :ui-scaffolds CLAUDE.md SettingsScreen note, and the project memory 'settings-may-depend-on-monetization'.

### Files
- docs/api-improvements/tier-2.md
- frnk/ui/scaffolds/CLAUDE.md

## databaseSingle Koin helper in :data-db-api (Tier 2.4)

- id: databasesingle-koin-helper-in-data-db-api-tier-2-4-20260625-153944
- type: architecture_decision
- status: active
- platform: kmp
- area: data/db-api
- date: 2026-06-25

Added `Module.databaseSingle(schema, name) { driver -> Db(driver) }`, an `inline reified` Koin Module extension in :data-db-api (database/ext/SqlDriverFactoryExt.kt) that registers a `single<T>` resolving the Koin SqlDriverFactory and forwarding schema/name to create(...). Replaces the hand-written `single { Db(get<SqlDriverFactory>().create(Db.Schema, "x.db")) }` boilerplate every host wrote (HOST_INTEGRATION §1).

Why / decisions:
- Chose a Module-receiver extension (not a function returning a Module) so it composes inside an existing `module { }` block alongside the host's other singles (e.g. the NoteStore). Reads identically to the ticket's proposed call site.
- :data-db-api now carries `api(libs.koin.core)` — deliberate: the `Module` receiver is in the public inline signature, so consumers must resolve Koin types. The *-api 'no third-party SDK' rule targets Ktor/SQLDelight-driver/RevenueCat/Firebase, not the DI framework; the ticket scopes the helper here. The only Koin in the module is this DSL helper — driver bindings still live in :data-db-impl (databaseModule).
- Module moved from `frnk.kmp.library` to `frnk.kmp.library.hosttest` to host DatabaseSingleTest (commonTest), which uses a recording SqlDriverFactory + a never-invoked NoopSqlDriver fixture to assert schema/name forwarding + driver pass-through.
- Additive/non-breaking: the raw `single { … }` long form stays valid. Demo's demoNotesModule rewired onto the helper (exercises it across demo-shared / demo-android / DemoKit.xcframework).

Verified: :data-db-api compile+testAndroidHostTest, full compileAndroidMain + :demo-android:compileDebugKotlin, full testAndroidHostTest (incl. demo-shared NoteStore round-trip), and DemoKit XCFramework assemble.

### Files
- frnk/data/db-api/src/commonMain/kotlin/dev/jdgarita/frnk/database/ext/SqlDriverFactoryExt.kt
- frnk/data/db-api/build.gradle.kts
- demo/shared/src/commonMain/kotlin/dev/jdgarita/frnk/demo/notes/DemoNotesModule.kt

## Tier 2.2 bootstrap presets + fail-fast validation

- id: tier-2-2-bootstrap-presets-fail-fast-validation-20260625-160937
- type: architecture_decision
- status: active
- platform: kmp
- area: DI / host bootstrap
- date: 2026-06-25

Shipped Tier 2.2 as TWO additive pieces because they cover different halves and neither subsumes the other:

WHY two pieces: Koin 4.2.1 cannot introspect a List<Module> before startKoin. So a *duplicate* observability/remote-config install (silent shadowing) can only be prevented STRUCTURALLY (a typed builder with single-assignment slots), while a *missing* required module can only be detected POST-start via getOrNull. The builder prevents duplicates; the validator catches missing — both together fully cover the documented footguns.

WHERE the builder lives — :ui-app, NOT :core-di. The builder must reference frnkUiModules() (in :ui-app) and auto-bundle monetizationModule (:monetization-api) + paywallScaffoldModule (:monetization-ui). :core-di is the bottom of the ui column and is forbidden by rule (OQ-7) from any capability knowledge, and must depend only on koin-core. So frnkModules { } + validateFrnkBootstrap live in :ui-app; :core-di only got an additive function-typed hook initializeFrnk(modules, validate, validator) that runs whatever check it is handed, naming no capability type.

CINTEROP rule: the builder NEVER references an *-impl val (firebaseObservabilityModule/revenueCatModule/remoteConfigModule/databaseModule) — that would drag Firebase/RevenueCat/SQLite native cinterops into every consumer and break the XCFramework. The host imports the impl val and assigns it to a slot. Only the noop modules (clean api modules) are referenced as defaults. One new clean api edge was added: :ui-app -> :remote-config-api (interfaces only) so the builder can default remoteConfig and the validator can resolve RemoteConfigService.

VALIDATOR required-vs-optional: REQUIRED (throw) = observability (AnalyticsTracker+CrashReporter), remote-config (RemoteConfigService), monetization (ObserveProStatusUseCase+EntitlementManager — frnkUiModules always renders Settings which needs pro-status; matches the 'monetization is always installed' decision in tier 2.3). OPTIONAL (never throw) = KeyValueStore/SqlDriverFactory (a local-only host omits them).

VALIDATOR gotcha: getOrNull throws InstanceCreationException (NOT caught by Koin's getOrNull) when a definition exists but a transitive dep is missing. A private Koin.isBound<T>() treats InstanceCreationException as 'bound' so a missing leaf (e.g. AnalyticsTracker) is reported ONCE by its own check (observability) instead of falsely reporting 'monetization missing' when monetizationModule IS installed.

DEMO: demo-android assembles its real-SDK override list via frnkModules { } (coexists with allowOverride(true) over the fake frnkAppModule); checkFrnkModules() runs inside bootstrapDemoKoin so both Android and iOS validate the fully-assembled graph. The explicit initializeFrnk(modules=…) path is untouched (additive).

### Files
- frnk/ui/app/src/commonMain/kotlin/dev/jdgarita/frnk/ui/app/FrnkModulesBuilder.kt
- frnk/ui/app/src/commonMain/kotlin/dev/jdgarita/frnk/ui/app/FrnkBootstrapValidation.kt
- frnk/core/di/src/commonMain/kotlin/dev/jdgarita/frnk/di/FrnkInitializer.kt

## Tier 2.6 EffectCollector/SyncMviConfig — Won't do

- id: tier-2-6-effectcollector-syncmviconfig-won-t-do-20260625-165522
- type: architecture_decision
- status: active
- platform: kmp
- area: mvi
- date: 2026-06-25

Decided (2026-06-25) Won't do for both Tier 2.6 helpers; no new public API.

FrnkScreen is the single sanctioned MVI effect-collection binding: lifecycle-gated (repeatOnLifecycle) + rememberUpdatedState, single-consumer onEffect callback. Collect effects in exactly one place.

EffectCollector: had NO consumer — every screen routes effects through FrnkScreen, and even the one VM-without-effects case (FrnkNestedNavScaffold) just passes onEffect={}. A standalone collector would be speculative and a second way to do the same thing.

SyncMviConfig: dropped by user. The receiving half (HomeIntent.ConfigChanged / SettingsIntent.ConfigChanged, the latter merges via mergedWith to preserve in-session state) already exists but is unwired, and there is no current runtime-changing config to sync. Re-entry if ever needed: LaunchedEffect(config){ vm.send(ConfigChanged(it)) }.

Mirrors how Tier 2.3 was resolved (Won't do, rationale in brain not docs).

### Files
- docs/api-improvements/tier-2.md
- frnk/ui/scaffolds/src/commonMain/kotlin/dev/jdgarita/frnk/ui/mvi/FrnkScreen.kt

## Type-safe feature gating: Feature is an open marker interface + FrnkFeature enum

- id: type-safe-feature-gating-feature-is-an-open-marker-interface-20260625-171045
- type: architecture_decision
- status: active
- platform: kmp
- area: monetization
- date: 2026-06-25

Tier 3.1. Replaced `data class Feature(val id: String)` (with companion constants) by an OPEN marker `interface Feature { val id: String }` plus the toolkit catalogue `enum class FrnkFeature(override val id) : Feature { Premium, UnlimitedExports, AdFree }`.

WHY: the old public constructor let a host call `gate.canUse(Feature("typo"))` and silently get false — no compile-time safety. An enum can't be the root because hosts can't extend an enum, so the root must be an open interface; the toolkit's own closed catalogue is an enum that IMPLEMENTS it, which is the exact shape hosts are told to copy (`enum class AppFeature(override val id) : Feature { ... }`) — typos become uncompilable. This mirrors the established NavKey(open marker)+FrnkRoute(sealed catalogue) pattern already in the codebase.

FeatureGate now matches `freeFeatures` by `Feature.id` (`feature.id in freeFeatureIds`) instead of object/set equality, so the check is correct across ANY Feature impl regardless of its equals (a host's object and the toolkit enum with the same id still match).

KNOWN GAP (left out of scope, type-safety only): `freeFeatures` is still not wired through `monetizationModule` — it binds the empty default, so configuring it requires overriding the FeatureGate Koin binding.

Blast radius was tiny (only the demo's DemoHomeViewModel used Feature.Premium -> FrnkFeature.Premium); clean break, no deprecation shims since the repo is private/foundation-phase. Added FeatureGateTest (none existed) covering canUse/observe/requestUpgrade + the by-id robustness claim. No build.gradle change: :monetization-api is already exported into DemoKit.

### Files
- frnk/capabilities/monetization-api/src/commonMain/kotlin/dev/jdgarita/frnk/monetization/Feature.kt
- frnk/capabilities/monetization-api/src/commonMain/kotlin/dev/jdgarita/frnk/monetization/FrnkFeature.kt
- frnk/capabilities/monetization-api/src/commonMain/kotlin/dev/jdgarita/frnk/monetization/FeatureGate.kt

## Component *State three-category taxonomy (Tier 3.2)

- id: component-state-three-category-taxonomy-tier-3-2-20260625-175602
- type: architecture_decision
- status: active
- platform: shared
- area: ui-components / design system
- date: 2026-06-25

Tier 3.2 asked to unify/document the component `*State` convention. Decision: **document, not align** — every divergence from the canonical `sealed interface { Content + Skeleton }` shape is justified, so aligning would be net-negative.

Three sanctioned `*State` shapes (canonical text in HOST_INTEGRATION.md §9):
- **A — Stateful (default):** `sealed interface` + `Content` data class + `data object Skeleton` (+ `Error`). For loading-capable components. Members: FrnkButton, FrnkSwitch, FrnkSegmentedControl, FrnkIcon, FrnkIconButton, FrnkListRow, FrnkLabeledValue, FrnkProfileHeader.
- **B — Variant (shared-field):** a `sealed class` whose variants share stored `open val` fields — the one case a sealed *class* (not interface) is correct, since an interface cannot carry stored props. Members: FrnkDividerState (Horizontal/Vertical, no skeleton — always-on chrome), FrnkTextState (semantic variants + per-subtype `skeleton` field + a Skeleton object).
- **C — Single-state:** plain `@Immutable data class`, no Skeleton. For single-state/terminal/chrome/skeleton-delegated. Members: FrnkTopAppBarState (single state), FrnkEmptyStateState (terminal zero-content), FrnkSwipeableState (interaction chrome), FrnkListSectionState (skeleton on child rows).

Cross-cutting: ergonomic secondary constructors (FrnkIconState/FrnkIconButtonState `ImageVector`→FrnkIconSource.Vector; FrnkTextState `String`→FrnkStringSource.Raw) are sanctioned — primary ctor stays the source-wrapper form; add a secondary only when the wrapped type is the overwhelmingly common case.

Also corrected a doc bug: ui/components/CLAUDE.md previously called FrnkDivider "non-sealed" — it is a Category-B sealed class. Each Category-B/C `*State` now carries a one-line `State shape — Category X` KDoc marker at its declaration. No API/behavior change; comments + docs only.

### Files
- docs/HOST_INTEGRATION.md
- frnk/ui/components/CLAUDE.md

## Tier 3.3 — FrnkRoute→FrnkTabRoute + paywall to root + symmetric nav config

- id: tier-3-3-frnkroute-frnktabroute-paywall-to-root-symmetric-na-20260625-182149
- type: architecture_decision
- status: active
- platform: kmp
- area: navigation / :core-nav
- date: 2026-06-25

Tightened the nav route catalogues in :core-nav.

(1) RENAME: tab-level catalogue FrnkRoute -> FrnkTabRoute so the level is explicit in the type name (FrnkRootRoute keeps its name). Hosts kept reaching for FrnkRoute thinking it was the general/root one when it was actually tab-level. After the prune FrnkTabRoute = Home/Settings/Custom, matching the fixed three-tab bar exactly.

(2) PRUNE + PAYWALL-TO-ROOT: removed the vestigial FrnkRoute.Onboarding/Paywall members — full-screen flows (FrnkFullScreenRoute) belong on FrnkRootRoute, above the bottom bar. Repointed :shared-monetization-ui frnkPaywallNavigation + rememberFrnkSettingsHandler from FrnkRoute.Paywall to FrnkRootRoute.Paywall (rememberFrnkSettingsHandler backStack must now be the ROOT stack). NOTE: those two helpers have NO live callers — the demo opens the paywall via FrnkRootRoute.Paywall already (frnkTabbedRootModule / FrnkTabNavigator.openPaywall), so the repoint is compile-verified + architecturally correct but NOT demo-exercised at runtime.

(3) SYMMETRIC CONFIG (real capability gap, not cosmetics): frnkRootNavConfig became a function frnkRootNavConfig(hostRoutes = …) symmetric with frnkNestedNavConfig. The old val never registered FrnkRootRoute.Custom (couldn't serialize) and offered no way to merge host root routes, so the root stack was not host-extensible. Added RootNavConfigTest (core-nav commonTest) as the only automated coverage for the gap fix.

SAFETY: nested/tab stacks are in-memory only (survive recomposition + config change, not process death), so the FrnkTabRoute serialName change needs no persistence migration; FrnkRootRoute was NOT renamed so the process-death-persisted root stack is unaffected.

VERIFIED: compileAndroidMain + :demo-android; full testAndroidHostTest (FrnkTabRouteTest + RootNavConfigTest + FrnkNestedNavViewModelTest); ktlintFormat; DemoKit xcframework link. Not committed (user reviews/ships).

### Files
- frnk/core/nav/src/commonMain/kotlin/dev/jdgarita/frnk/ui/nav/FrnkTabRoute.kt
- frnk/core/nav/src/commonMain/kotlin/dev/jdgarita/frnk/ui/nav/RootNavConfig.kt
- frnk/capabilities/monetization-ui/src/commonMain/kotlin/dev/jdgarita/frnk/monetization/ui/PaywallNav.kt
- frnk/capabilities/monetization-ui/src/commonMain/kotlin/dev/jdgarita/frnk/monetization/ui/FrnkSettingsHandler.kt

## FrnkApp exposes themeConfig pass-through for host theming

- id: frnkapp-exposes-themeconfig-pass-through-for-host-theming-20260625-233109
- type: architecture_decision
- status: active
- platform: shared
- area: ui-app / theming
- date: 2026-06-25

FrnkApp now takes `themeConfig: FrnkThemeConfig = FrnkThemeConfig.Default` (between `startRoute` and `onNavigationModule`) and passes it to the `FrnkTheme(config = themeConfig)` it owns. This is the sanctioned way a host brands the whole app via FrnkApp — previously the only path was wrapping your own FrnkTheme(config) around screens, which FrnkApp's path couldn't reach.

Why: token overrides (FrnkThemeConfig: light/darkColorOverrides, textStyle/shape/string/icon/spacing/iconSize overrides, fontFamily) merge over the bundled palette via Map.plus (host wins per token). FrnkApp publishes them through LocalFrnkThemeConfig; FrnkPlatformTheme re-reads + merges each axis per composition; bundled color tokens animate over 450ms via animateColorPalette. Default FrnkThemeConfig.Default keeps the bundled palette, so the new param is non-breaking for existing hosts.

Demo: FrnkDemoApp now passes themeConfig = demoRedThemeConfig() (DemoColors.kt) — red accent #DC2626 light / #F87171 dark (renamed from the previously-DEAD demoPurpleThemeConfig, which existed but was never wired). The demo is now the reference integration for host theming.

Note: only hosts that hand-wire the nav primitives without FrnkApp still wrap their own FrnkTheme(config). FrnkApp itself has no per-screen theme override path — it's one app-wide config.

### Files
- frnk/ui/app/src/commonMain/kotlin/dev/jdgarita/frnk/ui/app/FrnkApp.kt
- demo/shared/src/commonMain/kotlin/dev/jdgarita/frnk/demo/FrnkDemoApp.kt
- demo/shared/src/commonMain/kotlin/dev/jdgarita/frnk/demo/DemoColors.kt

## SqlDriverFactory gains opt-in SchemaUpgrade (wipe-on-version-bump)

- id: sqldriverfactory-gains-opt-in-schemaupgrade-wipe-on-version-20260626-195502
- type: architecture_decision
- status: active
- platform: kmp
- area: data/db
- date: 2026-06-26

-

### Files
- frnk/data/db-api/src/commonMain/kotlin/dev/jdgarita/frnk/database/SchemaUpgrade.kt
- frnk/data/db-impl/src/commonMain/kotlin/dev/jdgarita/frnk/database/impl/Defaults.kt
- frnk/data/db-impl/src/commonMain/kotlin/dev/jdgarita/frnk/database/impl/DatabaseModule.kt

## iOS DbPlatform pins explicit App-Support basePath (m2 closed)

- id: ios-dbplatform-pins-explicit-app-support-basepath-m2-closed-20260626-212611
- type: architecture_decision
- status: active
- platform: ios
- area: data-db-impl
- date: 2026-06-26

Closes the deferred m2 follow-up on SchemaUpgrade.WipeOnVersionBump. The iOS DbPlatform actual previously opened NativeSqliteDriver(schema, name) (SQLiter default path) but checked/deleted under NSDocumentDirectory — a path mismatch that made the wipe a silent no-op on iOS.

Root cause (decompiled SQLiter 1.3.3 sources, pulled by SQLDelight 2.3.2): NativeSqliteDriver writes to NSApplicationSupportDirectory/databases/<name> by default (co.touchlab.sqliter.DatabaseFileContext.iosDirPath("databases")), NOT NSDocumentDirectory. So delete-path != create-path.

Fix: createDriver/databaseFileExists/deleteDatabaseFiles now share one databasesDir() helper that pins NSApplicationSupportDirectory/databases. createDriver injects it via NativeSqliteDriver onConfiguration { extendedConfig.copy(basePath = …) }; exists/delete resolve the same dir → delete-path == create-path by construction, decoupled from any future SQLiter default change. The helper createDirectoryAtPath(withIntermediateDirectories=true) itself because passing an explicit basePath bypasses SQLiter auto-create. Chose App Support (= SQLiter default) so existing on-disk DBs are still found (no orphaning) and the DB keeps correct iOS semantics (not user-visible/iCloud). No :data-db-api SPI / Android actual / DbPlatform shape change.

Verified on a physical iPhone (iOS 26.5) via Still iosApp: v4 fresh install → exists=false, create @ Library/Application Support/databases, no wipe; v4 relaunch → no exists/delete (matching version short-circuit); 4→5 bump → delete fresh_track.db/-wal/-shm existedBefore=true→existsAfter=false at that path, recreate same dir; v5 relaunch → no wipe.

### Files
- frnk/data/db-impl/src/iosMain/kotlin/dev/jdgarita/frnk/database/impl/Defaults.ios.kt

## IdentitySource unifies identity fan-out; only the billing sink gates

- id: identitysource-unifies-identity-fan-out-only-the-billing-sin-20260811-190002
- type: architecture_decision
- status: active
- platform: kmp
- area: capabilities/identity + analytics + monetization
- date: 2026-08-11

Every consumer of a user identity now implements one contract — `IdentitySource.identify(id): AppResult<Unit, IdentityError>` in `:identity-api` — instead of each inventing its own log-in signature. Implemented by `AnalyticsTracker` + `CrashReporter` (`:analytics-api`) and `EntitlementProvider` + `EntitlementManager` (`:monetization-api`).

**Why `:identity-api`:** it already owns identity and depends on nothing but `shared-utils`, so hosting the contract there creates no cycle. The cost is one cross-capability api→api edge, `analytics-api ← identity-api`, accepted over duplicating the interface or adding a fifth module for one method.

**The load-bearing rule:** `DefaultSyncAuthUseCase` fans the uid out to all three sinks but **only `entitlementManager.identify` shapes the returned AppResult**. The two observability results are deliberately discarded (each already logs its own failure via `PrintLogger`). Reason: `SyncAuthUseCase` gates the scanner in the Faint host, so propagating a telemetry failure would mean an unconfigured Crashlytics blocks scanning — contradicting the standing graceful-degradation promise that an unconfigured SDK never blocks a host. `DefaultSyncAuthUseCaseTest.a_failing_observability_sink_never_fails_the_sync` exists specifically to stop someone "fixing" the discarded results into a checked call.

**Funnel placement:** `IdentitySynced` is emitted by the use case, not by `FirebaseAnalyticsTracker.identify`. The event has to follow the step that decides success, or it reports syncs that later failed in RevenueCat. Exactly one of `identity_synced` / `identity_sync_failed` fires per call; the failure carries `stage` (sign_in | entitlement) + `error_type`, both low-cardinality.

**Identity in Analytics goes to the reserved User-ID field** (`Firebase.analytics.setUserId`), never a custom user property or event param — a uid is unbounded-cardinality, so GA4 collapses it into `(other)` and it would burn one of the 25 user-scoped custom dimension slots.

### Files
- frnk/capabilities/identity-api/src/commonMain/kotlin/dev/jdgarita/frnk/identity/IdentitySource.kt
- frnk/capabilities/monetization-api/src/commonMain/kotlin/dev/jdgarita/frnk/monetization/usecase/DefaultSyncAuthUseCase.kt
- frnk/capabilities/analytics-impl/src/commonMain/kotlin/dev/jdgarita/frnk/backend/firebase/IdentitySink.kt

## frnk.android.firebase: toolkit owns the host's Firebase build wiring

- id: frnk-android-firebase-toolkit-owns-the-host-s-firebase-build-20260811-190015
- type: architecture_decision
- status: active
- platform: android
- area: build-logic
- date: 2026-08-11

frnk supplies the Firebase *runtime* bindings (`firebaseObservabilityModule`), but two things only a host **application** module can do: apply `google-services` (generates the resources `FirebaseInitProvider` reads before `Application.onCreate`) and apply `firebase-crashlytics` (uploads R8 mapping files so minified release traces deobfuscate). A library module cannot do either for its host.

`frnk.android.firebase` closes that gap as the first host-facing convention plugin. It is conditional on the host's `google-services.json` being present, so CI and fresh clones still build and the `runCatching`-wrapped bindings degrade to a logged no-op.

**Deliberately configuration-free.** An earlier iteration had a `frnk.crashlytics.debug` Gradle property plus per-build-type `manifestPlaceholders` and `mappingFileUploadEnabled`. All of it was removed: Crashlytics collection already defaults to on for every build type, and mapping upload already defaults to on wherever a mapping exists (release only — debug is not minified, so the upload task is never even created). The SDK defaults were exactly the desired behaviour, so the config was pure surface area.

**Host cost is one line**: `pluginManagement { includeBuild("frnk/build-logic") }`. Verified that this coexists with the top-level `includeBuild("frnk")` composite even though frnk also includes `build-logic` from its own `pluginManagement` — Gradle dedupes by build directory. This was the main risk and it was spiked before committing to the design.

**Side benefit:** frnk's catalog becomes the single source of truth for the `google-services` / `firebase-crashlytics` plugin versions; hosts delete their own declarations, removing a drift bug.

**Gotcha:** `uploadCrashlyticsMappingFileRelease` sits inside the `assembleRelease` task graph, so local release builds attempt a network upload. Use `-x uploadCrashlyticsMappingFileRelease` to skip.

### Files
- build-logic/src/main/kotlin/frnk.android.firebase.gradle.kts
- build-logic/build.gradle.kts
