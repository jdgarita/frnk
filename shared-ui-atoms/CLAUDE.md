# shared-ui-atoms

Compose Multiplatform design system: tokens, theme engine, host-configurable theme, and a small set of `Frnk*` atoms built on `compose-unstyled` 2.x. Headless and unopinionated — **not** Material3.

## Contents

- `ui/tokens/` — design tokens (single source of truth, plain Kotlin objects).
  - `ColorTokens.kt` — `FrnkPrimitiveColors` (raw scales) and `FrnkColors.Light/Dark` (semantic).
  - `TypographyTokens.kt` — `FrnkTypography` (Material 3 scale).
  - `SpacingTokens.kt`, `ShapeTokens.kt`, `IconSizeTokens.kt` — Dp + Shape constants.
- `ui/theme/` — the theme engine.
  - `FrnkTheme.kt` — `ThemeProperty<T>` + `ThemeToken<T>` declarations for all axes, `LightPalette`/`DarkPalette` maps, `Appearance` enum + `LocalAppearanceController`, and the public `@Composable fun FrnkTheme(config, content)` built on `buildPlatformTheme`.
  - `FrnkThemeConfig.kt` — immutable host config. Hosts pass `Map`s of token → override for any axis (light colors, dark colors, text styles, shapes, strings, icons). Empty by default.
  - `FrnkStrings.kt` / `FrnkIcons.kt` — default toolkit strings and icons + the `ThemeToken<String>` / `ThemeToken<ImageVector>` constants. Hosts override via `FrnkThemeConfig.stringOverrides` / `iconOverrides`.
- `ui/atoms/` — `Frnk*` atoms, each in its own file with an `@Immutable` `*State` class.
  - `FrnkText.kt` — sealed `FrnkTextState` (Text/Title/TitleMedium/HeadlineSmall/Body/BodyMedium/BodySmall/AppName).
  - `FrnkButton.kt` — `FrnkButtonState` + `FrnkButtonVariant` (Filled/Outlined/Ghost).
  - `FrnkIcon.kt` — `FrnkIconState`.
  - `FrnkIconButton.kt` — `FrnkIconButtonState`.
  - `FrnkDivider.kt` — sealed `FrnkDividerState` (Horizontal/Vertical).

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
- **Atoms read tokens, not hard-coded values.** Use `Theme[colors][primary]` not `Color(0xFF...)`. Use `FrnkSpacing.md` not `16.dp` for spacing constants.
- **Atoms are platform-agnostic** — `commonMain` only. No `LocalContext`, `UIViewController`, etc.
- **Every atom has a `*State` class.** State is `@Immutable`, fields default to sensible values, and callbacks (`onClick`, etc.) are separate parameters on the composable.
- **Host configuration flows through `FrnkThemeConfig`.** Don't expose ad-hoc setters or extra composition locals for per-axis overrides; that contract is the single entry point.
- ViewModel logic (MVI contracts, base class) stays in `:shared-ui-api`. This module is for tokens, theme, and composables only.
