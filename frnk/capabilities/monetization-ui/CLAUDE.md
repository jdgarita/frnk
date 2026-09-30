# shared-monetization-ui

Monetization **UI** for the toolkit (BACKLOG P3-3): the basic paywall, its toolkit-owned navigation entry,
and the Settings monetization wiring. It lives in its own module so the design system (`:ui-theme`/`:ui-components`/`:ui-scaffolds`)
stays monetization-agnostic — this is the one place that depends on **both** the design system and the
monetization domain (`:monetization-api`).

## Contents

- `PaywallScreen.kt` — `PaywallViewModel` (MVI) + stateless `PaywallScreenContent` + the VM-backed
  `PaywallScreen(source, features, onEffect)`. The VM loads `offerings()`, tracks the funnel
  (`Paywall_Viewed{source}`, `Purchase_*`, `Paywall_Dismissed`), and runs purchase/restore through the
  injectable `PaywallPurchaseUseCase` (`:monetization-api`, delegates to `EntitlementManager` — so the
  VM stays SDK/manager-agnostic); success → `PaywallEffect.Dismiss`, cancel/failure →
  `PaywallEffect.Message` (never throws). `PaywallEffect.Message` carries a **`FrnkStringSource`**
  (not a raw `String`) so toolkit copy stays a theme token — hosts hold it in state and render via
  `resolve()` so `stringOverrides`/locale apply. **Restore hardening:** every store interaction is
  sequenced behind `SyncAuthUseCase.identify()` so the entitlement lands on the host's stable uid,
  never RC's transient anonymous id (identity failure aborts restore with
  `stringPaywallIdentityError`). On attach the VM also runs a best-effort **silent receipt sync**
  (`PaywallPurchaseUseCase.sync()`) — a reinstalled Pro user is dismissed with Pro restored instead of
  being sold to; sync failures are silent. A purchase failing with `MonetizationError.AlreadyOwned`
  falls through to a restore automatically. `isRestoring` disables + relabels the Restore button
  (`stringPaywallRestoring`) while a restore is in flight. UI is **stacked selectable plan cards** (radio + price + per-month + free-trial/best-value
  badge), a single CTA ("Start free trial" when the selected plan has a trial, else "Continue"), and
  Restore + Terms/Privacy. Product list shows a loading skeleton while offerings load.
  **Hard mode (0.11.0):** `dismissible = false` (on `PaywallScreen` / `FrnkPaywallDestination` /
  `frnkPaywallNavigation`, carried to the VM in `PaywallArguments`) hides the ✕ (`FrnkFullScreenScaffold
  (showCloseButton = false)`), installs a swallowing `BackHandler` inside the screen (it outranks a
  `FrnkNavDisplay`'s pop and `FrnkScreen`'s own handler), and the VM ignores `PaywallIntent.Close`; success
  paths (purchase / restore / silent sync) still emit `Dismiss`; a pending purchase does not (it shows
  `stringPaywallPurchasePending`), and the VM also collects the optional `ObserveProStatusUseCase` to close on
  Pro from any cause — every Pro path goes through one deduplicating `dismissForPro()`. A hard presentation
  uses its own VM key. `PaywallIntent.Retry` reloads offerings; the stock screen shows Retry under the empty
  copy. `PaywallScreenTest` (Robolectric) pins it.
  **Footer:** `legalLinks: PaywallLegalLinks?` (`PaywallLegalLinks.kt`) turns "Terms · Privacy" into links
  opened via `LocalUriHandler` (failures swallowed — hosts supply their own `UriHandler` to report them),
  `onLegalLinkClick` is the analytics hook; `planDisclosure` is the host's slot under the CTA for the
  selected plan. The internal `PaywallScreen(viewModel, …)` overload exists for tests.
- `PaywallScaffoldModule.kt` — `paywallScaffoldModule` registers `PaywallViewModel` (`source` arrives at
  attach time via `PaywallArguments`; `PaywallPurchaseUseCase` + `AnalyticsTracker` + `SyncAuthUseCase`
  from the graph). Hosts
  install it alongside `revenueCatModule` + `monetizationModule` in their `initializeFrnk(...)` list.
- `PaywallNav.kt` — the Navigation3 paywall: a route-agnostic `@Composable FrnkPaywallDestination(features,
  source, onMessage, onClose)` destination body (`onMessage: (FrnkStringSource) -> Unit` — hold it in
  state, resolve at the rendering leaf) **and** `Module.frnkPaywallNavigation(...)` which registers it
  at `FrnkRootRoute.Paywall` via Koin's `navigation<Route> { }` DSL (resolved by `FrnkNavDisplay`'s default
  `koinEntryProvider()`). **The toolkit owns the paywall destination; the host owns the `NavBackStack`.** A host
  with its own paywall route just calls `FrnkPaywallDestination(...)` inside its own `navigation<MyRoute.Paywall>`
  block. (No `kotlin-serialization` plugin needed here anymore — that was for the old nav2 `frnkComposable<T>`.)
- `NoSubscriptionFoundDialog.kt` — the empty-restore dialog (plan A3, from Faint): a neutral `FrnkDialog` with the `iconReceipt` glyph, copy from the `stringNoSubscriptionFound*` tokens (EN + ES), the body naming the one store the build ships through and the product (`{product}` placeholder, "<appName> <proName>" by default). `onShown` fires once per presentation for host analytics; the dialog records nothing itself. Not yet raised by `rememberFrnkSettingsHandler` / the paywall (they keep their `onMessage` "Nothing to restore" copy) — see `docs/plans/a4-settings-proposal.md`.
- `FrnkAppStore.kt` — `FrnkAppStore { AppStore, GooglePlay }` + `expect val frnkAppStore` (GooglePlay on Android, AppStore on iOS): a per-target constant for store-specific copy.
- `FrnkSettingsHandler.kt` — `rememberFrnkSettingsHandler(backStack, entitlements, analytics, onMessage,
  fallback)` returns a `(SettingsEffect) -> Unit` that wires the monetization Settings rows for free:
  `UpgradeToPro` → `backStack.navigateTo(FrnkRootRoute.Paywall)`, `RestorePurchases` → `entitlements.restorePurchases()`,
  `ManageSubscription` → `entitlements.manageSubscriptionsUrl()` opened via `LocalUriHandler`; the
  `GOD_MODE_TOGGLE_ID` toggle → `entitlements.setGodMode(...)`; everything else (theme, other actions)
  goes to `fallback`. `GOD_MODE_TOGGLE_ID` is the stable id a host gives the god-mode `SettingsToggleRow`.
- `ext/EntitlementManagerExt.kt` — the **public** `suspend fun EntitlementManager.manageSubscriptionsUrl():
  String`: the provider's customer-specific management URL, **falling back to
  `platformManageSubscriptionsUrl()`** (the OS subscriptions deep link) when the provider has no URL or
  fails — so a "Manage Subscription" row always lands somewhere useful. The one public seam for hosts
  with their own Settings UI (the handler above uses it too).
- `ManageSubscriptions.kt` (+ `.android.kt`/`.ios.kt`) — `internal expect fun platformManageSubscriptionsUrl():
  String`, the module's only `expect/actual`: the native subscriptions deep link (Google Play on Android,
  App Store on iOS). Returned as a URL so it opens through Compose's `LocalUriHandler` without threading a
  platform `Context`/`UIApplication`.

## Entry points (host pattern)

Two always-on paywall entry points the demo wires (and real hosts copy):
1. **Home top bar** — a top-right `FrnkTopAppBarAction` (crown / `iconUpgrade`), hidden once `isPro`,
   `onActionClick` → `gate.requestUpgrade(...)` / navigate `FrnkRootRoute.Paywall`.
2. **Settings** — the default catalog's Subscription rows (Free: Upgrade + Restore; Pro: "Pro Member"
   badge + Manage Subscription), routed through `rememberFrnkSettingsHandler`.
   God mode lives in the Settings hidden Developer section (reveal: tap the version footer 7×, or the host
   `showDeveloperSection` flag).

## Rules

- **No billing SDK here** — this is UI + the frnk `EntitlementManager`/`FeatureGate` only. RevenueCat stays
  in `:monetization-impl`. Pure Kotlin/Compose, so umbrella XCFrameworks stay clean.
- Reads styling from `Theme[...]` tokens (paywall strings/icons live in `:ui-theme` `FrnkStrings`/
  `FrnkIcons` — `stringAppName`, `stringPaywall*`, `stringProName`, `iconUpgrade`, `iconCheck`).

## Dependencies

- `api(projects.uiScaffolds)`, `api(projects.monetizationApi)`, `implementation(compose-ui-backhandler)` (the hard paywall's back handler) (transitively `:core-nav` for
  `FrnkRootRoute` + the nav3 back-stack helpers, and the nav3 engine via `:ui-scaffolds`). `commonTest`: `kotlin.test` +
  `kotlinx.coroutines.test`. `androidHostTest` (with `isIncludeAndroidResources`): `compose-ui-test` +
  `ui-test-manifest` + `robolectric`, for `NoSubscriptionFoundDialogTest` and `PaywallScreenTest` (which reuses
  the `internal` fakes in `PaywallViewModelTest.kt`) (the module has no `commonDebug`
  preview source set; the dialog's look is previewed through `FrnkDialog`'s neutral preview in `:ui-components`).
- Plugins: compose (+ hosttest). No `kotlin-serialization` — the nav3 route serializers live in `:core-nav`.
