# Bugfixes

<!--
Bugfixes and workarounds worth remembering for this project.
Append entries with: mobiai brain save bugfix (coming in Phase 2).
Mark temporary workarounds as status: temporary so the agent does not
treat them as permanent decisions.
-->

## OnboardingScreen buttons unresponsive when pushed as a nav3 destination

- id: onboardingscreen-buttons-unresponsive-when-pushed-as-a-nav3-20260612-030729
- type: platform_workaround
- status: temporary
- platform: android
- area: navigation
- date: 2026-06-12

### Problem
With `OnboardingScreen` pushed onto a tab's back stack (demo: Settings → Show Onboarding), its **buttons don't respond** — close-X and Next/Back fire no intent — while the `HorizontalPager` swipe and system/predictive back work fine.

### Status
OPEN. Discovered 2026-06-10 during scaffold-system device verification (Pixel 7a, emulator API 36). **Reproduces identically on main @ 87aba0e**, so it predates `FrnkAppShell` — NOT a regression of the shell's `entry(ToolkitRoute.Onboarding)` registration (verified by A/B-installing both branches on the same emulator).

### Suspected root cause (unconfirmed)
Either the onboarding `koinViewModel`'s intent collector vs. the nav-entry `ViewModelStoreOwner`, or button taps never reaching the composables on that destination.

### Workaround
Pager swipe + system back. (This is the toolkit's tracked open bug — recorded here in the brain.)

### Files
- frnk/ui/scaffolds/src/commonMain/kotlin/dev/jdgarita/frnk/ui/scaffolds/OnboardingScreen.kt

## AGP9 KMP-library Compose resources not packaged into Android APK

- id: agp9-kmp-library-compose-resources-not-packaged-into-android-20260612-030729
- type: platform_workaround
- status: temporary
- platform: android
- area: build_compose_resources
- date: 2026-06-12

### Problem
Under AGP 9.2.1 `com.android.kotlin.multiplatform.library` + CMP 1.11.1, the Compose-resources plugin does **not** package `DrawableResource`s declared in a `shared-*` KMP **library** module into the Android APK. `copyAndroidMainComposeResourcesToAndroidAssets` fails (`outputDirectory` unconfigured) and `prepareComposeResourcesTaskForAndroidMain` is `NO-SOURCE`. iOS assembles fine; Android throws `MissingResourceException` at runtime.

### Where it surfaced
The adaptive-nav-bar (narendraanjana09) POC, whose icons are resource-based (`DrawableResource` + SF-Symbol, no ImageVector/slot). A meaningful strike against adopting any library that ships drawables-in-a-KMP-library until the AGP/Compose-resources gap closes. frnk's own atoms avoid this by using `ImageVector`/Lucide, not packaged drawables.

### Workaround
Ship the raw drawable XML in the **app** module's assets: `demo/android-app/src/main/assets/composeResources/<pkg>/drawable/…` (see that dir's README). A real host would have to do the same.

### Status
Temporary external-tooling limitation; re-check on AGP/CMP upgrades.

### Files
- demo/android-app/src/main/assets/composeResources/README.md

## HorizontalFloatingToolbar: no-FAB overload defaults to 0dp elevation (no shadow) — pin to WithFab elevation

- id: horizontalfloatingtoolbar-no-fab-overload-defaults-to-0dp-el-20260612-173729
- type: bug_fix
- status: active
- platform: android
- area: ui-bottom-nav
- date: 2026-06-12

Symptom: the Android FrnkBottomNavBar floating pill cast a drop shadow on Home (where a primary-action FAB is wired) but NOT on Components/Settings (no FAB).

Root cause: Material3's HorizontalFloatingToolbar has two overloads with DIFFERENT default shadow elevations:
- WithFab overload (floatingActionButton slot): expandedShadowElevation defaults to FloatingToolbarDefaults.ContainerExpandedElevationWithFab = ElevationTokens.Level1 = 1.dp -> visible shadow.
- plain overload (no FAB): expandedShadowElevation defaults to FloatingToolbarDefaults.ContainerExpandedElevation = ElevationTokens.Level0 = 0.dp -> NO shadow.
Since FrnkBottomNavBar.android.kt picks the overload based on whether primaryAction+onPrimaryAction are wired, screens without a primary action got the 0dp plain pill.

Fix: pass expandedShadowElevation = FloatingToolbarDefaults.ContainerExpandedElevationWithFab explicitly to the no-FAB HorizontalFloatingToolbar call, so the pill casts the same Level1 shadow on every screen. (expanded=true always, so collapsedShadowElevation is irrelevant.)

Verified on-device (android run + android screen capture): Settings pill now shows the same drop shadow as Home.

### Files
- frnk/ui/bottom-nav/src/androidMain/kotlin/dev/jdgarita/frnk/ui/bottomnav/FrnkBottomNavBar.android.kt

## God-mode toggle didn't flip: mapRows skipped developerSection (masked by old VM re-keying)

- id: god-mode-toggle-didn-t-flip-maprows-skipped-developersection-20260616-204438
- type: bug_fix
- status: active
- platform: kmp
- area: state_management
- date: 2026-06-16

### Symptom
After switching Settings from VM re-keying to the reactive `ConfigChanged` merge, the demo's god-mode toggle (Settings -> Developer section) no longer flipped on when tapped — it stayed off.

### Root cause
`SettingsViewModel.mapRows` (which backs `withToggle`/`withTheme`) only reduced `sections`, never `developerSection`. The god-mode toggle lives in `developerSection`, so `ToggleChanged` never updated it in VM state. The old re-key approach masked this latent bug because every flip re-seeded a fresh VM from the recomputed catalogue (`demoSettingsState(isGodMode=...)`). With the reactive merge, `mergedWith` then preserved the VM's stale `false` over the incoming `true`, so the toggle could never turn on.

### Fix
`mapRows` now also maps `developerSection.rows` alongside the visible `sections`. Tapping the dev-section toggle updates VM state optimistically; the round-trip `ConfigChanged` keeps it. Regression test: `SettingsViewModelTest.toggling_a_developer_section_row_updates_state_and_survives_config_changed`.

Surfaced as a regression while implementing [[reactive-settings-home-vm-config-sync]].

### Files
- frnk/ui/scaffolds/src/commonMain/kotlin/dev/jdgarita/frnk/ui/scaffolds/SettingsViewModel.kt
- frnk/ui/scaffolds/src/androidHostTest/kotlin/dev/jdgarita/frnk/ui/scaffolds/SettingsViewModelTest.kt

## Dependency-bump traps: navigation3 split ref + compose-unstyled compileSdk 37

- id: dependency-bump-traps-navigation3-split-ref-compose-unstyled-20260831-173230
- type: bug_fix
- status: active
- platform: kmp
- area: build
- date: 2026-08-31

Two traps hit during the 2026-08 "latest versions" bump (Kotlin 2.4.10 / AGP 9.3.2 / CMP 1.12.0).

## 1. navigation3 — one ref fed two INDEPENDENT coordinate groups
`navigation3UI` was shared by:
- `androidx.navigation3:navigation3-runtime` (AndroidX) — publishes 1.1.2…1.1.7
- `org.jetbrains.androidx.navigation3:navigation3-ui` (JetBrains CMP port) — stops at **1.1.1**, then jumps to 1.2.0-alpha02

Setting it to 1.1.7 failed with `Could not find org.jetbrains.androidx.navigation3:navigation3-ui:1.1.7`.
**Fix:** split into two refs — `navigation3UI = "1.1.1"` and `navigation3Runtime = "1.1.7"`. Bump each against its own maven-metadata; they do NOT release in lockstep. Verified runtime 1.1.7 + CMP UI 1.1.1 works together at runtime (root nav + nested tab nav both exercised on device).

## 2. compose-unstyled 2.9.2 requires compileSdk 37
2.9.0 ships `minCompileSdk=1`; **2.9.2 ships `minCompileSdk=37`**. With compileSdk 36 this fails `checkAndroidHostTestAarMetadata` on :ui-components/:ui-scaffolds with 44 issues.

⚠️ **This does NOT surface in `compileAndroidMain`** — only in the test/assemble tasks that run `checkAarMetadata`. A compile-only pre-push check will ship it green.

**Fix:** `android-compileSdk = "37"`. AGP 9.3.2 supports it (sdklib 32.3.2 `HIGHEST_SUPPORTED_API = 37`); the old catalog comment "AGP 9 caps compileSdk at 36" was true of 9.2.1 only. Note the catalog is the SDK source of truth for composite-build host apps, so every host inherits 37.

## Verifying a compose-unstyled bump before taking it
    curl -s -o p.aar https://repo.maven.apache.org/maven2/com/composables/composeunstyled-primitives-android/<v>/composeunstyled-primitives-android-<v>.aar
    unzip -p p.aar META-INF/com/android/build/gradle/aar-metadata.properties | grep minCompileSdk

### Files
- gradle/libs.versions.toml

## Demo Home dropped every DemoHomeEffect (dead paywall button + no toasts)

- id: demo-home-dropped-every-demohomeeffect-dead-paywall-button-n-20260831-174949
- type: bug_fix
- status: active
- platform: kmp
- area: demo
- date: 2026-08-31

## Symptom
Demo Home: "Open Paywall" did nothing, and no toast ever appeared (Track event, Restore, Log breadcrumb, …). The crown in the top bar still opened the paywall, which masked the bug.

## Root cause
**Two ViewModels meet on the Home tab, each with its own single-consumer effect channel — only one was collected.**
- `FrnkHomeScreen` (toolkit scaffold) owns a pass-through `HomeViewModel`; its `HomeEffect`s went to `HomeScreen(onEffect)` ✅
- `DemoHomeViewModel` (the demo logic) was obtained via `koinViewModel()` with only `state` collected — **nothing ever collected `viewModel.effects`**, so every `DemoHomeEffect.Navigate` / `.Toast` was silently buffered and dropped ❌

Effects are a `Channel(capacity = Channel.BUFFERED)` (64), so they buffer rather than jam the intent loop — the failure is silent, with no crash and no log.

Almost certainly lost when `FrnkDemoApp` became the unified shared entry point: `demo-android`s `ContextExt.toast` KDoc still points at `MainActivity.handleEffect`, which no longer exists, and the helper now has **zero usages**.

## Fix
Bind `DemoHomeViewModel` through the toolkit primitive **`FrnkScreen(viewModel, arguments, onEffect) { state -> … }`** instead of hand-rolling `koinViewModel()` + `collectAsStateWithLifecycle()`. It attaches the VM, collects state lifecycle-aware, and consumes the effect channel. Route `Navigate` → `onOpenPaywall()` (new param, wired to `nav.openPaywall()` in `FrnkDemoApp`), and `Toast` → a shared overlay.

`FeatureGate.requestUpgrade()` returns a route **key** (`FeatureGate.PAYWALL_ROUTE_KEY = "toolkit/paywall"`), not a route — `:monetization-api` stays Compose/nav-free, so the host maps the key onto its own graph.

## Transient messages are a shared Compose overlay, not a platform toast
`DemoMessageOverlay` is built from toolkit atoms in **commonMain**, because `FrnkDemoApp` is the one composable both `demo-android` and `iosDemoApp` mount — so both platforms get the same feedback with no `expect`/`actual`, and DemoKit stays cinterop-free. It carries a monotonic `id` so the *same* text twice still re-triggers, and retains the last text so it does not blank mid-fade.

## Gotcha for next time
`attach()` is idempotent and `DemoHomeViewModel` does not override `onAttached`, so moving to `FrnkScreen` is behaviour-safe. The intent collector starts in `MviViewModel.init`, **not** in `attach` — so intents always worked; only effects were lost.

### Files
- demo/shared/src/commonMain/kotlin/dev/jdgarita/frnk/demo/ui/home/DemoHomeScreen.kt
- demo/shared/src/commonMain/kotlin/dev/jdgarita/frnk/demo/ui/home/DemoMessageOverlay.kt
- demo/shared/src/commonMain/kotlin/dev/jdgarita/frnk/demo/FrnkDemoApp.kt
