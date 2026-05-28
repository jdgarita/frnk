# shared-monetization-revenuecat

RevenueCat implementation of `:shared-monetization-api`. Bundled inside `:shared` and always registered by `frnkModules(...)` (there is no Firebase-vs-Supabase split for monetization — `BackendChoice` does not affect this module).

## Contents

- `RevenueCatEntitlementManager.kt` — `EntitlementManager` backed by the RevenueCat KMP SDK.
- `RevenueCatModule.kt` — exports `val revenueCatModule = module { ... }`. `:shared/FrnkModules.kt` always adds it.

## iOS native-framework contract (important)

The RevenueCat KMP SDK cinterops with the native `PurchasesHybridCommon.framework`. That framework is **not** vendored inside `FrnkKit.xcframework` — the consumer Xcode project supplies it via CocoaPods or SPM (`pod 'PurchasesHybridCommon'`). `:iosApp` sets `linkerOpts("-undefined", "dynamic_lookup")` precisely to make this module's iOS link succeed without the native dep present locally; the symbols resolve when the consumer app links.

If you change anything that touches this contract — e.g. adding a new cinterop, depending on a different RC SDK module — document it in `docs/ARCHITECTURE.md` so consumers know what extra pod to add.

## Rules

- All RevenueCat imports stay in this module. Feature code (and `:shared-monetization-api`) must not import `com.revenuecat.*`.
- Return `AppResult` from public methods; wrap RC `Error` results into `AppResult.Failure` with a domain `AppError`.

## Dependencies

- `api(projects.sharedMonetizationApi)`.
- `implementation`: `koin.core`, `revenuecat.{core,result,datetime}`.
