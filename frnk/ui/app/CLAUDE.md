# :ui-app

The apex of the ui column (`dev.jdgarita.frnk.ui.app`) — the batteries-included app root relocated from the deleted `:shared` aggregator at restructure Stage 1 (OQ-7). Final home: `frnk/ui/app` (Stage 3).

## Public surface

- `FrnkAppScaffold(appName, appVersion, …) { homeContent }` — `:shared-ui-nav`'s `FrnkAppShell` plus the runtime-resolved batteries: fail-fast Koin check (`:core-di`'s `requireFrnkKoin()`), Settings driven by the **live** `EntitlementManager.isPro` (VM re-keys on flips; degrades to Free when no monetization modules are installed), the monetization-aware Settings handler (`rememberFrnkSettingsHandler` with appearance/onboarding/feedback fallbacks), and the auto-mounted `ToolkitRoute.Paywall`. Shares the `dev.jdgarita.frnk.ui.app` package with `FrnkAppShell`/`FrnkAppScope` (different module — namespace `…ui.app` is unique).
- `frnkUiModules(): List<Module>` — the SDK-free scaffold VM modules (home/settings/onboarding/bottomNav) every host prepends to its `initializeFrnk(...)` list. Only scaffold VMs belong here; anything touching a third-party SDK is a separate module the host installs explicitly.

## Rules

- **No `*-impl` compile deps — ever.** `EntitlementManager`/`AnalyticsTracker` resolve from Koin at runtime; the compile surface is `:shared-ui-nav` + `:shared-monetization-ui` + `:analytics-api` + `:core-di`.
- Monetization is optional: resolve it leniently (`koin.getOrNull`), degrade UI when absent. Don't add a hard `get<EntitlementManager>()`.
- Material3 arrives transitively via `:shared-ui-nav` (the accepted batteries-included trade). Hosts that refuse Material3 hand-wire `FrnkAppShell`'s lower-level pieces instead and skip this module.

## Dependencies

- `commonMain`: `api(projects.sharedUiNav)`, `api(projects.sharedMonetizationUi)`, `api(projects.analyticsApi)`, `api(projects.coreDi)`. Compose/lifecycle/nav3/koin-compose arrive via `shared-ui-nav → shared-ui-atoms` `api()` exports.
- `commonTest`: list-inspection test for `frnkUiModules()` (`FrnkUiModulesTest`); run with `./gradlew :ui-app:testAndroidHostTest`.

## Demo

`androidDemoApp`'s `AppScaffoldSmokeActivity` is the device smoke (`adb shell am start -n dev.jdgarita.frnk.demo/.AppScaffoldSmokeActivity`); `:shared-demo` can't exercise this module (it composes `FrnkAppShell` directly).
