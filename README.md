# Frnk KMP Toolkit 🛠️

A Kotlin Multiplatform + Compose Multiplatform **toolkit** — not a standalone app. Consumed by downstream apps as a Git submodule via a Gradle composite build (`includeBuild("../frnk")`), it provides a strict, modular baseline so feature work doesn't start from zero.

> See [`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md) for the canonical module graph and api/impl rationale.

## 🎯 Objective

Give indie / small-team apps a fast-compiling foundation with a clean architecture, aggressive modularization, and a standardized MVI presentation layer — so the day-to-day work stays on features and business logic.

## 🏗️ Architecture

A single `:shared` module is the consumer-facing surface. Internally it aggregates a flat **api / impl** module split: `*-api` modules hold only interfaces and DTOs; `*-impl` modules hold the concrete bindings (Ktor, SQLDelight, Firebase, Supabase, RevenueCat) wired via Koin. `:shared` bundles every api **and** every impl, so host apps depend on one module and pick which backend to install at runtime via `BackendChoice`.

### Module map

| Module | Purpose |
| --- | --- |
| `shared` | Consumer-facing aggregator. Re-exports every `shared-*` module via `api(...)`, exposes `frnkModules(BackendChoice)` and `initializeFrnk()` for one-shot Koin bootstrap. |
| `shared-utils` | Root utilities (coroutines, datetime). Every other shared module depends on this. |
| `shared-ui-api` | UI-layer interfaces — lifecycle ViewModel + the MVI contracts. |
| `shared-ui-atoms` | Headless Compose components on `compose-unstyled`, **and** the MVI engine: `MviContract`, `MviViewModel<S, A, E>`, `ObserveAsEvents`. New screens subclass `MviViewModel`. |
| `shared-backend-api` | Auth / Analytics / CrashReporter / RemoteData interfaces. Owns `AppResult<D, E : AppError>`. |
| `shared-backend-firebase` | Firebase impl of `shared-backend-api`. Exposes `firebaseBackendModule`. |
| `shared-backend-supabase` | Supabase + Ktor impl of `shared-backend-api`. Exposes `supabaseBackendModule`. |
| `shared-database-api` | Persistence contracts (SqlDriverFactory, KeyValueStore). |
| `shared-database-impl` | SQLDelight + Multiplatform Settings impl. Exposes `databaseModule`. |
| `shared-monetization-api` | Entitlement / feature-gate interfaces. |
| `shared-monetization-revenuecat` | RevenueCat impl. Exposes `revenueCatModule`. |
| `androidApp` | Public entry point as an `com.android.library`. `api(projects.shared)` — one dep, no surprises. |
| `iosApp` | KMP target producing the fat `FrnkKit.xcframework` (consumed via SPM). `export(projects.shared)`. |
| `androidDemoApp` / `iosDemoApp` | Internal smoke harnesses — not the shipping product. |

## 🧰 Tech stack

- **Language:** Kotlin 2.0.21
- **UI:** Compose Multiplatform + `compose-unstyled` (`com.composables:core`)
- **DI:** Koin
- **Navigation:** AndroidX Navigation 3 (`androidx.navigation3`)
- **Persistence:** SQLDelight, Multiplatform Settings / DataStore
- **Backend:** Supabase + Ktor and GitLive Firebase, both impls bundled — host picks via `BackendChoice`
- **Monetization:** RevenueCat
- **Secrets:** BuildKonfig (reads from `local.properties`)
- **Build:** AGP 8.7.0, JDK 17 (auto-provisioned via Foojay resolver)

## 🚀 Consume as a composite build

This toolkit is designed to live alongside the consuming app as a Git submodule.

**1. Add the submodule:**
```bash
git submodule add git@github.com:jdgarita/frnk.git frnk
git submodule update --init --recursive
```

**2. Wire it into the consumer's `settings.gradle.kts`:**
```kotlin
pluginManagement {
    includeBuild("frnk")
    repositories { google(); mavenCentral(); gradlePluginPortal() }
}

dependencyResolutionManagement {
    repositories { google(); mavenCentral() }
}

rootProject.name = "MyApp"
include(":app")
```

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
import dev.jdgarita.frnk.shared.initializeFrnk
import org.koin.android.ext.koin.androidContext

initializeFrnk(backend = BackendChoice.Supabase) {
    androidContext(this@MyApp)
    modules(myAppModule, sqlDelightSchemaModule)
}
```

For iOS, the `FrnkKit.xcframework` produced by `:iosApp:assembleFrnkKitReleaseXCFramework` lands at `iosApp/build/XCFrameworks/release/FrnkKit.xcframework` for SPM consumption. From Swift, call `FrnkKitKt.bootstrapFrnkKit(backend:)`.

> ⚠️ The consumer iOS Xcode project must bring in RevenueCat's native `PurchasesHybridCommon` framework (and any Firebase frameworks if using `BackendChoice.Firebase`) via CocoaPods or SPM. The toolkit defers their symbol resolution via `-undefined dynamic_lookup`.

## ⚙️ Setup

`local.properties` is gitignored and **required** — `BuildKonfig` fails at configuration time without the keys below.

```bash
cp local.properties.template local.properties
# then populate SUPABASE_URL, SUPABASE_ANON_KEY, FIREBASE_*, BUILD_VARIANT
```

Demo apps additionally need:
- **Android:** a valid `google-services.json` in `androidDemoApp/`
- **iOS:** a valid `GoogleService-Info.plist` in `iosDemoApp/`

## 🔧 Common commands

```bash
./gradlew ktlintCheck                       # lint — CI gates on this
./gradlew ktlintFormat                      # auto-fix style
./gradlew assemble                          # build every target
./gradlew allTests                          # run commonTest across all KMP modules
./gradlew :shared-database-impl:allTests    # run a single module's tests
./gradlew :iosApp:assembleXCFramework       # produce FrnkKit.xcframework
./gradlew clean
```

Shared constants (package name, min/compile/target SDK, iOS framework name `FrnkKit`, database class `FrnkDB`) live in `buildSrc/src/main/kotlin/ProjectConfiguration.kt` — read from there rather than hardcoding.

## 🧪 CI & quality

`.github/workflows/main.yml` is the authoritative pipeline. It gates on:

- **`ktlint`** — `./gradlew ktlintCheck` (failures block merges; the plugin runs with `ignoreFailures.set(false)` across all projects)
- **`build & test`** — `./gradlew assemble allTests`

Every `*-impl` module ships `commonTest` and platform-specific (`androidUnitTest`, `iosTest`) source sets so concrete implementations are validated before consumers see them.
