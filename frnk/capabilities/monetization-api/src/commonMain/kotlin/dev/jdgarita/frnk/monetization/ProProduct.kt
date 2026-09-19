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
 * @param price the same total as [priceFormatted], as a number with its currency — for analytics
 * and revenue reporting, never for display. `null` when the provider has no store price (a fake
 * provider, or a product the store answered without one).
 */
data class ProProduct(
    val id: String,
    val plan: ProPlan,
    val title: String,
    val priceFormatted: String,
    val pricePerMonthFormatted: String? = null,
    val hasFreeTrial: Boolean = false,
    val badge: String? = null,
    val price: ProPrice? = null
)

/**
 * A store price as a number: what [ProProduct.priceFormatted] renders, in a shape an analytics
 * event can carry as `value` + `currency`.
 *
 * @param amountMicros the total in micro-units of [currencyCode] (1,000,000 = one unit), the
 * store SDKs' lossless representation — `39_990_000` for "$39.99".
 * @param currencyCode ISO 4217 code of the storefront's currency, e.g. `USD`.
 */
data class ProPrice(
    val amountMicros: Long,
    val currencyCode: String
) {
    /** [amountMicros] in whole currency units — `39.99` for "$39.99". The shape Firebase's `value` takes. */
    val amount: Double get() = amountMicros / MICROS_PER_UNIT

    private companion object {
        const val MICROS_PER_UNIT = 1_000_000.0
    }
}

data class ProBenefit(
    val key: String,
    val value: String
)

data class ProMetadata(
    val title: String,
    val subtitle: String,
    val benefits: List<ProBenefit>
) {
    companion object {
        val DUMMY =
            ProMetadata(
                title = "title",
                subtitle = "subtitle",
                benefits = emptyList()
            )

        /**
         * Brand-free English fallback for when the provider supplies no paywall metadata (offline,
         * or a dashboard without the `title`/`subtitle`/`benefits` entries). Hosts should inject
         * their own localized copy instead of shipping this (RevenueCat hosts via
         * `RevenueCatConfig.paywallFallback`).
         */
        val GENERIC =
            ProMetadata(
                title = "Pro",
                subtitle = "Unlock the full experience.",
                benefits = emptyList()
            )
    }
}