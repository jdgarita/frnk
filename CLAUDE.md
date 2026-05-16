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
./gradlew compileDebugKotlinAndroid         # fast compile-only check (what CI runs); Gradle parallelises across modules
./gradlew testDebugUnitTest                 # commonTest + androidUnitTest across all KMP modules (what CI runs)
./gradlew :shared-database-impl:testDebugUnitTest   # run a single module's tests
./gradlew ktlintFormat                      # auto-fix style (also runs from the pre-commit hook)
./gradlew assemble                          # full build of every target — only when producing release artifacts
./gradlew :iosApp:assembleFrnkKitReleaseXCFramework  # produce iosApp/build/XCFrameworks/release/FrnkKit.xcframework
./gradlew :shared-demo:assembleDemoKitDebugXCFramework  # produce shared-demo/build/XCFrameworks/debug/DemoKit.xcframework (iosDemoApp consumes this)
./gradlew clean
```

`local.properties` is gitignored and required — `BuildKonfig` will fail at configuration time without `SUPABASE_URL`, `SUPABASE_ANON_KEY`, `FIREBASE_*`, and `BUILD_VARIANT`.

## Toolchain pinning (don't fight it)

- **JDK 17** everywhere. Android Studio bundles JDK 21; the Foojay resolver in `settings.gradle.kts` auto-provisions 17, and every KMP module explicitly calls `kotlin { jvmToolchain(17) }` because AGP's javac and the Kotlin task would otherwise disagree about target. Don't change this without changing both sides.
- **Kotlin 2.3.21 + AGP 9.2.1 + Gradle 9.5.1.** AGP 9 forbids combining `org.jetbrains.kotlin.multiplatform` with `com.android.library` in the same subproject — every KMP-Android module here applies `com.android.kotlin.multiplatform.library` (alias `libs.plugins.android.kotlin.multiplatform.library`) and configures the Android side via `kotlin { android { … } }`, not a top-level `android {}` block. Compile/min SDK live inside that nested block.
- `:androidDemoApp` (the only `com.android.application`) uses AGP 9's **built-in Kotlin** — it does **not** apply `kotlin.android`. Don't re-add that plugin.
- Shared constants (package name, min/compile/target SDK, iOS framework name `FrnkKit`, database class name `FrnkDB`) live in `buildSrc/src/main/kotlin/ProjectConfiguration.kt`. Read from there rather than hardcoding. AGP 9 caps compileSdk at 36.

## Architecture you need to respect

**`:shared` is the only consumer-facing module.** It aggregates every `shared-*` module via `api(...)` — both interfaces and implementations — and exposes `frnkModules(BackendChoice)` + `initializeFrnk()` for one-shot Koin bootstrap. `androidApp` and `iosApp` each depend on `:shared` only, and re-export it.

**api/impl module split.** Each domain that pulls in a third-party SDK has two modules:

- `shared-backend-api`, `shared-database-api`, `shared-monetization-api` — pure-interface, no Ktor / no SQLDelight / no RevenueCat. Domain code depends on these.
- `shared-backend-firebase`, `shared-backend-supabase`, `shared-database-impl`, `shared-monetization-revenuecat` — concrete bindings exposed as Koin modules (`firebaseBackendModule`, `supabaseBackendModule`, `databaseModule`, `revenueCatModule`).

The point is swap-ability and parallel compilation. **Do not** add a third-party SDK dependency to an `*-api` module, and do not call into an `*-impl` package from anywhere except `:shared`'s Koin wiring.

**Backend choice is runtime, not compile-time.** `:shared` bundles both `shared-backend-firebase` and `shared-backend-supabase`. `frnkModules(BackendChoice.Supabase)` registers `supabaseBackendModule`; `BackendChoice.Firebase` registers `firebaseBackendModule`. The unchosen backend's Koin module is simply never installed, so its bindings never appear in the graph.

**`shared-utils` is the root.** It owns coroutines + datetime + `BuildKonfig`-generated config. `AppResult<D, E : AppError>` (sealed `Success`/`Failure`) currently lives in `shared-backend-api`. Every `*-api` interface returns `AppResult` rather than throwing — preserve this when adding new interfaces, because callers rely on exhaustive `when` for error handling.

**`shared-ui-atoms`** owns:
- Headless Compose components built on `compose-unstyled` (`com.composables:core`).
- The MVI engine: `MviContract` (`UiState` / `UiAction` / `UiEffect` markers), `MviViewModel<S, A, E>` (StateFlow + action SharedFlow + effect Channel), and `ObserveAsEvents` for one-shot effects in composables. New screens subclass `MviViewModel`, write a pure reducer, and override `onAction` for side-effectful work.

**Public entry points** are `androidApp` (a KMP-Android library via `com.android.kotlin.multiplatform.library`, **not** an application) and `iosApp` (a KMP target producing the fat `FrnkKit` XCFramework via `XCFramework("FrnkKit")`). Both depend on `:shared` only and re-export it. Downstream consumers depend on `dev.jdgarita.frnk:androidApp` / the XCFramework — that's it.

**`:shared-demo` is demo-only.** A KMP module that owns the cross-platform `DemoScreen` composable, `DemoViewModel` (MVI), `demoModule` (Koin bindings with `FakeEntitlementManager` + logging fakes), and an iOS `MainViewController()` factory that Swift mounts via `UIViewControllerRepresentable`. It produces its own `DemoKit.xcframework`. Critically, `:shared-demo` does **not** depend on `:shared` — only the `*-api` modules + `shared-ui-atoms`. This keeps `DemoKit.xcframework` free of Firebase / RevenueCat / SQLite native cinterops, so `iosDemoApp` boots on a clean simulator with no CocoaPods. `androidDemoApp` and `iosDemoApp` share `bootstrapDemoKoin()` as their single Koin entry point — it installs only `demoModule`, so the demo screen exercises `FeatureGate` against the fake `EntitlementManager` without any real backend init. Production consumers never touch `:shared-demo`; they use `FrnkKit.xcframework` from `:iosApp`.

**iOS linker quirk.** The `iosApp` framework binaries set `linkerOpts("-undefined", "dynamic_lookup")` because `:shared` bundles `shared-monetization-revenuecat`, which cinterops the native `PurchasesHybridCommon` framework — and that native framework is expected to be supplied by the consumer Xcode project via CocoaPods or SPM. Deferring symbol resolution lets the toolkit's XCFramework link locally; the consumer app's own link step resolves PurchasesHybridCommon (and any Firebase native pods) at integration time.

## Ktlint runs locally, not in CI

`./build.gradle.kts` applies the ktlint plugin to **all** projects with `ignoreFailures.set(false)`. Style is enforced via a **git pre-commit hook** (`.githooks/pre-commit`) that runs `./gradlew ktlintFormat` and re-stages the fixed files — so commits land already-formatted and CI no longer needs a separate ktlint job.

The hook activates automatically: the root build registers an `installGitHooks` task that points `core.hooksPath` at `.githooks/`, and it's wired to `prepareKotlinBuildScriptModel` so IDE sync (or any `./gradlew` invocation that triggers it) installs the hook. To install manually: `./gradlew installGitHooks`. To bypass for one commit: `SKIP_KTLINT=1 git commit ...` or `git commit --no-verify`.

## CI

`.github/workflows/main.yml` is a single job that runs `compileAndroidMain :androidDemoApp:compileDebugKotlin` followed by `testDebugUnitTest`, both with `--parallel --build-cache`. Compile covers `commonMain`+`androidMain` for every shared module (iOS targets are skipped on Linux runners) plus the demo app's Kotlin. The `compileAndroidMain` task name comes from the AGP 9 KMP-Android plugin — `compileDebugKotlinAndroid` no longer exists for KMP modules. Tests cover `commonTest`+`androidUnitTest`. No `assemble`, no `allTests`, no `ktlintCheck` — those are heavier than what CI needs to gate merges.

## Conventions to follow when adding code

- New backend call: define the interface + DTOs in `shared-backend-api`, add concrete impls in `shared-backend-firebase` **and** `shared-backend-supabase` (both have to satisfy the contract), register the bindings in `FirebaseBackendModule.kt` / `SupabaseBackendModule.kt`. Return `AppResult`, never throw.
- New persisted entity: add the `.sq` file under `shared-database-impl` (SQLDelight generates into `dev.jdgarita.frnk.database.sql` — set in the database module's `build.gradle.kts`); expose access through an interface in `shared-database-api`; bind in `DatabaseModule.kt`.
- New screen: ViewModel + state/action/effect types in the relevant module (or `shared-ui-atoms` for shared infra); compose the UI with atoms from `shared-ui-atoms`; collect effects via `ObserveAsEvents`.
- iOS-visible API surface: everything flows through `:shared`, which `iosApp` already `export`s. Anything Swift needs to call must be in a `shared-*` module that `:shared` `api()`-depends on (which is all of them). No per-module `export(...)` edits required.
- **Every new feature is demoed in all three demo layers.** A feature isn't done until it's exercised in `:shared-demo` (cross-platform), `androidDemoApp` (installs and runs on a device/emulator), and `iosDemoApp` (builds the `DemoKit.xcframework` and runs in a simulator). If a feature genuinely can't be demoed (e.g., a build-time-only refactor), say so explicitly and explain why.
