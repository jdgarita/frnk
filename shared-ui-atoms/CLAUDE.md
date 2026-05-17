# shared-ui-atoms

Compose Multiplatform design system: tokens, theme engine, host-configurable theme, and a small set of `Frnk*` atoms built on `compose-unstyled` 2.x. Headless and unopinionated — **not** Material3.

## Contents

- `ui/tokens/` — design tokens (single source of truth, plain Kotlin objects).
  - `ColorTokens.kt` — `FrnkPrimitiveColors` (raw scales) and `FrnkColors.Light/Dark` (semantic).
  - `TypographyTokens.kt` — `FrnkTypography` (Material 3 scale).
  - `SpacingTokens.kt`, `ShapeTokens.kt`, `IconSizeTokens.kt` — Dp + Shape constants.
- `ui/theme/` — the theme engine.
  - `FrnkTheme.kt` — `ThemeProperty<T>` + `ThemeToken<T>` declarations for all axes, `LightPalette`/`DarkPalette` maps, `Appearance` enum + `LocalAppearanceController`, and the public `@Composable fun FrnkTheme(config, appearanceController, content)` built on `buildPlatformTheme`. The `appearanceController` defaults to a `remember`-scoped instance and is provided via `CompositionLocalProvider`, so the toggle works zero-config; hosts that need process-death survival can hoist their own controller (e.g. with `rememberSaveable` or a DataStore-backed flow) and pass it in.
  - **Token naming**: color tokens use the `color` prefix (`colorPrimary`, `colorOnPrimary`, `colorError`, …) so importing them doesn't shadow `kotlin.error()` or common local variable names. Shape tokens use `shape*`, string tokens `string*`, icon tokens `icon*`. Text-style tokens stay unprefixed (`bodyLarge`, `titleMedium`) — they don't collide with anything common.
  - `FrnkThemeConfig.kt` — immutable host config. Hosts pass `Map`s of token → override for any axis (light colors, dark colors, text styles, shapes, strings, icons) plus an optional `fontFamily: FontFamily?` that's applied to every default text style. Prefer `FrnkThemeConfig.Default` over `FrnkThemeConfig()` at call sites to avoid per-recomposition allocation.
  - `FrnkStrings.kt` / `FrnkIcons.kt` — default toolkit strings and icons + the `ThemeToken<String>` / `ThemeToken<ImageVector>` constants. Hosts override via `FrnkThemeConfig.stringOverrides` / `iconOverrides`.
- `ui/atoms/` — `Frnk*` atoms, each in its own file with an `@Immutable` `*State` class.
  - `FrnkText.kt` — sealed `FrnkTextState` (Text/Title/TitleMedium/HeadlineSmall/Body/BodyMedium/BodySmall/AppName).
  - `FrnkButton.kt` — `FrnkButtonState` + `FrnkButtonVariant` (Filled/Outlined/Ghost).
  - `FrnkIcon.kt` — `FrnkIconState`.
  - `FrnkIconButton.kt` — `FrnkIconButtonState`.
  - `FrnkDivider.kt` — sealed `FrnkDividerState` (Horizontal/Vertical).
- `ui/scaffolds/` — higher-than-atom, lower-than-feature page templates with a fixed UI shape and a configurable state class. Each scaffold ships **two** entry points: a stateless `*Content` composable (used by previews and advanced hosts that hoist their own state), and a VM-backed convenience wrapper that resolves a `Mvi`-based ViewModel via Koin (`koinViewModel { parametersOf(initialState) }`). A per-scaffold Koin module (`xxxScaffoldModule`) registers the VM; downstream Koin modules `includes(...)` it. Default copy comes from `FrnkStrings`; hosts override per-token through `FrnkThemeConfig.stringOverrides`.
  - `OnboardingScreen.kt` — `OnboardingScreen` (VM-backed) + `OnboardingScreenContent` (stateless). Close-X (top-right), `HorizontalPager` (configurable `pagerHeight: Dp? = null` — null fills remaining space, non-null pins exact height), animated pip indicator, Back/Next buttons that flip to "Get Started" on the last page.
  - `OnboardingScreenState.kt` — `OnboardingPageState` (title/description/icon, all reused atom states), `OnboardingScreenState`, `OnboardingIntent` (PageSelected/NextClicked/PreviousClicked/CloseClicked), `OnboardingEffect` (CloseRequested/Completed).
  - `OnboardingViewModel.kt` — pure UI-state machine, no injected deps.
  - `OnboardingScaffoldModule.kt` — `onboardingScaffoldModule` Koin module.

## Source sets

- `commonMain` — production code (tokens, theme, atoms).
- `commonDebug` — `@Preview` composables for every atom. Sits between `commonMain` and each platform source set (`androidMain`, `iosArm64Main`, `iosSimulatorArm64Main` all `dependsOn` it). Uses the multiplatform `androidx.compose.ui.tooling.preview.Preview` annotation from `org.jetbrains.compose.ui:ui-tooling-preview`. New preview files go under `src/commonDebug/kotlin/dev/jdgarita/frnk/ui/atoms/previews/`. Wrap content in `PreviewSurface(appearance = ...)` so the preview renders under a real `FrnkTheme` against the chosen palette.

  **Caveat:** AGP 9's `com.android.kotlin.multiplatform.library` plugin has a single `androidMain` compilation (no `compileAndroidDebug`/`compileAndroidRelease` split), so today `commonDebug` sources also end up in release AARs. They're inert `@Composable` functions and R8 strips them — but if you need true debug-only exclusion (size budget, license posture, etc.), promote `commonDebug` to a sibling Gradle module that only the demo / debug consumers depend on.

## Dependencies

- `api(projects.sharedUiApi)` — atoms expose the same MVI types feature ViewModels use.
- `api(compose.runtime / foundation / ui)` so downstream modules don't redeclare Compose deps.
- `api(koin.compose, koin.compose.viewmodel)` — `koinViewModel()` works at call sites without extra wiring.
- `implementation(compose-unstyled.{primitives,theming,platformtheme,button,icon,separators})` — kept `implementation` so the consumer surface stays slim.
- `implementation(icons-lucide)` — default icons in `FrnkIcons.kt`. Hosts can override every icon, so consumers don't have to take a Lucide dependency unless they reference Lucide icons at call sites.

## Rules

- **No Material3.** Don't add `compose.material3` — atoms wrap `com.composeunstyled.*` headless primitives (`UnstyledButton`, `UnstyledIcon`, `UnstyledHorizontalSeparator`, `UnstyledVerticalSeparator`, `Text`) and resolve styling via `Theme[colors][...]` / `Theme[textStyles][...]`.
- **Atoms read tokens, not hard-coded values.** Use `Theme[colors][colorPrimary]` not `Color(0xFF...)`. Use `FrnkSpacing.md` not `16.dp` for spacing constants.
- **Atoms are platform-agnostic** — `commonMain` only. No `LocalContext`, `UIViewController`, etc.
- **Every atom has a `*State` class.** State is `@Immutable`, fields default to sensible values, and callbacks (`onClick`, etc.) are separate parameters on the composable.
- **Host configuration flows through `FrnkThemeConfig`.** Don't expose ad-hoc setters or extra composition locals for per-axis overrides; that contract is the single entry point.
- ViewModel logic (MVI contracts, base class) stays in `:shared-ui-api`. This module is for tokens, theme, and composables only.
