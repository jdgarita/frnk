# frnk — Prioritized Backlog

> Derived from the gap analysis in `EVALUATION.md`, measured against
> `REQUIREMENTS.md`. Tasks are ordered so foundational work (guards, tests, data
> layer, DI, navigation) lands before higher-level work (analytics, advanced
> design-system layers, monetization UI).
>
> **How to use:** pick the top open task in the highest-priority tier whose
> dependencies are met. Each task is sized to be completed and reviewed
> independently, with explicit Acceptance Criteria (AC) that double as the
> "evidence of done."
>
> **Conventions for every task** (apply unless overridden):
> - Respect all invariants in `REQUIREMENTS.md` §2 and the strict UI rules §4.
> - `*-api` modules stay SDK-free; impls bind via Koin; interfaces return
>   `AppResult`.
> - **Demo rule:** a feature is not done until exercised in `:shared-demo`,
>   `androidDemoApp`, and `iosDemoApp` (or a written justification of why not).
> - Must pass `./gradlew compileAndroidMain` and `./gradlew testAndroidHostTest`
>   (KMP modules; `:androidDemoApp` uses `testDebugUnitTest`); pre-commit
>   `ktlintFormat` must leave the tree clean.

Priority tiers: **P0** (protect & unblock) → **P1** (data layer truth) →
**P2** (navigation & DI completeness) → **P3** (analytics & monetization) →
**P4** (design-system depth & polish).

---

## P0 — Protect the foundation & unblock everything else

These are cheap, high-leverage, and prevent regression of the project's
strongest areas. Do them first.

### P0-1 — Reconcile documentation with the codebase ✅ DONE (2026-05-29)
**Description:** Fix the drift found in the evaluation so the "source of truth"
guarantee holds.
**Rationale (priority):** Contributors (human and AI) act on these docs; stale
references cause wasted work and wrong patterns. Trivial effort, immediate value.
**Scope:** `docs/ARCHITECTURE.md` (primary); during the doc-wide sweep, two more
instances of the same drift were found and fixed: `CHANGELOG.md` and
`shared/shared-ui-api/CLAUDE.md`.
**Acceptance Criteria:**
- [x] MVI section references `UiIntent`/`onIntent` (not `UiAction`/`onAction`) and
      removes the `ObserveAsEvents.kt` reference (no such file exists; the doc now
      describes the real `LaunchedEffect(vm) { vm.effects.collect(...) }` pattern).
- [x] MVI section points at the correct module path
      (`shared/shared-ui-api/.../ui/mvi/`, not `shared-ui-atoms`).
- [x] CI section says `compileAndroidMain :androidDemoApp:compileDebugKotlin`
      (not `compileDebugKotlinAndroid`), matching `CLAUDE.md` and the AGP 9 KMP plugin.
- [x] `CHANGELOG.md` 0.1.0 entry no longer lists the phantom `ObserveAsEvents` and
      attributes the MVI engine to `shared-ui-api`.
- [x] `shared-ui-api/CLAUDE.md` no longer claims the root `CLAUDE.md` uses "Action".
- [x] A reviewer can follow `ARCHITECTURE.md` and find every symbol/task it names
      (verified by repo-wide grep: no residual `onAction`/`UiAction`/`ObserveAsEvents`/
      `compileDebugKotlinAndroid` references except the corrected "no longer exists" notes).

### P0-2 — Automated guard against Material / `compose.material*` / `composables:core` ✅ DONE (2026-05-29 — resolved as "enforce by convention, not a guard")
**Description:** Add a build- or CI-level check that fails if any forbidden
dependency or import appears.
**Rationale (priority):** The no-Material rule is the project's defining
constraint and is currently clean — lock that in before the surface grows.
**Scope:** a Gradle verification task (or a CI grep step) + wire into the build.
**Resolution:** A `checkForbiddenDeps` Gradle task (resolved-dependency scan +
Kotlin import scan, wired into CI) was prototyped and **deliberately reverted**.
Decision: the no-Material constraint is enforced **by convention** — REQUIREMENTS.md
§4 (NON-NEGOTIABLE), root `CLAUDE.md`, module `CLAUDE.md`s, and code review — rather
than by an automated build check. No `checkForbiddenDeps` task exists and none should
be re-added unless this decision is revisited. The tree remains clean of direct
Material/`composables:core` deps and imports (EVALUATION.md §5).
**Open follow-up (not blocking):** while prototyping, the Android *resolved* graph
appeared to pull `androidx.compose.material3` **transitively** via `compose.ui.tooling`
(the `@Preview` tooling in `shared-ui-atoms` `commonDebug`/`androidMain`). Unverified
(the test used a bogus version). Direct deps/imports are clean; confirm the transitive
pull only if it ever becomes load-bearing.
**Acceptance Criteria:**
- [x] Decision recorded: enforce the no-Material rule by convention/docs/review, not
      an automated guard.
- [x] Working tree confirmed clean of direct forbidden deps and imports.
- [N/A] Automated task in CI/pre-commit — intentionally **not** added per the decision above.

### P0-3 — Seed the test harness + first reducer tests ✅ DONE (2026-05-29)
**Description:** Stand up `commonTest` in `shared-ui-api` (and one other module)
and write the first MVI reducer/`AppResult` tests, so the unit-test gate stops
being a no-op.
**Rationale (priority):** CI ran a unit-test gate but there were zero
tests; every later task should land with tests, which requires the harness now.
**Scope:** test source sets + dependencies (kotlin-test, coroutines-test) + a
`FakeEntitlementManager`/fake backend pattern reusable by later tasks.
**Key finding:** the unit-test gate was a no-op for a second reason beyond "no
tests" — the AGP 9 KMP-Android host test task is **`testAndroidHostTest`**, not
`testDebugUnitTest` (which only exists on `:androidDemoApp`). Each KMP module must
opt in with `kotlin { android { withHostTest {} } }`. CI and all docs were
reconciled to `testAndroidHostTest :androidDemoApp:testDebugUnitTest`.
**Acceptance Criteria:**
- [x] At least one `MviViewModel` subclass reducer has a passing unit test
      (state transition on intent, effect emission) — `MviViewModelTest` in
      `shared-ui-api` (2 tests).
- [x] `AppResult` success/failure folding is unit-tested — added `AppResult.fold(...)`
      + `AppResultTest` in `shared-backend-api` (4 tests).
- [x] The unit-test gate executes ≥1 test and is green — `testAndroidHostTest` runs
      9 tests across the two modules (the task name was corrected from
      `testDebugUnitTest`, which is the demo app's task).
- [x] A documented fake pattern exists for downstream tasks to reuse —
      `FakeAuthService` (+ `FakeAuthServiceTest`, 3 tests) in `shared-backend-api`
      `commonTest`, documented in that module's `CLAUDE.md`. Reused by P1-2/P1-3.

---

## P1 — Make the data layer functionally true

The largest functional gap and highest product risk: impls compile but do not
work. Close the api↔impl behavior gap.

### P1-1 — SQLDelight database (`FrnkDB`) end-to-end ✅ DONE (2026-05-29)
**Description:** Introduce the SQLDelight Gradle DSL, a first `.sq` schema, and
driver factories so relational persistence actually works.
**Rationale (priority):** §3.4 is entirely missing; many features (caching,
offline, entitlement cache) depend on it. Foundational for P3+.
**Scope:** `shared-database-impl` build DSL (generate into
`dev.jdgarita.frnk.database.sql`, db class `FrnkDB`), one `.sq` file, an
interface in `shared-database-api`, binding in `DatabaseModule`, Android + native
`SqlDriverFactory` actuals.
**Key decisions:**
- `AppResult`/`AppError`/`CommonError`/`fold` **moved to `shared-utils`** (the neutral
  root) so `shared-database-api` returns `AppResult` without a sibling `*-api`→`*-api`
  dependency. All `*-api` modules now import it from `dev.jdgarita.frnk.utils`.
- DB failures reuse `CommonError` (no new `DatabaseError`).
- First entity: `Note(id, content, createdAt: Instant)`.
- Demo persists via an **in-memory `FakeNoteStore`** bound in `demoModule` (keeps
  `DemoKit.xcframework` free of the SQLite native cinterop, per §2.4); the **real**
  `SqlDelightNoteStore` is covered by the round-trip unit test + the real `frnkModules` binding.
**Acceptance Criteria:**
- [x] `FrnkDB` is generated (SQLDelight plugin + `databases { create("FrnkDB") { packageName
      = "dev.jdgarita.frnk.database.sql" } }`); the `shared-database-api` `NoteStore`
      interface exposes typed access returning `AppResult<…, CommonError>`.
      *(Evidence: `:shared-database-impl:generateCommonMainFrnkDBInterface` runs;
      `Note.sq` + `NoteStore.kt`.)*
- [x] `DatabaseModule` binds the impl (`single<NoteStore> { SqlDelightNoteStore(get()) }`
      + a `FrnkDB` singleton); `:shared` exposes it via `frnkModules` (already includes
      `databaseModule`, no change needed).
- [x] A round-trip insert/query is covered by a unit test using an in-memory driver.
      *(Evidence: `NoteStoreRoundTripTest` (2 tests, green) in `androidHostTest` using
      `JdbcSqliteDriver.IN_MEMORY`; `testAndroidHostTest` passes.)*
- [x] No SQLDelight/SQLite **driver/generated-code** dependency leaks into
      `shared-database-api` (the pre-existing intentional `api(sqldelight-runtime)` is
      retained per the module's CLAUDE.md). The JDBC driver is test-only in
      `:shared-database-impl`'s `androidHostTest` source set.
- [x] Demoed in all three layers: `:shared-demo` (`FakeNoteStore` + "3. Persistence"
      section in `DemoScreen`), `androidDemoApp` (installed + run on a Pixel 6 —
      tapping **Add note** updated "FrnkDB — N saved" and listed `Note #N`), `iosDemoApp`
      (compiles for `iosSimulatorArm64`; `DemoKit.xcframework` assembled — a booted iOS
      simulator was unavailable in this environment, so not launched on-device).

### P1-2 — Firebase backend: real Auth implementation
**Description:** Replace the `TODO()` bodies in `FirebaseAuthService` with real
`dev.gitlive:firebase-auth` calls, mapping outcomes to `AppResult`.
**Rationale (priority):** Auth is the most commonly needed backend capability;
Firebase is one of the two default backends.
**Scope:** `shared-backend-firebase` only; do not touch `shared-backend-api`
contract unless a gap is found (if so, change the api first and update both
backends).
**Acceptance Criteria:**
- [ ] `signIn`/`signUp`/`signOut` call the SDK and return `AppResult` (never
      throw); errors mapped to `CommonError`.
- [ ] No remaining `TODO()` in `FirebaseAuthService`.
- [ ] Contract parity with `SupabaseAuthService` (same interface satisfied).
- [ ] Unit test with a faked SDK boundary covers success + failure mapping.

### P1-3 — Supabase backend: real Auth implementation
**Description:** Same as P1-2 for `SupabaseAuthService` using
`io.github.jan-tennert.supabase:auth-kt`.
**Rationale (priority):** Supabase is the **default** `BackendChoice`; parity is
required so backend swap is real, not theoretical.
**Acceptance Criteria:**
- [ ] `signIn`/`signUp`/`signOut` implemented against the SDK, returning
      `AppResult`; no `TODO()`.
- [ ] Behaviorally interchangeable with the Firebase impl for the same calls.
- [ ] Unit test covers success + failure mapping.

### P1-4 — RemoteData implementations (Firestore + Supabase Postgrest)
**Description:** Implement `FirestoreRemoteData` and `SupabaseRemoteData`
read/write against their SDKs.
**Rationale (priority):** Remote reads/writes are the second core backend
capability after auth.
**Acceptance Criteria:**
- [ ] Both impls implement the `RemoteData` contract returning `AppResult`; no
      `TODO()`.
- [ ] Serialization path verified for at least one DTO round-trip.
- [ ] Unit tests for success + failure on both impls.

### P1-5 — Analytics & crash: Firebase implementations
**Description:** Uncomment/implement `FirebaseAnalyticsTracker` and
`FirebaseCrashReporter` against the SDKs.
**Rationale (priority):** Completes the Firebase backend contract; analytics
interface already exists.
**Acceptance Criteria:**
- [ ] `logEvent`/`setUserProperty`/crash recording call the SDK.
- [ ] No-op behavior preserved when Firebase isn't configured (no crash).
- [ ] Supabase path keeps `Noop*` defaults documented as intentional.

---

## P2 — Navigation & DI completeness

### P2-1 — Toolkit navigation layer (type-safe, MVI-integrated)
**Description:** Build the tailored navigation system: type-safe routes
(extending the existing `ToolkitRoute`), a host-owned back stack, a `NavHost`
graph builder usable from common code, argument passing, and Android
system-back/gesture handling.
**Rationale (priority):** §3.3 is missing and blocks most real multi-screen
features; the demo currently navigates ad hoc.
**Scope:** `shared-ui-api` (route contract) + `shared-ui-atoms` (Compose host),
integrated with the MVI effect channel (navigation as a `UiEffect`).
**Acceptance Criteria:**
- [ ] Type-safe routes with arguments; back stack owned by the toolkit host.
- [ ] Navigation is driven by `UiEffect` and consumed without leaking across
      recompositions.
- [ ] Works in Compose Multiplatform common code (Android + iOS).
- [ ] Android system back / predictive-back honored.
- [ ] `DemoScreen` migrated onto the new navigation; demoed in all three layers.

### P2-2 — Verify `BackendChoice` swap is observable end-to-end
**Description:** Add a demo/test that proves selecting Firebase vs Supabase
installs the corresponding Koin module and **only** that one.
**Rationale (priority):** The runtime-swap claim is central (§2.2) and currently
unverified by any test.
**Acceptance Criteria:**
- [ ] A test asserts the chosen backend's bindings resolve and the unchosen
      backend's bindings are absent from the graph.
- [ ] Documented how a host switches backends.

---

## P3 — Analytics provider & monetization

### P3-1 — PostHog analytics tracker
**Description:** Add a PostHog `AnalyticsTracker` implementation as the
provider-neutral option named in §3.6.
**Rationale (priority):** Named requirement; unblocks product analytics without
coupling to a backend choice.
**Scope:** likely a small new impl module (e.g. `shared-analytics-posthog`) or an
addition under the backend-agnostic analytics path — decide and record the
decision; keep the api SDK-free.
**Acceptance Criteria:**
- [ ] `AnalyticsTracker` implemented against PostHog; bound via its own Koin
      module; selectable independently of `BackendChoice`.
- [ ] No-op fallback remains the safe default.
- [ ] Demoed (an event fired from `DemoScreen`, visible in logs/fake in demo).

### P3-2 — RevenueCat: real EntitlementManager + entitlement state
**Description:** Wire `RevenueCatEntitlementManager` to
`com.revenuecat.purchases.kmp.Purchases`: fetch customer info, expose active
entitlements reactively, and back `FeatureGate` with real data.
**Rationale (priority):** Monetization is a headline feature; the gate already
exists and only needs a real source.
**Acceptance Criteria:**
- [ ] `EntitlementManager` returns real entitlement state (reactive to changes);
      no `TODO()`.
- [ ] `FeatureGate` gates a feature on a real active entitlement.
- [ ] iOS native-dependency contract honored (`PurchasesHybridCommon` supplied by
      consumer; `dynamic_lookup` unchanged); `:shared-demo` stays SDK-free.
- [ ] Demoed via the fake in `:shared-demo`; real path covered in `androidApp`.

### P3-3 — RevenueCat: offerings, paywall, purchase/restore flow
**Description:** Add fetch-offerings, a paywall presentation hook, and a
purchase/restore flow.
**Rationale (priority):** Completes §3.7; depends on P3-2.
**Acceptance Criteria:**
- [ ] Offerings fetched and exposed via api returning `AppResult`.
- [ ] Purchase + restore flows implemented; entitlement updates propagate to
      `FeatureGate`.
- [ ] Cancellation and failure paths mapped to typed errors (no throws).
- [ ] Demoed with fakes in `:shared-demo`.

---

## P4 — Design-system depth & polish

### P4-1 — Molecules layer
**Description:** Introduce the Molecules tier of Atomic Design (compositions of
atoms, e.g. labeled fields, list rows, cards) under a `ui/molecules/` package.
**Rationale (priority):** §3.1 calls for Atoms→Molecules→Organisms; only Atoms +
Scaffolds exist today.
**Acceptance Criteria:**
- [ ] ≥3 molecules built purely from existing atoms + tokens (no literals).
- [ ] Each has an `@Immutable *State`, a `@Preview`, and a skeleton decision
      recorded.
- [ ] Showcased in the demo.

### P4-2 — Organisms layer
**Description:** Introduce Organisms (compositions of molecules/atoms forming a
distinct section, e.g. a form, a feed item, a header block).
**Rationale (priority):** Completes the Atomic Design hierarchy; depends on P4-1.
**Acceptance Criteria:**
- [ ] ≥2 organisms composed from molecules/atoms; tokens-only styling.
- [ ] Previews + skeleton decisions + demo coverage.

### P4-3 — Typed preferences wrapper over `KeyValueStore`
**Description:** Provide a small typed-key convenience API over the existing
`KeyValueStore` (e.g. typed delegates with defaults) so hosts avoid stringly
access.
**Rationale (priority):** Quality-of-life on an already-working primitive; low
risk.
**Acceptance Criteria:**
- [ ] Typed accessors with defaults; unit-tested.
- [ ] `KeyValueStore` contract unchanged; lives in `shared-database-api`.

### P4-4 — Backfill tests for the existing design system
**Description:** Add Compose/unit tests for the highest-value existing atoms and
scaffolds (state-driven rendering, skeleton toggling, collapsible-bars logic).
**Rationale (priority):** The design system is the most-used surface and is
currently untested; do this once the harness (P0-3) exists.
**Acceptance Criteria:**
- [ ] `CollapsibleBarsState` scroll logic unit-tested.
- [ ] At least the most complex atoms (`FrnkSegmentedControl`, `FrnkSwitch`,
      `FrnkTopAppBar` search mode) have state-driven tests.

---

## Dependency map (quick reference)

```
P0-1, P0-2, P0-3      (no deps — do first)
        │
P1-1 ───┤ (FrnkDB)            P1-2 ─┐
P1-2/3 ─┤ (auth)             P1-3 ─┤→ P2-2 (verify backend swap)
P1-4 ───┤ (remote data)      P1-5 ─┘
        │
P2-1 (navigation) ── depends on P0-3 harness; unblocks most feature screens
        │
P3-1 (PostHog)   P3-2 (RevenueCat entitlements) → P3-3 (paywall/purchase)
        │
P4-1 (molecules) → P4-2 (organisms)   P4-3 (typed prefs)   P4-4 (DS tests, needs P0-3)
```

**Suggested first sprint:** P0-1, P0-2, P0-3, then P1-1. These lock in the
constraints, make CI meaningful, and deliver the first real data capability.
