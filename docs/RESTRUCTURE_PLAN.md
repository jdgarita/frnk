# frnk Restructure Plan — flat `shared-*` → layered `frnk/{core,data,ui,capabilities}` + `demo/`

> Status: **approved, not started**. Each stage below is an independent unit of work (one frnk PR +
> one still PR). Update the checkbox + "Landed" column as stages ship.

## 1. Goal

Restructure the toolkit from today's flat module list (13 `shared-*` modules under `shared/` +
`:shared`/`:androidApp`/`:iosApp` aggregators + demo hosts) into:

```
frnk-repo (root)
├── build-logic                 # unchanged location; convention plugins evolve in place
├── demo                        # unified host apps (pure consumers)
│   ├── android-app             # ← androidDemoApp
│   ├── ios-app                 # ← iosDemoApp
│   └── shared                  # ← shared-demo (AppScaffold impl, dummy content, DemoKit.xcframework)
└── frnk                        # the toolkit root
    ├── core
    │   ├── mvi                 # MviContract, MviViewModel, UiText
    │   ├── nav                 # Compose-free Nav3 contract (deviation from original sketch — see §3)
    │   ├── di                  # Koin helpers/extensions (new scaffold)
    │   └── util                # AppResult, Logger, DateTimeFormat, PlatformInfo, FeedbackEmail
    ├── data
    │   ├── db-api              # SqlDriverFactory SPI (NoteStore is demo-only → demo/shared, OQ-2)
    │   ├── db-impl             # platform SQLDelight driver wiring (Android/Native/JDBC)
    │   ├── prefs-api           # KeyValueStore, Preference<T>
    │   └── prefs-impl          # multiplatform-settings (SettingsKeyValueStore)
    ├── ui
    │   ├── theme               # FrnkTheme, FrnkThemeConfig, tokens, FrnkStrings/Icons/Ripple
    │   ├── components          # atoms + molecules + organisms + placeholder
    │   ├── bottom-nav          # quarantined Material3/Calf module (UIKit on iOS) + FrnkAppShell
    │   ├── scaffolds           # FrnkScreenScaffold, Onboarding/Settings/BottomNav scaffolds,
    │   │                       #   Compose MVI bindings (FrnkMviScreen, EffectCollector),
    │   │                       #   Compose nav wiring (FrnkNavDisplay, tabbed stacks, animations)
    │   └── app                 # FrnkAppScaffold — batteries-included apex (OQ-7); Material3
    │                           #   arrives transitively via bottom-nav; hand-wiring hosts skip it
    └── capabilities
        ├── analytics-api       # AnalyticsTracker, CrashReporter contracts + no-ops
        ├── analytics-impl      # Firebase analytics/crashlytics (CrashKiOS in iosMain)
        ├── remote-config-api   # RemoteConfigService contract (OQ-1; separate from analytics)
        ├── remote-config-impl  # Firebase Remote Config bindings
        ├── monetization-api    # EntitlementProvider/Manager, FeatureGate, …
        ├── monetization-impl   # RevenueCat bindings (Compose-free)
        ├── monetization-ui     # PaywallScreen + subscription management (Compose)
        ├── haptics             # HapticFeedback/HapticType contract + multihaptic Compose engine
        ├── camera              # scaffold only (api + no-op), no impl yet
        └── permissions         # scaffold only (api + no-op), no impl yet
```

Hard requirement: **still (the host app) must build at every stage boundary.** still consumes frnk
as a git submodule + composite build (`includeBuild("frnk")`); substitution matches by
`group:name` where group = `dev.jdgarita.frnk` (hardcoded in `build-logic/frnk.kmp.base.gradle.kts`)
and name = the Gradle project name.

## 2. Locked decisions

| # | Decision |
|---|----------|
| D1 | **Drop `AuthService` entirely.** Remote data is repurposed as **Firebase Remote Config** → `shared-backend-supabase` is deleted. The Remote-Config reshape itself is deferred to Stage 11 (see OQ-1). |
| D2 | **Drop the aggregators** `:shared`, `:androidApp`, `:iosApp` (FrnkKit.xcframework). Consumers depend on individual coordinates. `DemoKit.xcframework` already lives in the demo shared module and stays there. |
| D3 | **Keep the monetization logic/UI split**: `monetization-api` + `monetization-impl` (RevenueCat, Compose-free) + `monetization-ui` (paywall Compose). The original sketch's single "monetization-impl: RevenueCat UI & Logic" is intentionally not followed. |
| D4 | **camera / permissions are scaffold-only** (interfaces + no-op defaults). Implementations are future feature work, out of scope here. Haptics *consolidation* (contract from `shared-ui-api` + engine from `shared-ui-atoms` → one capability module) IS in scope. |
| D5 | **Add `core/nav`** (deviation from the sketch). The Nav3 contract (`ToolkitRoute`, `NavBackStackExt`, `FrnkNavConfig`, `FrnkPendingRouteRequest`, `FrnkFullScreenRoute`) is Compose-free and consumed by ViewModels emitting route effects; parking it in `ui/scaffolds` would drag `compose.runtime` into every feature ViewModel. `ui/scaffolds` keeps the Compose half (`FrnkNavDisplay`, tabbed back stacks, animations). |
| D6 | **Create `core/di`.** The existing `ToolkitDiModule` (`dev.jdgarita.frnk.di`, in `shared-monetization-api`) is a vestigial stub — both actuals return `emptyList()` — and is deleted. `core/di` is scaffolded as the future home of real Koin helpers/extensions (see OQ-5 for seeding content). DI aggregation stays **host-side**: hosts compose per-capability Koin modules explicitly (still already does — it imports `revenueCatModule` directly and never calls `frnkModules`). |
| D7 | **Toolkit modules live under a literal `frnk/` subdirectory** of the repo root (`frnk-repo/frnk/core/mvi/…`), alongside `demo/` and `build-logic/`. From still's checkout the path is `frnk/frnk/core/…`. |
| D8 | **Kotlin packages `dev.jdgarita.frnk.*` do not change.** This is a module-layout restructure, not a package rename: still has ~180 frnk imports, substitution is independent of packages, and unchanged packages keep `git mv` rename detection (reviewable diffs). A package-rename initiative, if ever, is separate and later. |

## 3. Naming scheme & module map

Gradle project names must be unique across the build, and the **artifact name = project name**
drives composite-build substitution. We keep **flat, hyphenated project names** with nested
physical directories, reconciled by `projectDir` remaps in `settings.gradle.kts` — the exact
pattern the repo already uses for `shared/<name>` today.

> ✅ **OQ-6 resolved (2026-06-10): flat descriptive names everywhere; PR #47's nested paths are
> overridden.** Rationale: composite-build substitution matches `group:name` where *name is the
> leaf project name* — `:shared:backend:api` advertises `dev.jdgarita.frnk:api`, and generic
> leaves (`api`, `impl`) collide in coordinate space as soon as a second nested domain uses them.
> Consequence: Stage 5's rename of `:shared:backend:*` → `:analytics-api`/`:analytics-impl` is
> also the re-flattening (no other nested groups may be introduced before then), and the
> `frnk.backend.*` convention plugins fold back into the standard `frnk.kmp.*` set at Stage 5.

| New project (= artifact `dev.jdgarita.frnk:<name>`) | Directory | Origin |
|---|---|---|
| `:core-util` | `frnk/core/util` | `shared-utils` (AppResult, Logger, DateTimeFormat, PlatformInfo, FeedbackEmail) |
| `:core-mvi` | `frnk/core/mvi` | `shared-ui-api` → `ui/mvi/*` + `UiText.kt` |
| `:core-nav` | `frnk/core/nav` | `shared-ui-api` → `ui/nav/*` (Compose-free contract) |
| `:core-di` | `frnk/core/di` | host-facing Koin assembly helpers (OQ-5/OQ-7): slimmed `initializeFrnk(context, modules)`, fail-fast assertion, androidMain context absorption; replaces deleted `ToolkitDiModule` stub |
| `:data-db-api` | `frnk/data/db-api` | `shared-database-api` → SqlDriverFactory SPI (NoteStore → `demo/shared`, OQ-2) |
| `:data-db-impl` | `frnk/data/db-impl` | `shared-database-impl` → platform driver wiring (Note.sq + SqlDelightNoteStore → `demo/shared`, OQ-2) |
| `:data-prefs-api` | `frnk/data/prefs-api` | `shared-database-api` → KeyValueStore, Preference |
| `:data-prefs-impl` | `frnk/data/prefs-impl` | `shared-database-impl` → SettingsKeyValueStore |
| `:ui-theme` | `frnk/ui/theme` | `shared-ui-atoms` → `ui/theme/*` + `ui/tokens/*` |
| `:ui-components` | `frnk/ui/components` | `shared-ui-atoms` → `ui/atoms/*`, `ui/molecules/*`, `ui/organisms/*`, `ui/placeholder/*` |
| `:ui-scaffolds` | `frnk/ui/scaffolds` | `shared-ui-atoms` → `ui/scaffolds/*` + Compose `ui/mvi/*` (FrnkMviScreen, EffectCollector) + Compose `ui/nav/*` (FrnkNavDisplay, FrnkNavTab, FrnkTabbedBackStacks/-Handler, animations) |
| `:ui-bottom-nav` | `frnk/ui/bottom-nav` | `shared-ui-nav` (Calf/Material3 quarantine — unchanged rule: Material3 nowhere else; keeps `FrnkAppShell`/`FrnkAppScope`) |
| `:ui-app` | `frnk/ui/app` | `:shared` → `FrnkAppScaffold` + `frnkUiModules()` (OQ-7); apex of the ui column, no `*-impl` compile deps |
| `:remote-config-api` | `frnk/capabilities/remote-config-api` | `:shared:backend:api` → `RemoteData.kt` reshaped to `RemoteConfigService` (OQ-1) |
| `:remote-config-impl` | `frnk/capabilities/remote-config-impl` | `:shared:backend:firebase` → Firestore remote-data impl replaced by Firebase Remote Config |
| `:analytics-api` | `frnk/capabilities/analytics-api` | `:shared:backend:api` (post Auth drop: Analytics.kt, NoopObservability.kt, RemoteData.kt pending OQ-1) |
| `:analytics-impl` | `frnk/capabilities/analytics-impl` | `:shared:backend:firebase` (FirebaseAnalyticsTracker, FirebaseCrashReporter, CrashKiOS iosMain) |
| `:monetization-api` | `frnk/capabilities/monetization-api` | `shared-monetization-api` (minus `di/`) |
| `:monetization-impl` | `frnk/capabilities/monetization-impl` | `shared-monetization-revenuecat` |
| `:monetization-ui` | `frnk/capabilities/monetization-ui` | `shared-monetization-ui` |
| `:haptics` | `frnk/capabilities/haptics` | `shared-ui-api` → `ui/haptics` contract **+** `shared-ui-atoms` → `ui/haptics` engine (FrnkHaptics, MultiHapticEngine) |
| `:camera` | `frnk/capabilities/camera` | new, api scaffold only |
| `:permissions` | `frnk/capabilities/permissions` | new, api scaffold only |
| `:demo-shared` | `demo/shared` | `shared-demo` (keeps DemoKit.xcframework production) |
| `:demo-android` | `demo/android-app` | `androidDemoApp` |
| (Xcode, not Gradle) | `demo/ios-app` | `iosDemoApp` |

Per-module Android `namespace` must stay unique (e.g. `dev.jdgarita.frnk.ui.scaffolds`); two
modules owning slices of the same Kotlin package (`ui.mvi` contract in `core-mvi`, Compose binding
in `ui-scaffolds`) is legal and already the case today.

iOS framework `baseName` auto-derives from the project name (`-`→`_`) in
`frnk.kmp.library.gradle.kts`, so renames change framework names — cosmetic once FrnkKit is gone
and DemoKit links via Gradle, but see OQ-4.

### Dependency rules (enforce in review; codify in ARCHITECTURE.md at Stage 12)

```
core-util ← everything
core-mvi, core-nav, core-di:    no Compose, no upward deps (core-mvi ← core-util)
data-*-api ← data-*-impl;        capabilities may depend on data-*-api, never on a *-impl
haptics ← ui-theme ← ui-components ← ui-scaffolds ← ui-bottom-nav ← ui-app
ui-app ← {monetization-ui, analytics-api}   # resolves impls via Koin at runtime, never at compile time
monetization-api ← {analytics-api, data-prefs-api}
monetization-ui  ← {ui-scaffolds, monetization-api}
remote-config-api ← remote-config-impl      # sibling of analytics, never merged into it
Material3 only in ui-bottom-nav (ui-app inherits it transitively — the accepted batteries-included trade)
Only demo modules may depend on *-impl modules from code; hosts wire impls via Koin modules only
```

The haptics direction is load-bearing: atoms call `LocalFrnkHaptics`, `FrnkTheme` installs it —
so `:haptics` (contract **and** engine) must sit *below* `ui-theme` and must not depend on any ui
module.

## 4. What still consumes (the blast-radius boundary)

still declares exactly **5 version-less coordinates** (`still/gradle/libs.versions.toml:144-148`):

| still catalog entry | Used by still module | Replaced at Stage 9 by |
|---|---|---|
| `frnk-shared-ui-api` | `:shared` | `frnk-core-mvi` + `frnk-core-nav` |
| `frnk-shared-ui-atoms` | `:core-design-system` | `frnk-ui-theme` + `frnk-ui-components` + `frnk-ui-scaffolds` |
| `frnk-shared-ui-nav` | `:shared` | `frnk-ui-bottom-nav` |
| `frnk-shared-monetization-api` | `:core-data` | `frnk-monetization-api` |
| `frnk-shared-monetization-revenuecat` | `:shared`, `:androidApp` | `frnk-monetization-impl` |
| *(none — transitive leak)* | `:core-data` imports `dev.jdgarita.frnk.utils.AppResult` | **add** `frnk-core-util` |

Renaming/splitting any module *not* in this table is invisible to still (transitive project deps
are substituted automatically). All still-visible changes are deliberately concentrated into a
single stage (Stage 9). Zero Kotlin import changes in still at any stage (D8).

## 5. Mechanical split recipe (keeps every intermediate green)

Per split, in a single commit:

1. Add the new module: directory + `build.gradle.kts` (convention plugins + namespace + deps),
   `include(":new-name")` + `projectDir` remap in `settings.gradle.kts`.
2. `git mv` whole package directories, preserving `src/<sourceSet>/kotlin/…` structure. **Tests
   and `commonDebug` previews travel with their subjects** in the same commit.
3. The old module's build file drops the moved third-party deps and gains
   `api(projects.<newModule>)` — it becomes (or trends toward) a **facade**. Facades preserve the
   old coordinate for still until the flip; `api()` keeps the transitive classpath identical.
4. Run frnk verification (below). Because packages never change, no source file is edited except
   build scripts — `git mv` rename detection keeps review trivial.

Facade modules (`shared-ui-api`, `shared-ui-atoms`) are deleted only at Stage 9, after still flips.

### Per-stage verification (default; deltas noted per stage)

```bash
# frnk repo
./gradlew compileAndroidMain :androidDemoApp:compileDebugKotlin --parallel   # what CI runs
./gradlew testAndroidHostTest :androidDemoApp:testDebugUnitTest --parallel   # what CI runs
./gradlew compileKotlinIosSimulatorArm64                                     # iosMain coverage (local Mac; CI is Linux)

# still repo (after bumping the frnk submodule pin to the stage's frnk commit)
./gradlew assembleDebug && ./gradlew :shared:allTests
```

Every stage = **one frnk PR + one still PR** (submodule pin bump; catalog edits only at Stage 9).
The still PR is the real gate: a stage isn't done until still is green at the new pin.

## 6. Stages

### ☑ Stage 1 — Delete the aggregators (`:shared`, `:androidApp`, `:iosApp`)  — risk: **medium** (was low-med; raised by PR #46) — **landed 2026-06-11**

- **OQ-7 resolved (2026-06-10)** — `:shared` is no longer wiring-only (PR #46 added
  `FrnkAppScaffold` + the substantive bootstrap), so this stage relocates before deleting:
  - **`FrnkAppScaffold` → new `:ui-app`** (`frnk/ui/app`; until Stage 3, dir can sit at
    `shared/ui-app`): deps `ui-bottom-nav` (composes `FrnkAppShell`) + `monetization-ui` +
    `analytics-api` — at this stage still spelled `:shared-ui-nav`/`:shared-monetization-ui`/
    `:shared:backend:api`; resolves `EntitlementManager`/`AnalyticsTracker` from Koin at runtime,
    **no `*-impl` compile deps**. Also exposes `frnkUiModules()` (the SDK-free scaffold VM modules
    `frnkModules()` installs unconditionally today).
  - **Slimmed bootstrap → new `:core-di`** (created here, not Stage 8): generic
    `initializeFrnk(context, modules: List<Module>)` (Koin start + `androidContext` +
    `DatabaseContext.application` absorption; androidMain gets the `koin-android` dep), plus the
    fail-fast Koin assertion `FrnkAppScaffold` uses. **The choice enums
    (`BackendChoice`/`ObservabilityChoice`/`MonetizationChoice`) and `frnkModules()` are retired** —
    hosts pass an explicit module list: `initializeFrnk(context, frnkUiModules() +
    revenueCatModule + firebaseObservabilityModule + …)`. Copy-paste snippet goes in
    `docs/HOST_INTEGRATION.md` in the same PR.
- Pre-flight: repo-wide grep for `frnkModules|initializeFrnk|FrnkAppScaffold|FrnkKit|projects.shared\b|projects.androidApp` to find hidden consumers (`:shared-demo` uses `FrnkAppScaffold` as of #46 — re-point it to `:ui-app`).
- Then delete `shared/src/` + `shared/build.gradle.kts` (the `shared/` directory keeps housing the
  other modules until Stage 3), `androidApp/`, `iosApp/`; remove the three from
  `settings.gradle.kts`.
- **Per OQ-3/OQ-4**: also drop the per-module iOS framework declarations from
  `frnk.kmp.library` (keep the bare iOS targets). iOS consumption is umbrella-only — `DemoKit`
  (explicit `XCFramework` in the demo shared module) for the demo, and each host's own shared-module
  framework (e.g. still's `FreshTrackKit`) for hosts; no per-module `.framework`s, no FrnkKit.
- `androidDemoApp/build.gradle.kts`: replace `implementation(projects.androidApp)` with the
  individual modules it actually uses (it imports `firebaseObservabilityModule`, `revenueCatModule`,
  `FrnkTheme`; most arrive transitively via `:shared-demo` — add explicit deps as the compiler demands).
- Delete `ProjectConfiguration.IOS_FRAMEWORK_NAME`; strip FrnkKit from `docs/RELEASING.md`,
  `docs/HOST_INTEGRATION.md`, root `CLAUDE.md` commands.
- still impact: **none** (no consumed coordinate touched). Pin bump only.
- Extra verification: `./gradlew :shared-demo:assembleDemoKitDebugXCFramework`.

### ☑ Stage 2 — Drop Auth + Supabase  — risk: low — **landed 2026-06-10**

- Delete `:shared:backend:supabase` (dir `shared/backend/supabase/`) entirely + its
  `frnk.backend.supabase` convention plugin; delete `Auth.kt` + auth fakes from
  `:shared:backend:api`; delete `FirebaseAuthService.kt` from `:shared:backend:firebase`.
- Pre-flight: grep `shared-demo` and demo hosts for `AuthService`/`FakeAuthService` usage.
- Purge Supabase from `gradle/libs.versions.toml`, `local.properties.template`
  (`SUPABASE_URL`, `SUPABASE_ANON_KEY`), and CI's `local.properties` seeding block in
  `.github/workflows/main.yml` (lines 43–44). Note: no code reads these keys — BuildKonfig was
  removed at some point and root `CLAUDE.md` is stale about it (fix at Stage 12).
- `RemoteData.kt` / `FirestoreRemoteData` stay in place untouched — the Remote-Config repurpose is
  Stage 11 (behavioral change, kept out of the structural migration).
- still impact: none.

### ☑ Stage 3 — Physical re-root, zero renames  — risk: low — **landed 2026-06-11**

- `git mv` every module directory to its **final** location per the §3 table, **keeping all Gradle
  project names unchanged** (e.g. `:shared-ui-atoms` temporarily points at `frnk/ui/components`;
  `:shared-demo` at `demo/shared`). Rewrite the `projectDir` remap block in `settings.gradle.kts`.
  The `shared/` directory disappears. `androidDemoApp` → `demo/android-app`, `iosDemoApp` →
  `demo/ios-app` directories move now too (project names rename later, Stage 10).
- Modules that later *split* (ui-api, ui-atoms, database-*) go to the dir of their **largest
  surviving fragment** (`shared-ui-api` → `frnk/core/mvi`? No — keep it simple: park un-split
  modules at the dir matching the §3 table row of their main successor; splits create the sibling
  dirs in their own stages).
- iosDemoApp Xcode framework search paths: update for the moved `demo/shared` DemoKit output path
  (or defer to Stage 10 if the move is split — do **not** move `iosDemoApp`/`shared-demo` here in
  that case; keep this stage internally consistent).
- `./gradlew clean` before verifying — stale `build/` dirs can confuse SQLDelight/BuildKonfig
  generated-source paths.
- still impact: none (substitution is by name, not path).

### ☑ Stage 4 — Data split: db vs prefs (+ NoteStore → demo, OQ-2)  — risk: medium — **landed 2026-06-11**

- `shared-database-api` → `:data-db-api` (SqlDriverFactory SPI) + new `:data-prefs-api`
  (KeyValueStore, Preference). `shared-database-impl` → `:data-db-impl` (platform driver wiring:
  AndroidSqliteDriver / NativeSqliteDriver / JDBC-for-host-tests) + `:data-prefs-impl`
  (SettingsKeyValueStore).
- **OQ-2 resolved**: `NoteStore`, `Note.sq`, `SqlDelightNoteStore` are demo scaffolding — move
  them into the demo shared module (which gains the SQLDelight plugin + a demo-owned schema,
  consuming `:data-db-api`'s `SqlDriverFactory` exactly the way a real host would). The JDBC
  round-trip tests follow the schema to the demo module; driver-factory tests stay in
  `:data-db-impl`.
- Split the Koin `databaseModule` into `databaseModule` + `prefsModule`; if demo wiring references
  the old name, keep a `@Deprecated` combined module for one stage.
- Re-point `monetization-api`'s `api(projects.sharedDatabaseApi)` → `api(projects.dataPrefsApi)`
  (god-mode persistence only needs KeyValueStore — verify by grep before assuming).
- Host tests move with their halves (`:data-db-impl` keeps the JDBC-driver round-trip tests).
- still impact: none (transitive substitution follows project deps).

### ☑ Stage 5 — backend → analytics capability  — risk: low — **landed 2026-06-11**

- Move/rename `:shared:backend:api` → `:analytics-api`, `:shared:backend:firebase` →
  `:analytics-impl` (CrashKiOS iosMain cinterop rides along); drop the now-empty `:shared:backend`
  parent. Fold/rename the `frnk.backend.*` convention plugins accordingly (subject to OQ-6's
  flat-vs-nested resolution). Update `monetization-api`'s project dep.
- Koin module names (`firebaseObservabilityModule`, `noopObservabilityModule`) unchanged.
- still impact: none (still never declared these coordinates).

### ☑ Stage 6 — Split `shared-ui-api` → `:core-mvi`, `:core-nav`, `:haptics`  — risk: low — **landed 2026-06-11**

- New `:core-mvi` (`ui/mvi/*`, `UiText.kt`, MviViewModelTest; keeps `frnk/core/mvi`), `:core-nav`
  (`ui/nav/*` + tests; keeps `androidx-navigation3-runtime` + `kotlinx-serialization-core` + the
  serialization plugin), `:haptics` (contract only for now: HapticType, HapticFeedback, Default/NoOp +
  test; Compose-free, `frnk/capabilities/haptics`).
- `shared-ui-api` becomes an empty facade: no `src/`, just
  `commonMain.dependencies { api(core-mvi); api(core-nav); api(haptics) }` — preserving the exact
  transitive classpath (incl. the `shared-utils` export via `core-mvi`). Parked at
  `frnk/core/ui-api-facade` (two projects can't share `frnk/core/mvi`). still's coordinate keeps
  resolving; its imports untouched.
- `:shared-ui-atoms` keeps depending on the facade (still-invisibility proof); `demo/shared` re-points
  its iOS `export(...)` + `api(...)` to the three successors (Kotlin/Native `export` is non-transitive,
  so the src-less facade would carry no Swift symbols).

### ☑ Stage 7 — Split `shared-ui-atoms` (the big one)  — risk: **high**; 2–3 frnk PRs — **landed 2026-06-11**

The facade pattern keeps every intermediate green; landed as two frnk commits (7a, then 7b).

**Outcome notes (as built):**
- **FrnkSectionCard landmine → promoted to public** in `:ui-components` (not the wrapper option) —
  cleanest since organisms (`:ui-components`) and the Settings scaffold (`:ui-scaffolds`) now sit in
  different modules.
- **`PreviewSurface` collision:** the per-module preview helper copy in `:ui-scaffolds` had to move to
  its own `…ui.scaffolds.previews` package — an identical signature in the original `…ui.atoms.previews`
  collides at the Kotlin/Native link step ("symbol already bound") because `commonDebug` feeds `iosMain`
  and DemoKit links both klibs. `RobolectricComposeTest`/`setFrnkContent` are `androidHostTest`-only
  (JVM), so their per-module copy keeps the original package with no collision.
- **ripple-indication / icons-lucide landed in `:ui-theme`/`:ui-components` per actual usage**, not the
  plan's tentative split: `:ui-theme` uses both (FrnkRipple + FrnkIcons defaults); `:ui-components` keeps
  `icons-lucide` only for the FrnkBottomNavBar `@Preview`.
- **`:shared-monetization-ui` re-pointed to `:ui-scaffolds`** → its direct deps are now exactly
  `{ui-scaffolds, monetization-api}` (the Stage 8 precondition, verified). `:shared-ui-nav` stays on the
  `:shared-ui-atoms` facade as the still-invisibility proof.

The facade pattern keeps every intermediate green; land as separate PRs, each pin-bumped in still.

- **7a** — extract `:ui-theme` (`ui/theme/*` + `ui/tokens/*`); move the multihaptic engine
  (`FrnkHaptics.kt`, `MultiHapticEngine.kt`) into `:haptics`, which now applies
  `frnk.kmp.library.compose` + the multihaptic deps. `ui-theme` depends on `:haptics` (it installs
  `LocalFrnkHaptics`). Atoms facades both via `api()`.
- **7b** — opening commit: extract a **`frnk.kmp.library.composehosttest`** convention plugin in
  `build-logic` deduplicating atoms' manual `withHostTest { isIncludeAndroidResources = true }` +
  `androidHostTest` Robolectric deps + the `commonDebug` previews source-set wiring
  (`dependsOn` edges to androidMain + both iOS source sets). Then extract:
  - `:ui-components` — `ui/atoms`, `ui/molecules`, `ui/organisms`, `ui/placeholder`; deps:
    `ui-theme`, the granular compose-unstyled artifacts, ripple-indication, icons-lucide.
  - `:ui-scaffolds` — `ui/scaffolds`, Compose `ui/mvi` (FrnkMviScreen, EffectCollector), Compose
    `ui/nav` (FrnkNavDisplay, FrnkNavTab, FrnkTabbedBackStacks/-Handler, animations); deps:
    `ui-components`, nav3 ui/viewmodel, koin-compose(+viewmodel,+navigation3),
    lifecycle-runtime-compose, backhandler.
  - `shared-ui-atoms` reduces to a facade `api()`-ing theme/components/scaffolds (+haptics).
- Keep today's `api()` exports (compose runtime/foundation/ui via the `.compose` plugin; nav3/koin/
  lifecycle) in whichever split module their consumers need — preserve still's transitive classpath
  exactly.
- Known landmine: `FrnkSectionCard` is `internal`, shared by organisms **and** the Settings
  scaffold. Options: promote to public in `:ui-components`, or keep internal + move scaffolds' use
  behind a public wrapper. Decide in-PR; do not duplicate the file.
- Compose UI tests (`androidHostTest`) and `commonDebug` previews split per-file with their
  subjects.
- still impact: none yet (facade). Budget for iteration — this is the largest module with the most
  interleaved internals.

### ☑ Stage 8 — Monetization tidy  — risk: low — **landed 2026-06-11**

- Delete `di/ToolkitDiModule.kt` + platform actuals from `monetization-api` (vestigial — both
  actuals return `emptyList()`; pre-flight grep `toolkitCoreModules|frnk.di` in frnk **and** still).
- `:core-di` already exists (created at Stage 1 with the slimmed bootstrap, per OQ-5/OQ-7);
  fold in any further host-facing Koin assembly helpers that surfaced during Stages 4–7.
- Verify `monetization-ui` deps are exactly `{ui-scaffolds, monetization-api}` after Stage 7.
- still impact: none.

### ☐ Stage 9 — The coordinate flip (the only still-churn stage)  — risk: medium, mechanical

- frnk: delete facades `:shared-ui-api`, `:shared-ui-atoms`; rename `:shared-ui-nav` →
  `:ui-bottom-nav`, `:shared-monetization-api` → `:monetization-api`,
  `:shared-monetization-revenuecat` → `:monetization-impl`. Update all internal project deps +
  typesafe accessors (`projects.sharedUiNav` → `projects.uiBottomNav`, …).
- still (`gradle/libs.versions.toml` + the 4 consuming build files; **zero Kotlin changes**):
  - `frnk-shared-ui-api` → `frnk-core-mvi` + `frnk-core-nav`  (`:shared`)
  - `frnk-shared-ui-atoms` → `frnk-ui-theme` + `frnk-ui-components` + `frnk-ui-scaffolds`
    (`:core-design-system`; scaffolds covers the `LocalFrnkBottomBarInset` import)
  - `frnk-shared-ui-nav` → `frnk-ui-bottom-nav`  (`:shared`)
  - `frnk-shared-monetization-api` → `frnk-monetization-api`  (`:core-data`)
  - `frnk-shared-monetization-revenuecat` → `frnk-monetization-impl`  (`:shared`, `:androidApp`)
  - **add** `frnk-core-util` to `:core-data` (kills the `AppResult` transitive leak)
- Extra verification in still:
  `./gradlew :shared:dependencies --configuration debugRuntimeClasspath | grep dev.jdgarita.frnk`
  — confirm every frnk artifact substitutes to a project.
- Failures can only be missing-coordinate compile errors — everything already builds at its new home.

### ☐ Stage 10 — Demo project renames + Xcode  — risk: medium (Xcode not CI-covered)

- Rename projects `:shared-demo` → `:demo-shared`, `:androidDemoApp` → `:demo-android` (dirs moved
  at Stage 3). Update typesafe accessors; `demo-shared`'s iosMain
  `implementation(projects.sharedMonetizationRevenuecat)` → `projects.monetizationImpl`.
- iosDemoApp: re-check framework search paths → `demo/shared/build/XCFrameworks/...`; DemoKit
  baseName unchanged (explicit `XCFramework("DemoKit")`).
- CI `.github/workflows/main.yml`: `:androidDemoApp:` task prefixes → `:demo-android:`.
- Manual verification: build + run `demo/ios-app` in a simulator; install `:demo-android` on a
  device/emulator.
- still impact: none.

### ☐ Stage 11 — Remote Config capability + new capability scaffolds  — risk: low (additive)

- Scaffold `:camera` + `:permissions`: api-only modules (interfaces + no-op defaults,
  `frnk.kmp.library.hosttest`), no impl (D4).
- **Remote Config (OQ-1 resolved)**: own capability pair `:remote-config-api` /
  `:remote-config-impl` under `frnk/capabilities/` — **sibling of analytics, kept separate from
  it**. Reshape `RemoteDataService` → `RemoteConfigService`; replace `FirestoreRemoteData` with a
  Firebase Remote Config impl exposed as a Koin module. Note still already does Remote Config
  natively on Android (`:androidApp`, legal-URL resolution) — candidate first consumer.

### ☐ Stage 12 — CI + docs consistency sweep  — risk: low

- `docs/ARCHITECTURE.md`: full rewrite of the module graph + the §3 dependency rules.
- `docs/HOST_INTEGRATION.md`: new coordinates table, explicit Koin assembly snippet, updated
  composite-build instructions.
- Root + per-module `CLAUDE.md`s, `README.md`, `HOST_ALIGNMENT.md`, `BACKLOG.md`: paths, commands,
  the retired "implement every backend call in both Firebase and Supabase" convention, retired
  FrnkKit/`:shared` references.
- Load-bearing doc edits (commands, paths) still happen incrementally in each stage; this stage is
  the consistency pass.

## 7. Granularity

12 stages ≈ 11 still pin-bump PRs (Stage 7 lands as 2–3 frnk PRs, which may share a pin bump).
Compression floor if fewer PRs are wanted: merge 1+2 (pure deletions), 4+5 (still-invisible
renames), fold 8 into 9 → **~7 still PRs**. Never merge Stage 7 with anything else; keep Stage 9
isolated and small.

## 8. Open questions — **all resolved 2026-06-10**

| # | Resolution |
|---|------------|
| OQ-1 | Remote Config = own capability pair `:remote-config-api`/`:remote-config-impl` under `capabilities/`, **sibling of analytics, kept separate from it** (Stage 11). |
| OQ-2 | `NoteStore`/`Note.sq`/`SqlDelightNoteStore` are demo-only → move to the demo shared module at Stage 4. `data/db-*` keeps the `SqlDriverFactory` SPI + platform driver wiring; the demo consumes it like a real host. |
| OQ-3 | iOS consumption is **umbrella-only for demo AND hosts**: `DemoKit` for the demo, each host's own shared-module framework (e.g. still's `FreshTrackKit`) for hosts. Per-module framework declarations dropped from `frnk.kmp.library` at Stage 1 (bare iOS targets remain). |
| OQ-4 | Ignore still for now — no baseName pre-check needed (moot anyway: per-module frameworks die at Stage 1 per OQ-3). |
| OQ-5 | `:core-di` = **host-facing Koin assembly helpers**: slimmed `initializeFrnk(context, modules)`, fail-fast Koin assertion, androidMain context absorption. Created at Stage 1 (not 8) since the bootstrap relocation needs it. |
| OQ-6 | Flat descriptive project names everywhere; PR #47's nested `:shared:backend:*` paths overridden (re-flattened at Stage 5, `frnk.backend.*` plugins folded back into `frnk.kmp.*`). See the ✅ note in §3. |
| OQ-7 | `FrnkAppScaffold` → new top-of-stack `:ui-app` (no `*-impl` compile deps; resolves via Koin). Choice enums + `frnkModules()` retired; explicit module lists + `frnkUiModules()` instead. See Stage 1. |
