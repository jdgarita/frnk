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
