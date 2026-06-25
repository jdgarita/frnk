# :ui-app

The apex of the ui column (`dev.jdgarita.frnk.ui.app`) — the app root relocated from the deleted `:shared` aggregator at restructure Stage 1 (OQ-7). Final home: `frnk/ui/app` (Stage 3).

## Public surface

- `FrnkApp(onSavedStateConfiguration, startRoute = FrnkRootRoute.Onboarding, onNavigationModule)` (`FrnkApp.kt`) — the **low-level app-root entry point**. It owns only the app chrome — `FrnkTheme` + `AppearanceController`-driven dark/light + system-bar appearance + a single root `NavDisplay` seeded at `startRoute` — and hands the navigation graph to the host: `onSavedStateConfiguration` supplies the root config (`frnkRootNavConfig`) and `onNavigationModule(backStack)` returns a **Koin navigation module** (`org.koin.dsl.navigation3.navigation<Route> { … }`), loaded via `loadKoinModules`. No batteries auto-wired: the host registers its own root destinations (onboarding/tab-shell/paywall) and wires the nested tab navigation itself (the tabbed surface at `FrnkRootRoute.Tab` is `:ui-bottom-nav`'s `FrnkNestedNavScaffold`). Live-entitlement Settings, first-launch onboarding, and the paywall are wired by the host, not auto-presented. `startRoute` lets a host skip onboarding for returning users (default seeds `Onboarding`; restored saved stacks override it).
- `frnkTabbedRootModule(customTab) { … }` + `FrnkTabNavigator` + `rememberFrnkRootStartRoute(showOnboarding = true)` (`FrnkTabbedRootModule.kt`) — the **batteries-included convenience over `FrnkApp`** for the common `onboarding → tab shell (Home · <custom> · Settings) → paywall` shape. The DSL builds the `onNavigationModule` lambda from content slots — `home { nav -> }`, `custom { route, nav -> }`, `settings { nav -> }` (required) + optional `onboarding { onComplete -> }` / `paywall { onClose -> }` — hiding the root+nested Koin modules, the `navigation<…> { }` DSL + `KoinExperimentalAPI` opt-in, the cross-level threading, and the onboarding→Tab / paywall→back conventions. The tab slots receive a `FrnkTabNavigator` (`open(route)` / `back()` within the active tab; `openPaywall()` / `showOnboarding()` to the root). `rememberFrnkRootStartRoute()` returns the gated start route (Onboarding until the `OnboardingGate` is seen, then Tab) for `FrnkApp`'s `startRoute`; the helper marks the gate seen when onboarding is presented. For full control, use the low-level `FrnkApp` `onNavigationModule` directly. `:demo-shared`'s `FrnkDemoApp` is the reference integration.
- `frnkUiModules(): List<Module>` — the SDK-free scaffold VM modules (home/settings/onboarding, plus `frnkNestedNavModule`) every host prepends to its `initializeFrnk(...)` list. Only scaffold VMs belong here; anything touching a third-party SDK is a separate module the host installs explicitly.

## Rules

- **No `*-impl` compile deps — ever.** `EntitlementManager`/`AnalyticsTracker` resolve from Koin at runtime; the compile surface is `:ui-bottom-nav` + `:shared-monetization-ui` + `:analytics-api` + `:core-di`.
- Monetization is optional: resolve it leniently (`koin.getOrNull`), degrade UI when absent. Don't add a hard `get<EntitlementManager>()`.
- Material3 arrives transitively via `:ui-bottom-nav`. Hosts that refuse Material3 hand-wire the nav3 primitives (`rememberFrnkNavBackStack` + `FrnkNavDisplay` + their own bar) instead and skip both this module and `:ui-bottom-nav`.

## Dependencies

- `commonMain`: `api(projects.uiBottomNav)`, `api(projects.sharedMonetizationUi)`, `api(projects.analyticsApi)`, `api(projects.coreDi)`. Compose/lifecycle/nav3/koin-compose arrive via `ui-bottom-nav → ui-scaffolds` `api()` exports.
- `commonTest`: list-inspection test for `frnkUiModules()` (`FrnkUiModulesTest`); run with `./gradlew :ui-app:testAndroidHostTest`.

## Demo

`:demo-shared`'s `FrnkDemoApp` is the reference integration — the single shared composable that calls `FrnkApp`, called by **both** `demo-android`'s `MainActivity` and `iosDemoApp`'s `MainViewController`. It supplies its own root navigation module (registering `FrnkRootRoute.Onboarding`/`Tab`/`Paywall`, with `Tab` mounting `FrnkNestedNavScaffold`) and wires its own batteries (onboarding, paywall, custom Settings). `:demo-shared` depending on `:ui-app` is **safe for `DemoKit.xcframework`** precisely because this module holds no `*-impl`/cinterop deps (the "no `*-impl` compile deps" rule above) — adding it leaks no RevenueCat/SQLite/Firebase symbols, so the framework stays clean while both platforms render the same root.
