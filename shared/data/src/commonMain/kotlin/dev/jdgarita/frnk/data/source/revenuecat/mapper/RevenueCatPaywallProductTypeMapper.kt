package dev.jdgarita.frnk.data.source.revenuecat.mapper

import com.revenuecat.purchases.kmp.models.ProductType
import dev.jdgarita.frnk.domain.entity.PaywallProductType
import dev.jdgarita.frnk.domain.mapper.ModelToEntityMapper

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
