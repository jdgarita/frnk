# Architecture

## Module topology

The toolkit is a flat, layered module tree under `frnk/{core,data,ui,capabilities}/`
(physical dirs) with flat Gradle names. The arrows below are the steady-state graph
that the 12-stage restructure produced.

```
core/   (no upward deps; util is the root everything depends on)
  util  ── coroutines + datetime + AppResult + PlatformInfo + Frnk.VERSION
  mvi   ── MviContract, MviViewModel, UiText             (no Compose)
  nav   ── Navigation3 contract, ToolkitRoute, registries (no Compose)
  di    ── initializeFrnk(modules) + requireFrnkKoin()

data/  + capabilities/   — each SDK-backed domain is an api ── impl pair (impl installed via Koin):
  db-api    ── db-impl     (SqlDriverFactory)     analytics-api     ── analytics-impl     (Firebase)
  prefs-api ── prefs-impl  (KeyValueStore)        remote-config-api ── remote-config-impl (Firebase Remote Config)
                                                  monetization-api  ── monetization-impl  (RevenueCat)
                                                  haptics       (contract + multihaptic engine; UI-feedback, no api/impl split)
                                                  camera, permissions   (api-only no-op scaffolds — no impl yet)

ui/   (floor → ceiling; haptics ← theme ← components ← scaffolds ← bottom-nav ← app)
  theme       ── FrnkTheme + tokens (compose-unstyled; installs ripple + haptics)
  components  ── atoms / molecules / organisms
  scaffolds   ── page templates + Compose MVI bindings (FrnkMviScreen/EffectCollector) + nav bindings (FrnkNavDisplay)
  bottom-nav  ── adaptive bottom nav + FrnkAppShell      (the SOLE Material3 module)
  app         ── FrnkAppScaffold + frnkUiModules()       (no *-impl compile deps)

  monetization-ui ── PaywallScreen + frnkPaywallDestination + rememberFrnkSettingsHandler
                     (above monetization-api + ui-scaffolds; no RevenueCat dep itself)

demo/   (pure consumers; hosts never depend on these)
  shared      ── DemoScreen + DemoViewModel + demoModule + fakes + MainViewController → DemoKit.xcframework
  android-app (:demo-android) ┐ smoke harnesses that consume demo/shared
  ios-app     (iosDemoApp)    ┘
```

There is **no aggregator** (deleted at restructure Stage 1): hosts depend on the
individual modules they use, with `:ui-app` as the optional batteries-included
apex and `:core-di` as the bootstrap. `:demo-shared` and its `DemoKit.xcframework`
are demo-only — a parity layer for the two smoke harnesses (`demo-android`,
`iosDemoApp`). Downstream consumers never depend on `:demo-shared`.

`:demo-shared` deliberately depends only on the `*-api` modules plus
`:ui-theme`/`:ui-components`/`:ui-scaffolds`, `:ui-bottom-nav`, and
`:shared-monetization-ui` — no `*-impl` modules in its common surface. This keeps
`DemoKit.xcframework` free of the Firebase / RevenueCat / SQLite native cinterop
references that would otherwise force iosDemoApp to ship `PurchasesHybridCommon`
+ Firebase pods just to launch. The demo binds fakes (`FakeEntitlementProvider`,
`FakeKeyValueStore`, `FakeNoteStore`, `LoggingAnalyticsTracker`, `LoggingCrashReporter`) and never
touches a real SDK, so it boots on a clean simulator with no extra setup. The demo
also owns its own SQLDelight schema (`DemoDB` + `Note.sq` + the
`dev.jdgarita.frnk.demo.notes` NoteStore) — the toolkit owns no schema (Stage 4 /
OQ-2), and the generated code is driver-free, so DemoKit stays clean; `demo-android`
overrides the fake with the real path (`databaseModule` + `demoNotesModule`) to
exercise `SqlDriverFactory` on a device exactly like a host would.

**Entitlement layering (P3-3).** Free/Pro is frnk-owned and independent of any
billing SDK: an `EntitlementProvider` (RevenueCat, or the demo fake) supplies
purchased state + offerings + purchase/restore, and the pure-Kotlin
`DefaultEntitlementManager` (`:monetization-api`, bound by
`monetizationModule`) wraps it and overlays a persisted **god mode** override —
so a developer can force Pro even in a release build. `FeatureGate` reads the
manager. The basic paywall + its toolkit-owned route (`frnkPaywallDestination`)
+ the Settings monetization wiring (`rememberFrnkSettingsHandler`) live in
`shared-monetization-ui`, above the design system.

**Haptics.** A simplified, host-facing haptics layer ships with the design system rather than as an api/impl backend split, because `multihaptic` is a UI-feedback library (like the ripple), not a swappable infrastructure SDK, and has no native cinterop. The Compose-free contract — `HapticType` (semantic enum) + `HapticFeedback` + the `HapticEngine` SPI + `DefaultHapticFeedback` (in-memory enabled flag, gated `perform`) — lives in `:haptics`, so ViewModels can inject it. The `multihaptic` binding (`MultiHapticEngine`) and the `LocalFrnkHaptics` composition local **also live in `:haptics`**, installed by `:ui-theme`'s `FrnkTheme` via `rememberFrnkHaptics()` (resolves the platform `Vibrator` with no Context plumbing). Interactive atoms call `perform(...)` on press; the toolkit's default Settings catalog ships the "Haptic feedback" toggle (`HAPTICS_TOGGLE_ID`) and `rememberFrnkSettingsHandler` flips `HapticFeedback.setEnabled`, so hosts get the whole loop with zero custom code.

Consumers face the modules directly: Android hosts declare the `dev.jdgarita.frnk:<module>` coordinates they use (substituted by the composite build); iOS hosts export the same modules from their own umbrella XCFramework (see `docs/HOST_INTEGRATION.md` §6).

### On-disk layout vs Gradle paths

The diagram above names Gradle projects, not folders. Modules live at their layered
locations — `frnk/core/*`, `frnk/data/*`, `frnk/ui/*`, `frnk/capabilities/*`, and
`demo/{shared,android-app,ios-app}` — and keep flat Gradle names via `projectDir`
remaps in `settings.gradle.kts` (e.g. `:ui-components` at `frnk/ui/components`,
accessor `projects.uiComponents`; `:demo-shared` at `demo/shared`). `settings.gradle.kts`
is the single source of truth for the name→dir→accessor mapping. Two Gradle names still carry a legacy `shared-`
prefix — `:shared-utils` (`frnk/core/util`) and `:shared-monetization-ui`
(`frnk/capabilities/monetization-ui`) — both current and correct; everything else is at
its final flat name.

### Dependency rules

These are enforced in review:

```
core-util ← everything                       # the root; depends on nothing in the graph
core-mvi, core-nav, core-di:                 no Compose, no upward deps
data-*-api ← data-*-impl                      # capabilities may depend on data-*-api, never on a *-impl
haptics ← ui-theme ← ui-components ← ui-scaffolds ← ui-bottom-nav ← ui-app
ui-app ← {monetization-ui, analytics-api}     # resolves impls via Koin at runtime, never at compile time
monetization-api ← {analytics-api, data-prefs-api}
monetization-ui  ← {ui-scaffolds, monetization-api}
remote-config-api ← remote-config-impl        # sibling of analytics, never merged into it
Material3 only in ui-bottom-nav (ui-app inherits it transitively — the accepted batteries-included trade)
Only demo modules may depend on *-impl modules from code; hosts wire impls via Koin modules only
```

The haptics direction is load-bearing: atoms call `LocalFrnkHaptics`, `FrnkTheme`
installs it — so `:haptics` (contract **and** engine) sits *below* `:ui-theme` and
must not depend on any `ui` module.

### Why api/impl split

Each domain that pulls in a third-party SDK is split:

- **`*-api`** — pure-interface module. No Ktor, no Firebase, no SQLDelight. Domain code depends only on these.
- **`*-impl`** (e.g. `:analytics-impl`, `:data-db-impl`, `:data-prefs-impl`, `:monetization-impl`, `:remote-config-impl`) — concrete bindings exposed as Koin modules.

Capabilities (`frnk/capabilities/`) follow the same rule: **`:remote-config-api`** (`RemoteConfigService` — read-only typed key→value + `fetchAndActivate`; a sibling of `:analytics-*`, **not** part of it) is backed by **`:remote-config-impl`** (Firebase Remote Config). **`:camera`** and **`:permissions`** are api-only **scaffolds** (Stage 11) — interface + no-op default + Koin module, no impl yet, no native cinterop — so they stay out of every XCFramework's link surface until a real impl lands.

Benefits:
- **Parallel Gradle compilation** — api modules build before any impl module starts.
- **Faster incremental builds** — touching an impl doesn't invalidate api consumers.
- **Test isolation** — fakes for testing live in test source sets of api consumers and never need to import the real impl.

### Why no aggregator (restructure Stage 1)

The old `:shared` aggregator bundled every api **and** impl module behind one coordinate and selected capabilities with enums (`BackendChoice`/`ObservabilityChoice`/`MonetizationChoice` + a generated aggregator helper). That coupling died at Stage 1 (OQ-7): hosts now depend on exactly the modules they use and pass an **explicit Koin module list** to `initializeFrnk(...)` — what isn't passed is never in the graph, and there's no compile-time bundle of unused SDKs.

## Bootstrap surface (`:core-di` + `:ui-app`)

```kotlin
// dev.jdgarita.frnk.di (:core-di)
fun initializeFrnk(modules: List<Module>, extraConfig: KoinApplication.() -> Unit = {}): KoinApplication
fun initializeFrnk(context: Context, modules: List<Module>, extraConfig: ... = {}): KoinApplication // androidMain
fun requireFrnkKoin(): Koin   // fail-fast accessor FrnkAppScaffold uses

// dev.jdgarita.frnk.ui.app (:ui-app)
fun frnkUiModules(): List<Module>   // the SDK-free scaffold VM modules (Home/Settings/Onboarding/BottomNav)
@Composable fun FrnkAppScaffold(appName, appVersion, …) { homeContent }
```

Analytics + crash reporting stay a **separate axis from the data backend** (BACKLOG P1-5): a
local-storage-only app can still install `firebaseObservabilityModule` to ship Firebase
Analytics + Crashlytics; `noopObservabilityModule` (`:analytics-api`) binds the no-op
defaults (`Noop{Analytics,Crash}`). Install exactly one of the two.
On iOS, `firebaseObservabilityModule` additionally installs the CrashKiOS unhandled-exception hook
(`:analytics-impl`'s `enableNativeCrashHandler`, iOS-only — no-op on Android) so *uncaught*
Kotlin crashes reach Crashlytics symbolicated, not just the exceptions a caller explicitly
`recordException`s (BACKLOG P1-5b).

The Android `initializeFrnk(context, modules)` overload also sets `DatabaseContext.application` (the shared Context seam, owned by `:core-di` androidMain since Stage 4) and registers `androidContext(...)`. **The toolkit owns no SQLDelight schema** (restructure Stage 4 / OQ-2): `databaseModule` (`:data-db-impl`) binds only the platform `SqlDriverFactory`, and the host's own schema module builds its database through it (`MyDb(factory.create(MyDb.Schema, "my.db"))` — the demo's `DemoDB`/`demoNotesModule` is the worked example). Key-value persistence is the separate `prefsModule` (`:data-prefs-impl`, binds `KeyValueStore`). The full copy-paste snippet lives in `docs/HOST_INTEGRATION.md` §4.

## Module communication flow

1. A composable dispatches a `UiIntent` via `viewModel.send(intent)`.
2. The ViewModel handles it in `onIntent`: it reduces state purely with `setState { copy(...) }` and/or calls a `*-api` interface (e.g. `RemoteConfigService` from `:remote-config-api`).
3. Koin resolves the interface to the concrete impl from a `*-impl` module — whichever the host installed in its `initializeFrnk(modules = …)` list.
4. The impl returns an `AppResult<Data, AppError>`.
5. The ViewModel folds the result into the next state or emits a `UiEffect` (navigation, toast) via `emit(effect)`.
6. The composable collects one-shot effects with the lifecycle-aware `EffectCollector(vm.effects) { … }` (in `:ui-scaffolds`, `ui/mvi/`) — use it instead of a hand-rolled `LaunchedEffect(vm) { vm.effects.collect(...) }` so effects don't leak across recompositions or fire at a backgrounded screen. **Navigation** is one such effect: a single collector mutates the host-owned `NavBackStack` via `navigateTo`/`back`/`clearAndNavigateTo` (see Navigation below).

## Result wrapper

`AppResult<D, E : AppError>` (in `shared-utils`, the neutral root) is sealed: `Success(data)` / `Failure(error)`. Every `*-api` interface returns `AppResult` instead of throwing, so callers handle errors exhaustively at compile time. It lives in `shared-utils` (not `:analytics-api`) so any domain — backend, persistence, monetization, and the demo's `NoteStore` — can return it without a sibling `*-api`→`*-api` dependency.

## iOS native dependency contract

`:monetization-impl` and `:analytics-impl` cinterop with the native RevenueCat (purchases-ios) and Firebase SDKs. The toolkit does NOT ship those native frameworks — the consumer Xcode project brings them in via CocoaPods or SPM. An umbrella framework that bundles these modules (the demo's `DemoKit`; a host's own XCFramework) uses `linkerOpts("-undefined", "dynamic_lookup")` so the link succeeds locally; the symbols resolve when the consumer's iOS app links (see `docs/HOST_INTEGRATION.md` §6). Both `demo-android` and `iosDemoApp` call `DemoBootstrapKt.bootstrapDemoKoin()` to install the demo's fake bindings.

## Consuming via composite build (includeBuild)

In a downstream app's `settings.gradle.kts`:

```kotlin
pluginManagement {
    includeBuild("../frnk")  // path to this submodule
}

dependencyResolutionManagement {
    repositories { google(); mavenCentral() }
}

rootProject.name = "MyApp"
include(":app")
```

Add the submodule:

```bash
git submodule add git@github.com:jdgarita/frnk.git frnk
git submodule update --init --recursive
```

Then declare the modules you use:

```kotlin
// In MyApp/app/build.gradle.kts
dependencies {
    implementation("dev.jdgarita.frnk:ui-app")            // FrnkAppScaffold + frnkUiModules() (+ core-di transitively)
    implementation("dev.jdgarita.frnk:data-db-impl")      // databaseModule (SqlDriverFactory)
    implementation("dev.jdgarita.frnk:data-prefs-impl")   // prefsModule (KeyValueStore)
    // + the impl modules you install (observability, monetization, backend)
}
```

In the host `Application`:

```kotlin
import dev.jdgarita.frnk.di.initializeFrnk
import dev.jdgarita.frnk.ui.app.frnkUiModules

initializeFrnk(
    context = this,
    modules = frnkUiModules() + databaseModule + prefsModule + firebaseObservabilityModule +
        listOf(myAppModule, sqlDelightSchemaModule),
)
```

### Why composite builds over published artifacts

- **Live edits** — change frnk source and rebuild the consumer with no publish cycle.
- **Atomic refactors** — rename an api signature across both repos in one commit.
- **No registry overhead** — no Maven Central / GitHub Packages setup while the toolkit is private.

When the toolkit stabilises, you can flip to published artifacts by keeping the module Maven coordinates the same.

## MVI engine

See `frnk/core/mvi/src/commonMain/kotlin/.../ui/mvi/`:

- `MviContract.kt` — `UiState`, `UiIntent`, `UiEffect` marker interfaces.
- `MviViewModel.kt` — abstract base; owns `StateFlow<S>`, an intent `SharedFlow<I>` (replay=0, buffer=16, `DROP_OLDEST`), and a one-shot effect `Channel<E>` (BUFFERED) exposed as `effects`.

ViewModels subclass `MviViewModel<S, I, E>`, reduce state purely with `setState { copy(...) }`, and override `suspend fun onIntent(intent: I)` for impure work (network, db), emitting one-shots via `emit(effect)`. Composables dispatch with `send(intent)` and collect effects via `EffectCollector(vm.effects) { … }` (or `FrnkMviScreen`'s built-in `onEffect`).

## Navigation

The toolkit-owned navigation layer is built on **AndroidX Navigation3** (type-safe `NavKey` routes + a `NavBackStack` the host owns and mutates), split across the no-Compose / Compose boundary like everything else:

- **`:core-nav`** (`ui/nav/`) holds the Compose-free contract (the nav3 *runtime* is pure Kotlin/MP): `ToolkitRoute` — a `@Serializable sealed interface ToolkitRoute : NavKey` of default routes — the back-stack mutation helpers (`NavBackStack<NavKey>.navigateTo(route, popScreen?, singleTop=true)` / `back()` / `clearAndNavigateTo(route)`), `frnkNavConfiguration(hostRoutes)` (builds the `SavedStateConfiguration` that persists/restores the back stack across config change + process death, registering `ToolkitRoute` plus the host's own polymorphic routes), the `FrnkPendingRouteRequest` deep-link signal, and the `FrnkFullScreenRoute` marker (a route implementing it hides the tabbed scaffold's bottom bar — `FrnkTabbedNavScaffold`'s default `hideBarFor`). Routes are `@Serializable` (needs `kotlinx-serialization-core` — **not** `-json`; nav3 encodes routes via savedstate's `SavedStateEncoder`) and depend on `androidx-navigation3-runtime` (`NavKey`/`NavBackStack`). No Compose here, so feature ViewModels and effect handlers compile without `compose.runtime`.
- **`:ui-scaffolds`** (`ui/nav/`) holds the Compose bindings: `rememberFrnkNavBackStack(config, start)` (a `rememberSaveable` `NavBackStack`), `FrnkNavDisplay(backStack, entryProvider, …)` (over nav3's `NavDisplay`, baking in the saveable-state + ViewModel-store entry decorators and the slide transitions), `rememberFrnkTabbedBackStacks(config, navTabs)` (per-tab back stacks for the **multiple-back-stack** bottom-nav pattern) + `FrnkTabbedBackHandler` (back-from-non-home-root → home), and `FrnkNavTab` (the unified per-tab declaration: key + root + icon + label). `entryProvider` defaults to Koin's `koinEntryProvider()` (pair with the `navigation<Route> { … }` DSL); pass an inline `entryProvider { entry<Route> { … } }` to register destinations directly.
- **`ui-bottom-nav`** (`ui/bottomnav/`) holds the one-call tabbed scaffold: `FrnkTabbedNavScaffold(tabbed, tabs, hideBarFor, entryProvider)` wraps the `FrnkNavDisplay` + the persistent platform-adaptive bottom bar + tab switching / re-tap-to-root + `FrnkTabbedBackHandler` + full-screen bar hiding (`hideBarFor` defaults to `{ it is FrnkFullScreenRoute }`) + the bottom-inset (provided via `LocalFrnkBottomBarInset`, so screens on `FrnkScreenScaffold`/`FrnkMviScreen` reserve the bar automatically). The bar (`FrnkBottomFloatingBar`) is an `expect`/`actual` composable — a Material3 Expressive `HorizontalFloatingToolbar` on Android, a native glassy `UITabBar` (over [adaptive-nav-bar](https://github.com/narendraanjana09/adaptive-navigation-bar)) on iOS; for multiple-back-stack nav3 without Material3, hand-wire the primitives with a custom bar. Its `primaryActionRegistry` param routes the bar's built-in primary-action button to the **currently active screen** (`FrnkPrimaryActionRegistry` in `:core-nav`, claimed via `FrnkPrimaryActionHandler` in `:ui-scaffolds`; the host-level `onPrimaryAction` stays the fallback, the button hides when neither is wired). Bar icons are `ImageVector` (Android) + SF-Symbol string (iOS); Android never touches `DrawableResource`, so there is **no host-side asset step**.
- **`ui-bottom-nav`** (`ui/app/`) also holds **`FrnkAppShell`** — the one-call **app shell** over everything above: theme wrap, nav config, Home + middle + Settings tabs with per-tab back stacks, built-in Home (`HomeScreen` content slot) / Settings (default catalogue + `extraSections`) / optional Onboarding destinations, deep-links, and the primary-action registry; each host extension point is handed a `FrnkAppScope` (`navigateTo`/`back` + registry). **`:ui-app`** layers **`FrnkAppScaffold`** on top (Koin fail-fast assertion via `:core-di`'s `requireFrnkKoin()`, live `EntitlementManager`-driven Settings, `rememberFrnkSettingsHandler`, auto-mounted `ToolkitRoute.Paywall`) — the host-facing "whole app in one call" entry point (`docs/HOST_INTEGRATION.md` §8); Android hosts bootstrap with the androidMain `initializeFrnk(context, modules)` overload (`:core-di`) first.

**The toolkit ships the nav display + scaffold machinery; the host owns the back-stack instance** (it creates it via `rememberFrnkNavBackStack` / `rememberFrnkTabbedBackStacks` and passes it in). Navigation is driven by the MVI effect channel: a ViewModel emits a navigation `UiEffect`, and a single `EffectCollector` above the display mutates the host-owned back stack via `navigateTo`/`back`/`clearAndNavigateTo` (collect it in exactly one place — the effect channel is single-consumer). On Android `NavDisplay` consumes the system back button / predictive-back gesture to pop the back stack automatically (the host activity sets `android:enableOnBackInvokedCallback="true"`); on iOS the back-gesture support depends on the Compose Multiplatform runtime, so screens should also carry an on-screen back affordance. `:demo-shared`'s `DemoScreen` is the reference integration — rebuilt over `FrnkAppShell`: the shell supplies the Home/Settings/Onboarding destinations and three bottom-nav tabs (each keeping its own back stack); the demo registers only its middle "Components" tab, the pushed `ComponentDetail(name)`, and the `Paywall` destination.

## CI

**Build/test CI is paused while the repo is private.** Free-tier GitHub Actions minutes are capped on private repos, and the per-push `compile & test` job (`main.yml`) was exhausting the spending limit during foundation work — so it and the auto PR review (`claude-code-review.yml`) were removed. Two workflows remain, both triggered only rarely:

- **`release.yml`** — on a `v*` tag push, publishes the GitHub Release (see `RELEASING.md`). The deliberately-kept tag-release job.
- **`claude.yml`** — on-demand `@claude` assistant on issues/PRs.

**Validate locally before pushing** (this is the gate the old CI job enforced). After seeding a dummy `local.properties` so `BuildKonfig` resolves, run in order:

1. `./gradlew compileAndroidMain :demo-android:compileDebugKotlin --parallel --build-cache`
2. `./gradlew testAndroidHostTest :demo-android:testDebugUnitTest --parallel --build-cache`

`compileAndroidMain` covers `commonMain` + `androidMain` for every KMP module under the AGP 9 KMP-Android plugin (the old `compileDebugKotlinAndroid` task no longer exists for these modules); `:demo-android:compileDebugKotlin` covers the pure-Android smoke harness. `testAndroidHostTest` runs `commonTest` + `androidHostTest` for every KMP module that opted in via `kotlin { android { withHostTest {} } }` — that is the AGP 9 KMP-Android host unit-test task (there is no `testDebugUnitTest` for KMP modules); `:demo-android` is a `com.android.application`, so its unit tests run under `testDebugUnitTest`. iOS targets are skipped on a Linux runner.

Style is enforced **locally** via a git pre-commit hook (`.githooks/pre-commit`) that runs `ktlintFormat` and re-stages the fixes. The hook is installed automatically the first time `./gradlew` runs (the root build registers an `installGitHooks` task wired to `prepareKotlinBuildScriptModel`).

`assemble` and `allTests` stay out of the local gate too: compile-only is enough to catch breakage, and downstream consumer apps (or a manual `./gradlew assemble`) cover full release assembly and the iOS link step.

**When the foundation is in place and the repo goes public** (unlimited Actions minutes): re-add branch protection on `main`, enable PRs, and restore the `compile & test` job — optionally `claude-code-review.yml` too.
