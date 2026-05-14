# Architecture

## Module topology

```
                ┌───────────────┐
                │  core-common  │  AppResult, UiText, AppError
                └───────┬───────┘
        ┌───────────────┼───────────────┐
        ▼               ▼               ▼
 core-network-api  core-database-api   core-ui-atoms
        ▲               ▲               (MVI engine,
        │               │                headless atoms)
 core-network-impl core-database-impl
        ▲               ▲
        └─────┬─────────┘
              ▼
        androidApp (com.android.library)
        iosApp     (KMP framework / SPM)
              ▲
              │
   androidDemoApp / iosDemoApp
```

### Why api/impl split

Each domain (network, database) is split into two modules:

- **`*-api`** — pure-interface module. No third-party impls (no Ktor, no
  SQLDelight). Domain code depends only on these.
- **`*-impl`** — concrete implementation behind a Koin binding. Swap Ktor
  for the official Firebase KMP SDK later by replacing only the impl module.

Benefits:
- **Parallel Gradle compilation** — api modules build before any impl
  module starts.
- **Faster incremental builds** — touching the impl doesn't invalidate
  api consumers.
- **Test isolation** — fakes for testing live in test source sets of api
  consumers and never need to import the real impl.

## Module communication flow

1. A `core-ui-atoms` MVI ViewModel dispatches an action.
2. The reducer pure-mutates state; `onAction` calls a `*-api` interface
   (e.g. `NetworkClient`).
3. Koin resolves the interface to the concrete impl from `*-impl`.
4. The impl returns an `AppResult<Data, AppError>`.
5. The ViewModel folds the result into the next state or emits a
   `UiEffect` (navigation, toast).
6. `ObserveAsEvents` in the composable consumes effects without
   leaking across recompositions.

## Result wrapper

`AppResult<D, E : AppError>` is sealed: `Success(data)` / `Failure(error)`.
Every `*-api` interface returns `AppResult` instead of throwing, so
callers handle errors exhaustively at compile time.

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

Then declare dependencies on individual frnk modules in your app:

```kotlin
// In MyApp/app/build.gradle.kts
dependencies {
    implementation("dev.jdgarita.frnk:androidApp")
    implementation("dev.jdgarita.frnk:core-ui-atoms")
}
```

### Why composite builds over published artifacts

- **Live edits** — change frnk source and rebuild the consumer with no
  publish cycle.
- **Atomic refactors** — rename an api signature across both repos in
  one commit.
- **No registry overhead** — no Maven Central / GitHub Packages setup
  while the toolkit is private.

When the toolkit stabilises, you can flip to published artifacts by
keeping the module Maven coordinates the same.

## MVI engine

See `core-ui-atoms/src/commonMain/kotlin/.../ui/mvi/`:

- `MviContract.kt` — `UiState`, `UiAction`, `UiEffect` markers.
- `MviViewModel.kt` — abstract base; owns `StateFlow<S>`, action
  `SharedFlow<A>`, and an effect `Channel<E>` exposed as a flow.
- `ObserveAsEvents.kt` — Composable helper for one-shot effects.

ViewModels subclass `MviViewModel<S, A, E>`, implement a pure reducer,
and optionally override `onAction` for impure work (network, db).

## CI

`.github/workflows/main.yml` runs two jobs on every push and PR:

1. **ktlint** — `./gradlew ktlintCheck`
2. **build & test** — `./gradlew assemble allTests`

The `ktlint` job gates the build job to fail fast on style issues.
