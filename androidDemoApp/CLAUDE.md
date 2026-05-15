# androidDemoApp

Internal smoke harness for the toolkit on Android. **Not shipped.** Used to manually verify the public surface (Koin wiring, MVI flow, atoms) without standing up a separate consumer repo.

`com.android.application` — this is the only `application` module in the project. Everything else (including `:androidApp`) is a library.

## Contents

- `DemoApplication.kt` — sets `DatabaseContext.application = this` (the toolkit's default database binding reads a lateinit `Context` from there) and calls `startKoin { modules(demoModule) }`. Note: the demo currently uses its **own** Koin module rather than `initializeFrnk(...)` — it's a stripped-down harness, not a full integration sample.
- `DemoModule.kt` — Koin module wiring the demo's ViewModel and any fakes.
- `MainActivity.kt` — Compose entry point that hosts `DemoScreen`.
- `DemoScreen.kt` / `DemoViewModel.kt` — a single-screen MVI sample exercising `:shared-ui-atoms`.

## Build quirks

- This is the only module that applies `com.android.application`. It does **not** apply `kotlin.android` — AGP 9's built-in Kotlin support compiles its sources. Don't re-add the `kotlin.android` plugin.
- `release` build type has `isMinifyEnabled = false`. This is a harness; release-mode validation belongs in a real consumer app.
- Depends on `projects.androidApp` (which re-exports `:shared`). **Do not** add direct `projects.shared*` deps here — the harness should consume the toolkit the same way a real downstream app does.

## Rules

- Treat this module as **disposable**. Don't put production logic here; if something looks reusable, push it down into the appropriate `shared-*` module.
- The demo's Koin setup is intentionally minimal. If you want to demonstrate `initializeFrnk(...)` end-to-end, swap `DemoApplication.onCreate()` to call it directly rather than expanding `demoModule`.
- No `iosDemoApp` companion is wired into `settings.gradle.kts`; an `iosDemoApp/` directory exists at the repo root but isn't `include(...)`d. Leave that alone unless adding it is the actual task.
