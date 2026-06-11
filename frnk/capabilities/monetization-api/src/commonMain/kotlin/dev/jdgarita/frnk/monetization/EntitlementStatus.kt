package dev.jdgarita.frnk.monetization

/** Where a user's Pro access comes from. [GodMode] is a local override (testing), independent of the provider. */
enum class ProSource { None, Purchase, GodMode }

/** The toolkit's canonical Free-vs-Pro snapshot. A trial user is simply [isPro] = true with [ProSource.Purchase]. */
data class EntitlementStatus(
    val isPro: Boolean,
    val source: ProSource,
) {
    companion object {
        val Free = EntitlementStatus(isPro = false, source = ProSource.None)
    }
}
