# monetization-api

Pure-interface monetization contract. No RevenueCat, no Play Billing, no StoreKit.

## Contents — the frnk-owned Free/Pro layer (P3-3)

Two layers, so god mode + Pro logic stay independent of any billing SDK:

- `monetization/EntitlementProvider.kt` — the **pluggable billing backend** (RC + the demo fake implement
  it): `isPro: StateFlow<Boolean>` + `refresh()` + `offerings()` + `purchase(id)` + `restore()`, all
  returning `AppResult` (never throw).
- `monetization/EntitlementManager.kt` — the **toolkit's canonical source of truth** feature code reads.
  Wraps a provider and overlays a persisted **god mode** override. `status: StateFlow<EntitlementStatus>`,
  `isPro`, `isGodMode`, `setGodMode(...)`, + delegating `offerings`/`purchase`/`restorePurchases`. Also
  holds `Feature` (opaque feature ids).
- `monetization/DefaultEntitlementManager.kt` — the pure-Kotlin impl. `isPro = provider.isPro || godMode`;
  god mode persisted via `KeyValueStore`'s typed `booleanPreference("frnk.god_mode", default = false)`
  (P4-3 — same key/representation as a raw `putBoolean`); sets analytics user-properties
  `is_pro`/`pro_source`/`god_mode` and emits the purchase funnel events.
- `monetization/EntitlementStatus.kt` — `EntitlementStatus(isPro, source: ProSource{None,Purchase,GodMode})`.
- `monetization/ProProduct.kt` — SDK-free purchasable plan (`ProPlan{Weekly,Monthly,Yearly,Lifetime,Other}`,
  prices, `hasFreeTrial`, `badge`) the paywall renders.
- `monetization/MonetizationError.kt` — typed offerings/purchase/restore failures.
- `monetization/FeatureGate.kt` — gating helper over the manager: `canUse(feature)`, reactive
  `observe(feature)`, `requestUpgrade(source)` (emits `Paywall_Viewed`, returns `PAYWALL_ROUTE_KEY`),
  host-configurable `freeFeatures`.
- `monetization/MonetizationModule.kt` — `monetizationModule` binds `EntitlementManager`
  (`DefaultEntitlementManager`) + `FeatureGate` over whatever `EntitlementProvider` the host installs.
  Requires an `EntitlementProvider` + `KeyValueStore` + `AnalyticsTracker` in the graph.

## Rules

- **No SDK dependencies.** RevenueCat artifacts live in `:monetization-impl`. A new provider
  (Adapty, …) implements `EntitlementProvider` in its own `*-impl` module, installed by the host's
  `initializeFrnk(...)` module list; `monetizationModule` is unchanged.
- **No DI-bootstrap seam here.** Host-facing Koin assembly lives entirely in `:core-di`
  (`initializeFrnk` + `requireFrnkKoin`); the vestigial `di/ToolkitDiModule.kt` expect/actual
  (both actuals returned `emptyList()`) was deleted at restructure Stage 8.
- `api`-exports `:analytics-api` (`AnalyticsTracker`, `AppResult`/error types) **and**
  `:data-prefs-api` (`KeyValueStore` + the typed `Preference` layer, for god-mode persistence in
  `DefaultEntitlementManager` — monetization never touches the SQL driver SPI, so `:data-db-api`
  is deliberately NOT a dependency since the Stage 4 split).
- `koin.core` is on the `api` surface — these types are resolved via Koin at call sites.

## Dependencies

- `api(projects.analyticsApi)`, `api(projects.dataPrefsApi)`, `api(libs.kotlinx.coroutines.core)`,
  `api(libs.koin.core)`. `commonTest`: `kotlin.test` + `kotlinx.coroutines.test`.
