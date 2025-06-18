package com.tweener.kmpship.domain.entity

/**
 * @author Vivien Mahe
 * @since 16/01/2025
 */
data class PaywallProduct(
    val id: String,
    val title: String,
    val amount: Amount,
    val discountAmount: Amount? = null,
    val type: PaywallProductType,
    val period: PaywallProductPeriod,
    val trial: PaywallProductTrial? = null,
)
