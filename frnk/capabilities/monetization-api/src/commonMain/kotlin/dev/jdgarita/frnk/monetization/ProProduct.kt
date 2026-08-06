package dev.jdgarita.frnk.monetization

/** Billing period of a purchasable Pro plan, normalized from the provider's package type. */
enum class ProPlan { Weekly, Monthly, Yearly, Lifetime, Other }

/**
 * A purchasable Pro plan, mapped from the provider (RevenueCat `Package` + `StoreProduct`) into an
 * SDK-free shape the paywall renders.
 *
 * @param id provider package identifier (passed back to [EntitlementManager.purchase]).
 * @param priceFormatted localized total price, e.g. "$39.99".
 * @param pricePerMonthFormatted localized per-month price for comparison, e.g. "$3.33" (null for lifetime).
 * @param hasFreeTrial whether the product has an introductory free trial.
 * @param badge optional short marketing badge, e.g. "BEST VALUE" / "Save 33%".
 */
data class ProProduct(
    val id: String,
    val plan: ProPlan,
    val title: String,
    val priceFormatted: String,
    val pricePerMonthFormatted: String? = null,
    val hasFreeTrial: Boolean = false,
    val badge: String? = null
)

data class ProBenefit(
    val key: String,
    val value: String
)

data class ProMetadata(
    val title: String,
    val subtitle: String,
    val benefits: List<ProBenefit>
)