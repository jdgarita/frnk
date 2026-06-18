# :ui-app

The apex of the ui column (`dev.jdgarita.frnk.ui.app`) — the batteries-included app root relocated from the deleted `:shared` aggregator at restructure Stage 1 (OQ-7). Final home: `frnk/ui/app` (Stage 3).

## Public surface

- `FrnkAppScaffold(config: FrnkAppConfig, …) { homeContent }` — `:ui-bottom-nav`'s `FrnkTabbedNavScaffold` plus the runtime-resolved batteries: fail-fast Koin check (`:core-di`'s `requireFrnkKoin()`), Settings driven by the **live** `EntitlementManager.isPro` (the Settings VM reacts via `SettingsIntent.ConfigChanged`; degrades to Free when no monetization modules are installed), the monetization-aware Settings handler (`rememberFrnkSettingsHandler` with appearance/onboarding/feedback fallbacks), the auto-mounted `ToolkitRoute.Paywall`, and **first-launch onboarding** (`config.onboarding.showOnFirstLaunch`, default true) — auto-presented once via `:ui-scaffolds`' `rememberOnboardingGate()` + `:ui-bottom-nav`'s `FrnkFirstLaunchOnboardingEffect` when `config.onboarding.pages` are supplied. `FrnkAppScope` (the handle handed to its extension points) lives in `:ui-bottom-nav`, package `…ui.bottomnav` — imported here.
- `FrnkAppConfig` + `FrnkMonetizationConfig` (`FrnkAppConfig.kt`) — the host's `@Immutable` config bundle, the batteries-included superset of `:ui-bottom-nav`'s `FrnkTabbedNavConfig` (same feature sub-configs `FrnkAppInfo`/`FrnkNavConfig`/`FrnkThemeConfig`/`FrnkHomeConfig`/`FrnkSettingsConfig`/`FrnkOnboardingConfig`, plus `monetization`, the one piece `:ui-bottom-nav` can't depend on). `FrnkAppScaffold` maps it down via the internal `toTabbedNavConfig()` (in `ext/FrnkAppConfigExt.kt` — **extension functions live in their own `ext/<Type>Ext.kt` file**, never in the type's declaration file), injecting the one battery this layer owns: the Home top bar defaults to `config.app.name`. `config.settings` passes through verbatim — the Settings VM **reacts** to `isPro` via `SettingsIntent.ConfigChanged` (the `remember(config, isPro)`-rebuilt catalogue flows down through `settingsState` and is merged into the live VM, preserving in-session toggles + dev-reveal), so there is no `vmKey` re-keying. **Convention:** `*Config` = host input declared once; runtime, toolkit-owned state stays in `*State`/`*ViewState`. `@Composable` slots + event callbacks stay as `FrnkAppScaffold` params (they can't live in an `@Immutable` bundle).
- `frnkUiModules(): List<Module>` — the SDK-free scaffold VM modules (home/settings/onboarding) every host prepends to its `initializeFrnk(...)` list. Only scaffold VMs belong here; anything touching a third-party SDK is a separate module the host installs explicitly.

## Rules

- **No `*-impl` compile deps — ever.** `EntitlementManager`/`AnalyticsTracker` resolve from Koin at runtime; the compile surface is `:ui-bottom-nav` + `:shared-monetization-ui` + `:analytics-api` + `:core-di`.
- Monetization is optional: resolve it leniently (`koin.getOrNull`), degrade UI when absent. Don't add a hard `get<EntitlementManager>()`.
- Material3 arrives transitively via `:ui-bottom-nav` (the accepted batteries-included trade). Hosts that refuse Material3 hand-wire `FrnkTabbedNavScaffold`'s lower-level primitives (`rememberFrnkTabbedBackStacks` + `FrnkNavDisplay` + `FrnkTabbedBackHandler` + own bar) instead and skip this module.

## Dependencies

- `commonMain`: `api(projects.uiBottomNav)`, `api(projects.sharedMonetizationUi)`, `api(projects.analyticsApi)`, `api(projects.coreDi)`. Compose/lifecycle/nav3/koin-compose arrive via `ui-bottom-nav → ui-scaffolds` `api()` exports.
- `commonTest`: list-inspection test for `frnkUiModules()` (`FrnkUiModulesTest`); run with `./gradlew :ui-app:testAndroidHostTest`.

## Demo

`:demo-shared`'s `FrnkDemoApp` is the reference integration — the single shared composable that wraps `FrnkAppScaffold`, called by **both** `demo-android`'s `MainActivity` and `iosDemoApp`'s `MainViewController`. It feeds the scaffold-agnostic content builders into the scaffold and injects the demo's custom Settings (god-mode Developer section) via the `settingsState`/`settingsEffects` overrides. `:demo-shared` depending on `:ui-app` is **safe for `DemoKit.xcframework`** precisely because this module holds no `*-impl`/cinterop deps (the "no `*-impl` compile deps" rule above) — adding it leaks no RevenueCat/SQLite/Firebase symbols, so the framework stays clean while both platforms render the batteries-included root. (Earlier docs claimed `:demo-shared` "can't depend on `:ui-app`"; that was a soft purity choice built on a false premise — it's compile-safe.)
