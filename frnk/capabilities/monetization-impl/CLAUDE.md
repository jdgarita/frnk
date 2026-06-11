# shared-monetization-revenuecat

RevenueCat implementation of `:shared-monetization-api`. Installed at runtime by passing `revenueCatModule` (with `monetizationModule` + `paywallScaffoldModule`) to `initializeFrnk(modules = …)`.

## Contents

- `RevenueCatEntitlementProvider.kt` — **`EntitlementProvider`** backed by the RevenueCat KMP SDK
  (BACKLOG P3-2/P3-3). Reactive `isPro` (a `PurchasesDelegate` pushes background customer-info updates;
  `refresh()` reads `awaitCustomerInfoResult()`); `offerings()` maps `Offerings.current` packages →
  `ProProduct` (`PackageType`→`ProPlan`, `Price.formatted`/`pricePerMonth`, trial via `introductoryDiscount`,
  a "Save N%" yearly badge); `purchase(id)` resolves the cached `Package` and runs `awaitPurchaseResult(pkg)`,
  mapping `PurchasesTransactionException.userCancelled` → `MonetizationError.UserCancelled`; `restore()` reads
  `awaitRestoreResult()`. Exports the pure, SDK-free `isProFor(...)` mapper (unit-testable without the static
  `Purchases.sharedInstance`). **It does NOT own the Free/Pro logic or god mode** — that's
  `DefaultEntitlementManager` (`shared-monetization-api`), which wraps this provider.
- `RevenueCatConfig.kt` — `data class RevenueCatConfig(proEntitlementId = "pro")`. The entitlement
  identifier that means "Pro" (`customerInfo.entitlements[proEntitlementId]?.isActive == true`). Hosts whose
  dashboard uses a different id override `single { RevenueCatConfig(...) }` via Koin. Dashboard display name
  should be `"<App> Pro"`.
- `RevenueCatModule.kt` — exports `val revenueCatModule = module { ... }` binding **`EntitlementProvider`**
  (+ `RevenueCatConfig`) **only**; `EntitlementManager` + `FeatureGate` come from `monetizationModule`.
  `:shared/FrnkModules.kt` adds both.

## Configuration is the host's job (not the toolkit's)

The toolkit **never** calls `Purchases.configure(...)`. The host configures RevenueCat — platform
context + public SDK key + the native iOS pod — before the gate is used (Android captures the context
automatically via RevenueCat's `androidx.startup` initializer, so `Purchases.configure(apiKey)` in
`Application.onCreate` is enough). Every SDK access in the manager is wrapped in `runCatching`, so an
**unconfigured** `Purchases.sharedInstance` degrades to a safe no-op (`isPro` stays `false`) instead of
throwing — the same defensive pattern `FirebaseCrashReporter` uses. The manager also won't clobber a
`PurchasesDelegate` the host already set (such a host should call `refresh()` after entitlement changes).
`androidDemoApp` is the real-path smoke test: it calls `Purchases.configure(...)` then overrides the
demo's fake with `revenueCatModule` via Koin `allowOverride(true)` when a key is present in
`local.properties` (`REVENUECAT_ANDROID_API_KEY`).

## iOS native-framework contract (important)

The RevenueCat KMP SDK cinterops with the native purchases-ios SDK. That framework is **not** vendored inside any toolkit framework — the consumer Xcode project supplies it via SPM/CocoaPods. Umbrella XCFrameworks bundling this module (DemoKit, host frameworks) set `linkerOpts("-undefined", "dynamic_lookup")` precisely to make the iOS link succeed without the native dep present locally; the symbols resolve when the consumer app links.

If you change anything that touches this contract — e.g. adding a new cinterop, depending on a different RC SDK module — document it in `docs/ARCHITECTURE.md` so consumers know what extra pod to add.

## Rules

- All RevenueCat imports stay in this module. Feature code (and `:shared-monetization-api`) must not import `com.revenuecat.*`.
- Return `AppResult` from public methods; wrap RC `Error` results into `AppResult.Failure` with a domain `AppError`.

## Consumer checklist (iOS)

1. Add the native `PurchasesHybridCommon` framework (CocoaPods `pod 'PurchasesHybridCommon'` or SPM).
2. Call `Purchases.configure(apiKey:)` (RevenueCat's native iOS SDK or the KMP `configure`) on launch,
   before using `FeatureGate`.
3. Upload dSYMs as usual. `:iosApp`'s `linkerOpts("-undefined", "dynamic_lookup")` is what lets the
   toolkit's XCFramework link without the pod present locally; the consumer's link step resolves it.

## Dependencies

- `api(projects.sharedMonetizationApi)`.
- `implementation`: `koin.core`, `revenuecat.{core,result,datetime}`.
- `commonTest`: `kotlin.test` (host tests opted in via `kotlin { android { withHostTest {} } }`;
  run with `./gradlew :shared-monetization-revenuecat:testAndroidHostTest`).
