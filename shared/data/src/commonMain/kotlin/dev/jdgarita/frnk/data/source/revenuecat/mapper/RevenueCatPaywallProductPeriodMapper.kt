package dev.jdgarita.frnk.data.source.revenuecat.mapper

import com.revenuecat.purchases.kmp.models.PackageType
import dev.jdgarita.frnk.domain.entity.PaywallProductPeriod
import dev.jdgarita.frnk.domain.mapper.ModelToEntityMapper

/**
 * @author Vivien Mahe
 * @since 16/01/2025
 */
class RevenueCatPaywallProductPeriodMapper : ModelToEntityMapper<PackageType, PaywallProductPeriod> {

    override fun convertToEntity(model: PackageType): PaywallProductPeriod =
        when (model) {
            PackageType.WEEKLY -> PaywallProductPeriod.WEEKLY
            PackageType.MONTHLY -> PaywallProductPeriod.MONTHLY
            PackageType.ANNUAL -> PaywallProductPeriod.YEARLY
            PackageType.LIFETIME -> PaywallProductPeriod.LIFETIME
            else -> throw IllegalArgumentException("Unknown package type: $model")
        }
}
