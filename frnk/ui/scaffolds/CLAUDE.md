# ui-scaffolds

The toolkit's **page templates** + the **Compose binding layer** for the MVI and Navigation3 engines.
Extracted from `:shared-ui-atoms` at restructure **Stage 7b** — the Compose-bearing layer above
`:ui-components` / below the features and `:ui-bottom-nav`
(`haptics ← ui-theme ← ui-components ← ui-scaffolds ← ui-bottom-nav ← ui-app`). Kotlin packages
unchanged (`dev.jdgarita.frnk.ui.{scaffolds,mvi,nav}`). The `:shared-ui-atoms` facade that used to
re-export it was deleted at Stage 9; consumers depend on `:ui-scaffolds` directly.

## Contents

- `ui/scaffolds/` — higher-than-atom, lower-than-feature page templates with a fixed shape + a configurable state class. Each scaffold ships **two** entry points: a stateless `*Content` composable (previews + advanced hosts), and a VM-backed convenience wrapper resolving an `Mvi` ViewModel via Koin (`koinViewModel { parametersOf(initialState) }`). A per-scaffold Koin module (`xxxScaffoldModule`) registers the VM; downstream modules `includes(...)` it. Default copy comes from `FrnkStrings` (`:ui-theme`); hosts override per-token via `FrnkThemeConfig.stringOverrides`.
  - `OnboardingScreen.kt` / `OnboardingScreenState.kt` / `OnboardingViewModel.kt` / `OnboardingScaffoldModule.kt` — renders through `FrnkFullScreenScaffold` (✕ + insets) over a `HorizontalPager` (configurable `pagerHeight`) + animated pip indicator + Back/Next → "Get Started" on the last page. Pure UI-state machine.
  - `SettingsScreen.kt` / `SettingsScreenState.kt` / `SettingsViewModel.kt` / `SettingsScaffoldModule.kt` — a scrollable list of `SettingsSectionState` cards + optional footer. Three row styles (theme segmented toggle / clickable row / toggle row) + a non-interactive status row (e.g. "Pro Member" badge). The VM reduces theme/toggle changes into state **and** re-emits them as effects so the host applies appearance / persists toggles / performs actions. Renders the heterogeneous Settings sections through `:ui-components`' public `FrnkSectionCard` chrome.
  - `SettingsDefaults.kt` — `@Composable rememberDefaultSettingsState(version, appearance, isPro, …)` builds the batteries-included catalog (Appearance, Notifications, Subscription, Support, Legal) + footer. Strict Free/Pro Subscription matrix. Hosts inject custom sections via `extraSections` + `extraSectionsPlacement` (`AfterAppearance`/`BeforeSubscription`/`BeforeLegal` default/`End`). Reducer tests in `androidHostTest` (`SettingsDefaultsTest`, a Robolectric Compose host test).
  - `FrnkScreenScaffold.kt` / `FrnkBottomBarInset.kt` — the standard **screen template** (`FrnkTopAppBar` pinned over edge-to-edge scrollable content, both bars fixed while content scrolls under them). `bottomInset` defaults to `LocalFrnkBottomBarInset.current` so a screen under `FrnkTabbedNavScaffold` auto-reserves the bar footprint. `LocalFrnkBottomBarInset` (`compositionLocalOf { 0.dp }`, non-static) is declared here where the readers live; `:ui-bottom-nav`'s `FrnkTabbedNavScaffold` provides the value.
  - `FrnkFullScreenScaffold.kt` — the **immersive screen template** (no top bar; an always-on top-right ✕ inside `WindowInsets.safeDrawing`), counterpart to `FrnkScreenScaffold` for full-window surfaces (onboarding, paywall, full-screen media). Pure chrome — no sealed `*State`; it folds safe-area insets + a reserved close-button band (`FrnkFullScreenScaffoldDefaults.CloseButtonHeight`) + the caller's `contentPadding` into one merged `PaddingValues` handed to its `content` slot, so content clears the ✕ and a scroll list scrolls under it. Consumed by `OnboardingScreen` + `:shared-monetization-ui`'s `PaywallScreen`.
  - `OnboardingGate.kt` — `OnboardingGate` (`internal` ctor) + `@Composable rememberOnboardingGate(): OnboardingGate?` — a `KeyValueStore`-backed `Preference<Boolean>` ("`frnk.onboarding.seen`") for **first-launch** gating; resolved leniently from Koin (null when no store bound). Wired by `:ui-bottom-nav`'s `FrnkFirstLaunchOnboardingEffect` (which `FrnkAppScaffold` installs automatically).
  - `HomeScreen.kt` / `HomeScreenState.kt` / `HomeViewModel.kt` / `HomeScaffoldModule.kt` — the home-tab page template: pinned `FrnkTopAppBar` over a scaffold-owned scrolling `Column` the host fills via a `ColumnScope` `content` slot. Pass-through `HomeViewModel` re-emits each interaction as a `HomeEffect`; `primaryActionEnabled` makes the screen claim the bottom bar's primary-action button. Reducer tests in `androidHostTest` (`HomeViewModelTest`).
  - `FeedbackEmailLauncher.kt` — `@Composable rememberFeedbackEmailLauncher(...)` returns a `() -> Unit` opening the platform mail composer prefilled with an app/OS/device diagnostics block (via `shared-utils`' `FeedbackEmail`/`EmailDraft` + `LocalUriHandler`). Wire to `SettingsAction.SendFeedback`.
- `ui/mvi/` — the **Compose binding layer** for the MVI engine (pure contracts/base in `:core-mvi`).
  - `FrnkMviScreen.kt` — `@Composable fun <S,I,E> FrnkMviScreen(viewModel, topBar, …, onEffect, content)`: binds an `MviViewModel` to `FrnkScreenScaffold`, collects state with `collectAsStateWithLifecycle`, hands the slot `(state, viewModel::send, padding)`. Effects consumed by an internal `EffectCollector` **only when `onEffect` is non-null** (single-consumer channel — pass `null` when a shared VM's effects are collected elsewhere).
  - `EffectCollector.kt` — `@Composable fun <E> EffectCollector(effects, minActiveState, onEffect)`: the one correct way to consume a one-shot effect stream — lifecycle-gated via `repeatOnLifecycle` + `rememberUpdatedState`-wrapped handler. Use instead of `LaunchedEffect(vm){ vm.effects.collect { … } }`.
- `ui/nav/` — the **Compose binding for the Navigation3 engine** (the Compose-free route + back-stack contract lives in `:core-nav`). The **host owns the back stack** (`NavBackStack<NavKey>`); navigation integrates with the MVI effect channel (a VM emits a nav `UiEffect`, a single collector mutates the back stack).
  - `FrnkNavDisplay.kt` — `rememberFrnkNavBackStack(...)` + `FrnkNavDisplay(backStack, …)` over nav3's `NavDisplay`, baking in the saveable-state + ViewModel-store entry decorators + toolkit slide transitions. `entryProvider` defaults to Koin's `koinEntryProvider()`.
  - `FrnkNavigationAnimations.kt` — `frnkEnterTransition()` / `frnkExitTransition()` (250ms horizontal slide).
  - `FrnkTabbedBackStacks.kt` — `rememberFrnkTabbedBackStacks(...)`: per-tab `NavBackStack`s for true multiple back stacks + `FrnkTabbedBackHandler(tabbed)` (back from a non-home tab root → home).
  - `FrnkNavTab.kt` — `@Immutable FrnkNavTab(key, root, icon, label)`, consumed by `rememberFrnkTabbedBackStacks(navTabs = …)` + `:ui-bottom-nav`'s `FrnkTabbedNavScaffold`.
  - `FrnkPrimaryActionHandler.kt` — the Compose binding for `:core-nav`'s `FrnkPrimaryActionRegistry`: `LocalFrnkPrimaryActionRegistry` (provided by `FrnkTabbedNavScaffold`) + `@Composable FrnkPrimaryActionHandler(enabled) { onAction }` (DisposableEffect-scoped claim on the bottom bar's primary-action button).

## Source sets

- `commonMain` — production code.
- `androidHostTest` — reducer/Compose-host tests (`HomeViewModelTest` reducer; `SettingsDefaultsTest` Robolectric Compose host test). Carries a **per-module copy** of `RobolectricComposeTest` + `setFrnkContent` (in package `…ui.atoms` so the test files import them unchanged) — `:ui-components`' `androidHostTest` is a separate, non-shared source set. The test *dependency bundle* is shared via the `frnk.kmp.library.composehosttest` convention plugin.
- `commonDebug` — `@Preview` composables for the scaffolds. Carries a **per-module copy** of `PreviewSurface`, in **this** module's `…ui.scaffolds.previews` package (NOT `…ui.atoms.previews`) — `commonDebug` feeds `iosMain`, so two identical `PreviewSurface` signatures would collide at the Kotlin/Native link step. Keep it in sync with `:ui-components`' copy.

## Dependencies

- `api(projects.uiComponents)` — the atoms/molecules/organisms (and transitively `:ui-theme` + `:haptics`).
- `api(projects.coreMvi)`, `api(projects.coreNav)` — the MVI engine + the Compose-free Nav3 contract these bindings bind; `api` so the facade re-exports them.
- `implementation(projects.sharedUtils)` — `FeedbackEmail` behind `FeedbackEmailLauncher`.
- `api(koin.compose, koin.compose.viewmodel, koin.navigation3)`, `api(androidx-lifecycle-runtime-compose)`, `api(androidx-navigation3-ui/-runtime/-viewmodel)` — `api` so hosts build their own graphs/screens. Pure Kotlin/Compose, no native cinterop.
- `implementation(compose-ui-backhandler)` — `FrnkTabbedBackHandler`.
- Applies `frnk.kmp.library.composehosttest`.

## Rules

- **No Material3.** Scaffolds compose `:ui-components` atoms + read `:ui-theme` tokens; the Material3 adaptive bar lives only in `:ui-bottom-nav`.
- **State hoisting.** Screen/nav/business state lives in the scaffold's `MviViewModel`; composables stay stateless. `remember`/`rememberSaveable` only for genuinely-local UI holders.
- Reuse `:ui-components`' public `FrnkSectionCard` for titled-card chrome rather than re-rolling it.
