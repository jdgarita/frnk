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

## Demo Home dropped every DemoHomeEffect (dead paywall button + no toasts)

- id: demo-home-dropped-every-demohomeeffect-dead-paywall-button-n-20260831-180516
- type: bug_fix
- status: active
- platform: kmp
- area: demo
- date: 2026-08-31

## Symptom
Demo Home: "Open Paywall" did nothing and no toast ever appeared. The top-bar crown still opened the paywall, which masked the bug.

## Root cause
**Two ViewModels meet on the Home tab, each with its own single-consumer effect channel — only one was collected.**
- `FrnkHomeScreen` (toolkit scaffold) owns a pass-through `HomeViewModel`; its `HomeEffect`s reached `HomeScreen(onEffect)` ✅
- `DemoHomeViewModel` was resolved with `koinViewModel()` and only `state` collected — **nothing ever collected `viewModel.effects`**, so every `DemoHomeEffect.Navigate`/`.Toast` was buffered and dropped ❌

Effects use `Channel(capacity = Channel.BUFFERED)` (64), so they buffer rather than jam the intent loop — the failure is silent: no crash, no log. (The intent collector starts in `MviViewModel.init`, not `attach`, so intents always worked; only effects were lost.)

Likely lost when `FrnkDemoApp` became the unified shared entry point: `demo-android`s `ContextExt.toast` KDoc still points at a `MainActivity.handleEffect` that no longer exists, and the helper now has zero usages.

## Fix
Bind the VM through **`FrnkScreen(viewModel, arguments, onEffect) { state -> … }`** instead of hand-rolling `koinViewModel()` + `collectAsStateWithLifecycle()`. It attaches the VM, collects state lifecycle-aware, and consumes the effect channel. `attach()` is idempotent and `DemoHomeViewModel` does not override `onAttached`, so the switch is behaviour-safe.

`FeatureGate.requestUpgrade()` returns a route **key** (`PAYWALL_ROUTE_KEY = "toolkit/paywall"`), not a route, so `:monetization-api` stays Compose/nav-free — the host maps the key onto its own graph (`onOpenPaywall` -> `nav.openPaywall()`).

## Transient messages: shared overlay, not a platform toast
`DemoMessageOverlay` is built from toolkit atoms in **commonMain**, because `FrnkDemoApp` is the one composable both `demo-android` and `iosDemoApp` mount — so both platforms get the same feedback with no `expect`/`actual`, and DemoKit stays cinterop-free. Carries a monotonic `id` so the same text twice re-triggers, and retains the last text so it does not blank mid-fade.

## Still broken (verified pre-existing, NOT this bug)
"God mode" and "Add note" do nothing — confirmed by reverting the fix, rebuilding and re-testing. Related: "Restore" reports "Purchases restored" (provider `_isPro` true) while the header reads `Pro = false via None`. Points at the entitlement/notes layer.

### Files
- demo/shared/src/commonMain/kotlin/dev/jdgarita/frnk/demo/ui/home/DemoHomeScreen.kt
- demo/shared/src/commonMain/kotlin/dev/jdgarita/frnk/demo/ui/home/DemoMessageOverlay.kt

## Xcode demo build on Kotlin 2.4 + Xcode 27: KGP SwiftPM synthetic-linkage check, stale purchases-ios pin, stale Swift module cache (2026-09-22)

- id: xcode-demo-build-on-kotlin-2-4-xcode-27-kgp-swiftpm-syntheti-20260922-171756
- type: bug_fix
- status: active
- platform: ios
- area: demo iOS / Xcode build
- date: 2026-09-22

Three separate failures hit `xcodebuild build -project demo/ios-app/iosDemoApp.xcodeproj -scheme iosDemoApp -destination 'generic/platform=iOS Simulator' CODE_SIGNING_ALLOWED=NO` the first time it ran on Kotlin 2.4.10 + Xcode 27.0 (Swift 6.4):

1. `purchases-ios` 5.75.0 (the SPM minimum the pbxproj pinned) does not compile on Xcode 27 (`PaywallColor.swift: invalid redeclaration of synthesized memberwise init`). Upstream fixed it in 5.78.0 ("Xcode 27 Beta Compilation Fix"). FIX: minimum raised to **5.87.1** = the purchases-ios release `purchases-kmp` 3.7.0 wraps (rule: keep the SPM minimum at the version the pinned purchases-kmp names in its release notes). Package.resolved is gitignored; delete it to re-resolve.

2. Kotlin 2.4's SwiftPM import machinery: `posthog-kmp` 0.5.1 is published with KGP `swiftPMDependencies` metadata, so `:demo-shared` has a *transitive* SwiftPM dependency. When Gradle runs **inside an Xcode build phase** (KGP detects Xcode via env vars ACTION / SDK_NAME / CONFIGURATION / ARCHS / TARGET_BUILD_DIR / BUILT_PRODUCTS_DIR / FRAMEWORKS_FOLDER_PATH / PROJECT_FILE_PATH …), KGP wires `:demo-shared:checkSyntheticImportProjectIsCorrectlyIntegrated` ("You have SwiftPM dependencies with embedAndSign integration… run :demo-shared:integrateLinkagePackage") and `generateSyntheticLinkageSwiftPMImportProjectForEmbedAndSignLinkage` (writes `demo/ios-app/KotlinMultiplatformLinkedPackage/`, then fails once with "Synthetic project regenerated — Resolve Package Versions"). That flow assumes KGP's embedAndSign model; frnk's model is the opposite (plain XCFramework under `-undefined dynamic_lookup`, the host app links posthog-ios/sentry-cocoa/purchases-ios itself via SPM). FIX chosen: the "Run Script: Build DemoKit.xcframework" phase now calls `env -u ACTION -u ARCHS -u BUILT_PRODUCTS_DIR -u CONFIGURATION -u DWARF_DSYM_FOLDER_PATH -u ENABLE_USER_SCRIPT_SANDBOXING -u EXPANDED_CODE_SIGN_IDENTITY -u FRAMEWORKS_FOLDER_PATH -u KOTLIN_FRAMEWORK_BUILD_TYPE -u PROJECT_FILE_PATH -u SDK_NAME -u TARGET_BUILD_DIR ./gradlew "$TASK"` so Gradle behaves exactly as from a terminal (verified: no synthetic tasks run, nothing generated). REJECTED alternatives: `kotlin.suppressSwiftPMXcodeIntegrationCheck=true` in gradle.properties (skips only the check; the generator still runs and fails once per fresh clone / posthog-kmp bump); `integrateLinkagePackage` (rewires the project to KGP's embedAndSign, contrary to the documented host model). Hosts that hit the same thing (any Xcode script phase calling Gradle) apply the same env scrub. A terminal `./gradlew :demo-shared:assembleDemoKitDebugXCFramework` never triggers any of this.

3. After the XCFramework's ObjC header changes (e.g. a top-level iosMain function added/removed/re-signatured), Swift in the SAME DerivedData can keep compiling against the stale clang module ("cannot find 'DemoSdksKt' in scope" / "missing arguments for parameters … in call" although DemoKit.h is fresh). FIX: build into a fresh `-derivedDataPath` (or Product → Clean Build Folder in Xcode) — nothing to change in the repo.

ALSO (same day): iOS demo keys moved from Swift constants to Faint's xcconfig approach — `demo/ios-app/Configuration/Config.xcconfig` (tracked, `baseConfigurationReference` of both target configs) `#include? "Secrets.xcconfig"` (gitignored; `.template` tracked), Info.plist forwards `$(SENTRY_DSN)` / `$(POSTHOG_API_KEY)` / `$(POSTHOG_HOST)` / `$(REVENUECAT_API_KEY)`, and `bootstrapDemoKoinWithSdks()` (no args) reads them via `NSBundle.mainBundle.objectForInfoDictionaryKey` in `demo/shared` iosMain (an unexpanded `$(KEY)` counts as blank → the toolkit's config fails naming the key). xcconfig gotcha: `//` is a comment, so URLs are written `https:/$()/…`. Firebase is gone from the iOS demo entirely (package, `FirebaseApp.configure()`, plist, Crashlytics dSYM phase).

### Files
- demo/ios-app/iosDemoApp.xcodeproj/project.pbxproj
- demo/ios-app/Configuration/Config.xcconfig
- demo/ios-app/Configuration/Secrets.xcconfig.template
- demo/shared/src/iosMain/kotlin/dev/jdgarita/frnk/demo/DemoSdks.kt
