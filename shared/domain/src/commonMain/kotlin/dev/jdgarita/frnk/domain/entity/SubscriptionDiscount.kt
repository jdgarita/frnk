package dev.jdgarita.frnk.domain.entity

/**
 * @author Vivien Mahe
 * @since 06/02/2025
 */
data class SubscriptionDiscount(
    val id: String,
    val active: Boolean,
    val percent: Double,
    val period: PaywallProductPeriod,
)
