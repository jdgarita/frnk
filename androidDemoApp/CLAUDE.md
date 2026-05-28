# androidDemoApp

Internal smoke harness for the toolkit on Android. **Not shipped.** Used to manually verify the public surface (Koin wiring, MVI flow, atoms) without standing up a separate consumer repo.

`com.android.application` — this is the only `application` module in the project. Everything else (including `:androidApp`) is a library.

The actual demo UI (`DemoScreen`, `DemoViewModel`, `demoModule`, the fakes) lives in **`:shared-demo`** so it's shared with `iosDemoApp`. This module is just the thin Android host around it.

## Contents

- `DemoApplication.kt` — calls `bootstrapDemoKoin()` (from `:shared-demo`), the single Koin entry point both demo apps share. It installs only `demoModule` (fake `EntitlementManager` + logging fakes), so the demo exercises the toolkit without any real backend/database init.
- `MainActivity.kt` — Compose entry point. Calls `enableEdgeToEdge()`, hoists an `AppearanceController` so it can drive the system-bar icon contrast (`isAppearanceLightStatusBars` / `…NavigationBars`) off the in-app theme, wraps everything in `FrnkTheme(config = demoPurpleThemeConfig(), appearanceController = …)`, and hosts `:shared-demo`'s `DemoScreen`. MVI effects are surfaced as toasts.
- Custom launcher icon: adaptive-icon XML in `res/mipmap-anydpi-v26/` (`ic_launcher` + `ic_launcher_round`) over `res/values/colors.xml` (`ic_launcher_background`), with `ic_launcher_foreground` PNGs per density. `android:label` and the iOS bundle display name are both just **"frnk"** (visual only — internal package/identifiers stay `…frnk.demo`).

## Build quirks

- This is the only module that applies `com.android.application`. It does **not** apply `kotlin.android` — AGP 9's built-in Kotlin support compiles its sources. Don't re-add the `kotlin.android` plugin.
- `release` build type has `isMinifyEnabled = false`. This is a harness; release-mode validation belongs in a real consumer app.
- Depends on `projects.androidApp` (which re-exports `:shared`) **and** `projects.sharedDemo` (the shared demo UI). The `:shared-demo` dependency is the one exception to "consume the toolkit like a real app" — it exists only because the demo screen itself is shared cross-platform. Don't add direct `projects.shared*` (non-demo) deps here.

## Rules

- Treat this module as **disposable**. Don't put production logic here; reusable UI goes into the appropriate `shared-*` module, demo UI goes into `:shared-demo`.
- The demo's Koin setup is intentionally minimal (`demoModule` only). If you want to demonstrate `initializeFrnk(...)` end-to-end, swap `DemoApplication.onCreate()` to call it directly rather than expanding the demo wiring.
- Every new toolkit feature is demoed in all three layers — `:shared-demo` (cross-platform), here (`androidDemoApp`, runs on a device), and `iosDemoApp` (builds `DemoKit.xcframework`, runs in a simulator).
