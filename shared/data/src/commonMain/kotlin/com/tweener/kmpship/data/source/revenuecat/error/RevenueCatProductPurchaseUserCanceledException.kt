package com.tweener.kmpship.data.source.revenuecat.error

/**
 * @author Vivien Mahe
 * @since 27/01/2025
 */
class RevenueCatProductPurchaseUserCanceledException(val productId: String) : Exception("The user canceled the purchase of the product with ID: $productId.")
