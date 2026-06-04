package dev.jdgarita.frnk.shared

/**
 * Selects the monetization stack installed by [frnkModules] — a separate axis from [BackendChoice]
 * and [ObservabilityChoice].
 *
 * - [RevenueCat] (default): installs `revenueCatModule` (the RevenueCat-backed `EntitlementProvider`)
 *   + `monetizationModule` (the frnk-owned `EntitlementManager`/`FeatureGate` over it) +
 *   `paywallScaffoldModule` (the paywall VM). This is the batteries-included Free/Pro setup.
 * - [None]: installs **no** monetization bindings. Pick this when the host doesn't monetize, or uses a
 *   different billing provider — then supply your own `EntitlementProvider` implementation (and, if you
 *   want frnk's `EntitlementManager`/paywall over it, the toolkit's `monetizationModule` /
 *   `paywallScaffoldModule`) through `additionalModules`.
 */
enum class MonetizationChoice {
    RevenueCat,
    None,
}
