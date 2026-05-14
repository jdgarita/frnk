# Frnk KMP Toolkit 🛠️

A Kotlin Multiplatform + Compose Multiplatform **toolkit** — not a standalone app. Consumed by downstream apps as a Git submodule via a Gradle composite build (`includeBuild("../frnk")`), it provides a strict, modular baseline so feature work doesn't start from zero.

> See [`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md) for the canonical module graph and api/impl rationale.

## 🎯 Objective

Give indie / small-team apps a fast-compiling foundation with a clean architecture, aggressive modularization, and a standardized MVI presentation layer — so the day-to-day work stays on features and business logic.

## 🏗️ Architecture

The repo enforces a flat **api / impl** module split per external-dependency domain. `*-api` modules hold only interfaces and DTOs; `*-impl` modules hold the concrete bindings (Ktor, SQLDelight, etc.) wired via Koin. Swapping an implementation (e.g. Ktor → Firebase KMP SDK) touches only the impl module.

### Module map

| Module | Purpose |
| --- | --- |
| `core-common` | Root module. Owns `AppResult<D, E : AppError>`, `UiText`, and `BuildKonfig`-generated config. Every `*-api` interface returns `AppResult` instead of throwing. |
| `core-network-api` / `core-network-impl` | Network contracts (api) + Ktor bindings (impl). |
| `core-database-api` / `core-database-impl` | Persistence contracts (api) + SQLDelight bindings (impl). SQLDelight generates into `dev.jdgarita.frnk.database.sql`. |
| `core-ui-atoms` | Headless Compose components on `compose-unstyled`, **and** the MVI engine: `MviContract` (UiState / UiAction / UiEffect), `MviViewModel<S, A, E>`, `ObserveAsEvents`. New screens subclass `MviViewModel`. |
| `androidApp` | Public entry point as an `com.android.library`. `api(...)` re-exports every core module. |
| `iosApp` | KMP target producing the fat `FrnkKit.xcframework` (consumed via SPM). Exports `core-common`, `core-network-api`, `core-database-api`, `core-ui-atoms`. |
| `androidDemoApp` / `iosDemoApp` | Internal smoke harnesses for validating the toolkit — not the shipping product. |

## 🧰 Tech stack

- **Language:** Kotlin 2.0.21
- **UI:** Compose Multiplatform + `compose-unstyled` (`com.composables:core`)
- **DI:** Koin
- **Navigation:** AndroidX Navigation 3 (`androidx.navigation3`)
- **Persistence:** SQLDelight, Multiplatform Settings / DataStore
- **Backend:** Supabase, GitLive Firebase (abstracted behind `*-api` interfaces)
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

**3. Declare dependencies in the consumer's app module:**
```kotlin
dependencies {
    // Entry point — re-exports every core module
    implementation("dev.jdgarita.frnk:androidApp")

    // Or pin specific modules
    implementation("dev.jdgarita.frnk:core-ui-atoms")
    implementation("dev.jdgarita.frnk:core-network-api")
}
```

For iOS, the `FrnkKit.xcframework` produced by `:iosApp:assembleXCFramework` lands at `iosApp/build/XCFrameworks/release/FrnkKit.xcframework` for SPM consumption.

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
./gradlew ktlintCheck                  # lint — CI gates on this
./gradlew ktlintFormat                 # auto-fix style
./gradlew assemble                     # build every target
./gradlew allTests                     # run commonTest across all KMP modules
./gradlew :core-network-impl:allTests  # run a single module's tests
./gradlew :iosApp:assembleXCFramework  # produce FrnkKit.xcframework
./gradlew clean
```

Shared constants (package name, min/compile/target SDK, iOS framework name `FrnkKit`, database class `FrnkDB`) live in `buildSrc/src/main/kotlin/ProjectConfiguration.kt` — read from there rather than hardcoding.

## 🧪 CI & quality

`.github/workflows/main.yml` is the authoritative pipeline. It gates on:

- **`ktlint`** — `./gradlew ktlintCheck` (failures block merges; the plugin runs with `ignoreFailures.set(false)` across all projects)
- **`build & test`** — `./gradlew assemble allTests`

Every `*-impl` module ships `commonTest` and platform-specific (`androidUnitTest`, `iosTest`) source sets so concrete implementations are validated before consumers see them.
