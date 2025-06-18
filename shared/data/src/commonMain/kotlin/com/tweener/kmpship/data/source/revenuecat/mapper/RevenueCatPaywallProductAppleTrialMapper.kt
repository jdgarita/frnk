package com.tweener.kmpship.data.source.revenuecat.mapper

import com.revenuecat.purchases.kmp.models.StoreProductDiscount
import com.tweener.kmpship.domain.entity.PaywallProductTrial
import com.tweener.kmpship.domain.mapper.ModelToEntityMapper

/**
 * @author Vivien Mahe
 * @since 16/01/2025
 */
class RevenueCatPaywallProductAppleTrialMapper(
    private val revenueCatPaywallProductTrialPeriodMapper: RevenueCatPaywallProductTrialPeriodMapper,
) : ModelToEntityMapper<StoreProductDiscount, PaywallProductTrial> {

    override fun convertToEntity(model: StoreProductDiscount): PaywallProductTrial =
        PaywallProductTrial(
            period = revenueCatPaywallProductTrialPeriodMapper.convertToEntity(model.subscriptionPeriod.unit),
            duration = model.subscriptionPeriod.value,
        )
}
