package com.tweener.kmpship.domain.entity

/**
 * @author Vivien Mahe
 * @since 16/01/2025
 */
data class Paywall(
    val id: String,
    val description: String,
    val weeklyProduct: PaywallProduct?,
    val monthlyProduct: PaywallProduct?,
    val yearlyProduct: PaywallProduct?,
    val lifetimeProduct: PaywallProduct?,
)
