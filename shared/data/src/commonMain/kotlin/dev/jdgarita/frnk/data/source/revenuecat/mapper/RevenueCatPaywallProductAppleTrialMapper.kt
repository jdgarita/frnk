package dev.jdgarita.frnk.data.source.revenuecat.mapper

import com.revenuecat.purchases.kmp.models.StoreProductDiscount
import dev.jdgarita.frnk.domain.entity.PaywallProductTrial
import dev.jdgarita.frnk.domain.mapper.ModelToEntityMapper

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
