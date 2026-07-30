# frnk — Requirements & Source of Truth

> **Status:** Living document. This is the canonical specification for the `frnk`
> toolkit. When code and this document disagree, treat the disagreement as a
> defect in one of them and reconcile deliberately — do not let drift accumulate.
>
> **Companion documents:**
> - `docs/ARCHITECTURE.md` — canonical module graph and api/impl split.
> - `docs/HOST_INTEGRATION.md` — how a host app consumes the toolkit.
> - `CLAUDE.md` — operational rules for AI agents working in this repo.
> - **MobiAI brain** (`.mobiai/brain/`) — decision rationale, integration quirks, testing
>   patterns, bugfixes, and the open-work list (`mobiai brain search "open work"`).

---

## 1. Product definition

`frnk` is a **Kotlin Multiplatform (KMP) + Compose Multiplatform (CMP) toolkit** —
not a shippable application. It is a reusable foundation that downstream mobile
apps (Android + iOS) consume to avoid re-implementing the same cross-cutting
concerns (design system, DI, navigation, persistence, remote config, analytics,
monetization) on every new product.

### 1.1 Consumption model

- The toolkit is consumed as a **Git submodule via a Gradle composite build**
  (`includeBuild("../frnk")`), not as a published Maven artifact (see §8.7 for
  rationale). Maven coordinates are kept stable so a later flip to published
  artifacts is non-breaking.
- Downstream **Android** apps depend on the individual `dev.jdgarita.frnk:<module>`
  coordinates they use (no aggregator — restructure Stage 1).
- Downstream **iOS** apps build their own umbrella XCFramework exporting the frnk
  modules they use (the demo's `DemoKit` is the worked example; see
  `docs/HOST_INTEGRATION.md` §6).
- `demo-android` and `iosDemoApp` are **internal smoke harnesses only** — they
  are not the shipping product and downstream consumers never depend on them.

### 1.2 Non-goals

- `frnk` is not an app, a backend, or a UI kit for sale.
- `frnk` does not bundle the native iOS frameworks of its SDK dependencies
  (Firebase, RevenueCat/`PurchasesHybridCommon`); the consuming Xcode project
  supplies those via CocoaPods/SPM (see §8.6).
- `frnk` does not prescribe product-specific screens — it provides scaffolds and
  atoms that hosts compose into product screens.

---

## 2. Architecture requirements

The architecture is specified in full in `docs/ARCHITECTURE.md`. This section
states the **invariants that must hold** — they are requirements, not
descriptions.

### 2.1 Module graph & the api/impl split (MANDATORY)

- `shared-utils` is the root module: coroutines, datetime, generated
  `BuildKonfig` config, `PlatformInfo` (the only `expect/actual` there), and
  pure-Kotlin helpers. It depends on nothing else in the graph.
- Every domain that pulls in a **third-party SDK** is split into:
  - **`*-api`** — pure-interface module. **No** Firebase / SQLDelight / RevenueCat
    dependency may ever appear here.
  - **`*-impl`** — concrete bindings exposed as a Koin module.
- Current api/impl pairs:
  - `:analytics-api` ↔ `:analytics-impl` (analytics + crash; Firebase)
  - `:remote-config-api` ↔ `:remote-config-impl` (Remote Config; Firebase — a sibling of analytics, Stage 11)
  - `:data-db-api` ↔ `:data-db-impl` (SQL driver SPI; split at restructure Stage 4)
  - `:data-prefs-api` ↔ `:data-prefs-impl` (key-value; split at restructure Stage 4)
  - `:monetization-api` ↔ `:monetization-impl` (RevenueCat)
  - `:camera` / `:permissions` are api-only **scaffolds** (Stage 11) — interface + no-op default, no impl yet.
- **Rule:** Domain code depends only on `*-api`. Toolkit code never imports an
  `*-impl` package — impls enter the graph only through the host's
  `initializeFrnk(modules = …)` list (demo wiring is the sanctioned exception;
  the `DatabaseContext` bootstrap seam lives in `:core-di` itself since Stage 4).
- `:core-mvi` owns the MVI engine and `:core-nav` the type-safe Navigation3
  contract — both carry **no Compose dependency**, so ViewModels compile without
  dragging in `compose.runtime` (split out of the old `shared-ui-api` at Stage 6).
- `:core-platform` owns SDK-free host-service contracts for camera capture, image
  selection/decoding, application settings actions, and maps. It has no Compose or SDK dependencies.
- The design system (tokens, theme engine, atoms, molecules, organisms,
  scaffolds) lives across `:ui-theme` → `:ui-components` → `:ui-scaffolds` (split
  out of the old `shared-ui-atoms` at Stage 7); `:ui-theme` is the lowest module
  allowed to depend on Compose.

### 2.2 Explicit-module-list bootstrap (MANDATORY)

- There is **no aggregator** (restructure Stage 1, OQ-7): hosts depend on the
  individual modules they use. `:core-di` owns the bootstrap; `:ui-app` owns the
  app root (`FrnkApp`) + `frnkUiModules()`.
- The one-shot bootstrap:
  ```kotlin
  // dev.jdgarita.frnk.di (:core-di)
  fun initializeFrnk(modules: List<Module>, extraConfig: KoinApplication.() -> Unit = {}): KoinApplication
  fun initializeFrnk(context: Context, modules: List<Module>, …): KoinApplication // androidMain
  // dev.jdgarita.frnk.ui.app (:ui-app)
  fun frnkUiModules(): List<Module>
  ```
- **Capability selection is the module list, not an enum.** A capability the
  host doesn't pass (`firebaseObservabilityModule`, `revenueCatModule`, …) is
  never installed, so its bindings never enter the graph.

### 2.3 Error handling contract (MANDATORY)

- Every `*-api` interface returns `AppResult<D, E : AppError>` (sealed
  `Success`/`Failure`) — **never throws** — so callers handle errors
  exhaustively with a compile-checked `when`.
- New interfaces must preserve this contract.

### 2.4 `:demo-shared` isolation (MANDATORY)

- `:demo-shared` depends only on the `*-api` modules +
  `:ui-theme`/`:ui-components`/`:ui-scaffolds` + `:ui-bottom-nav` +
  `:shared-monetization-ui` — never on an `*-impl` module in
  its common surface. This keeps `DemoKit.xcframework` free of Firebase /
  RevenueCat / SQLite native cinterops so `iosDemoApp` boots on a clean
  simulator with no CocoaPods.
- The demo binds fakes (`FakeEntitlementManager`, logging analytics/crash
  reporters) via `demoModule` and `bootstrapDemoKoin()`.

### 2.5 Toolchain pinning (MANDATORY — do not fight it)

- **JDK 17** everywhere (Foojay resolver auto-provisions; every KMP module calls
  `jvmToolchain(17)`).
- **Kotlin 2.4.0 + AGP 9.2.1 + Gradle 9.5.1.**
- KMP-Android modules apply `com.android.kotlin.multiplatform.library` and
  configure Android via `kotlin { android { … } }` — **not** a top-level
  `android {}` block, and **not** `kotlin.android`.
- `:demo-android` is the only `com.android.application`; it uses AGP 9's
  built-in Kotlin and must not re-add `kotlin.android`.
- Shared constants live in `gradle/libs.versions.toml` (there is no `buildSrc`);
  the group id is the `frnk-groupId` catalog entry. AGP 9 caps `compileSdk` at 36.

---

## 3. Feature requirements

Each feature below lists its **target capability**. Remaining work to close any
gaps against these targets is tracked as open-work entries in the MobiAI brain
(`mobiai brain search "open work"`).

### 3.1 Design system (Atomic Design)

- A custom design system built on **Atomic Design** principles:
  **Atoms → Molecules → Organisms**, with **Scaffolds** (page/screen templates)
  above atoms and below feature code.
- Design tokens: colors (light/dark), typography, spacing, shapes, icon sizes.
- A theme engine (`FrnkTheme`) with host-overridable tokens (colors, text
  styles, shapes, strings, icons) and animated light/dark switching.
- Press feedback (ripple) installed automatically as `LocalIndication`.
- A loading-skeleton (placeholder) capability available to content-bearing
  components.
- Every new atom must: define an `@Immutable *State`, read styling from theme
  tokens (never hardcoded colors/`.dp`), ship a `@Preview`, and explicitly
  decide whether it needs a skeleton.

### 3.2 Dependency injection (Koin)

- Koin is the single DI container.
- Each module exposes its bindings as a named Koin module; the host assembles
  them explicitly via `initializeFrnk(modules = frnkUiModules() + …)`.
- ViewModels are resolved with `koinViewModel { parametersOf(initialState) }`.

### 3.3 Navigation (tailored, custom)

- A toolkit-owned navigation layer with **type-safe routes**, host-owned back
  stack, argument passing, and back-gesture/system-back handling on Android.
- Must integrate with the MVI effect channel (navigation as a `UiEffect`).
- Must work in Compose Multiplatform common code (Android + iOS).

### 3.4 Local data sources

- **SQLDelight** for relational/structured persistence. The toolkit owns the
  `SqlDriverFactory` SPI (`:data-db-api`, drivers bound by `databaseModule` in
  `:data-db-impl`) — **never a schema** (restructure Stage 4 / OQ-2): each host
  (and the demo, via its `DemoDB`) defines its own SQLDelight database and
  builds it through the factory.
- **Local preferences** (key-value) via the `KeyValueStore` abstraction +
  typed `Preference<T>` layer (`:data-prefs-api`), backed by
  `multiplatform-settings` (`:data-prefs-impl`, bound by `prefsModule`).
- Drivers and the settings impl live only in the `*-impl` modules.

### 3.5 Remote config

- Read-only typed remote configuration behind `:remote-config-api`:
  `RemoteConfigService` (typed key→value + `fetchAndActivate`). Its own
  capability pair, a **sibling of analytics** (restructure Stage 11, OQ-1) — it
  replaced the old generic Firestore-shaped `RemoteData` stub (`AuthService` +
  the Supabase impl were dropped at Stage 2; the Firestore stub deleted at
  Stage 11).
- Implementation: **Firebase Remote Config** (`dev.gitlive:firebase-config`),
  `:remote-config-impl`. Install `remoteConfigModule` for the real backend, XOR
  `noopRemoteConfigModule` (`:remote-config-api`) to read bundled defaults only.
- Installed at runtime by passing its Koin module to `initializeFrnk(...)`.

### 3.6 Analytics

- A backend-agnostic `AnalyticsTracker` interface (event logging, user
  properties).
- Concrete trackers: **Firebase Analytics** and at least one provider-neutral
  option (**PostHog** is the named target; a no-op tracker is the safe default
  when none is configured).
- Crash reporting (`CrashReporter`) tracked alongside analytics.

### 3.7 Monetization (RevenueCat)

- In-app purchases / entitlements via **RevenueCat** (`purchases-kmp`).
- `:monetization-api` owns `EntitlementManager` + `FeatureGate`;
  `:monetization-impl` provides the concrete RevenueCat `EntitlementProvider`.
- Capability target: fetch offerings, present a paywall, run a purchase/restore
  flow, and gate features on active entitlements — all reactive to entitlement
  changes.

### 3.8 Demo coverage (cross-cutting requirement)

- **Every feature is demoed in all three demo layers:** `:demo-shared`
  (cross-platform), `demo-android` (runs on device/emulator), and `iosDemoApp`
  (runs in a simulator). A feature is not "done" until it is exercised in all
  three, or a written justification explains why it cannot be.

---

## 4. Strict UI constraints (NON-NEGOTIABLE)

These are hard rules. A change that violates one of them must be rejected in
review regardless of other merits.

1. **No Material Design.** No Material 2 and no Material 3 dependency may appear
   in any Gradle file, version catalog entry, or import statement —
   `androidx.compose.material`, `androidx.compose.material3`, and
   `com.google.android.material` are all forbidden.
2. **Foundation is `composablehorizons/compose-unstyled`.** The UI layer is
   built on the granular `com.composables:composeunstyled-*` artifacts
   (`primitives`, `theming`, `platformtheme`, `button`, `icon`, `separators`)
   plus `icons-lucide-cmp` and the standalone ripple-indication artifact —
   **not** `com.composables:core`.
3. **Tokens, not literals.** Atoms read styling from `Theme[colors][...]` /
   `Theme[textStyles][...]` etc. Hardcoded `Color(0xFF…)` or raw `.dp` in an
   atom is a defect.
4. **State classes are `@Immutable`.** Each component takes an `@Immutable
   *State` plus separate callback lambdas plus a `Modifier`.

---

## 5. Quality & process requirements

- **CI** is **paused while the repo is private** — the per-push `compile & test`
  job (`main.yml`) and the auto PR review were removed to stay under the
  free-tier Actions cap (only `release.yml` + `claude.yml` remain). The same gate
  runs **locally** before pushing: compile-only (`compileAndroidMain` + the demo
  app's `compileDebugKotlin`) followed by `testAndroidHostTest` + the demo app's
  `testDebugUnitTest`, both `--parallel --build-cache`. No `assemble`, no
  `allTests`, no `ktlintCheck`. The job returns once the repo goes public.
- **Style** is enforced **locally** by the `.githooks/pre-commit` hook running
  `ktlintFormat` and re-staging — installed automatically via `installGitHooks`.
  Bypass for one commit with `SKIP_KTLINT=1` or `--no-verify`.
- **Tests:** ViewModels/reducers (pure) and api-layer logic are covered by
  `commonTest` + `androidHostTest`. KMP-Android modules run host unit tests under
  **`testAndroidHostTest`** (not `testDebugUnitTest`) and must opt in with
  `kotlin { android { withHostTest {} } }`. The shared `FakeAnalyticsTracker` /
  `FakeCrashReporter` test doubles in `:analytics-api`'s `commonTest` are the
  canonical fake pattern for `*-api` interfaces.
- **Bootstrap:** `cp local.properties.template local.properties` (only
  `sdk.dir` is required; demo extras like `REVENUECAT_ANDROID_API_KEY` are
  optional).

---

## 6. Technical decisions (justification & evidence)

The **why** behind each foundational choice — compose-unstyled over Material,
KMP/CMP, the api/impl split, no aggregator, Koin, runtime capability selection,
composite build over published artifacts, MVI-with-no-Compose-in-`:core-mvi`, and
the iOS `dynamic_lookup` linker option — lives in the **MobiAI brain** as a single
decision entry so the rationale stays current and searchable:

```bash
mobiai brain search "foundational technical decisions"
mobiai brain context --section decisions
```

The adaptive-bottom-nav choice (adaptive-nav-bar + Material3 accepted toolkit-wide,
isolated to `:ui-bottom-nav`; Calf removed when it became the default) and every
restructure-stage decision are recorded there too. Keep
new architecture rationale in the brain, not inline here — this spec states the
*invariants* (§2, §4); the brain records *why* they were chosen.

---

## 7. Acceptance of this document

This document is the source of truth for *intent*. Changes to scope, the strict
UI constraints, or the architecture invariants must be made **here first**, then
propagated to `docs/ARCHITECTURE.md`, `CLAUDE.md`, and the code — in that order.
