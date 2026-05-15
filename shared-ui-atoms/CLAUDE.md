# shared-ui-atoms

Compose Multiplatform atoms layered on top of `:shared-ui-api`. Headless, theme-able primitives built on `compose-unstyled` 2.x (`com.composables:composeunstyled-primitives` + `:composeunstyled-theming`) — **not** Material3. New atoms must stay unopinionated about look-and-feel.

## Contents

- `ui/atoms/ToolkitTheme.kt` — `CompositionLocal`-based theme entry point. The host wraps content in `ToolkitTheme { ... }`.
- `ui/atoms/ToolkitButton.kt`, `ToolkitTextField.kt` — headless atoms; styling flows from `ToolkitTheme`.

## Dependencies

- `api(projects.sharedUiApi)` — atoms expose the same MVI types feature ViewModels use.
- `api(compose.runtime / foundation / ui)` so downstream modules don't redeclare Compose deps.
- `api(koin.compose, koin.compose.viewmodel)` — `koinViewModel()` works at call sites without extra wiring.
- `implementation(compose-unstyled.{primitives,theming,platformtheme})` — kept `implementation` so the consumer surface stays slim.

## Rules

- **No Material3.** Don't add `compose.material3` as a dep — atoms are headless.
- Atoms must be platform-agnostic — `commonMain` only. Anything that needs `LocalContext`, `UIViewController`, etc. belongs in the host app or a feature module's platform source set.
- ViewModel logic (MVI contracts, base class) stays in `:shared-ui-api`. This module is for composables and the theme.
- A new atom should be a single small file under `ui/atoms/`; keep state hoisted, accept `Modifier` first, and read styling from `ToolkitTheme`.
