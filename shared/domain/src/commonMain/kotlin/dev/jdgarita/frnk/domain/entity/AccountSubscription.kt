package dev.jdgarita.frnk.domain.entity

import kotlinx.datetime.LocalDateTime

/**
 * @author Vivien Mahe
 * @since 17/01/2025
 */
data class AccountSubscription(
    val id: String,
    val paywallProduct: PaywallProduct,
    val store: AccountSubscriptionStore,
    val isTrial: Boolean,
    val willRenew: Boolean,
    val expirationDate: LocalDateTime? = null, // null for Lifetime access
)

enum class AccountSubscriptionStore {
    APP_STORE,
    GOOGLE_PLAY,
}
