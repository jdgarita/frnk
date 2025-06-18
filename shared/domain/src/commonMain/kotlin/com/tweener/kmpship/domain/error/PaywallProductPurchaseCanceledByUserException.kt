package com.tweener.kmpship.domain.error

/**
 * @author Vivien Mahe
 * @since 17/01/2025
 */
class PaywallProductPurchaseCanceledByUserException(val id: String) : Exception("Paywall product (ID: $id) purchase canceled by user.")
