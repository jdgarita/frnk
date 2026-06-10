# androidDemoApp

Internal smoke harness for the toolkit on Android. **Not shipped.** Used to manually verify the public surface (Koin wiring, MVI flow, atoms) without standing up a separate consumer repo.

`com.android.application` — this is the only `application` module in the project. Everything else (including `:androidApp`) is a library.

The actual demo UI (`DemoScreen`, `DemoViewModel`, `demoModule`, the fakes) lives in **`:shared-demo`** so it's shared with `iosDemoApp`. This module is just the thin Android host around it.

## Contents

- `DemoApplication.kt` — calls `bootstrapDemoKoin()` (from `:shared-demo`), the single Koin entry point both demo apps share. It installs `demoModule` (fake `EntitlementManager` + logging fakes) and then — **Android only** — overrides bindings with real toolkit modules (via Koin `allowOverride(true)`) to smoke-test the SDKs on a device: the real `firebaseObservabilityModule` for Firebase Analytics + Crashlytics (BACKLOG P1-5), and the real `revenueCatModule` for RevenueCat entitlements (BACKLOG P3-2). The RevenueCat override is installed **only when** a public Android SDK key is present in `local.properties` (`REVENUECAT_ANDROID_API_KEY`, surfaced as `BuildConfig.REVENUECAT_ANDROID_API_KEY`); `DemoApplication` calls `Purchases.configure(...)` first, then overrides. With no key the demo falls back to the in-memory fake (Toggle Pro). `revenueCatModule` arrives transitively via `:androidApp → :shared`; only `libs.revenuecat.core` is a direct dep (for the host's `configure` call). `iosDemoApp` keeps the same logging-fake bindings for `demoModule`, but additionally installs the **CrashKiOS** unhandled-exception hook + `FirebaseApp.configure()` in Swift so the "Force crash" panic button reports to Crashlytics there too (BACKLOG P1-5b) — so `DemoKit` is no longer fully SDK-free on iOS.
- `MainActivity.kt` — Compose entry point. Calls `enableEdgeToEdge()`, hoists an `AppearanceController` so it can drive the system-bar icon contrast (`isAppearanceLightStatusBars` / `…NavigationBars`) off the in-app theme, and hosts `:shared-demo`'s `DemoScreen(appearanceController = …)` — `DemoScreen` owns the `FrnkTheme` wrap itself now (via `FrnkAppShell`), so the activity only passes the controller through. MVI effects are surfaced as toasts.
- `AppScaffoldSmokeActivity.kt` — debug device smoke for `:shared`'s **`FrnkAppScaffold`** (the batteries-included app root `:shared-demo` can't exercise — it doesn't depend on `:shared`). Boots the scaffold over the Koin graph `DemoApplication` already started (fake `EntitlementManager` drives the live Free↔Pro Settings + auto-mounted paywall). No launcher icon — start with `adb shell am start -n dev.jdgarita.frnk.demo/.AppScaffoldSmokeActivity`. Note it does **not** call `initializeFrnk(context)` (Koin is process-global and the demo boots `bootstrapDemoKoin()`); that overload is a fresh-host path, documented in `HOST_ALIGNMENT.md` §3.
- Custom launcher icon: adaptive-icon XML in `res/mipmap-anydpi-v26/` (`ic_launcher` + `ic_launcher_round`) over `res/values/colors.xml` (`ic_launcher_background`), with `ic_launcher_foreground` PNGs per density. `android:label` and the iOS bundle display name are both just **"frnk"** (visual only — internal package/identifiers stay `…frnk.demo`).

## Build quirks

- This is the only module that applies `com.android.application`. It does **not** apply `kotlin.android` — AGP 9's built-in Kotlin support compiles its sources. Don't re-add the `kotlin.android` plugin.
- Applies the `google-services` + `firebase-crashlytics` Gradle plugins so `google-services.json` (project `frnk-demo`) is processed and Firebase auto-inits — the one real-SDK exception to the otherwise SDK-free harness, needed for the P1-5 analytics/crash smoke test. The toolkit's shared modules don't apply these (a consumer app does); they live here only because this is the device-runnable host.
- `release` build type has `isMinifyEnabled = false`. This is a harness; release-mode validation belongs in a real consumer app.
- Depends on `projects.androidApp` (which re-exports `:shared`) **and** `projects.sharedDemo` (the shared demo UI). The `:shared-demo` dependency is the one exception to "consume the toolkit like a real app" — it exists only because the demo screen itself is shared cross-platform. Don't add direct `projects.shared*` (non-demo) deps here.

## Rules

- Treat this module as **disposable**. Don't put production logic here; reusable UI goes into the appropriate `shared-*` module, demo UI goes into `:shared-demo`.
- The demo's Koin setup is intentionally minimal (`demoModule` only). If you want to demonstrate `initializeFrnk(...)` end-to-end, swap `DemoApplication.onCreate()` to call it directly rather than expanding the demo wiring.
- Every new toolkit feature is demoed in all three layers — `:shared-demo` (cross-platform), here (`androidDemoApp`, runs on a device), and `iosDemoApp` (builds `DemoKit.xcframework`, runs in a simulator).
