# Architecture

## Module topology

```
                           ┌──────────────┐
                           │ shared-utils │  coroutines + datetime root
                           └──────┬───────┘
        ┌─────────────────┬───────┴────────┬─────────────────┐
        ▼                 ▼                ▼                 ▼
 shared-ui-api    shared-database-api shared-backend-api  (interfaces only)
        ▲                 ▲                ▲
        │                 │                ├─── shared-backend-firebase
        │                 │                └─── shared-backend-supabase
        │                 │
 shared-ui-atoms  shared-database-impl
 (MVI engine,
  headless atoms)
                                  shared-monetization-api
                                          ▲
                                          └─── shared-monetization-revenuecat

                          ┌─────────────────────────────┐
                          │           shared            │
                          │  api()'s every *-api +      │
                          │  every *-impl module        │
                          │  + frnkModules() +          │
                          │    initializeFrnk()         │
                          └──────────────┬──────────────┘
                                         ▼
                          androidApp (com.android.library)
                          iosApp     (FrnkKit XCFramework)
                                         ▲
                                         │
                          ┌─────────────────────────────┐
                          │        shared-demo          │
                          │  Compose DemoScreen + MVI   │
                          │  + Koin demoModule +        │
                          │    fakes + MainViewController│
                          │  (DemoKit XCFramework)      │
                          └──────────────┬──────────────┘
                                         ▼
                              androidDemoApp / iosDemoApp
```

The production toolkit ends at `androidApp` / `iosApp` (`FrnkKit.xcframework`).
`:shared-demo` and its `DemoKit.xcframework` are demo-only — a parity layer for
the two smoke harnesses (`androidDemoApp`, `iosDemoApp`). Downstream consumers
never depend on `:shared-demo`.

`:shared-demo` deliberately depends only on the `*-api` modules plus
`shared-ui-atoms` — **not** `:shared`. This keeps `DemoKit.xcframework` free of
the Firebase / RevenueCat / SQLite native cinterop references that would
otherwise force iosDemoApp to ship `PurchasesHybridCommon` + Firebase pods just
to launch. The demo binds fakes (`FakeEntitlementManager`,
`LoggingAnalyticsTracker`, `LoggingCrashReporter`) and never touches a real
SDK, so it boots on a clean simulator with no extra setup.

`:shared` is the single consumer-facing surface. `androidApp` and `iosApp` each depend on `:shared` only — they re-export it for downstream apps and add nothing else.

### On-disk layout vs Gradle paths

The diagram above names Gradle projects, not folders. On disk, every `shared-*` module (plus `shared-utils` and `shared-demo`) lives **inside** the `shared/` directory next to the `:shared` aggregator itself. Gradle project paths stay flat (`:shared-ui-atoms`, `projects.sharedUiAtoms`) — `settings.gradle.kts` remaps each module's `projectDir` to `shared/<name>` so the type-safe accessors and `:module` task paths are unaffected by the move. Rule of thumb: a `shared/` prefix for filesystem paths, no prefix for Gradle paths.

### Why api/impl split

Each domain that pulls in a third-party SDK is split:

- **`*-api`** — pure-interface module. No Ktor, no Firebase, no SQLDelight. Domain code depends only on these.
- **`*-impl`** (e.g. `shared-backend-firebase`, `shared-backend-supabase`, `shared-database-impl`, `shared-monetization-revenuecat`) — concrete bindings exposed as Koin modules.

Benefits:
- **Parallel Gradle compilation** — api modules build before any impl module starts.
- **Faster incremental builds** — touching an impl doesn't invalidate api consumers.
- **Test isolation** — fakes for testing live in test source sets of api consumers and never need to import the real impl.

### Why a `:shared` aggregator

Before, `androidApp` and `iosApp` each listed six `api(projects.shared-*-api)` entries by hand, and host apps wired the impl modules themselves. Now `:shared` does that aggregation in one place and bundles **both** backend impls (firebase + supabase) plus the database and monetization impls. Hosts pick which backend at runtime via `BackendChoice` — the unchosen backend's Koin module is simply not registered.

## `:shared` public surface

```kotlin
// dev.jdgarita.frnk.shared
enum class BackendChoice { Supabase, Firebase }          // auth + remote data
enum class ObservabilityChoice { None, Firebase }        // analytics + crash — independent axis

fun frnkModules(
    backend: BackendChoice = BackendChoice.Supabase,
    observability: ObservabilityChoice = ObservabilityChoice.None,
): List<Module>

fun initializeFrnk(
    backend: BackendChoice = BackendChoice.Supabase,
    observability: ObservabilityChoice = ObservabilityChoice.None,
    extraConfig: KoinApplication.() -> Unit = {},
): KoinApplication
```

Analytics + crash reporting are a **separate axis from `BackendChoice`** (BACKLOG P1-5): a
local-storage-only app with no backend — or a Supabase-backed app — can still select
`ObservabilityChoice.Firebase` to ship Firebase Analytics + Crashlytics. `None` binds the no-op
defaults (`Noop{Analytics,Crash}` in `shared-backend-api`, via `noopObservabilityModule`).
On iOS, `ObservabilityChoice.Firebase` additionally installs the CrashKiOS unhandled-exception hook
(`shared-backend-firebase`'s `enableNativeCrashHandler`, iOS-only — no-op on Android) so *uncaught*
Kotlin crashes reach Crashlytics symbolicated, not just the exceptions a caller explicitly
`recordException`s (BACKLOG P1-5b).

`initializeFrnk` calls `startKoin { modules(frnkModules(backend, observability)); extraConfig() }`. Hosts add `androidContext(...)` via `extraConfig`. The toolkit owns its own SQLDelight schema (`FrnkDB`, generated into `dev.jdgarita.frnk.database.sql`): `databaseModule` builds it from the platform `SqlDriverFactory` + `FrnkDB.Schema` and binds `NoteStore`. Hosts may still install their own additional schema module via `extraConfig` if they want app-specific tables.

## Module communication flow

1. A composable dispatches a `UiIntent` via `viewModel.send(intent)`.
2. The ViewModel handles it in `onIntent`: it reduces state purely with `setState { copy(...) }` and/or calls a `*-api` interface (e.g. `AuthService` from `shared-backend-api`).
3. Koin resolves the interface to the concrete impl from a `*-impl` module — whichever the host installed via `frnkModules(BackendChoice.…)`.
4. The impl returns an `AppResult<Data, AppError>`.
5. The ViewModel folds the result into the next state or emits a `UiEffect` (navigation, toast) via `emit(effect)`.
6. The composable collects one-shot effects with the lifecycle-aware `EffectCollector(vm.effects) { … }` (in `:shared-ui-atoms`, `ui/mvi/`) — use it instead of a hand-rolled `LaunchedEffect(vm) { vm.effects.collect(...) }` so effects don't leak across recompositions or fire at a backgrounded screen. **Navigation** is one such effect: a single collector routes it into a `FrnkNavigator` (see Navigation below).

## Result wrapper

`AppResult<D, E : AppError>` (in `shared-utils`, the neutral root) is sealed: `Success(data)` / `Failure(error)`. Every `*-api` interface returns `AppResult` instead of throwing, so callers handle errors exhaustively at compile time. It lives in `shared-utils` (not `shared-backend-api`) so any domain — backend, database (`NoteStore`), monetization — can return it without a sibling `*-api`→`*-api` dependency.

## iOS native dependency contract

`:shared` bundles `shared-monetization-revenuecat` (and `shared-backend-firebase`), which cinterop with the native `PurchasesHybridCommon` (and Firebase) frameworks. The toolkit does NOT ship those native frameworks inside `FrnkKit.xcframework` — the consumer Xcode project must bring them in via CocoaPods or SPM (`pod 'PurchasesHybridCommon'`, `pod 'FirebaseAuth'`, etc.). `:iosApp` framework binaries use `linkerOpts("-undefined", "dynamic_lookup")` so the link succeeds locally; the symbols resolve when the consumer's iOS app links. From Swift, call `FrnkKitKt.bootstrapFrnkKit(backend:)` to start Koin. `iosDemoApp` does NOT consume `FrnkKit.xcframework` — it uses `DemoKit.xcframework` from `:shared-demo`, which excludes those native cinterops by design and so requires no pods at all. Both `androidDemoApp` and `iosDemoApp` call `DemoBootstrapKt.bootstrapDemoKoin()` to install the demo's fake bindings.

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

Then declare a single dependency on the toolkit:

```kotlin
// In MyApp/app/build.gradle.kts
dependencies {
    implementation("dev.jdgarita.frnk:androidApp")
}
```

In the host `Application`:

```kotlin
import dev.jdgarita.frnk.shared.BackendChoice
import dev.jdgarita.frnk.shared.initializeFrnk
import org.koin.android.ext.koin.androidContext

initializeFrnk(backend = BackendChoice.Supabase) {
    androidContext(this@MyApp)
    modules(myAppModule, sqlDelightSchemaModule)
}
```

### Why composite builds over published artifacts

- **Live edits** — change frnk source and rebuild the consumer with no publish cycle.
- **Atomic refactors** — rename an api signature across both repos in one commit.
- **No registry overhead** — no Maven Central / GitHub Packages setup while the toolkit is private.

When the toolkit stabilises, you can flip to published artifacts by keeping the module Maven coordinates the same.

## MVI engine

See `shared/shared-ui-api/src/commonMain/kotlin/.../ui/mvi/`:

- `MviContract.kt` — `UiState`, `UiIntent`, `UiEffect` marker interfaces.
- `MviViewModel.kt` — abstract base; owns `StateFlow<S>`, an intent `SharedFlow<I>` (replay=0, buffer=16, `DROP_OLDEST`), and a one-shot effect `Channel<E>` (BUFFERED) exposed as `effects`.

ViewModels subclass `MviViewModel<S, I, E>`, reduce state purely with `setState { copy(...) }`, and override `suspend fun onIntent(intent: I)` for impure work (network, db), emitting one-shots via `emit(effect)`. Composables dispatch with `send(intent)` and collect effects via `EffectCollector(vm.effects) { … }` (or `FrnkMviScreen`'s built-in `onEffect`).

## Navigation

The toolkit-owned navigation layer is a thin wrapper over JetBrains CMP `navigation-compose`, split across the no-Compose / Compose boundary like everything else:

- **`shared-ui-api`** (`ui/nav/`) holds the Compose-free contract: `ToolkitRoute` — a `@Serializable` sealed interface of default routes — and `FrnkNavigator` (`navigate(route)`, `navigate(route, options)`, `navigateUp`, `popBackStack`) with `FrnkNavOptions` (tab-switch save/restore back-stack flags). Routes are `@Serializable` (needs `kotlinx-serialization-core` — **not** `-json`; nav encodes routes via its own `SavedStateEncoder`).
- **`shared-ui-atoms`** (`ui/nav/`) holds the Compose primitives: `rememberFrnkNavController()`, `FrnkNavHost(navController, startRoute) { … }`, the reified `frnkComposable<T> { route -> … }` (type-safe destination + argument decode via `toRoute<T>()`), and `rememberFrnkNavigator(navController)` (adapts a `NavController` to `FrnkNavigator`).

**The toolkit ships the `NavHost` machinery; the host owns the back-stack instance** (it creates the `NavController` and passes it in). Navigation is driven by the MVI effect channel: a ViewModel emits a navigation `UiEffect`, and a single `EffectCollector` above the `FrnkNavHost` routes it into the `FrnkNavigator` (collect it in exactly one place — the effect channel is single-consumer). On Android `NavHost` consumes the system back button / predictive-back gesture to pop the back stack automatically (the host activity sets `android:enableOnBackInvokedCallback="true"`); on iOS the back-gesture support depends on the Compose Multiplatform runtime, so screens should also carry an on-screen back affordance. `:shared-demo`'s `DemoScreen` is the reference integration — three bottom-nav tab roots as top-level destinations (each with its own saved back stack via the tab-switch options) plus pushed `ComponentDetail(name)` / `Onboarding` / `Paywall` destinations.

## CI

`.github/workflows/main.yml` is a single `compile & test` job on every push and PR to `main` (Markdown, `docs/**`, and `LICENSE` changes are path-ignored). After seeding a dummy `local.properties` so `BuildKonfig` resolves (CI never exercises real backends), it runs, in order:

1. `./gradlew compileAndroidMain :androidDemoApp:compileDebugKotlin --parallel --build-cache`
2. `./gradlew testAndroidHostTest :androidDemoApp:testDebugUnitTest --parallel --build-cache`

`compileAndroidMain` covers `commonMain` + `androidMain` for every KMP module under the AGP 9 KMP-Android plugin (the old `compileDebugKotlinAndroid` task no longer exists for these modules); `:androidDemoApp:compileDebugKotlin` covers the pure-Android smoke harness. `testAndroidHostTest` runs `commonTest` + `androidHostTest` for every KMP module that opted in via `kotlin { android { withHostTest {} } }` — that is the AGP 9 KMP-Android host unit-test task (there is no `testDebugUnitTest` for KMP modules); `:androidDemoApp` is a `com.android.application`, so its unit tests run under `testDebugUnitTest`. iOS targets are skipped on the Linux runner.

Style is enforced **locally** via a git pre-commit hook (`.githooks/pre-commit`) that runs `ktlintFormat` and re-stages the fixes — so CI doesn't need a separate `ktlintCheck` job. The hook is installed automatically the first time `./gradlew` runs (the root build registers an `installGitHooks` task wired to `prepareKotlinBuildScriptModel`).

The CI job intentionally skips `assemble` and `allTests`: compile-only is enough to gate merges, and downstream consumer apps (or a manual `./gradlew assemble`) cover full release assembly and the iOS link step.
