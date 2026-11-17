package dev.jdgarita.frnk.monetization

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.time.Instant

/**
 * What an [EntitlementProvider] knows about the active **purchased** Pro entitlement, beyond
 * `isPro`: whether it is a free trial, when it expires (`null` for lifetime) and whether the store
 * will renew it. [None] when there is no active Pro entitlement.
 */
data class ProEntitlementDetails(
    val isTrial: Boolean = false,
    val expiresAt: Instant? = null,
    val willRenew: Boolean = false
) {
    companion object {
        val None = ProEntitlementDetails()

        /** Shared constant flow behind [EntitlementProvider.proDetails]'s default. */
        internal val NoneFlow: StateFlow<ProEntitlementDetails> = MutableStateFlow(None).asStateFlow()
    }
}