package com.tweener.kmpship.data.source.revenuecat.mapper

import com.revenuecat.purchases.kmp.models.ProductType
import com.tweener.kmpship.domain.entity.PaywallProductType
import com.tweener.kmpship.domain.mapper.ModelToEntityMapper

/**
 * @author Vivien Mahe
 * @since 16/01/2025
 */
class RevenueCatPaywallProductTypeMapper : ModelToEntityMapper<ProductType, PaywallProductType> {

    override fun convertToEntity(model: ProductType): PaywallProductType =
        when (model) {
            ProductType.SUBS -> PaywallProductType.SUBSCRIPTION
            ProductType.INAPP -> PaywallProductType.IN_APP_PURCHASE
            else -> throw IllegalArgumentException("Unknown product type: $model")
        }
}
