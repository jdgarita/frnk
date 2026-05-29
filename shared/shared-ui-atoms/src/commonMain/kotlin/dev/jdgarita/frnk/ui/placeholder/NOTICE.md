# Vendored: placeholder-compose

This package (`dev.jdgarita.frnk.ui.placeholder`) is a vendored, material-free subset of:

- **RevenueCat/placeholder-compose** — https://github.com/RevenueCat/placeholder-compose
- Copyright (c) 2025 RevenueCat, Inc.
- Licensed under the Apache License, Version 2.0 (see `LICENSE`).

## Why vendored instead of depended-upon

The upstream artifact (`com.revenuecat.purchases:placeholder`) hard-depends on Material3 / Material2 /
material-icons-extended in `commonMain`. `shared-ui-atoms` is a deliberately Material3-free, headless
design system (see the module's `CLAUDE.md`), so the published artifact could not be added without
violating that constraint. Only the **material-free core** is vendored here.

## Modifications from upstream

- Repackaged from `com.revenuecat.placeholder` to `dev.jdgarita.frnk.ui.placeholder`.
- Public API reduced to `internal` (this module does not use `explicitApi()`).
- Dropped the Material-coupled `Modifier.placeholderText(...)` and its `TextPlaceholder` machinery
  (the only Material3 import: `androidx.compose.material3.LocalTextStyle`).
- Dropped `PlaceholderTheme` / `LocalPlaceholderTheme`; `Modifier.placeholder(...)` now takes explicit
  `color` / `shape` / `highlight` (frnk resolves them from theme tokens via `Modifier.frnkSkeleton`).
- Trimmed `PlaceholderDefaults` to `shimmer` + `fade`; removed the `pulse` / `lightReveal` /
  `circularReveal` highlights (and their source files were not vendored).
- Removed the `androidx.annotation.FloatRange` annotations from `PlaceholderHighlight` to keep the
  code `commonMain`-safe.

Vendored files retain the original Apache-2.0 copyright header.
