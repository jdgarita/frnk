# monetization-api

Pure-interface monetization contract. No RevenueCat, no Play Billing, no StoreKit.

## Contents — the frnk-owned Free/Pro layer (P3-3)

Two layers, so god mode + Pro logic stay independent of any billing SDK:

- `monetization/EntitlementProvider.kt` — the **pluggable billing backend** (RC + the demo fake implement
  it): `isPro: StateFlow<Boolean>` + `refresh()` + `offerings()` + `purchase(id)` + `restore()`, all
  returning `AppResult` (never throw).
- `monetization/EntitlementManager.kt` — the **toolkit's canonical source of truth** feature code reads.
  Wraps a provider and overlays a persisted **god mode** override. `status: StateFlow<EntitlementStatus>`,
  `isPro`, `isGodMode`, `setGodMode(...)`, + delegating `offerings`/`purchase`/`restorePurchases`.
- `monetization/Feature.kt` — the **open marker** `interface Feature { val id: String }`. Type-safe and
  host-extensible (Tier 3.1): hosts implement it (typically via their own enum:
  `enum class AppFeature(override val id: String) : Feature { … }`), so `Feature("typo")` no longer
  compiles. Mirrors the `NavKey`-marker / `FrnkTabRoute`-catalogue split.
- `monetization/FrnkFeature.kt` — the toolkit's own catalogue: `enum class FrnkFeature(override val id) :
  Feature { Premium, UnlimitedExports, AdFree }` — closed, exhaustive, the shape hosts copy.
- `monetization/DefaultEntitlementManager.kt` — the pure-Kotlin impl. `isPro = provider.isPro || godMode`;
  god mode persisted via `KeyValueStore`'s typed `booleanPreference("frnk.god_mode", default = false)`
  (P4-3 — same key/representation as a raw `putBoolean`); sets analytics user-properties
  `is_pro`/`pro_source`/`god_mode` and emits the purchase funnel events. Construction launches
  `provider.refresh()` in the injected scope so purchased state hydrates on cold launch (a provider
  whose `isPro` starts `false` would otherwise report Free until some paywall interaction fetched
  customer info); the binding stays lazy (NOT `createdAtStart`, which would crash `startKoin` before
  `validateFrnkBootstrap` can explain a missing dep) — the validated path resolves it at bootstrap.
- `monetization/EntitlementStatus.kt` — `EntitlementStatus(isPro, source: ProSource{None,Purchase,GodMode})`.
- `monetization/ProProduct.kt` — SDK-free purchasable plan (`ProPlan{Weekly,Monthly,Yearly,Lifetime,Other}`,
  prices, `hasFreeTrial`, `badge`) the paywall renders.
- `monetization/MonetizationError.kt` — typed offerings/purchase/restore failures.
- `monetization/FeatureGate.kt` — gating helper over the manager: `canUse(feature)`, reactive
  `observe(feature)`, `requestUpgrade(source)` (emits `Paywall_Viewed`, returns `PAYWALL_ROUTE_KEY`),
  host-configurable `freeFeatures` (matched by `Feature.id`, so the check is correct across any `Feature`
  impl). Note: `freeFeatures` is **not** wired through `monetizationModule` today (binds the `emptySet()`
  default) — configuring it requires overriding the `FeatureGate` Koin binding.
- `monetization/usecase/ObserveProStatusUseCase.kt` — the toolkit's **first use case**: an injectable
  `fun interface` returning `StateFlow<Boolean>` so ViewModels read Free/Pro via Koin instead of having
  it threaded down through Compose. `DefaultObserveProStatusUseCase` re-exposes `EntitlementManager.isPro`.
  Consumed by `:ui-scaffolds`' `SettingsViewModel`.
- `monetization/usecase/PaywallPurchaseUseCase.kt` — the paywall's **purchase use case**: an injectable
  `interface` exposing `offerings()`/`purchase(id)`/`restore()` (all `AppResult`, never throw) so
  `PaywallViewModel` runs the billing flow via Koin instead of depending on `EntitlementManager`
  directly. `DefaultPaywallPurchaseUseCase` delegates to `EntitlementManager` (which keeps the analytics
  funnel). Consumed by `:shared-monetization-ui`'s `PaywallViewModel`.
- `monetization/usecase/SyncAuthUseCase.kt` — the **auth-sync use case** (moved in from the Faint host):
  `identify()` ensures the anonymous identity exists (`:identity-api`'s `AnonymousIdentityProvider`)
  and identifies it with the billing backend (`EntitlementManager.identify`), so a request never
  leaves the device with a uid the billing backend doesn't know. Gate-on-sync callers stop on the
  `AppResult.Failure`; best-effort callers (e.g. a bootstrap warmup) ignore the result.
  `DefaultSyncAuthUseCase` chains `ensureSignedIn()` → `identify(uid)`.
- `monetization/MonetizationModule.kt` — `monetizationModule` binds `EntitlementManager`
  (`DefaultEntitlementManager`) + `FeatureGate` + `ObserveProStatusUseCase`
  (`DefaultObserveProStatusUseCase`) + `PaywallPurchaseUseCase` (`DefaultPaywallPurchaseUseCase`) +
  `SyncAuthUseCase` (`DefaultSyncAuthUseCase`) over whatever `EntitlementProvider` the host installs.
  Requires an `EntitlementProvider` + `KeyValueStore` + `AnalyticsTracker` + `AnonymousIdentityProvider`
  in the graph.

## Rules

- **No SDK dependencies.** RevenueCat artifacts live in `:monetization-impl`. A new provider
  (Adapty, …) implements `EntitlementProvider` in its own `*-impl` module, installed by the host's
  `initializeFrnk(...)` module list; `monetizationModule` is unchanged.
- **No DI-bootstrap seam here.** Host-facing Koin assembly lives entirely in `:core-di`
  (`initializeFrnk` + `requireFrnkKoin`); the vestigial `di/ToolkitDiModule.kt` expect/actual
  (both actuals returned `emptyList()`) was deleted at restructure Stage 8.
- `api`-exports `:analytics-api` (`AnalyticsTracker`, `AppResult`/error types), `:data-prefs-api`
  (`KeyValueStore` + the typed `Preference` layer, for god-mode persistence in
  `DefaultEntitlementManager` — monetization never touches the SQL driver SPI, so `:data-db-api`
  is deliberately NOT a dependency since the Stage 4 split), **and** `:identity-api`
  (`AnonymousIdentityProvider`, the identity half of `SyncAuthUseCase`).
- `koin.core` is on the `api` surface — these types are resolved via Koin at call sites.

## Dependencies

- `api(projects.analyticsApi)`, `api(projects.identityApi)`, `api(projects.dataPrefsApi)`,
  `api(libs.kotlinx.coroutines.core)`, `api(libs.koin.core)`. `commonTest`: `kotlin.test` +
  `kotlinx.coroutines.test`.

## Demo

`SyncAuthUseCase` has no dedicated demo surface: it was moved in as-is from the Faint host (where the
scanner and the bootstrap warmup exercise it), and the demo has no backend to sync against. The demo
graph stays resolvable — `:demo-shared`'s `frnkAppModule` binds a `FakeAnonymousIdentityProvider`
alongside its `FakeEntitlementProvider` — and the behavior is covered by
`DefaultSyncAuthUseCaseTest` in `commonTest`.
