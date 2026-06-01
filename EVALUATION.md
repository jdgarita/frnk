# frnk — Current-State Evaluation

> **Date of analysis:** 2026-05-29
> **Method:** Static inspection of the working tree (`main` @ `c9c71e4`) against
> `REQUIREMENTS.md`. Findings are evidence-based — file paths and line markers are
> cited so each claim is verifiable.
>
> Legend: ✅ Done · 🟡 Partial / skeleton · ⛔ Missing · ⚠️ Tech debt / deviation

This document grades the toolkit against `REQUIREMENTS.md`. The remediation work
is enumerated, prioritized, and given acceptance criteria in `BACKLOG.md`.

---

## 1. Executive summary

`frnk` has a **strong, correct skeleton and an unusually mature design-system /
build foundation**, with the hard architectural invariants already enforced. The
risk is concentrated in the **data and integration layers**: the backend auth/remote
impls and RevenueCat are wired structurally but are **not functionally implemented**.
SQLDelight persistence is now functionally complete (P1-1: `FrnkDB` + `NoteStore`,
round-trip tested), and **Firebase analytics + crash reporting are now implemented and
decoupled into a backend-independent `ObservabilityChoice` axis** (P1-5). The backend
**auth + remote-data** impls (P1-2/3/4) are intentionally **deferred** while consuming
apps are local-storage-only. The remaining headline gaps are **RevenueCat** and the
**navigation system**.

| Area | Grade | One-line verdict |
| --- | --- | --- |
| Module graph & api/impl split | ✅ | Fully realized and enforced |
| Toolchain pinning (JDK17/Kotlin/AGP9) | ✅ | Correct and documented |
| `:shared` aggregator + `BackendChoice` | ✅ | Bootstrap present and coherent |
| Koin DI | ✅ | Per-module Koin modules + assembly |
| MVI engine | ✅ | Present, Compose-free as required |
| Design system (tokens/theme/atoms/scaffolds) | ✅ | Substantial; ~10 atoms, 4 scaffolds, skeleton, ripple |
| **No-Material constraint** | ✅ | Zero violations found |
| Local preferences (KeyValueStore) | ✅ | Backed by multiplatform-settings |
| Demo across 3 layers | ✅ | shared-demo + android + ios harnesses present |
| **Backend auth/remote (Firebase/Supabase)** | 🟡 | Interfaces real; **impls are `TODO()`** — deferred (local-only) |
| **Observability (analytics + crash)** | ✅ | Firebase impls done + decoupled into `ObservabilityChoice` (P1-5) |
| **Monetization (RevenueCat)** | 🟡 | Gate + fake work; **real manager is a `TODO()` skeleton** |
| **SQLDelight persistence** | ✅ | `FrnkDB` + `Note.sq` + `NoteStore`; round-trip tested, demoed (P1-1) |
| **Navigation** | ⛔ | Only a `ToolkitRoute` marker; no NavHost/back stack |
| **Analytics (PostHog)** | 🟡 | Firebase analytics/crash done (P1-5); provider-neutral PostHog still missing (P3-1) |
| Molecules / Organisms (Atomic Design) | ⛔ | Only atoms + scaffolds exist |
| **Automated tests** | 🟡 | Harness seeded (P0-3); MVI/`AppResult`/observability + reducer tests; broad coverage open (P4-4) |
| Documentation accuracy | ⚠️ | `docs/ARCHITECTURE.md` has drifted from code |

---

## 2. The Good — implemented & solid

### 2.1 Architecture & build (✅)
- The full module graph from `docs/ARCHITECTURE.md` exists on disk, with the
  flat-Gradle-path / nested-`shared/`-dir reconciliation in
  `settings.gradle.kts` working as documented.
- The **api/impl split is real and clean**: `*-api` modules contain only
  interfaces; no third-party SDK leaks into any `*-api` (verified — backend-api
  has only `Auth.kt`, `RemoteData.kt`, `Analytics.kt`, `AppResult.kt`).
- Toolchain is pinned exactly as specified (version catalog: Kotlin 2.3.21, AGP
  9.2.1; `ProjectConfiguration.kt` holds SDK + framework-name constants).

### 2.2 `:shared` + DI (✅)
- `:shared` exposes `BackendChoice`, `frnkModules(...)`, `initializeFrnk(...)`
  (`shared/src/.../shared/`), and the demo path has its own
  `bootstrapDemoKoin()` in `:shared-demo`.
- Koin modules exist per concern: `FirebaseBackendModule`,
  `SupabaseBackendModule`, `DatabaseModule`, `RevenueCatModule`, `demoModule`,
  plus per-scaffold modules (`BottomNavScaffoldModule`, `OnboardingScaffoldModule`,
  `SettingsScaffoldModule`).

### 2.3 MVI engine (✅)
- `shared-ui-api` carries `MviContract.kt`, `MviViewModel.kt`, `ToolkitRoute.kt`,
  `UiText.kt` — and **no Compose dependency**, satisfying §2.1.

### 2.4 Design system (✅ — the strongest area)
- **Tokens:** `ColorTokens`, `TypographyTokens`, `SpacingTokens`, `ShapeTokens`,
  `IconSizeTokens`.
- **Theme engine:** `FrnkTheme`, `FrnkThemeConfig`, `FrnkStrings`, `FrnkIcons`,
  `FrnkRipple` (ripple installed as `LocalIndication`).
- **Atoms (~10):** `FrnkText`, `FrnkButton`, `FrnkIcon`, `FrnkIconButton`,
  `FrnkDivider`, `FrnkSwitch`, `FrnkSegmentedControl`, `FrnkBottomNavBar`,
  `FrnkTopAppBar`, plus `FrnkSkeleton` + a full `placeholder/` package
  (Shimmer/Fade/Coordinator).
- **Scaffolds (4+):** `OnboardingScreen`, `SettingsScreen`, `BottomNavScaffold`,
  `FrnkScreenScaffold`, with `CollapsibleBarsState` scroll coordination and
  `FeedbackEmailLauncher`.
- **Previews infra:** dedicated `commonDebug` source set with `PreviewSurface`
  and per-atom/scaffold preview files.
- **No-Material constraint holds:** repo-wide search returns zero Material
  dependencies and zero Material imports; the only matches are comments asserting
  "no Material3".

### 2.5 shared-utils & local prefs (✅)
- `PlatformInfo` (expect/actual), `FeedbackEmail`, `Logger`, `DateTimeFormat`.
- `KeyValueStore` (`shared-database-api`) implemented by `SettingsKeyValueStore`
  over `multiplatform-settings`, with platform `Defaults` — preferences work.

### 2.6 Demo coverage (✅ structurally)
- `:shared-demo` (`DemoScreen`, `DemoViewModel`, `demoModule`,
  `MainViewController`), `androidDemoApp` (`MainActivity`, `DemoApplication`),
  and `iosDemoApp` (Swift `ComposeViewController`/`ContentView`) all present.

---

## 3. The Gaps — missing entirely

### 3.1 SQLDelight persistence (✅ — closed by P1-1, 2026-05-29)
- **Was:** no `.sq` files; `shared-database-impl` wired the Android + native drivers
  but defined no SQLDelight database, and there was no `FrnkDB`.
- **Now:** the SQLDelight Gradle plugin is applied and configured to generate `FrnkDB`
  into `dev.jdgarita.frnk.database.sql`; `Note.sq` defines the first entity; the
  `shared-database-api` `NoteStore` interface exposes typed access returning `AppResult`;
  `databaseModule` builds `FrnkDB` from `SqlDriverFactory` + `FrnkDB.Schema` and binds the
  impl (exposed via `frnkModules`). A `JdbcSqliteDriver.IN_MEMORY` round-trip test
  (`NoteStoreRoundTripTest`) passes, and the demo shows persisted notes (via an in-memory
  fake so DemoKit stays cinterop-free). Requirement §3.4 (relational persistence) is met.

### 3.2 Navigation (⛔)
- **Evidence:** the only navigation type is the `ToolkitRoute` marker in
  `shared-ui-api`; `androidx.navigation` is declared in the catalog but there is
  no `NavHost`, no graph builder, no back-stack ownership. `DemoScreen` does
  ad-hoc navigation locally.
- **Impact:** Requirement §3.3 (tailored navigation) is unmet at the toolkit
  level.

### 3.3 Analytics / PostHog (🟡 — Firebase done by P1-5; PostHog still open)
- **Was:** no working analytics provider — a no-op (`NoopAnalyticsTracker`) on the
  Supabase path and a **commented-out skeleton** on the Firebase path.
- **Now (P1-5, 2026-05-29):** `FirebaseAnalyticsTracker` / `FirebaseCrashReporter` are
  implemented against the gitlive SDKs (with `runCatching` no-op safety) and exposed via
  `firebaseObservabilityModule` on a new **backend-independent `ObservabilityChoice` axis**
  (`frnkModules(backend, observability)`); the `Noop*` defaults moved to `shared-backend-api`
  and back `ObservabilityChoice.None`. So a local-only app (no backend) can ship Firebase
  telemetry. Demoed across all three layers; `androidDemoApp` runs the real SDK.
- **Remaining:** the **provider-neutral PostHog** tracker named in Requirement §3.6 is still
  missing — see BACKLOG P3-1.

### 3.4 Molecules & Organisms (⛔)
- **Evidence:** the design system has Atoms + Scaffolds but no Molecules or
  Organisms layer.
- **Impact:** Atomic Design (Requirement §3.1) is only partially expressed.

### 3.5 Automated tests (🟡 — harness seeded 2026-05-29 via BACKLOG P0-3)
- **Original finding (⛔):** a repo-wide search for test files returned **none** —
  no `commonTest` source anywhere, and CI ran `testDebugUnitTest`, which for KMP
  modules is not even a real task (only `:androidDemoApp` has it), so the gate was a
  no-op.
- **Update (P0-3):** `commonTest` is now stood up in `shared-ui-api` (MVI engine
  reducer/effect test) and `shared-backend-api` (`AppResult.fold` + a reusable
  `FakeAuthService` test double), each opted in via `withHostTest {}`. CI now runs
  `testAndroidHostTest :androidDemoApp:testDebugUnitTest` and executes real tests.
  The earlier `testDebugUnitTest` reference was the root cause of the no-op gate and
  has been corrected across the docs.
- **Remaining:** broader coverage (scaffold reducers, `CollapsibleBarsState`, atoms)
  is still open — see BACKLOG P4-4. Requirement §5 is now partially met.

---

## 4. The Tech Debt — deviations & risks

### 4.1 Backend auth/remote implementations are skeletons (🟡 → ⚠️, deferred)
- **Evidence:** every Firebase/Supabase **auth & remote-data** method is
  `= TODO("wire …")`. (The Firebase **analytics/crash** methods are no longer
  skeletons — implemented in P1-5; see §3.3.)
- **Risk:** the toolkit *compiles* and *boots*, which can mask that no real
  auth/remote call works. Any host calling `AuthService.signIn(...)` against a real
  backend will hit `TODO()` at runtime (`NotImplementedError`).
- **Status:** **Deferred** (BACKLOG P1-2/P1-3/P1-4, 2026-05-29) while consuming apps
  are local-storage-only. Severity is low until a networked/auth app is planned, at
  which point these become the priority.

### 4.2 RevenueCat manager is a skeleton (🟡 → ⚠️)
- **Evidence:** `RevenueCatEntitlementManager.kt` is marked
  `TODO: wire com.revenuecat.purchases.kmp.Purchases. Skeleton kept callable.`
  The `FeatureGate` + `FakeEntitlementManager` path is functional; the real
  purchase/offerings/paywall flow does not exist.

### 4.3 Documentation drift (⚠️ → ✅ RESOLVED 2026-05-29 via BACKLOG P0-1)
- **`docs/ARCHITECTURE.md` references that no longer match code:**
  - It names `ObserveAsEvents.kt` and `UiAction`/`onAction` in the MVI section,
    but the code uses `UiIntent` / `onIntent` and has **no `ObserveAsEvents.kt`**
    in `shared-ui-api`.
  - It states CI runs `compileDebugKotlinAndroid`, while `CLAUDE.md` and the AGP
    9 KMP plugin use `compileAndroidMain` (the former task no longer exists for
    KMP modules).
- **Risk:** a contributor following `ARCHITECTURE.md` verbatim will reference
  symbols/tasks that don't exist. Low severity, but it erodes the "source of
  truth" guarantee.

### 4.4 SQLDelight config without a database (✅ — resolved by P1-1, 2026-05-29)
- Was: drivers were declared but no SQLDelight Gradle DSL block / `.sq` schema
  existed — dormant config. Now the plugin + `FrnkDB` DSL + `Note.sq` + `NoteStore`
  binding make persistence live and round-trip tested; the config is no longer dormant.

### 4.5 No CI coverage of iOS or assembly (accepted, by design)
- CI is compile + unit-test on Linux only; iOS targets and `assemble` are
  intentionally out of scope (documented). Noted here for completeness, not as a
  defect — but it means iOS regressions are caught only locally.

---

## 5. Constraint compliance audit (the strict UI rules)

| Constraint (REQUIREMENTS §4) | Result | Evidence |
| --- | --- | --- |
| No Material 2/3 dependency | ✅ Pass | Zero hits in `*.kts`/`*.toml` |
| No Material imports | ✅ Pass | Only comments referencing "no Material3" |
| Built on `compose-unstyled` granular artifacts | ✅ Pass | Catalog uses `composeunstyled-{primitives,theming,platformtheme,button,icon,separators}` + `icons-lucide-cmp`, not `com.composables:core` |
| Tokens, not literals, in atoms | ✅ Pass (spot-checked) | Atom docs reference `Theme[colors]`/`Theme[textStyles]`; recommend a lint/review check to keep it true |
| `@Immutable *State` per component | ✅ Pass (by convention) | Documented and followed in existing atoms |

**No strict-constraint violations were found.** This is the project's cleanest
area and should be protected with an automated guard (see `BACKLOG.md`).

---

## 6. Recommended focus order (rationale for the backlog)

1. **Protect what's good** — add an automated guard against Material/`core`
   creeping in, and seed the test harness (currently zero), since both are cheap
   and prevent regression of the strongest areas.
2. **Make the data layer real** — SQLDelight + backend impls + RevenueCat are the
   largest functional gap and the highest product risk.
3. **Build navigation** — required before most real feature screens can exist.
4. **Fill analytics (PostHog)** and **grow the design system upward**
   (Molecules/Organisms) once the foundation is verified.
5. **Reconcile docs** continuously so the source-of-truth guarantee holds.

This ordering is encoded as priority tiers in `BACKLOG.md`. **Re-prioritization
(2026-05-29):** because near-term consuming apps are local-storage-only, the backend
**auth + remote-data** impls (P1-2/3/4) are deferred, and **analytics + crash** (P1-5)
was pulled forward and completed. Recommended next is **navigation** (P2-1).
