package dev.jdgarita.frnk.monetization

import kotlinx.coroutines.flow.StateFlow

/** Single source of truth for Free vs Pro across the toolkit. */
interface EntitlementManager {
    val isPro: StateFlow<Boolean>
    suspend fun refresh()
    suspend fun restorePurchases(): Boolean
}

/** Stable, opaque feature identifiers. Host apps add their own as enum-like constants. */
data class Feature(val id: String) {
    companion object {
        val Premium = Feature("premium")
        val UnlimitedExports = Feature("unlimited_exports")
        val AdFree = Feature("ad_free")
    }
}
