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

`:shared` is the single consumer-facing surface. `androidApp` and `iosApp` each depend on `:shared` only — they re-export it for downstream apps and add nothing else.

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
enum class BackendChoice { Supabase, Firebase }

fun frnkModules(backend: BackendChoice = BackendChoice.Supabase): List<Module>

fun initializeFrnk(
    backend: BackendChoice = BackendChoice.Supabase,
    extraConfig: KoinApplication.() -> Unit = {},
): KoinApplication
```

`initializeFrnk` calls `startKoin { modules(frnkModules(backend)); extraConfig() }`. Hosts add `androidContext(...)` or their own SQLDelight schema module via `extraConfig`.

## Module communication flow

1. A `shared-ui-atoms` MVI ViewModel dispatches an action.
2. The reducer pure-mutates state; `onAction` calls a `*-api` interface (e.g. `AuthService` from `shared-backend-api`).
3. Koin resolves the interface to the concrete impl from a `*-impl` module — whichever the host installed via `frnkModules(BackendChoice.…)`.
4. The impl returns an `AppResult<Data, AppError>`.
5. The ViewModel folds the result into the next state or emits a `UiEffect` (navigation, toast).
6. `ObserveAsEvents` in the composable consumes effects without leaking across recompositions.

## Result wrapper

`AppResult<D, E : AppError>` (in `shared-backend-api`) is sealed: `Success(data)` / `Failure(error)`. Every `*-api` interface returns `AppResult` instead of throwing, so callers handle errors exhaustively at compile time.

## iOS native dependency contract

`:shared` bundles `shared-monetization-revenuecat` (and `shared-backend-firebase`), which cinterop with the native `PurchasesHybridCommon` (and Firebase) frameworks. The toolkit does NOT ship those native frameworks inside `FrnkKit.xcframework` — the consumer Xcode project must bring them in via CocoaPods or SPM (`pod 'PurchasesHybridCommon'`, `pod 'FirebaseAuth'`, etc.). Both `:iosApp` and `:shared-demo` framework binaries use `linkerOpts("-undefined", "dynamic_lookup")` so the link succeeds locally; the symbols resolve when the consumer's iOS app links. From Swift, call `FrnkKitKt.bootstrapFrnkKit(backend:)` to start Koin. `iosDemoApp` calls `DemoBootstrapKt.bootstrapDemoKoin(backend:)` instead — it adds the demo's fake `EntitlementManager` / `AnalyticsTracker` / `CrashReporter` bindings on top of `frnkModules(backend)` and is what `androidDemoApp` calls as well, so the two demos share a single entry point.

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

See `shared-ui-atoms/src/commonMain/kotlin/.../ui/mvi/`:

- `MviContract.kt` — `UiState`, `UiAction`, `UiEffect` markers.
- `MviViewModel.kt` — abstract base; owns `StateFlow<S>`, action `SharedFlow<A>`, and an effect `Channel<E>` exposed as a flow.
- `ObserveAsEvents.kt` — Composable helper for one-shot effects.

ViewModels subclass `MviViewModel<S, A, E>`, implement a pure reducer, and optionally override `onAction` for impure work (network, db).

## CI

`.github/workflows/main.yml` is a single job on every push and PR. It runs, in order:

1. `./gradlew compileDebugKotlinAndroid --parallel --build-cache`
2. `./gradlew testDebugUnitTest --parallel --build-cache`

Style is enforced **locally** via a git pre-commit hook (`.githooks/pre-commit`) that runs `ktlintFormat` and re-stages the fixes — so CI doesn't need a separate `ktlintCheck` job. The hook is installed automatically the first time `./gradlew` runs (the root build registers an `installGitHooks` task wired to `prepareKotlinBuildScriptModel`).

The CI job intentionally skips `assemble` and `allTests`: compile-only is enough to gate merges, and downstream consumer apps (or a manual `./gradlew assemble`) cover full release assembly and the iOS link step.
