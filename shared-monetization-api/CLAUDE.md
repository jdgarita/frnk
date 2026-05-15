# shared-monetization-api

Pure-interface monetization contract. No RevenueCat, no Play Billing, no StoreKit.

## Contents

- `monetization/EntitlementManager.kt` — interface for entitlement lookup / restore / purchase flows.
- `monetization/FeatureGate.kt` — small helper for gating a UI region on a given entitlement.
- `di/ToolkitDiModule.kt` (`commonMain`) + `.android.kt` / `.ios.kt` actuals — expect/actual seam for platform DI scaffolding the monetization layer needs (e.g. Android `Context`).

## Rules

- **No SDK dependencies.** The RevenueCat artifacts live in `:shared-monetization-revenuecat`. If you add a new provider (Adapty, RC alt SDK, …), give it its own `*-impl` module and bind it in `:shared`.
- This module `api`-exports `:shared-backend-api` — error types and `AppResult` are reused across domains, so entitlement methods return `AppResult` like everything else.
- `koin.core` is on the `api` surface because `EntitlementManager` etc. are expected to be resolved via Koin at call sites.

## Dependencies

- `api(projects.sharedBackendApi)`, `api(libs.kotlinx.coroutines.core)`, `api(libs.koin.core)`.
