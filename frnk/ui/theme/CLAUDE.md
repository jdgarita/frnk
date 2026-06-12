# ui-theme

The toolkit's Compose Multiplatform **design-system foundation** — design tokens + the theme engine.
Extracted from `:shared-ui-atoms` at restructure **Stage 7a** as the bottom of the `ui` column
(`haptics ← ui-theme ← ui-components ← ui-scaffolds ← ui-bottom-nav ← ui-app`). Headless and
unopinionated — **not** Material3. Kotlin package is unchanged (`dev.jdgarita.frnk.ui.theme` /
`dev.jdgarita.frnk.ui.tokens`).

The `:shared-ui-atoms` facade that used to re-export it was deleted at Stage 9; consumers depend on
`:ui-theme` directly.

## Contents

- `ui/tokens/` — design tokens (single source of truth, plain Kotlin objects).
  - `ColorTokens.kt` — `FrnkPrimitiveColors` (raw scales) and `FrnkColors.Light/Dark` (semantic).
  - `TypographyTokens.kt` — `FrnkTypography` (Material 3 scale, no Material dependency).
  - `SpacingTokens.kt`, `ShapeTokens.kt`, `IconSizeTokens.kt` — Dp + Shape constants.
- `ui/theme/` — the theme engine.
  - `FrnkTheme.kt` — `ThemeProperty<T>` + `ThemeToken<T>` declarations for all axes, `LightPalette`/`DarkPalette` maps, `Appearance` enum + `LocalAppearanceController`, and the public `@Composable fun FrnkTheme(config, appearanceController, content)` built on `buildPlatformTheme`. Installs a **press ripple** as the ambient `LocalIndication` (`config.indication ?: rememberFrnkRipple()`) and **haptics** (`LocalFrnkHaptics`, via `haptics: HapticFeedback = rememberFrnkHaptics()`) so every interactive atom under the theme gets touch feedback + vibration with no per-component wiring. Bridges the chosen appearance onto the native interface style via `applyNativeInterfaceStyle(...)` (in `shared-utils`) from a `LaunchedEffect` — pins iOS `overrideUserInterfaceStyle` so native UIKit chrome follows the toggle; no-op on Android.
  - `FrnkRipple.kt` — `rememberFrnkRipple(...)` over `com.composables:ripple-indication` (theme-agnostic, **not** Material3). Hosts pass it to `clickable(indication = …)` or restyle the global default via `FrnkThemeConfig.indication`.
  - `FrnkThemeConfig.kt` — immutable host config: per-axis token override `Map`s (light/dark colors, text styles, shapes, strings, icons), an optional `fontFamily`, and an optional `indication`. Prefer `FrnkThemeConfig.Default` at call sites to avoid per-recomposition allocation.
  - `FrnkStrings.kt` / `FrnkIcons.kt` — default toolkit strings/icons + the `ThemeToken<String>` / `ThemeToken<ImageVector>` constants. Hosts override via `FrnkThemeConfig.stringOverrides` / `iconOverrides`.
  - **Token naming**: color tokens use the `color` prefix (`colorPrimary`, …) so they don't shadow `kotlin.error()` or locals; shapes `shape*`, strings `string*`, icons `icon*`; text-style tokens stay unprefixed (`bodyLarge`, `titleMedium`).

## Dependencies

- `api(projects.haptics)` — `FrnkTheme` installs `LocalFrnkHaptics`; api so the haptics contract +
  `LocalFrnkHaptics` flow transitively to `:ui-components` (atoms call them).
- `implementation(projects.sharedUtils)` — `applyNativeInterfaceStyle` (iOS appearance plumbing).
- `api(compose-unstyled-theming)` — `ThemeProperty`/`ThemeToken` are part of the public token API.
- `implementation(compose-unstyled-platformtheme)` — `buildPlatformTheme`.
- `implementation(compose-ripple-indication)` — the ripple renderer behind `rememberFrnkRipple`.
- `implementation(icons-lucide)` — default icons in `FrnkIcons.kt`. Hosts can override every icon, so
  consumers don't take a Lucide dependency unless they reference Lucide vectors at call sites.
- `api(compose.runtime / foundation / ui)` via `frnk.kmp.library.compose`.

## Rules

- **No Material3.** Tokens + theme resolve via `com.composeunstyled.*`; text styles are a plain
  `FrnkTypography` object, not `MaterialTheme.typography`.
- **No upward `ui` deps.** This is the floor of the ui column — it may depend only on `:haptics` and
  `core` modules, never on `:ui-components` / `:ui-scaffolds` / above.
- Host configuration flows through `FrnkThemeConfig` — the single override entry point. Don't expose
  ad-hoc setters or extra composition locals per axis.
