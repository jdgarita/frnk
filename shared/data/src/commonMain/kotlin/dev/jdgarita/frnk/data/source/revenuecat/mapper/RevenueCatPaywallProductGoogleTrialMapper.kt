package dev.jdgarita.frnk.data.source.revenuecat.mapper

import com.revenuecat.purchases.kmp.models.SubscriptionOption
import dev.jdgarita.frnk.domain.entity.PaywallProductTrial
import dev.jdgarita.frnk.domain.mapper.ModelToEntityMapper

/**
 * @author Vivien Mahe
 * @since 16/01/2025
 */
class RevenueCatPaywallProductGoogleTrialMapper(
    private val revenueCatPaywallProductTrialPeriodMapper: RevenueCatPaywallProductTrialPeriodMapper,
) : ModelToEntityMapper<SubscriptionOption, PaywallProductTrial> {

    override fun convertToEntity(model: SubscriptionOption): PaywallProductTrial {
        requireNotNull(model.pricingPhases.firstOrNull()?.billingPeriod) { "SubscriptionOption must contain as least one PricingPhase!" }

        val billPeriod = model.pricingPhases.firstOrNull()!!.billingPeriod

        return PaywallProductTrial(
            period = revenueCatPaywallProductTrialPeriodMapper.convertToEntity(billPeriod.unit),
            duration = billPeriod.value,
        )
    }
}
