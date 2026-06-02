<p align="center">
  <img width="224" height="353" alt="frnk-2-clean (1)" src="https://github.com/user-attachments/assets/dc37abec-66be-4754-ad36-a92093c91e0b" />
</p>


# frnk

A Kotlin Multiplatform + Compose Multiplatform **toolkit** — not a standalone app. Consumed by downstream apps as a Git submodule via a Gradle composite build (`includeBuild("../frnk")`), it provides a strict, modular baseline so feature work doesn't start from zero.

> See [`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md) for the canonical module graph and api/impl rationale. Product scope and roadmap live in [`REQUIREMENTS.md`](REQUIREMENTS.md), [`EVALUATION.md`](EVALUATION.md), and [`BACKLOG.md`](BACKLOG.md).

## 🎯 Objective

Give indie / small-team apps a fast-compiling foundation with a clean architecture, aggressive modularization, and a standardized MVI presentation layer — so the day-to-day work stays on features and business logic.

## 🏗️ Architecture

A single `:shared` module is the consumer-facing surface. Internally it aggregates a flat **api / impl** module split: `*-api` modules hold only interfaces and DTOs; `*-impl` modules hold the concrete bindings (Ktor, SQLDelight, Firebase, Supabase, RevenueCat) wired via Koin. `:shared` bundles every api **and** every impl, so host apps depend on one module and pick what to install at runtime: the auth/remote backend via `BackendChoice`, and analytics + crash reporting via `ObservabilityChoice` (a **separate axis** — a local-only app with no backend can still ship Firebase telemetry).

### Module map

| Module | Purpose |
| --- | --- |
| `shared` | Consumer-facing aggregator. Re-exports every `shared-*` module via `api(...)`, exposes `frnkModules(BackendChoice, ObservabilityChoice)` and `initializeFrnk()` for one-shot Koin bootstrap. |
| `shared-utils` | Root utilities — coroutines, datetime, `Logger`, `PlatformInfo` (the module's only `expect/actual`: OS + device), `FeedbackEmail` (`mailto:` draft builder), and `Frnk.VERSION`. Every other shared module depends on this. |
| `shared-ui-api` | The **MVI engine**, no Compose deps: `MviContract` (`UiState` / `UiIntent` / `UiEffect`), `MviViewModel<S, I, E>` (StateFlow + intent flow + effect channel; `setState`/`onIntent`/`emit`), plus `ToolkitRoute` and `UiText`. Feature ViewModels subclass `MviViewModel` here without pulling in Compose. |
| `shared-ui-atoms` | The **design system** on headless `compose-unstyled` (no Material3): tokens (`FrnkColors` / `FrnkTypography` / `FrnkSpacing` / `FrnkShapes` / `FrnkIconSize`), the `FrnkTheme` engine, `Frnk*` atoms (`FrnkText`, `FrnkButton`, `FrnkIcon`, `FrnkIconButton`, `FrnkDivider`, `FrnkSwitch`, `FrnkSegmentedControl`, `FrnkTopAppBar`, `FrnkBottomNavBar`), molecules (compositions of atoms — `FrnkListRow`, `FrnkLabeledValue`, `FrnkEmptyState`), organisms (self-contained sections composed from molecules/atoms — `FrnkListSection`, `FrnkProfileHeader`), and page scaffolds (`OnboardingScreen`, `SettingsScreen`, `BottomNavScaffold`, `FrnkScreenScaffold` + `CollapsibleBarsState`). Atoms ship a built-in **loading skeleton** (`FrnkSkeleton` + `Modifier.frnkSkeleton`), automatic **press ripple** (`FrnkTheme` installs `rememberFrnkRipple()` as `LocalIndication`), and automatic **haptics** (`FrnkTheme` installs `LocalFrnkHaptics`; atoms vibrate on press, gated by the Settings "Haptic feedback" toggle — `LocalFrnkHaptics.current.perform(HapticType.Success)` for host code). |
| `shared-backend-api` | Auth / Analytics / CrashReporter / RemoteData interfaces + the no-op observability defaults (`Noop{Analytics,Crash}`). Owns `AppResult<D, E : AppError>`. |
| `shared-backend-firebase` | Firebase impl of `shared-backend-api`. Exposes `firebaseBackendModule` (auth + remote data) and `firebaseObservabilityModule` (analytics + crash). |
| `shared-backend-supabase` | Supabase + Ktor impl of `shared-backend-api`. Exposes `supabaseBackendModule`. |
| `shared-database-api` | Persistence contracts (`SqlDriverFactory`, `KeyValueStore`, `NoteStore`). |
| `shared-database-impl` | SQLDelight (`FrnkDB`) + Multiplatform Settings impl — `SqlDelightNoteStore`, `SettingsKeyValueStore`. Exposes `databaseModule`. |
| `shared-monetization-api` | Entitlement / feature-gate interfaces. |
| `shared-monetization-revenuecat` | RevenueCat impl. Exposes `revenueCatModule`. |
| `androidApp` | Public entry point as a KMP-Android library (`com.android.kotlin.multiplatform.library`). `api(projects.shared)` — one dep, no surprises. |
| `iosApp` | KMP target producing the fat `FrnkKit.xcframework` (consumed via SPM). `export(projects.shared)`. |
| `shared-demo` | Demo-only KMP module — bundles `DemoScreen` / `DemoViewModel` / `demoModule` + fakes for the smoke harnesses. Depends only on `*-api` modules + `shared-ui-atoms`, so `DemoKit.xcframework` is free of native cinterops (no Pods required to run `iosDemoApp`). |
| `androidDemoApp` / `iosDemoApp` | Internal smoke harnesses — not the shipping product. |

## 🧰 Tech stack

- **Language:** Kotlin 2.3.21
- **UI:** Compose Multiplatform 1.11.0 + `compose-unstyled` 2.4.0 (the granular `com.composables:composeunstyled-*` artifacts — **not** `com.composables:core`, **not** Material3) + Lucide icons (`icons-lucide-cmp` 2.2.1)
- **DI:** Koin 4.2.1
- **Navigation:** Compose Multiplatform Navigation 2.9.2 (`org.jetbrains.androidx.navigation`)
- **Persistence:** SQLDelight 2.3.2, Multiplatform Settings 1.3.0
- **Backend:** Supabase 3.6.0 + Ktor 3.5.0, and GitLive Firebase 2.4.0 — both impls bundled, host picks via `BackendChoice`
- **Observability:** GitLive Firebase Analytics + Crashlytics 2.4.0 — opt in via `ObservabilityChoice.Firebase`, independent of the backend choice
- **Monetization:** RevenueCat 3.0.2
- **Haptics:** multihaptic 0.3.2 (`top.ltfan.multihaptic`) — cross-platform Android/iOS, no native cinterop
- **Build:** AGP 9.2.1, Gradle 9.5.1, JDK 17 (auto-provisioned via the Foojay resolver in `settings.gradle.kts`)

## 🚀 Consume as a composite build

This toolkit is designed to live alongside the consuming app as a Git submodule.

**1. Add the submodule and pin it to a release tag:**
```bash
git submodule add git@github.com:jdgarita/frnk.git frnk
cd frnk && git checkout v0.1.0 && cd ..    # pin to a published release — see Releases page
git add frnk && git commit -m "Pin frnk to v0.1.0"
```

> The submodule pointer is a commit SHA — checking out a tag records the tagged commit, which is how the consumer locks the toolkit version. To bump later: `cd frnk && git fetch --tags && git checkout vX.Y.Z && cd .. && git add frnk && git commit`.

**2. Wire it into the consumer's `settings.gradle.kts`:**
```kotlin
includeBuild("../frnk")

dependencyResolutionManagement {
    repositories { google(); mavenCentral() }
}

rootProject.name = "MyApp"
include(":app")
```

> `includeBuild` belongs at the top level of `settings.gradle.kts`, not inside `pluginManagement` — frnk ships libraries, not Gradle plugins, so the host's normal `implementation(...)` dependency on `dev.jdgarita.frnk:androidApp` is what triggers the composite-build substitution.

**3. Declare the dependency in the consumer's app module:**
```kotlin
dependencies {
    // One dep — re-exports every shared-* api + impl transitively
    implementation("dev.jdgarita.frnk:androidApp")
}
```

**4. Bootstrap in `Application.onCreate`:**
```kotlin
import dev.jdgarita.frnk.shared.BackendChoice
import dev.jdgarita.frnk.shared.ObservabilityChoice
import dev.jdgarita.frnk.shared.initializeFrnk
import dev.jdgarita.frnk.database.impl.DatabaseContext
import org.koin.android.ext.koin.androidContext

DatabaseContext.application = applicationContext

// `observability` is independent of `backend` — a local-only app can pass
// BackendChoice.Supabase (or skip backend calls entirely) and still get Firebase telemetry.
initializeFrnk(backend = BackendChoice.Supabase, observability = ObservabilityChoice.Firebase) {
    androidContext(this@MyApp)
    modules(hostDatabaseModule, hostFeatureModules)   // host-defined; see docs/HOST_INTEGRATION.md
}
```

> The toolkit owns the driver factory, not the schema. The host defines `hostDatabaseModule` against the injected `SqlDriverFactory` — see [`docs/HOST_INTEGRATION.md`](docs/HOST_INTEGRATION.md) for the full pattern.

For iOS, the `FrnkKit.xcframework` produced by `:iosApp:assembleFrnkKitReleaseXCFramework` lands at `iosApp/build/XCFrameworks/release/FrnkKit.xcframework` for SPM consumption. From Swift, call `FrnkKitKt.bootstrapFrnkKit(backend:)`.

> ⚠️ The consumer iOS Xcode project must bring in RevenueCat's native `PurchasesHybridCommon` framework (and the relevant Firebase frameworks if using `BackendChoice.Firebase` or `ObservabilityChoice.Firebase`) via CocoaPods or SPM. The toolkit defers their symbol resolution via `-undefined dynamic_lookup`.

## ⚙️ Setup

The toolkit itself has no required secrets — backend credentials are supplied by the host app at runtime (the Supabase / Firebase clients are configured in your `Application` / `AppDelegate`, not baked into frnk).

`local.properties` is gitignored and only needs `sdk.dir`, which Android Studio writes automatically on first open. From the CLI, copy the template:

```bash
cp local.properties.template local.properties   # then point sdk.dir at your Android SDK
```

Demo apps (the internal smoke harnesses) additionally need:
- **Android:** a valid `google-services.json` in `androidDemoApp/`
- **iOS:** a valid `GoogleService-Info.plist` in `iosDemoApp/`

## 🔧 Common commands

```bash
./gradlew compileAndroidMain                          # fast compile-only check across every shared module (what CI runs)
./gradlew :androidDemoApp:compileDebugKotlin          # compile the demo harness
./gradlew testAndroidHostTest                         # commonTest + androidHostTest across all KMP modules
./gradlew :shared-database-impl:testAndroidHostTest   # run a single module's tests
./gradlew ktlintFormat                                # auto-fix style (also runs from the pre-commit hook)
./gradlew assemble                                    # full build of every target (Android library + iOS frameworks)
./gradlew :iosApp:assembleFrnkKitReleaseXCFramework       # produce FrnkKit.xcframework (consumer-facing)
./gradlew :shared-demo:assembleDemoKitDebugXCFramework    # produce DemoKit.xcframework (iosDemoApp consumes this)
./gradlew clean
```

> Under the AGP 9 KMP-Android plugin (`com.android.kotlin.multiplatform.library`), the per-module compile task is `compileAndroidMain` — `compileDebugKotlinAndroid` is the AGP 8 name and no longer exists for KMP-Android modules. The host unit-test task is likewise `testAndroidHostTest` (not `testDebugUnitTest`), and a module only gets it after opting in with `kotlin { android { withHostTest {} } }`. The demo app is a plain `com.android.application`, so it keeps `compileDebugKotlin` / `testDebugUnitTest`.

Shared constants (package name, min/compile/target SDK, iOS framework name `FrnkKit`, database class `FrnkDB`) live in `buildSrc/src/main/kotlin/ProjectConfiguration.kt` — read from there rather than hardcoding.

## 🎨 Style: pre-commit hook, not CI

Ktlint is enforced via a **git pre-commit hook** at `.githooks/pre-commit`. It runs `./gradlew ktlintFormat` against staged Kotlin files and re-stages the fixes, so commits land already-formatted and CI doesn't burn time on style.

Installation is automatic: the root build registers `installGitHooks`, wired to `prepareKotlinBuildScriptModel`, so it runs on IDE sync. To install on a fresh checkout without opening the IDE: `./gradlew installGitHooks`. To bypass for one commit: `SKIP_KTLINT=1 git commit ...` or `git commit --no-verify`.

## 🏷️ Releases & versioning

Releases are cut as Git tags (`vMAJOR.MINOR.PATCH`) on `main`. There are no published artifacts — downstream apps pin via submodule checkout (see above). See [`docs/RELEASING.md`](docs/RELEASING.md) for the maintainer procedure and [`CHANGELOG.md`](CHANGELOG.md) for the history.

Pre-1.0 versioning policy: `0.x.0` may break API, `0.x.y` is additive/fix-only. Once `1.0.0` ships, standard SemVer applies. The current version is exposed at runtime as `dev.jdgarita.frnk.utils.Frnk.VERSION`.

## 💖 Sponsor

If frnk saves you time, consider [sponsoring the project on GitHub](https://github.com/sponsors/jdgarita). Sponsorships fund ongoing maintenance, releases, and new modules.

## 🧪 CI

`.github/workflows/main.yml` is the authoritative pipeline — a single job that runs:

1. `./gradlew compileAndroidMain :androidDemoApp:compileDebugKotlin --parallel --build-cache` — covers every shared module's `commonMain` + `androidMain` plus the demo harness
2. `./gradlew testAndroidHostTest :androidDemoApp:testDebugUnitTest --parallel --build-cache` — covers every shared module's `commonTest` + `androidHostTest` (KMP host tests run under `testAndroidHostTest`, not `testDebugUnitTest`) plus the demo app's unit tests

`assemble`, `allTests`, and `ktlintCheck` are intentionally out — they duplicate work the local pre-commit hook (style) and downstream consumer builds (release assembly, iOS link) already cover.

Every `*-impl` module ships `commonTest` and platform-specific (`androidUnitTest`, `iosTest`) source sets so concrete implementations are validated before consumers see them.
