package dev.jdgarita.frnk.data.source.revenuecat.mapper

import com.revenuecat.purchases.kmp.models.PeriodUnit
import dev.jdgarita.frnk.domain.entity.PaywallProductTrialPeriod
import dev.jdgarita.frnk.domain.mapper.ModelToEntityMapper

/**
 * @author Vivien Mahe
 * @since 16/01/2025
 */
class RevenueCatPaywallProductTrialPeriodMapper : ModelToEntityMapper<PeriodUnit, PaywallProductTrialPeriod> {

    override fun convertToEntity(model: PeriodUnit): PaywallProductTrialPeriod =
        when (model) {
            PeriodUnit.DAY -> PaywallProductTrialPeriod.DAY
            PeriodUnit.WEEK -> PaywallProductTrialPeriod.WEEK
            PeriodUnit.MONTH -> PaywallProductTrialPeriod.MONTH
            PeriodUnit.YEAR -> PaywallProductTrialPeriod.YEAR
            else -> throw IllegalArgumentException("Unknown period unit: $model")
        }
}
