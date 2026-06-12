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
