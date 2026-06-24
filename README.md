<p align="center">
  <img width="224" height="353" alt="frnk-2-clean (1)" src="https://github.com/user-attachments/assets/dc37abec-66be-4754-ad36-a92093c91e0b" />
</p>


# frnk

A Kotlin Multiplatform + Compose Multiplatform **toolkit** — not a standalone app. Consumed by downstream apps as a Git submodule via a Gradle composite build (`includeBuild("../frnk")`), it provides a strict, modular baseline so feature work doesn't start from zero.

> See [`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md) for the canonical module graph and api/impl rationale. Product scope lives in [`REQUIREMENTS.md`](REQUIREMENTS.md); decision rationale and the open-work roadmap live in the MobiAI brain (`mobiai brain search "open work"`).

## 🎯 Objective

Give indie / small-team apps a fast-compiling foundation with a clean architecture, aggressive modularization, and a standardized MVI presentation layer — so the day-to-day work stays on features and business logic.

## 🏗️ Architecture

Hosts depend on the **individual modules** they use (there is no aggregator), organized as an **api / impl** split: `*-api` modules hold only interfaces and DTOs; impl modules hold the concrete bindings (SQLDelight, Firebase, RevenueCat) wired via Koin. All modules use flat Gradle paths (the last nested `:shared:backend:*` pair became `:analytics-api`/`:analytics-impl` at restructure Stage 5). What runs is decided by the **explicit Koin module list** the host passes to `initializeFrnk(...)` — un-passed capability modules never enter the graph, and the axes stay independent (a local-only app with no backend can still install Firebase telemetry).

### Module map

| Module | Purpose |
| --- | --- |
| `core-di` | Host bootstrap: `initializeFrnk(modules)` (+ the Android `initializeFrnk(context, modules)` overload that wires `androidContext` + `DatabaseContext.application`) and the fail-fast `requireFrnkKoin()`. |
| `ui-app` | The app root: `FrnkApp(onSavedStateConfiguration, onNavigationModule)` (owns `FrnkTheme` + appearance + a root `NavDisplay` seeded at `FrnkRootRoute.Onboarding`, handing the nav graph to the host — no batteries auto-wired) + `frnkUiModules()` (the SDK-free scaffold VM modules). No `*-impl` compile deps. |
| `shared-utils` | Root utilities — coroutines, datetime, `Logger`, `PlatformInfo` (the module's only `expect/actual`: OS + device), `FeedbackEmail` (`mailto:` draft builder), and `Frnk.VERSION`. Every other shared module depends on this. |
| `core-mvi` | The **MVI engine**, no Compose deps: `MviContract` (`UiState` / `UiIntent` / `UiEffect`), `MviViewModel<S, I, E>` (StateFlow + intent flow + effect channel; `setState`/`onIntent`/`emit`), plus `UiText`. Feature ViewModels subclass `MviViewModel` here without pulling in Compose. |
| `core-nav` | The Compose-free **type-safe Navigation3 contract**: `FrnkRoute`, the `NavBackStack<NavKey>` mutation helpers, the saved-state config builders (`frnkRootNavConfig` + `frnkNestedNavConfig(...)`), the `FrnkPendingRouteRequest` deep-link signal, and the `FrnkFullScreenRoute` marker. No `compose.runtime`. |
| `haptics` | The Compose-free `HapticFeedback`/`HapticType` contract + the `multihaptic`-backed engine binding and `LocalFrnkHaptics`. A UI-feedback concern (like the ripple), not an api/impl split. |
| `ui-theme` | The **design-system foundation** on headless `compose-unstyled` (no Material3): tokens (`FrnkColors` / `FrnkTypography` / `FrnkSpacing` / `FrnkShapes` / `FrnkIconSize`) and the `FrnkTheme` engine (`FrnkThemeConfig` / `FrnkStrings` / `FrnkIcons` / `FrnkRipple`). `FrnkTheme` installs the automatic **press ripple** (`rememberFrnkRipple()` as `LocalIndication`) and **haptics** (`LocalFrnkHaptics`). |
| `ui-components` | The **component tier** on `compose-unstyled`: `Frnk*` atoms (`FrnkText`, `FrnkButton`, `FrnkIcon`, `FrnkIconButton`, `FrnkDivider`, `FrnkSwitch`, `FrnkSegmentedControl`, `FrnkTopAppBar`), molecules (`FrnkListRow`, `FrnkLabeledValue`, `FrnkEmptyState`, `FrnkSwipeable` swipe-to-action), organisms (`FrnkListSection`, `FrnkProfileHeader`), and the built-in **loading skeleton** (`FrnkSkeleton` + `Modifier.frnkSkeleton`). |
| `ui-scaffolds` | The **page templates** + the Compose binding layer for the MVI/nav engines: scaffolds (`OnboardingScreen`, `SettingsScreen`, `HomeScreen`, `FrnkScreenScaffold`), `FrnkMviScreen` + `EffectCollector`, and `FrnkNavDisplay` + `rememberFrnkNavBackStack` + the slide animations. |
| `ui-bottom-nav` | **Platform-adaptive bottom navigation** — `FrnkBottomFloatingBar`, an `expect`/`actual` composable: a Material3 *Expressive* `HorizontalFloatingToolbar` (floating pill) on Android and a native glassy `UITabBar` (iOS 26+) / Material3 bar (older) on iOS (via [adaptive-nav-bar](https://github.com/narendraanjana09/adaptive-navigation-bar)), both themed from `FrnkTheme` tokens. It also owns `FrnkNestedNavScaffold(tabs, onSavedStateConfiguration, onNestedNavigationModule)` (replaced `FrnkTabbedNavScaffold`) — a flexible multiple-back-stack tabbed scaffold with no fixed shape and no built-in Home/Settings: the host supplies its own `tabs` + nested destinations, and the bar's view state (`items` + `selectedIndex`) lives in the MVI `FrnkNestedNavViewModel` (`frnkNestedNavModule`). A single shared back stack drives every tab for now (per-tab stacks are a planned follow-up). Icons are `FrnkIconSource` (Android) + SF-Symbol string (iOS). **The toolkit's sole Material3 dependency**, deliberately isolated here so `ui-theme`/`ui-components`/`ui-scaffolds` stay `compose-unstyled`-only. Android never touches `DrawableResource`, so there's **no host-side asset step**. |
| `analytics-api` | Analytics / CrashReporter interfaces, the no-op observability defaults (`Noop{Analytics,Crash}`), and `noopObservabilityModule`. |
| `analytics-impl` | Firebase impl of `analytics-api`. Exposes `firebaseObservabilityModule` (analytics + crash). |
| `remote-config-api` | `RemoteConfigService` — read-only typed key→value + `fetchAndActivate`. A capability sibling of `analytics-*` (Stage 11), with `noopRemoteConfigModule` reading bundled defaults only. |
| `remote-config-impl` | Firebase Remote Config impl. Exposes `remoteConfigModule`. |
| `camera` / `permissions` | api-only **scaffolds** (Stage 11) — interface + no-op default (`NoopCameraController` / `NoopPermissionController`) + Koin module (`cameraModule` / `permissionsModule`); no impl yet, no native cinterop. |
| `data-db-api` | The SQL persistence SPI: `SqlDriverFactory` (the toolkit owns no schema — hosts bring their own SQLDelight database; the demo's `DemoDB` is the worked example). |
| `data-db-impl` | Platform SQLDelight drivers (Android/Native). Exposes `databaseModule`. |
| `data-prefs-api` | Key-value contracts: `KeyValueStore` + the typed `Preference<T>` accessors. |
| `data-prefs-impl` | Multiplatform Settings impl — `SettingsKeyValueStore`. Exposes `prefsModule`. |
| `monetization-api` | Entitlement / feature-gate interfaces. |
| `monetization-impl` | RevenueCat impl. Exposes `revenueCatModule`. |
| `shared-monetization-ui` | frnk-owned monetization **UI** (no RevenueCat dep): the `PaywallScreen`/`PaywallViewModel` MVI paywall wired via `frnkPaywallDestination(...)` + `paywallScaffoldModule`, plus the host-facing `rememberFrnkSettingsHandler()` (backed by an internal `platformManageSubscriptionsUrl()` `expect/actual` supplying the native subscription-management URL). |
| `demo-shared` | Demo-only KMP module — bundles `FrnkDemoApp` / `DemoViewModel` / `demoModule` + fakes for the smoke harnesses. Depends only on `*-api` modules + `ui-theme`/`ui-components`/`ui-scaffolds`/`ui-app`, so `DemoKit.xcframework` is free of native cinterops (no Pods required to run `iosDemoApp`). |
| `demo-android` / `iosDemoApp` | Internal smoke harnesses — not the shipping product. |

## 🧰 Tech stack

- **Language:** Kotlin 2.4.0
- **UI:** Compose Multiplatform 1.11.1 + `compose-unstyled` 2.5.0 (the granular `com.composables:composeunstyled-*` artifacts — **not** `com.composables:core`, **not** Material3) + Lucide icons (`icons-lucide-cmp` 2.2.1). The lone exception to the no-Material3 rule is `ui-bottom-nav`'s adaptive bottom bar (adaptive-nav-bar 1.0.1, `io.github.narendraanjana09:adaptive-nav-bar`), which pulls Material3 for a native glassy `UITabBar` on iOS 26+ — isolated to that one module.
- **DI:** Koin 4.2.1
- **Navigation:** AndroidX Navigation3 1.1.1 — `navigation3-runtime` (`androidx.navigation3`, NavKey/NavBackStack) + the JetBrains CMP `navigation3-ui` port (`org.jetbrains.androidx.navigation3`), with the `lifecycle-viewmodel-navigation3` 2.10.0 decorator
- **Persistence:** SQLDelight 2.3.2, Multiplatform Settings 1.3.0
- **Remote Config:** GitLive Firebase Remote Config 2.4.0 (`dev.gitlive:firebase-config`) — opt in by installing `remoteConfigModule`, its own capability pair (`:remote-config-api`/`:remote-config-impl`)
- **Observability:** GitLive Firebase Analytics + Crashlytics 2.4.0 — opt in by installing `firebaseObservabilityModule`, independent of every other capability
- **Monetization:** RevenueCat 3.0.5
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

> `includeBuild` belongs at the top level of `settings.gradle.kts`, not inside `pluginManagement` — frnk ships libraries, not Gradle plugins, so the host's normal `implementation(...)` dependencies on `dev.jdgarita.frnk:<module>` coordinates are what trigger the composite-build substitution.

**3. Declare the modules you use in the consumer's app module:**
```kotlin
dependencies {
    implementation("dev.jdgarita.frnk:ui-app")                          // FrnkApp + frnkUiModules() (pulls ui/nav/theme + core-di)
    implementation("dev.jdgarita.frnk:data-db-impl")                    // databaseModule (SqlDriverFactory)
    implementation("dev.jdgarita.frnk:data-prefs-impl")                 // prefsModule (KeyValueStore)
    implementation("dev.jdgarita.frnk:monetization-impl")  // revenueCatModule (optional)
    // + any other impl modules you install (e.g. dev.jdgarita.frnk:analytics-impl for firebaseObservabilityModule)
}
```

**4. Bootstrap in `Application.onCreate`:**
```kotlin
import dev.jdgarita.frnk.di.initializeFrnk
import dev.jdgarita.frnk.ui.app.frnkUiModules

// Explicit module list — what you don't pass is never in the graph. The Android overload
// also wires androidContext(...) and DatabaseContext.application for you.
initializeFrnk(
    context = this,
    modules = frnkUiModules() +                  // scaffold VMs (Home/Settings/Onboarding/BottomNav)
        listOf(
            databaseModule, prefsModule,         // SQLDelight driver factory / KeyValueStore
            firebaseObservabilityModule,         // or noopObservabilityModule for no telemetry
            revenueCatModule, monetizationModule, paywallScaffoldModule, // optional monetization stack
        ) + listOf(hostDatabaseModule) + hostFeatureModules, // host-defined; see docs/HOST_INTEGRATION.md
)
```

> The toolkit owns the driver factory, not the schema. The host defines `hostDatabaseModule` against the injected `SqlDriverFactory` — see [`docs/HOST_INTEGRATION.md`](docs/HOST_INTEGRATION.md) for the full pattern.

For iOS, there is no prebuilt toolkit framework: the host adds a small KMP shared module that `api()`-depends on the frnk modules it uses and bundles them into its own umbrella `XCFramework` — the demo's `DemoKit` (`demo/shared/build.gradle.kts`) is the worked example, and [`docs/HOST_INTEGRATION.md`](docs/HOST_INTEGRATION.md) §6 has the recipe.

> ⚠️ The consumer iOS Xcode project must bring in RevenueCat's native SDK (and the relevant Firebase frameworks if installing `firebaseObservabilityModule`) via CocoaPods or SPM. The umbrella framework defers their symbol resolution via `-undefined dynamic_lookup`.

## ⚙️ Setup

The toolkit itself has no required secrets — backend credentials are supplied by the host app at runtime (the Firebase / RevenueCat clients are configured in your `Application` / `AppDelegate`, not baked into frnk).

`local.properties` is gitignored and only needs `sdk.dir`, which Android Studio writes automatically on first open. From the CLI, copy the template:

```bash
cp local.properties.template local.properties   # then point sdk.dir at your Android SDK
```

Demo apps (the internal smoke harnesses) additionally need:
- **Android:** a valid `google-services.json` in `demo/android-app/`
- **iOS:** a valid `GoogleService-Info.plist` in `demo/ios-app/iosDemoApp/`

## 🔧 Common commands

```bash
./gradlew compileAndroidMain                          # fast compile-only check across every shared module (the standard pre-push gate)
./gradlew :demo-android:compileDebugKotlin            # compile the demo harness
./gradlew testAndroidHostTest                         # commonTest + androidHostTest across all KMP modules
./gradlew :data-prefs-api:testAndroidHostTest         # run a single module's tests
./gradlew ktlintFormat                                # auto-fix style (also runs from the pre-commit hook)
./gradlew assemble                                    # full build of every target
./gradlew :demo-shared:assembleDemoKitDebugXCFramework    # produce DemoKit.xcframework (iosDemoApp consumes this)
./gradlew clean
```

> Under the AGP 9 KMP-Android plugin (`com.android.kotlin.multiplatform.library`), the per-module compile task is `compileAndroidMain` — `compileDebugKotlinAndroid` is the AGP 8 name and no longer exists for KMP-Android modules. The host unit-test task is likewise `testAndroidHostTest` (not `testDebugUnitTest`), and a module only gets it after opting in with `kotlin { android { withHostTest {} } }`. The demo app is a plain `com.android.application`, so it keeps `compileDebugKotlin` / `testDebugUnitTest`.

Shared constants live in `gradle/libs.versions.toml` (there is no `buildSrc`): the `dev.jdgarita.frnk` group id is the `frnk-groupId` catalog entry (read via `libs.versions.frnk.groupId.get()` in build scripts, `findVersion("frnk-groupId")` in the convention plugin), and min/compile/target SDK live there too — read from the catalog rather than hardcoding. (The toolkit owns no SQLDelight schema since restructure Stage 4 — the demo's `DemoDB` is configured inline in `demo/shared/build.gradle.kts`.)

## 🎨 Style: pre-commit hook, not CI

Ktlint is enforced via a **git pre-commit hook** at `.githooks/pre-commit`. It runs `./gradlew ktlintFormat` against staged Kotlin files and re-stages the fixes, so commits land already-formatted and CI doesn't burn time on style.

Installation is automatic: the root build registers `installGitHooks`, wired to `prepareKotlinBuildScriptModel`, so it runs on IDE sync. To install on a fresh checkout without opening the IDE: `./gradlew installGitHooks`. To bypass for one commit: `SKIP_KTLINT=1 git commit ...` or `git commit --no-verify`.

## 🏷️ Releases & versioning

Releases are cut as Git tags (`vMAJOR.MINOR.PATCH`) on `main`. There are no published artifacts — downstream apps pin via submodule checkout (see above). See [`docs/RELEASING.md`](docs/RELEASING.md) for the maintainer procedure and [`CHANGELOG.md`](CHANGELOG.md) for the history.

Pre-1.0 versioning policy: `0.x.0` may break API, `0.x.y` is additive/fix-only. Once `1.0.0` ships, standard SemVer applies. The current version is exposed at runtime as `dev.jdgarita.frnk.utils.Frnk.VERSION`.

## 💖 Sponsor

If frnk saves you time, consider [sponsoring the project on GitHub](https://github.com/sponsors/jdgarita). Sponsorships fund ongoing maintenance, releases, and new modules.

## 🧪 CI

**Build/test CI is paused while the repo is private** — free-tier GitHub Actions minutes are limited, and the per-push pipeline was exhausting them during foundation work. The only workflow that runs today is **`release.yml`** (fires on a `vX.Y.Z` tag push to publish the GitHub Release — see [`docs/RELEASING.md`](docs/RELEASING.md)); an on-demand `@claude` assistant (`claude.yml`) is also kept. The build/test job returns — along with branch protection and PRs — once the foundation lands and the repo goes public.

**Until then, validate locally before pushing** (this is what the old CI job ran):

1. `./gradlew compileAndroidMain :demo-android:compileDebugKotlin --parallel --build-cache` — covers every shared module's `commonMain` + `androidMain` plus the demo harness
2. `./gradlew testAndroidHostTest :demo-android:testDebugUnitTest --parallel --build-cache` — covers every shared module's `commonTest` + `androidHostTest` (KMP host tests run under `testAndroidHostTest`, not `testDebugUnitTest`) plus the demo app's unit tests

`assemble`, `allTests`, and `ktlintCheck` are intentionally out — the local pre-commit hook (style) and downstream consumer builds (release assembly, iOS link) already cover them.

Every `*-impl` module ships `commonTest` and platform-specific (`androidUnitTest`, `iosTest`) source sets so concrete implementations are validated before consumers see them. The **design system** is tested too: `ui-components` carries Compose UI tests for its highest-value atoms (`FrnkSwitch`, `FrnkSegmentedControl`, `FrnkTopAppBar` search mode) that drive a real composition with `runComposeUiTest` and assert the semantics tree. They run as JVM host tests under **Robolectric** (`GraphicsMode.LEGACY`, no device needed) from an `androidHostTest` source set, so they gate in the same `testAndroidHostTest` step — see `frnk/ui/components/CLAUDE.md`.
