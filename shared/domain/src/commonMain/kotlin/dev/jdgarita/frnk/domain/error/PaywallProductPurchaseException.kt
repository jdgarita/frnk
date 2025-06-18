package dev.jdgarita.frnk.domain.error

/**
 * @author Vivien Mahe
 * @since 17/01/2025
 */
class PaywallProductPurchaseException(val id: String, val errorMessage: String? = null) : Error("Failed to purchase paywall product (ID: $id): $errorMessage")
