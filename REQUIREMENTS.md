# frnk — Requirements & Source of Truth

> **Status:** Living document. This is the canonical specification for the `frnk`
> toolkit. When code and this document disagree, treat the disagreement as a
> defect in one of them and reconcile deliberately — do not let drift accumulate.
>
> **Companion documents:**
> - `docs/ARCHITECTURE.md` — canonical module graph and api/impl split.
> - `EVALUATION.md` — current-state gap analysis against this document.
> - `BACKLOG.md` — prioritized, acceptance-criteria-driven task list derived from the gaps.
> - `CLAUDE.md` — operational rules for AI agents working in this repo.

---

## 1. Product definition

`frnk` is a **Kotlin Multiplatform (KMP) + Compose Multiplatform (CMP) toolkit** —
not a shippable application. It is a reusable foundation that downstream mobile
apps (Android + iOS) consume to avoid re-implementing the same cross-cutting
concerns (design system, DI, navigation, persistence, remote data, analytics,
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
- `androidDemoApp` and `iosDemoApp` are **internal smoke harnesses only** — they
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
  - `:shared:backend:api` ↔ `:shared:backend:firebase`
  - `shared-database-api` ↔ `shared-database-impl`
  - `shared-monetization-api` ↔ `shared-monetization-revenuecat`
- **Rule:** Domain code depends only on `*-api`. Toolkit code never imports an
  `*-impl` package — impls enter the graph only through the host's
  `initializeFrnk(modules = …)` list (demo wiring and `:core-di`'s androidMain
  `DatabaseContext` bootstrap are the sanctioned exceptions).
- `shared-ui-api` owns the MVI engine and carries **no Compose dependency**, so
  ViewModels compile without dragging in `compose.runtime`.
- `shared-ui-atoms` owns the design system (tokens, theme engine, atoms,
  scaffolds) and is the lowest module allowed to depend on Compose.

### 2.2 Explicit-module-list bootstrap (MANDATORY)

- There is **no aggregator** (restructure Stage 1, OQ-7): hosts depend on the
  individual modules they use. `:core-di` owns the bootstrap; `:ui-app` owns the
  batteries-included app root (`FrnkAppScaffold`) + `frnkUiModules()`.
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

### 2.4 `:shared-demo` isolation (MANDATORY)

- `:shared-demo` depends only on the `*-api` modules + `shared-ui-atoms` +
  `shared-ui-nav` + `shared-monetization-ui` — never on an `*-impl` module in
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
- `:androidDemoApp` is the only `com.android.application`; it uses AGP 9's
  built-in Kotlin and must not re-add `kotlin.android`.
- Shared constants live in `buildSrc/.../ProjectConfiguration.kt`. AGP 9 caps
  `compileSdk` at 36.

---

## 3. Feature requirements

Each feature below lists its **target capability**. Maturity against these
targets is tracked in `EVALUATION.md`; the work to close gaps is in `BACKLOG.md`.

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

- **SQLDelight** for relational/structured persistence, generating into
  `dev.jdgarita.frnk.database.sql`, exposed through `shared-database-api`
  interfaces and bound in `DatabaseModule`. Database class name: `FrnkDB`.
- **Local preferences** (key-value) via a `KeyValueStore` abstraction backed by
  `multiplatform-settings`.
- Both behind `shared-database-api`; drivers (Android/native) live only in
  `shared-database-impl`.

### 3.5 Remote data sources

- Pluggable backend behind `:shared:backend:api`: `RemoteData`,
  `AnalyticsTracker`, `CrashReporter`. (`AuthService` + the Supabase impl were
  dropped in restructure Stage 2 — remote data is being repurposed as Firebase
  Remote Config at Stage 11.)
- Implementation: **Firebase** (`dev.gitlive:firebase-*`) — firestore,
  analytics, crashlytics.
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
- `shared-monetization-api` owns `EntitlementManager` + `FeatureGate`;
  `shared-monetization-revenuecat` provides the concrete `EntitlementManager`.
- Capability target: fetch offerings, present a paywall, run a purchase/restore
  flow, and gate features on active entitlements — all reactive to entitlement
  changes.

### 3.8 Demo coverage (cross-cutting requirement)

- **Every feature is demoed in all three demo layers:** `:shared-demo`
  (cross-platform), `androidDemoApp` (runs on device/emulator), and `iosDemoApp`
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

- **CI** (`.github/workflows/main.yml`): compile-only gate
  (`compileAndroidMain` + the demo app's `compileDebugKotlin`) followed by
  `testAndroidHostTest` + the demo app's `testDebugUnitTest`, both
  `--parallel --build-cache`. No `assemble`, no `allTests`, no `ktlintCheck` in CI.
- **Style** is enforced **locally** by the `.githooks/pre-commit` hook running
  `ktlintFormat` and re-staging — installed automatically via `installGitHooks`.
  Bypass for one commit with `SKIP_KTLINT=1` or `--no-verify`.
- **Tests:** ViewModels/reducers (pure) and api-layer logic are covered by
  `commonTest` + `androidHostTest`. KMP-Android modules run host unit tests under
  **`testAndroidHostTest`** (not `testDebugUnitTest`) and must opt in with
  `kotlin { android { withHostTest {} } }`. The shared `FakeAnalyticsTracker` /
  `FakeCrashReporter` test doubles in `:shared:backend:api`'s `commonTest` are the
  canonical fake pattern for `*-api` interfaces.
- **Bootstrap:** `cp local.properties.template local.properties` (only
  `sdk.dir` is required; demo extras like `REVENUECAT_ANDROID_API_KEY` are
  optional).

---

## 6. Technical decisions (justification & evidence)

This section records **why** each foundational choice was made, so the direction
is defensible and reversible decisions are distinguished from load-bearing ones.

### 6.1 Why `compose-unstyled` over Material

| Concern | Material 3 | `compose-unstyled` |
| --- | --- | --- |
| Visual identity | Ships an opinionated Google look; overriding it is a fight against defaults | Headless/unstyled primitives — the design system *is* ours from line one |
| Theming model | `MaterialTheme` color/typography system is the contract; deviating is friction | Token axes we define (`colors`, `textStyles`, `shapes`, `strings`, `icons`) are the contract |
| Binary weight | Pulls a large component set, much unused | Granular artifacts — we depend only on `primitives/theming/button/icon/separators` |
| Lock-in | App-wide assumptions about Material components | No framework opinions to unwind later |

**Decision:** Build the design system on `compose-unstyled`. **Evidence the rule
holds today:** a repo-wide search finds zero Material dependencies in Gradle/TOML
and zero Material imports in Kotlin; the only `material` matches are comments
documenting their *absence*.

### 6.2 Why Kotlin Multiplatform + Compose Multiplatform

One shared codebase (logic **and** UI) across Android and iOS. The toolkit's
value is amortizing cross-cutting concerns once; KMP/CMP is the only stack that
shares both the architecture and the rendered UI while still allowing per-platform
escapes via `expect/actual`.

### 6.3 Why the api/impl module split

- **Parallel compilation** — api modules build before any impl starts.
- **Faster incremental builds** — touching an impl doesn't invalidate api
  consumers.
- **Swap-ability** — backends/providers swap by changing the installed Koin module, not a recompile of domain code.
- **Test isolation** — fakes live in test sources of api consumers and never
  import a real SDK.

### 6.4 Why no aggregator (revised at restructure Stage 1)

The original `:shared` aggregator bundled every api + impl behind one
coordinate. It was deleted (Stage 1, OQ-7): explicit per-module dependencies
keep unused SDKs out of host builds, composite-build substitution works
per-coordinate anyway, and the "one call" ergonomics survive in
`initializeFrnk(modules)` + `FrnkAppScaffold`.

### 6.5 Why Koin (not a compile-time DI)

KMP-friendly, no annotation processing across targets, and runtime module
composition is exactly what the explicit-module-list bootstrap needs (install
only what the host passes).

### 6.6 Why runtime capability selection

Capabilities are installed by passing their Koin modules to
`initializeFrnk(...)`; domain code resolves only `*-api` interfaces, so
providers swap without recompiling features — and what a host doesn't install
never ships in its dependency graph at all.

### 6.7 Why composite build over published artifacts

- **Live edits** — change toolkit source and rebuild the consumer with no
  publish cycle.
- **Atomic refactors** — rename an api signature across both repos in one commit.
- **No registry overhead** while the toolkit is private.
- Reversible: Maven coordinates are stable, so flipping to published artifacts
  later is non-breaking.

### 6.8 Why MVI with no Compose in `shared-ui-api`

Keeps the presentation contract (`UiState`/`UiIntent`/`UiEffect`,
`MviViewModel`) compilable and testable without `compose.runtime`, so the engine
is reusable and unit-testable in isolation.

### 6.9 Why the iOS `dynamic_lookup` linker option

`shared-monetization-revenuecat` cinterops the native RevenueCat SDK and
`:shared:backend:firebase` references Firebase. The toolkit does not ship those
native frameworks; an umbrella XCFramework bundling these modules uses
`linkerOpts("-undefined", "dynamic_lookup")` to defer symbol resolution so it
links locally and the consumer's Xcode project resolves the native SDKs at
integration time (the demo's `DemoKit` does exactly this for its iosMain
CrashKiOS + RevenueCat cinterops).

---

## 7. Acceptance of this document

This document is the source of truth for *intent*. Changes to scope, the strict
UI constraints, or the architecture invariants must be made **here first**, then
propagated to `docs/ARCHITECTURE.md`, `CLAUDE.md`, and the code — in that order.
