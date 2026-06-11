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
      + `AppResultTest` in `:shared:backend:api` (4 tests).
- [x] The unit-test gate executes ≥1 test and is green — `testAndroidHostTest` runs
      9 tests across the two modules (the task name was corrected from
      `testDebugUnitTest`, which is the demo app's task).
- [x] A documented fake pattern exists for downstream tasks to reuse —
      `FakeAuthService` (+ `FakeAuthServiceTest`, 3 tests) in `:shared:backend:api`
      `commonTest`, documented in that module's `CLAUDE.md`. Reused by P1-2/P1-3.

---

## P1 — Make the data layer functionally true

The largest functional gap and highest product risk: impls compile but do not
work. Close the api↔impl behavior gap.

> **Re-prioritization (2026-05-29).** All near-term apps consuming the toolkit are
> **local-storage-only** — no auth, no remote data. P1-2 (Firebase Auth), P1-3
> (Supabase Auth), and P1-4 (RemoteData) are therefore **deferred** below everything
> else; revisit them when a networked/auth app is actually planned. Conversely, **every**
> project wants analytics + crash reporting, so **P1-5 is promoted to the next task**
> after P1-1. Order now: P1-1 ✅ → **P1-5** → P2/P3/P4 → (deferred) P1-2/P1-3/P1-4.

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

### P1-2 — Firebase backend: real Auth implementation ❌ RETIRED (2026-06-10, restructure Stage 2)
**Retired:** `AuthService` was dropped entirely (RESTRUCTURE_PLAN D1) — `Auth.kt`, `FirebaseAuthService`, and the Supabase backend are deleted. Kept for the record only.
**Deferred (2026-05-29):** near-term apps don't use a remote auth backend; revisit when one does.
**Description:** Replace the `TODO()` bodies in `FirebaseAuthService` with real
`dev.gitlive:firebase-auth` calls, mapping outcomes to `AppResult`.
**Rationale (priority):** Auth is the most commonly needed backend capability;
Firebase is one of the two default backends.
**Scope:** `:shared:backend:firebase` only; do not touch `:shared:backend:api`
contract unless a gap is found (if so, change the api first and update both
backends).
**Acceptance Criteria:**
- [ ] `signIn`/`signUp`/`signOut` call the SDK and return `AppResult` (never
      throw); errors mapped to `CommonError`.
- [ ] No remaining `TODO()` in `FirebaseAuthService`.
- [ ] Contract parity with `SupabaseAuthService` (same interface satisfied).
- [ ] Unit test with a faked SDK boundary covers success + failure mapping.

### P1-3 — Supabase backend: real Auth implementation ❌ RETIRED (2026-06-10, restructure Stage 2)
**Retired:** with P1-2 — `:shared:backend:supabase` is deleted (RESTRUCTURE_PLAN D1). Kept for the record only.
**Deferred (2026-05-29):** same rationale as P1-2 — no remote auth backend in near-term apps.
**Description:** Same as P1-2 for `SupabaseAuthService` using
`io.github.jan-tennert.supabase:auth-kt`.
**Rationale (priority):** Supabase is the **default** `BackendChoice`; parity is
required so backend swap is real, not theoretical.
**Acceptance Criteria:**
- [ ] `signIn`/`signUp`/`signOut` implemented against the SDK, returning
      `AppResult`; no `TODO()`.
- [ ] Behaviorally interchangeable with the Firebase impl for the same calls.
- [ ] Unit test covers success + failure mapping.

### P1-4 — RemoteData implementations (Firestore + Supabase Postgrest) ❌ SUPERSEDED (restructure Stage 11)
**Superseded:** the Supabase half died with Stage 2; the Firestore `RemoteData` path is being repurposed as **Firebase Remote Config** at restructure Stage 11 (RESTRUCTURE_PLAN OQ-1) — track it there.
**Deferred (2026-05-29):** no remote reads/writes in near-term apps; revisit alongside P1-2/P1-3.
**Description:** Implement `FirestoreRemoteData` and `SupabaseRemoteData`
read/write against their SDKs.
**Rationale (priority):** Remote reads/writes are the second core backend
capability after auth.
**Acceptance Criteria:**
- [ ] Both impls implement the `RemoteData` contract returning `AppResult`; no
      `TODO()`.
- [ ] Serialization path verified for at least one DTO round-trip.
- [ ] Unit tests for success + failure on both impls.

### P1-5 — Analytics & crash: Firebase implementations ✅ DONE (2026-05-29)
**Description:** Implement `FirebaseAnalyticsTracker` and `FirebaseCrashReporter` against the
gitlive SDKs, and expose them **decoupled from `BackendChoice`** so local-only apps can use them.
**Rationale (priority):** Promoted to next-up — every project wants analytics + crash, even the
local-storage-only ones.
**Key decisions:**
- **Observability is its own axis.** Analytics/crash were moved out of `firebaseBackendModule` /
  `supabaseBackendModule` into a new `ObservabilityChoice { None, Firebase }` selector on
  `frnkModules(backend, observability)` + `initializeFrnk(...)`. `firebaseObservabilityModule`
  (in `:shared:backend:firebase`) binds the real impls; `noopObservabilityModule` (in `:shared`,
  over the relocated `NoopAnalyticsTracker`/`NoopCrashReporter` now in `:shared:backend:api`) is the
  `None` default. So an app with **no backend** (or a Supabase-backed app) can still ship Firebase
  Analytics + Crashlytics. (Pre-stages P3-1 PostHog, "selectable independently of `BackendChoice`".)
- **Real SDK smoke-tested in `androidDemoApp`.** It applies the `google-services` +
  `firebase-crashlytics` Gradle plugins (its `google-services.json` for project `frnk-demo` already
  existed) and installs `firebaseObservabilityModule` over the demo's logging fakes via Koin
  `allowOverride(true)`. `:shared-demo` + `iosDemoApp` stay **SDK-free** (logging fakes) so
  `DemoKit.xcframework` remains cinterop-free.
**Acceptance Criteria:**
- [x] `logEvent`/`setUserProperty`/`recordException`/`log`/`setUserId` call the gitlive SDK
      (`Firebase.analytics` / `Firebase.crashlytics`); event params coerced to Firebase types.
- [x] No-op when Firebase isn't configured: every SDK call wrapped in `runCatching` + logged warning.
- [x] Analytics/crash decoupled from `BackendChoice` via `ObservabilityChoice`; `None` → Noop default
      (documented as intentional in `:shared:backend:api`).
- [x] Reusable `FakeAnalyticsTracker`/`FakeCrashReporter` (+ `ObservabilityTest`) in
      `:shared:backend:api` `commonTest`; `DemoViewModelTest` in `:shared-demo` covers the new intents.
- [x] Demoed in all three layers: `:shared-demo` (Analytics & Crash section + logging fakes),
      `androidDemoApp` (real Firebase via `firebaseObservabilityModule`), `iosDemoApp` (logging fakes).

### P1-5b — iOS unhandled-crash symbolication (CrashKiOS) ✅ DONE (2026-06-01)
**Description:** Close the iOS half of P1-5's crash story. gitlive's `CrashReporter.recordException`
only reports exceptions the app *explicitly catches*; on iOS an **uncaught** Kotlin exception aborts
via `konan` with no symbolicated Kotlin stack in Crashlytics. Adopt `co.touchlab.crashkios:crashlytics`
to install the Kotlin/Native unhandled-exception hook so those crashes reach Crashlytics symbolicated.
**Rationale (priority):** Direct extension of the just-shipped P1-5; uncaught crashes are the most
valuable ones and were previously invisible on iOS.
**Key decisions:**
- **iOS-only, behind the existing axis.** CrashKiOS (`0.9.0`) lives in `:shared:backend:firebase`'s
  **`iosMain`** (it has no JVM variant — must stay out of `commonMain`). An `internal expect fun
  enableNativeCrashHandler()` has an iOS actual (`enableCrashlytics()` + `setCrashlyticsUnhandledExceptionHook()`,
  `runCatching`-wrapped + idempotent) and an Android **no-op** actual (the Crashlytics Android SDK
  already hooks uncaught JVM exceptions). It's invoked lazily from the `CrashReporter` binding in
  `firebaseObservabilityModule`, so it runs exactly when `ObservabilityChoice.Firebase` is selected and
  never for `None`.
- **`bootstrapFrnkKit` gained an `observability` param** (additive, default `None`) so iOS hosts can
  actually reach `ObservabilityChoice.Firebase`.
- **klib compatibility verified:** CrashKiOS 0.9.0 (built with Kotlin 1.9.24) compiles/links under this
  project's Kotlin 2.3.21 — confirmed on macOS via `compileKotlinIosSimulatorArm64` +
  `assembleFrnkKitDebugXCFramework`.
**Acceptance Criteria:**
- [x] CrashKiOS hook installed on iOS when `ObservabilityChoice.Firebase` is selected; no-op on Android;
      never installed for `ObservabilityChoice.None`.
- [x] `*-api` stays SDK-free; CrashKiOS confined to `:shared:backend:firebase` `iosMain`; no per-framework
      `linkerOpts` (resolves under the existing `dynamic_lookup`).
- [x] Host test (`testAndroidHostTest`): `enableNativeCrashHandler` is a safe JVM no-op and `CrashReporter`
      resolves from `firebaseObservabilityModule` without throwing (`FirebaseObservabilityModuleTest`, 2 tests).
- [x] `compileAndroidMain` + `testAndroidHostTest` green; iOS compile/link green on macOS.
      **CI caveat (recorded):** Linux CI does not compile `iosMain`, so a CrashKiOS API drift ships green
      — a local macOS iOS compile/link is a mandatory pre-merge gate.
- [x] Demoed: `:shared-demo` adds a "Force crash (unhandled)" panic-button action (platform-agnostic
      throw on a background dispatcher); `androidDemoApp` covers real Android delivery via the real
      `firebaseObservabilityModule`. **`iosDemoApp` now tests the real iOS path too** — `DemoKit` gains
      the lightweight **CrashKiOS** cinterop in `iosMain` (plus `dynamic_lookup`), an `enableDemoCrashlytics()`
      installer, and the Swift side calls `FirebaseApp.configure()` + the hook. **Trade-off accepted:**
      `DemoKit` is no longer SDK-free — `iosDemoApp` must link the native Firebase SDK (via SPM) and ship
      `GoogleService-Info.plist`; it no longer launches on a bare simulator. (Only `iosDemoApp` consumes
      `DemoKit`, so the blast radius is the demo harness alone.) Pressing the button → CrashKiOS hook →
      crash visible in the Firebase Crashlytics console (see `iosDemoApp/README.md`). The native
      Firebase SPM add + simulator run + console check are manual Mac/Xcode steps.
- [x] dSYM/symbolication responsibility documented for consumers (static framework → consumer uploads
      all dSYMs) in `iosApp/CLAUDE.md` + `:shared:backend:firebase/CLAUDE.md`.

---

## P2 — Navigation & DI completeness

### P2-0 — BUG: OnboardingScreen buttons unresponsive when pushed as a nav3 destination
**Description:** With `OnboardingScreen` pushed onto a tab's back stack (Settings → Show
Onboarding), its **buttons don't respond** — the close-X and Next/Back fire no intent — while the
`HorizontalPager` swipe and system/predictive back work fine. Discovered during the scaffold-system
device verification (2026-06-10); **reproduces identically on `main` (87aba0e)**, so it predates
`FrnkAppShell` (not a regression of the shell's `entry(ToolkitRoute.Onboarding)` registration —
verified by A/B-installing both branches on the same emulator). Repro: demo app → Settings → Show
Onboarding → tap X or Next (emulator API 36 + Pixel 7a). Suspects: the onboarding `koinViewModel`'s
intent collector vs. the nav-entry ViewModelStoreOwner, or the button taps never reaching the
composables on that destination. Workaround: pager swipe + system back.

### P2-1 — Toolkit navigation layer (type-safe, MVI-integrated) ✅ DONE (2026-06-01)
**Description:** Build the tailored navigation system: type-safe routes
(extending the existing `ToolkitRoute`), a host-owned back stack, a `NavHost`
graph builder usable from common code, argument passing, and Android
system-back/gesture handling.
**Rationale (priority):** §3.3 is missing and blocks most real multi-screen
features; the demo currently navigates ad hoc.
**Scope:** `shared-ui-api` (route contract) + `shared-ui-atoms` (Compose host),
integrated with the MVI effect channel (navigation as a `UiEffect`).
**Key decisions:**
- **Wrapped JetBrains CMP `navigation-compose` 2.9.2** (already in the catalog) with a thin
  toolkit layer rather than hand-rolling a back stack. `ToolkitRoute` is now a `@Serializable`
  sealed interface; routes need `kotlinx-serialization-core` (**not** `-json` — nav encodes via
  its own `SavedStateEncoder`), and the `kotlin-serialization` plugin is applied to `shared-ui-api`,
  `shared-ui-atoms`, and `:shared-demo`.
- **Split across the no-Compose / Compose boundary:** route contract + Compose-free `FrnkNavigator`/
  `FrnkNavOptions` in `shared-ui-api`; `FrnkNavHost` / `frnkComposable<T>` / `rememberFrnkNavController`
  / `rememberFrnkNavigator` in `shared-ui-atoms`. **Toolkit ships the `NavHost`; the host owns the
  `NavController` back-stack instance.** This supersedes the old "toolkit never owns the NavHost" note.
- **Nav-as-effect:** a single `EffectCollector` above the `FrnkNavHost` routes a navigation `UiEffect`
  into the `FrnkNavigator` (the channel is single-consumer). `navigation-compose` is pure Kotlin/Compose
  — no native cinterop — so `DemoKit`/`FrnkKit` XCFrameworks stay clean.
- **Full demo rewrite:** the three bottom-nav tabs are top-level destinations (each with its own saved
  back stack via tab-switch save/restore options); `ComponentDetail(name)` (type-safe arg), `Onboarding`,
  and `Paywall` are pushed routes. Removed the ad-hoc `selectedTabIndex`/`showOnboarding`/`selectedComponent`
  state; Android `NavHost` auto-pops on system back (`android:enableOnBackInvokedCallback="true"`), so the
  only manual `BackHandler` left closes the Components search field.
**Acceptance Criteria:**
- [x] Type-safe routes with arguments; back stack owned by the toolkit host.
- [x] Navigation is driven by `UiEffect` and consumed without leaking across
      recompositions (`EffectCollector` → `routeDemoEffect` → `FrnkNavigator`).
- [x] Works in Compose Multiplatform common code (Android + iOS).
- [x] Android system back / predictive-back honored.
- [x] `DemoScreen` migrated onto the new navigation; demoed in all three layers.
      *(Evidence: `compileAndroidMain` + `testAndroidHostTest` green incl. new `ToolkitRouteTest`,
      `DemoNavigationTest`, and the updated `DemoViewModelTest`; `androidDemoApp` installs + runs; iOS
      compile/link of `DemoKit` is the mandatory local macOS pre-merge gate — CI does not build iOS.)*

### P2-2 — Verify `BackendChoice` swap is observable end-to-end ❌ RETIRED (2026-06-11, restructure Stage 1)
**Retired:** the choice enums and `frnkModules()` are gone — hosts pass an explicit module list to `initializeFrnk(...)`, so "only the chosen module is installed" holds by construction. Kept for the record only.
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
      module; installable independently of the data backend.
- [ ] No-op fallback remains the safe default.
- [ ] Demoed (an event fired from `DemoScreen`, visible in logs/fake in demo).

### P3-2 — RevenueCat: real EntitlementManager + entitlement state ✅ DONE (2026-06-01)
**Description:** Wire `RevenueCatEntitlementManager` to
`com.revenuecat.purchases.kmp.Purchases`: fetch customer info, expose active
entitlements reactively, and back `FeatureGate` with real data.
**Rationale (priority):** Monetization is a headline feature; the gate already
exists and only needs a real source.
**Key decisions:**
- **Configurable "Pro" identifier.** New `RevenueCatConfig(proEntitlementId = "pro")` (Koin
  `single`, host-overridable); `isPro = proEntitlementId in customerInfo.entitlements.active`. The
  decision is isolated in a pure, SDK-free `isProFor(...)` mapper so it is unit-testable without the
  static `Purchases.sharedInstance`.
- **Host configures, toolkit reads.** The toolkit never calls `Purchases.configure(...)`; the manager
  reads `Purchases.sharedInstance` lazily, every call `runCatching`-wrapped → safe no-op
  (`isPro=false`) while unconfigured (mirrors `FirebaseCrashReporter`). Reactivity via a
  `PurchasesDelegate` installed once (won't clobber a host delegate) + `refresh()`/`restorePurchases()`
  reading `awaitCustomerInfoResult()`/`awaitRestoreResult()` from `purchases-kmp-result`.
- **Demo decoupled from the concrete fake.** `DemoViewModel` now depends on the `EntitlementManager`
  interface (not `FakeEntitlementManager`), exposing `canTogglePro` + `RefreshEntitlements`/
  `RestorePurchases` intents, so `androidDemoApp` can override the fake with the real `revenueCatModule`.
- **Real Android smoke test, demo stays SDK-free.** `androidDemoApp` calls `Purchases.configure(...)`
  (key from `local.properties` `REVENUECAT_ANDROID_API_KEY` → `BuildConfig`) and overrides with
  `revenueCatModule` via Koin `allowOverride(true)`. `:shared-demo`/`DemoKit` keep the fake (cinterop-free).
**Acceptance Criteria:**
- [x] `EntitlementManager` returns real entitlement state (reactive to changes via `PurchasesDelegate`
      + `refresh`/`restore`); no `TODO()` — `RevenueCatEntitlementManager` rewritten against the 3.0.2 SDK.
- [x] `FeatureGate` gates a feature on a real active entitlement (`isPro` derives from
      `customerInfo.entitlements.active`; the demo's Request Upgrade → Paywall is driven by it).
- [x] iOS native-dependency contract honored (`PurchasesHybridCommon` supplied by consumer;
      `dynamic_lookup` unchanged); `:shared-demo` stays SDK-free (fake only).
- [x] Demoed via the fake in `:shared-demo`; **real path covered in `androidDemoApp`** (the device-runnable
      host — `androidApp` is a library and can't run; matches how P1-5 smoke-tested real Firebase there).
      *(Evidence: `compileAndroidMain` + `testAndroidHostTest` green incl. new
      `RevenueCatEntitlementManagerTest` (`isProFor` truth table + Koin resolution) and updated
      `DemoViewModelTest`; iOS compile/link of `FrnkKit.xcframework` is the mandatory local macOS gate —
      CI does not build iOS.)*
      **iosDemoApp justification:** `DemoKit` is intentionally SDK-free, so the real iOS RC path (native
      `PurchasesHybridCommon` pod + iOS key) is **not** wired into `iosDemoApp`; the iOS RC code is instead
      compile/link-verified through `FrnkKit.xcframework`.

### P3-3 — RevenueCat: offerings, paywall, purchase/restore + frnk-owned Pro layer ✅ DONE (2026-06-02)
**Description:** Add fetch-offerings, a basic toolkit paywall, and a purchase/restore flow — plus a
frnk-owned Free/Pro layer (independent of RevenueCat), god mode, and two always-on paywall entry points.
**Rationale (priority):** Completes §3.7; depends on P3-2. Scope expanded per product requirements into a
small monetization subsystem.
**Key decisions:**
- **Provider/Manager split.** Renamed the old `EntitlementManager` → **`EntitlementProvider`** (RC + the
  demo fake implement it: real entitlement + `offerings()`/`purchase()`/`restore()`). New frnk-owned
  **`EntitlementManager`** (`DefaultEntitlementManager`, pure Kotlin in `shared-monetization-api`) wraps the
  provider and overlays **god mode** (persisted via `KeyValueStore`, key `frnk.god_mode`) + analytics
  user-properties (`is_pro`/`pro_source`/`god_mode`). `isPro = provider.isPro || godMode`;
  `EntitlementStatus(isPro, source: None|Purchase|GodMode)`. `FeatureGate` reads the manager + gained
  `observe(feature)` and a configurable `freeFeatures`. Bound by new `monetizationModule`; `revenueCatModule`
  now binds the **provider only**.
- **God mode** reachable in release builds: hidden **Settings → tap version 7×** Developer section
  (`SettingsScreenState.developerSection` + `showDeveloperSection`) **and** a host opt-in flag; persisted.
- **New module `shared-monetization-ui`** (keeps `shared-ui-atoms` monetization-agnostic): the basic
  **Paywall** scaffold (stacked selectable plan cards, free-trial/savings badges, loading skeleton, VM-driven
  purchase/restore via the manager), `frnkPaywallDestination` (toolkit-owned `ToolkitRoute.Paywall` graph
  entry), and `rememberFrnkSettingsHandler` (centralizes Upgrade→paywall nav, Restore, god-mode toggle).
- **Two entry points** (toolkit-owned nav): Home top-app-bar top-right crown action (hidden once Pro) + the
  Settings Upgrade/Restore rows. Products: monthly + yearly demanded; **free trial + lifetime supported,
  optional**. RC entitlement id stays `pro`; dashboard display name `"<App> Pro"`.
- **Analytics** woven in: `Paywall_Viewed{source}` (home_topbar/settings/demo), `Purchase_Started/Completed/
  Failed{product}`, `Paywall_Dismissed`, custom `god_mode_toggled`, + the user-properties above.
- **Suggested extras shipped:** source tagging, `is_pro`/`pro_source`/`god_mode` user-properties, reactive
  `FeatureGate.observe`, trial + best-value badges, paywall skeleton. **Deferred** (noted): Manage-Subscription
  deep link, trial-countdown UI (needs tiered status), paywall A/B via RC offering id, promo/host-granted
  `ProSource`.
**Acceptance Criteria:**
- [x] Offerings fetched + exposed via api returning `AppResult` (`EntitlementProvider.offerings()` →
      `List<ProProduct>`; RC maps `Offerings.current` packages, `PackageType`→`ProPlan`, prices, trial).
- [x] Purchase + restore flows implemented; entitlement updates propagate to `FeatureGate` (manager combines
      provider + god mode; the paywall VM runs them and closes on success).
- [x] Cancellation + failure mapped to typed `MonetizationError` (no throws; `awaitPurchaseResult` →
      `PurchasesTransactionException.userCancelled` → `UserCancelled`).
- [x] Demoed in all three layers: `:shared-demo` (fake provider + the real `monetizationModule` over it,
      exercising god mode cross-platform), **`androidDemoApp`** (crown→paywall, Settings entry, god-mode flips
      Pro=true via GodMode with the crown hiding — verified on device against the **RevenueCat Test Store**:
      products + a current offering configured via MCP, `test_` key in `local.properties`), **`iosDemoApp`**
      (parity — `DemoKit` `iosMain` gains the RevenueCat cinterop so `bootstrapDemoKoinWithRevenueCat(...)`
      installs the real `revenueCatModule` against the same Test Store; the app adds the `RevenueCat` SPM
      package — see `iosDemoApp/README.md`).
      *(Evidence: `compileAndroidMain` + `testAndroidHostTest` green incl. `DefaultEntitlementManagerTest`,
      `PaywallViewModelTest`, `RevenueCatEntitlementProviderTest`, updated `DemoViewModelTest`; `ktlintFormat`
      clean; `FrnkKit` + `DemoKit` debug XCFrameworks link on macOS — the latter now linking the RC iOS cinterop.)*

---

## P4 — Design-system depth & polish

### P4-1 — Molecules layer ✅ DONE (2026-06-02)
**Description:** Introduce the Molecules tier of Atomic Design (compositions of
atoms, e.g. labeled fields, list rows, cards) under a `ui/molecules/` package.
**Rationale (priority):** §3.1 calls for Atoms→Molecules→Organisms; only Atoms +
Scaffolds exist today.
**Key decisions:**
- **New `ui/molecules/` package** in `:shared-ui-atoms`, above `ui/atoms/` and below
  `ui/scaffolds/`. Three molecules, each composed purely from existing atoms + tokens
  (no `Color(0xFF…)`, no raw `.dp` spacing): **`FrnkListRow`** (icon + title/subtitle +
  trailing slot, optionally clickable — reuses the `SettingsRow` layout idiom),
  **`FrnkLabeledValue`** (muted label + value, `Inline`/`Stacked`), **`FrnkEmptyState`**
  (centered icon/title/subtitle + optional `FrnkButton`).
- **Skeleton spectrum recorded:** ListRow → whole-row block; LabeledValue → the *value*
  carries the skeleton (label is static chrome); EmptyState → **none, by design** (a
  terminal zero-content state is never a loading state).
- **No re-wiring of feedback:** clickable molecules reuse `FrnkTheme`'s ambient ripple
  (`LocalIndication`) + fire `LocalFrnkHaptics` like the atoms.
- **No unit tests added:** molecules are pure stateless view code, matching the atoms tier
  (previews only). `@Preview`s (Light/Dark/skeleton) live in `commonDebug/.../ui/molecules/previews/`.
- **Demo:** added to the `:shared-demo` Components gallery (`componentNames` + `ComponentContent`);
  Android/iOS pick it up automatically, `DemoKit` stays cinterop-free; clickable demos reuse the
  existing `DemoEffect.Toast` (no `DemoViewModel` change). Also removed the now-redundant
  "Theme + Atoms" section from the demo Home tab. (PR #31.)
**Acceptance Criteria:**
- [x] ≥3 molecules built purely from existing atoms + tokens (no literals).
      `FrnkListRow`, `FrnkLabeledValue`, `FrnkEmptyState` under `ui/molecules/`.
- [x] Each has an `@Immutable *State`, a `@Preview`, and a skeleton decision
      recorded (ListRow/LabeledValue → skeleton; EmptyState → none, by design).
- [x] Showcased in the demo (Components gallery, all three layers).

### P4-2 — Organisms layer ✅ DONE (2026-06-02)
**Description:** Introduce Organisms (compositions of molecules/atoms forming a
distinct section, e.g. a form, a feed item, a header block).
**Rationale (priority):** Completes the Atomic Design hierarchy; depends on P4-1.
**Key decisions:**
- **New `ui/organisms/` package** in `:shared-ui-atoms`, above `ui/molecules/` and below
  `ui/scaffolds/`. Two organisms, each composed purely from P4-1 molecules + atoms + tokens
  (no `Color(0xFF…)`, no raw `.dp` spacing), visually distinct from each other and the molecules:
  **`FrnkListSection`** (optional title + a `shapeCard`/`colorSurface` card stacking N `FrnkListRow`
  molecules separated by `FrnkDivider`s, `animateContentSize()`, optional footnote — generalises the
  `SettingsScreen` section-card idiom; `onRowClick(index)` + per-row `trailing(index)` slot) and
  **`FrnkProfileHeader`** (circular `colorPrimaryContainer` avatar chip + name/subtitle + an optional
  even row of `FrnkLabeledValue` stat tiles).
- **Shared card chrome, not duplicated:** the titled-card layout (title + `shapeCard` surface +
  `FrnkDivider`-between-rows + `animateContentSize()` + footnote) lives once in an `internal`
  `FrnkSectionCard<T>` helper with a `row(index, item)` slot; both `FrnkListSection` and the Settings
  scaffold's private `SettingsSection` delegate to it (review follow-up — `SettingsSection` was
  refactored to drop its inline copy), so the chrome has a single source of truth.
- **Skeleton decisions recorded:** ListSection → **carried by the rows** (enable `skeleton` per
  `FrnkListRowState` so a partially-loaded list skeletonizes per row; card/title/dividers are static
  framing — no section-level flag). ProfileHeader → **passed through** (one `skeleton` flag drives
  avatar + name + subtitle + each stat value; the avatar chip **drops its brand fill while loading**
  so its rim doesn't peek around the glyph skeleton, the `FrnkSwitch` precedent).
- **No re-wiring of feedback:** clickable rows reuse `FrnkTheme`'s ambient ripple + fire
  `LocalFrnkHaptics` via the underlying `FrnkListRow` molecule.
- **No unit tests added:** organisms are pure stateless view code, matching the atoms/molecules tier
  (previews only). `@Preview`s (Light/Dark/skeleton) live in `commonDebug/.../ui/organisms/previews/`.
- **Demo:** added to the `:shared-demo` Components gallery (`componentNames` + `ComponentContent`);
  Android/iOS pick it up automatically, `DemoKit` stays cinterop-free; clickable rows reuse the
  existing `DemoEffect.Toast` (no `DemoViewModel` change). Verified on a Pixel 6 (resting + skeleton).
**Acceptance Criteria:**
- [x] ≥2 organisms composed from molecules/atoms; tokens-only styling.
      `FrnkListSection`, `FrnkProfileHeader` under `ui/organisms/`.
- [x] Previews + skeleton decisions + demo coverage (all three layers).

### P4-3 — Typed preferences wrapper over `KeyValueStore` ✅ DONE (2026-06-02)
**Description:** Provide a small typed-key convenience API over the existing
`KeyValueStore` (e.g. typed delegates with defaults) so hosts avoid stringly
access.
**Rationale (priority):** Quality-of-life on an already-working primitive; low
risk.
**Key decisions:**
- **API shape:** `Preference<T> : kotlin.properties.ReadWriteProperty<Any?, T>` (`Preference.kt` in
  `shared-database-api` `commonMain`), so a single object serves both imperative `pref.value` get/set
  **and** `var x by pref` delegation. Created via `KeyValueStore` extension factories
  `stringPreference`/`booleanPreference`/`intPreference`/`enumPreference`. Backing `internal
  KeyValuePreference<T>` takes `read`/`write` lambdas so each type owns its encoding. Pure stdlib —
  no new deps, SDK-free, no cinterop (`DemoKit`/`FrnkKit` stay clean).
- **Type set:** String, Boolean (native) + Int and Enum **encoded losslessly over the String
  primitive** — Int via `toString()`/`toIntOrNull()`, Enum via `name`/`firstOrNull { it.name == … }`
  (**not** the throwing `enumValueOf`, so a renamed/removed constant degrades to the default). Unset
  **or** undecodable values fall back to the default. `Long`/`Double`/nullable-string deliberately
  deferred (no consumer; AC forbids contract changes — `Preference<T>` already supports `T = String?`
  as a non-breaking future add).
- **`KeyValueStore` contract unchanged:** every accessor rides on the existing
  `getString`/`putString`/`getBoolean`/`putBoolean`/`remove`.
- **Dogfood (real consumer):** `DefaultEntitlementManager` god-mode persistence now goes through
  `keyValueStore.booleanPreference("frnk.god_mode", default = false)` instead of raw
  `getBoolean`/`putBoolean` + a stringly `GOD_MODE_KEY`. Same key + same `getBoolean`/`putBoolean`
  under the hood ⇒ **no persisted-data migration**; `DefaultEntitlementManagerTest` passes unchanged.
- **Tests:** `shared-database-api` opted into `withHostTest {}` + a `commonTest` source set; added the
  canonical `InMemoryKeyValueStore` fixture + `PreferenceTest` (round-trip + default per type, the
  corrupt-Int and unknown-Enum fallback edges, `by`-delegation, `remove`, independence). Collapsing the
  two other in-memory `FakeKeyValueStore` copies (monetization `commonTest`, demo `commonMain`) is out
  of scope — test source sets aren't shared across modules.
**Acceptance Criteria:**
- [x] Typed accessors with defaults; unit-tested. *(Evidence: `PreferenceTest` (13 tests) green via
      `:shared-database-api:testAndroidHostTest`.)*
- [x] `KeyValueStore` contract unchanged; lives in `shared-database-api` (`Preference.kt`).
**Demo rule:** the typed-prefs layer is a build-time/library convenience with no UI of its own; it is
transitively exercised in all three demo layers via the existing god-mode toggle (Settings → tap
version 7×) that now persists through `booleanPreference` — `:shared-demo` (in-memory store, cross-
platform), `androidDemoApp` (real `SharedPreferences`-backed store, device-verified), `iosDemoApp`
(`DemoKit`, `NSUserDefaults`-backed). No new demo surface needed.

### P4-4 — Backfill tests for the existing design system ✅ DONE (2026-06-04)
**Description:** Add Compose/unit tests for the highest-value existing atoms and
scaffolds (state-driven rendering, skeleton toggling).
**Rationale (priority):** The design system is the most-used surface and is
currently untested; do this once the harness (P0-3) exists.
**Key decisions:**
- **Compose UI tests, not reducer tests.** The atoms are headless, fully state-hoisted
  composables — there's no reducer to unit-test. Their state-driven behavior is verified by
  driving a real composition with `runComposeUiTest` and querying the semantics tree
  (`onNode(isToggleable())`, `onNodeWithText`, `onNodeWithContentDescription`, `performClick`,
  `performTextInput`). This is the **first** design-system test code — the atoms/molecules/organisms
  tier was previously "previews only," and this task carves out the documented exception for the
  highest-value atoms.
- **Robolectric on the JVM host, so it gates in CI.** Tests run as `testAndroidHostTest` (the task
  CI already runs) under Robolectric — **no device/emulator**. They live in a new `androidHostTest`
  source set (not `commonTest`): the Compose UI-test runtime + Robolectric have no common/iOS variant,
  mirroring `shared-database-impl`'s androidHostTest-scoped JDBC driver. A shared annotated base
  `RobolectricComposeTest` (`@RunWith(RobolectricTestRunner)` + `@Config(sdk=[34])` + `GraphicsMode.NATIVE`)
  centralises the wiring/rationale (the first two are `@Inherited`).
- **No JVM/desktop test target.** A desktop Skiko target would be the other CMP UI-test path, but
  `:shared-ui-atoms` depends on `multihaptic` (Android+iOS only), which has no JVM artifact — so a
  `jvm()` target can't resolve. Robolectric-on-androidHostTest is the only path that fits the pins.
- **New test-only deps (catalog):** `org.jetbrains.compose.ui:ui-test` (referenced by direct
  coordinate, not the `compose.uiTest` accessor, which is gated behind `@ExperimentalComposeLibrary`),
  `androidx.compose.ui:ui-test-manifest` (registers the `ComponentActivity` the test host launches),
  and `org.robolectric:robolectric`. All scoped to `androidHostTest`. `withHostTest { isIncludeAndroidResources = true }`
  so Robolectric can inflate the test host. No production/`commonMain` surface change; XCFrameworks stay clean.
**Acceptance Criteria:**
- [x] At least the most complex atoms (`FrnkSegmentedControl`, `FrnkSwitch`,
      `FrnkTopAppBar` search mode) have state-driven tests.
      *(Evidence: 18 green tests across `FrnkSwitchTest` (5: on/off semantics, toggle emits flipped
      value, disabled + skeleton suppress interaction), `FrnkSegmentedControlTest` (6: renders all
      options, tap emits index, re-tap of selected still emits, out-of-range `selectedIndex` clamps
      and stays interactive, disabled + skeleton don't emit), and `FrnkTopAppBarTest` (7: title-mode
      title+action, search mode swaps in close+placeholder and hides title/trigger, clear button shown
      only for a non-empty query, typing streams `onSearchQueryChange`, clear emits "", close fires
      `onSearchClose`). Run via `:shared-ui-atoms:testAndroidHostTest`; `compileAndroidMain` +
      `testAndroidHostTest` green project-wide, `ktlintFormat` clean.)*
**Demo rule:** N/A — these are library tests with no UI of their own; the atoms under test are already
exercised in all three demo layers (Components gallery + Settings).

### P4-5 — Cross-platform haptics ✅ DONE (2026-06-02)
**Description:** A simplified, host-facing haptics API backed by `multihaptic`
(`top.ltfan.multihaptic`), wired to the existing Settings "Haptic feedback" toggle,
working on Android + iOS with zero host code.
**Rationale (priority):** Tactile feedback is table-stakes polish for the design
system; `multihaptic` matches the toolkit's Kotlin/AGP/Compose pins and ships
Android/iOS impls with no native cinterop, so it slots in like the ripple.
**Acceptance Criteria:**
- [x] Compose-free contract in `shared-ui-api` (`HapticType`, `HapticFeedback`,
      `HapticEngine`, `DefaultHapticFeedback`, `NoOpHapticFeedback`); unit-tested
      (`DefaultHapticFeedbackTest` — gating + `setEnabled`).
- [x] `multihaptic` binding (`MultiHapticEngine`) + `LocalFrnkHaptics` installed by
      `FrnkTheme` via `rememberFrnkHaptics()` (no Context plumbing) in
      `shared-ui-atoms`.
- [x] Interactive atoms auto-fire (`FrnkButton`/`FrnkIconButton` → `Click`;
      `FrnkSwitch`/`FrnkSegmentedControl`/`FrnkBottomNavBar` → `Selection`).
- [x] Toolkit default Settings catalog ships the "Haptic feedback" toggle
      (`HAPTICS_TOGGLE_ID`); `rememberFrnkSettingsHandler` flips
      `HapticFeedback.setEnabled` — demo carries no custom haptics logic.
- [x] Demoed in all three layers (`:shared-demo` components + Settings toggle,
      `androidDemoApp`, `iosDemoApp` — iOS needs a physical device for the taptic
      engine).
- [x] No native cinterop added; `DemoKit`/`FrnkKit` XCFrameworks stay clean.

---

## Dependency map (quick reference)

```
P0-1, P0-2, P0-3      (no deps — done)
        │
P1-1 ✅ (FrnkDB)   P1-5 ✅ (analytics/crash — ObservabilityChoice, backend-independent)
                   P1-5b ✅ (iOS unhandled-crash symbolication — CrashKiOS)
        │
P2-1 ✅ (navigation — FrnkNavHost over CMP navigation-compose; unblocks feature screens)
        │
P3-1 (PostHog)   P3-2 ✅ (RevenueCat entitlements) → P3-3 ✅ (paywall/purchase + frnk Pro layer + god mode)
        │
P4-1 ✅ (molecules) → P4-2 ✅ (organisms)   P4-3 ✅ (typed prefs)   P4-4 ✅ (DS tests, Robolectric)
        ┊
        ┊  ❌ RETIRED by the restructure (Stage 1 dropped the choice enums, Stage 2 dropped Auth+Supabase):
        └── P1-2/P1-3 (auth)   P2-2 (verify backend swap)   P1-4 (remote data → Remote Config, Stage 11)
```

**Next up:** P1-1 ✅, P1-5 ✅, P1-5b ✅, **P2-1 ✅ (navigation)**, **P3-2 ✅**, **P3-3 ✅ (paywall +
frnk Pro layer + god mode)**, **P4-5 ✅ (haptics)**, **P4-1 ✅ (molecules)**, **P4-2 ✅ (organisms)**,
**P4-3 ✅ (typed preferences wrapper)**, and **P4-4 ✅ (design-system tests)** are done — the Atomic
Design hierarchy (Atoms → Molecules → Organisms → Scaffolds) is complete, `KeyValueStore` has a typed
convenience layer, and the highest-value atoms now have Robolectric-backed Compose UI tests that gate in CI.
Recommended next is **P3-1 (PostHog)** to round out analytics. P1-2/P1-3/P2-2 are retired by the
restructure (Auth + the choice enums are gone); P1-4 lives on as the Stage 11 Remote Config reshape.
