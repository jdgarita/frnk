# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this repo is

`frnk` is a Kotlin Multiplatform + Compose Multiplatform **toolkit** (not a standalone app) intended to be consumed by downstream apps as a Git submodule via a Gradle composite build (`includeBuild("../frnk")`). The `androidDemoApp` / `iosDemoApp` directories exist only as internal smoke harnesses — they are not the shipping product.

Read `docs/ARCHITECTURE.md` first when reasoning about cross-module changes; it is the canonical description of the module graph and the api/impl split.

## Common commands

Bootstrap (one-time per checkout):
```bash
cp local.properties.template local.properties   # then fill in Supabase/Firebase keys; BuildKonfig reads from this
```

Day-to-day:
```bash
./gradlew ktlintCheck                  # lint — CI gates on this before assembling
./gradlew ktlintFormat                 # auto-fix style
./gradlew assemble                     # build every target (Android library + iOS frameworks)
./gradlew allTests                     # run commonTest across all KMP modules
./gradlew :core-network-impl:allTests  # run a single module's tests
./gradlew :iosApp:assembleXCFramework  # produce iosApp/build/XCFrameworks/release/FrnkKit.xcframework
./gradlew clean
```

`local.properties` is gitignored and required — `BuildKonfig` will fail at configuration time without `SUPABASE_URL`, `SUPABASE_ANON_KEY`, `FIREBASE_*`, and `BUILD_VARIANT`.

## Toolchain pinning (don't fight it)

- **JDK 17** everywhere. Android Studio bundles JDK 21; the Foojay resolver in `settings.gradle.kts` auto-provisions 17, and `androidApp` explicitly calls `kotlin { jvmToolchain(17) }` because AGP's javac and the Kotlin task would otherwise disagree about target. Don't change this without changing both sides.
- **Kotlin 2.0.21 + AGP 8.7.0.** AGP is one minor ahead of the version KMP 2.0.21 was tested against (8.5). The compatibility warning is suppressed in `gradle.properties` (`kotlin.mpp.androidGradlePluginCompatibility.nowarn=true`) — don't "fix" that line.
- Shared constants (package name, min/compile/target SDK, iOS framework name `FrnkKit`, database class name `FrnkDB`) live in `buildSrc/src/main/kotlin/ProjectConfiguration.kt`. Read from there rather than hardcoding.

## Architecture you need to respect

**api/impl module split.** Each external-dependency domain has two modules:

- `core-network-api` / `core-database-api` — pure-interface, no Ktor / no SQLDelight. Downstream code depends on these.
- `core-network-impl` / `core-database-impl` — concrete bindings registered via Koin.

The point is swap-ability (e.g. replacing Ktor with the Firebase KMP SDK touches only the impl) and parallel compilation. **Do not** add a third-party client dependency to an `*-api` module, and do not call into an `*-impl` package from anywhere except `androidApp` / `iosApp` wiring.

**`core-common` is the root.** It owns `AppResult<D, E : AppError>` (sealed `Success`/`Failure`), `UiText`, and `BuildKonfig`-generated config. Every `*-api` interface returns `AppResult` rather than throwing — preserve this when adding new interfaces, because callers rely on exhaustive `when` for error handling.

**`core-ui-atoms`** owns:
- Headless Compose components built on `compose-unstyled` (`com.composables:core`).
- The MVI engine: `MviContract` (`UiState` / `UiAction` / `UiEffect` markers), `MviViewModel<S, A, E>` (StateFlow + action SharedFlow + effect Channel), and `ObserveAsEvents` for one-shot effects in composables. New screens subclass `MviViewModel`, write a pure reducer, and override `onAction` for side-effectful work.

**Public entry points** are `androidApp` (an `com.android.library`, **not** an application) and `iosApp` (a KMP target producing the fat `FrnkKit` XCFramework via `XCFramework("FrnkKit")`). Both `api(...)` re-export every core module so a downstream consumer only depends on `dev.jdgarita.frnk:androidApp` / the XCFramework.

## Ktlint is enforced

`./build.gradle.kts` applies the ktlint plugin to **all** projects with `ignoreFailures.set(false)`. CI (`.github/workflows/main.yml`) runs `ktlintCheck` as a gating job before `assemble allTests` — a style violation will fail PRs. Run `./gradlew ktlintFormat` before committing.

Note: there are two CI workflow files (`main.yml` and the older `KtlintCheck.yml`) both triggering on push to `main` — they overlap, and `main.yml` is the authoritative pipeline.

## Conventions to follow when adding code

- New networking call: define the interface + DTOs in `core-network-api`, the Ktor implementation in `core-network-impl/internal/`, register the binding in `NetworkModule.kt`. Return `AppResult`, never throw.
- New persisted entity: add the `.sq` file under `core-database-impl` (SQLDelight generates into `dev.jdgarita.frnk.database.sql` — set in the database module's `build.gradle.kts`); expose access through an interface in `core-database-api`; bind in `DatabaseModule.kt`.
- New screen: ViewModel + state/action/effect types in the relevant module (or `core-ui-atoms` for shared infra); compose the UI with atoms from `core-ui-atoms`; collect effects via `ObserveAsEvents`.
- iOS-visible API surface: anything Swift needs to call must be `export`ed from `iosApp/build.gradle.kts` (currently `core-common`, `core-network-api`, `core-database-api`, `core-ui-atoms`).
