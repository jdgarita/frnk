# shared-monetization-ui

Monetization **UI** for the toolkit (BACKLOG P3-3): the basic paywall, its toolkit-owned navigation entry,
and the Settings monetization wiring. It lives in its own module so the design system (`:shared-ui-atoms`)
stays monetization-agnostic — this is the one place that depends on **both** the design system and the
monetization domain (`:shared-monetization-api`).

## Contents

- `PaywallScreen.kt` — `PaywallViewModel` (MVI) + stateless `PaywallScreenContent` + the VM-backed
  `PaywallScreen(source, features, onEffect)`. The VM loads `offerings()`, tracks the funnel
  (`Paywall_Viewed{source}`, `Purchase_*`, `Paywall_Dismissed`), and runs purchase/restore through the
  frnk `EntitlementManager`; success → `PaywallEffect.Dismiss`, cancel/failure → `PaywallEffect.Message`
  (never throws). UI is **stacked selectable plan cards** (radio + price + per-month + free-trial/best-value
  badge), a single CTA ("Start free trial" when the selected plan has a trial, else "Continue"), and
  Restore + Terms/Privacy. Product list shows a loading skeleton while offerings load.
- `PaywallScaffoldModule.kt` — `paywallScaffoldModule` registers `PaywallViewModel` (`source` via
  `parametersOf`; `EntitlementManager` + `AnalyticsTracker` from the graph). `:shared`'s `frnkModules(...)`
  includes it.
- `PaywallNav.kt` — the Navigation3 paywall: a route-agnostic `@Composable FrnkPaywallDestination(features,
  source, onMessage, onClose)` destination body **and** `Module.frnkPaywallNavigation(...)` which registers it
  at `ToolkitRoute.Paywall` via Koin's `navigation<Route> { }` DSL (resolved by `FrnkNavDisplay`'s default
  `koinEntryProvider()`). **The toolkit owns the paywall destination; the host owns the `NavBackStack`.** A host
  with its own paywall route just calls `FrnkPaywallDestination(...)` inside its own `navigation<MyRoute.Paywall>`
  block. (No `kotlin-serialization` plugin needed here anymore — that was for the old nav2 `frnkComposable<T>`.)
- `FrnkSettingsHandler.kt` — `rememberFrnkSettingsHandler(backStack, entitlements, analytics, onMessage,
  fallback)` returns a `(SettingsEffect) -> Unit` that wires the monetization Settings rows for free:
  `UpgradeToPro` → `backStack.navigateTo(ToolkitRoute.Paywall)`, `RestorePurchases` → `entitlements.restorePurchases()`,
  `ManageSubscription` → `entitlements.managementUrl()` opened via `LocalUriHandler`, **falling back to
  `platformManageSubscriptionsUrl()`** (the OS subscriptions deep link) when the provider has no
  customer-specific URL — so the row always lands somewhere useful; the `GOD_MODE_TOGGLE_ID` toggle →
  `entitlements.setGodMode(...)`; everything else (theme, other actions) goes to `fallback`.
  `GOD_MODE_TOGGLE_ID` is the stable id a host gives the god-mode `SettingsToggleRow`.
- `ManageSubscriptions.kt` (+ `.android.kt`/`.ios.kt`) — `internal expect fun platformManageSubscriptionsUrl():
  String`, the module's only `expect/actual`: the native subscriptions deep link (Google Play on Android,
  App Store on iOS). Returned as a URL so it opens through Compose's `LocalUriHandler` without threading a
  platform `Context`/`UIApplication`.

## Entry points (host pattern)

Two always-on paywall entry points the demo wires (and real hosts copy):
1. **Home top bar** — a top-right `FrnkTopAppBarAction` (crown / `iconUpgrade`), hidden once `isPro`,
   `onActionClick` → `gate.requestUpgrade(...)` / navigate `ToolkitRoute.Paywall`.
2. **Settings** — the default catalog's Subscription rows (Free: Upgrade + Restore; Pro: "Pro Member"
   badge + Manage Subscription), routed through `rememberFrnkSettingsHandler`.
   God mode lives in the Settings hidden Developer section (reveal: tap the version footer 7×, or the host
   `showDeveloperSection` flag).

## Rules

- **No billing SDK here** — this is UI + the frnk `EntitlementManager`/`FeatureGate` only. RevenueCat stays
  in `:shared-monetization-revenuecat`. Pure Kotlin/Compose, so `DemoKit`/`FrnkKit` XCFrameworks stay clean.
- Reads styling from `Theme[...]` tokens (paywall strings/icons live in `:shared-ui-atoms` `FrnkStrings`/
  `FrnkIcons` — `stringAppName`, `stringPaywall*`, `stringProName`, `iconUpgrade`, `iconCheck`).

## Dependencies

- `api(projects.sharedUiAtoms)`, `api(projects.sharedMonetizationApi)` (transitively `:shared-ui-api` for
  `ToolkitRoute` + the nav3 back-stack helpers, and the nav3 engine via atoms). `commonTest`: `kotlin.test` +
  `kotlinx.coroutines.test`.
- Plugins: compose (+ hosttest). No `kotlin-serialization` — the nav3 route serializers live in `:shared-ui-api`.
