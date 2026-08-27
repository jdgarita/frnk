# monetization-impl

RevenueCat implementation of `:monetization-api`. Installed at runtime by passing `revenueCatModule` (with `monetizationModule` + `paywallScaffoldModule`) to `initializeFrnk(modules = …)`.

## Contents

- `RevenueCatEntitlementProvider.kt` — **`EntitlementProvider`** backed by the RevenueCat KMP SDK
  (BACKLOG P3-2/P3-3). Reactive `isPro` (a `PurchasesDelegate` pushes background customer-info updates;
  `refresh()` reads `awaitCustomerInfoResult()`); `offerings()` maps `Offerings.current` packages →
  `ProProduct` (`PackageType`→`ProPlan`, `Price.formatted`/`pricePerMonth`, trial via `introductoryDiscount`,
  a "Save N%" yearly badge); `purchase(id)` resolves the cached `Package` and runs `awaitPurchaseResult(pkg)`;
  `restore()` reads `awaitRestoreResult()` and `syncPurchases()` reads `awaitSyncPurchasesResult()` (the
  silent, no-store-UI receipt sync) — both compute the returned `isPro` from the call's **own**
  `CustomerInfo`, not `_isPro.value`, so a pre-existing Pro state (e.g. god mode) can't masquerade as a
  successful restore. RC failures map through the pure `monetizationErrorFor(code, userCancelled)`
  (exported SDK-statics-free like `isProFor(...)`, unit-testable without `Purchases.sharedInstance`):
  cancellation → `UserCancelled`, `ProductAlreadyPurchasedError`/`ReceiptAlreadyInUseError` →
  `AlreadyOwned` (so the paywall can fall through to a restore), `NetworkError` → `NetworkUnavailable`,
  else `Unknown`. **It does NOT own the Free/Pro logic or god mode** — that's
  `DefaultEntitlementManager` (`:monetization-api`), which wraps this provider.
- `RevenueCatConfig.kt` — `data class RevenueCatConfig(proEntitlementId = "pro", paywallFallback, savingsBadgeTemplate)`.
  The entitlement identifier that means "Pro" (`customerInfo.entitlements[proEntitlementId]?.isActive == true`).
  Hosts whose dashboard uses a different id override `single { RevenueCatConfig(...) }` via Koin. Dashboard
  display name should be `"<App> Pro"`. `paywallFallback` (`suspend () -> ProMetadata`) and
  `savingsBadgeTemplate` (`suspend () -> String`, applied via literal `replace("%1$d", …)`) are **suspend
  providers resolved per call** so hosts localize them from suspending resource APIs against the current
  locale — never bake one language in at DI time.
- `ResolvePaywallMetadata.kt` — the pure, SDK-free paywall-metadata resolver (exported for tests like
  `isProFor`). Offering metadata schema is **additive**: flat `title`/`subtitle`/`benefits[{key,value}]`
  are the canonical copy (what older clients read — never restructure them), and an optional
  `localizations` object keyed by language code carries per-locale overrides. The device's
  `platformLanguageTag()` matches a key exact-tag-first, then by primary-language prefix (`es-MX` → `es`);
  each field resolves locale override → flat key → `paywallFallback`, benefits at whole-list granularity,
  malformed nodes degrading a tier instead of throwing. Pinned by `ResolvePaywallMetadataTest`.
- `RevenueCatModule.kt` — exports `val revenueCatModule = module { ... }` binding **`EntitlementProvider`**
  (+ `RevenueCatConfig`) **only**; `EntitlementManager` + `FeatureGate` come from `monetizationModule`.
  The host's `initializeFrnk(...)` module list installs both.

## Configuration is the host's job (not the toolkit's)

The toolkit **never** calls `Purchases.configure(...)`. The host configures RevenueCat — platform
context + public SDK key + the native iOS pod — before the gate is used (Android captures the context
automatically via RevenueCat's `androidx.startup` initializer, so `Purchases.configure(apiKey)` in
`Application.onCreate` is enough). Every SDK access in the manager is wrapped in the private `sdkCall`
helper (a `runCatching`-alike that rethrows `CancellationException` so cancelled callers aren't handed
a bogus `StoreUnavailable`), so an
**unconfigured** `Purchases.sharedInstance` degrades to a safe no-op (`isPro` stays `false`) instead of
throwing — the same defensive pattern `FirebaseCrashReporter` uses. The manager also won't clobber a
`PurchasesDelegate` the host already set (such a host should call `refresh()` after entitlement changes).
`demo-android` is the real-path smoke test: it calls `Purchases.configure(...)` then overrides the
demo's fake with `revenueCatModule` via Koin `allowOverride(true)` when a key is present in
`local.properties` (`REVENUECAT_ANDROID_API_KEY`).

## iOS native-framework contract (important)

The RevenueCat KMP SDK cinterops with the native purchases-ios SDK. That framework is **not** vendored inside any toolkit framework — the consumer Xcode project supplies it via SPM/CocoaPods. Umbrella XCFrameworks bundling this module (DemoKit, host frameworks) set `linkerOpts("-undefined", "dynamic_lookup")` precisely to make the iOS link succeed without the native dep present locally; the symbols resolve when the consumer app links.

If you change anything that touches this contract — e.g. adding a new cinterop, depending on a different RC SDK module — document it in `docs/ARCHITECTURE.md` so consumers know what extra pod to add.

## Rules

- All RevenueCat imports stay in this module. Feature code (and `:monetization-api`) must not import `com.revenuecat.*`.
- Return `AppResult` from public methods; wrap RC `Error` results into `AppResult.Failure` with a domain `AppError`.

## Consumer checklist (iOS)

1. Add the native `PurchasesHybridCommon` framework (CocoaPods `pod 'PurchasesHybridCommon'` or SPM).
2. Call `Purchases.configure(apiKey:)` (RevenueCat's native iOS SDK or the KMP `configure`) on launch,
   before using `FeatureGate`.
3. Upload dSYMs as usual. The umbrella XCFramework's `linkerOpts("-undefined", "dynamic_lookup")` is
   what lets it link without the pod present locally; the consumer's link step resolves it.

## Dependencies

- `api(projects.monetizationApi)`.
- `implementation`: `koin.core`, `revenuecat.{core,result,datetime}`.
- `commonTest`: `kotlin.test` (host tests opted in via `kotlin { android { withHostTest {} } }`;
  run with `./gradlew :monetization-impl:testAndroidHostTest`).
