package com.tweener.kmpship.data.source.revenuecat.datasource

import com.revenuecat.purchases.kmp.PurchasesDelegate
import com.revenuecat.purchases.kmp.models.CustomerInfo
import com.revenuecat.purchases.kmp.models.PurchasesError
import com.revenuecat.purchases.kmp.models.StoreProduct
import com.revenuecat.purchases.kmp.models.StoreTransaction

/**
 * @author Vivien Mahe
 * @since 17/01/2025
 */
class RevenueCatPurchasesDelegateWrapper(
    private val onCustomerInfoUpdated: (customerInfo: CustomerInfo) -> Unit,
) : PurchasesDelegate {

    override fun onCustomerInfoUpdated(customerInfo: CustomerInfo) {
        onCustomerInfoUpdated.invoke(customerInfo)
    }

    override fun onPurchasePromoProduct(
        product: StoreProduct,
        startPurchase: (onError: (error: PurchasesError, userCancelled: Boolean) -> Unit, onSuccess: (storeTransaction: StoreTransaction, customerInfo: CustomerInfo) -> Unit) -> Unit
    ) {
        // No need to implement this for now
    }
}
