package com.tweener.kmpship.data.source.revenuecat.mapper

import com.revenuecat.purchases.kmp.models.EntitlementInfo
import com.revenuecat.purchases.kmp.models.Package
import com.revenuecat.purchases.kmp.models.PeriodType
import com.tweener.kmpkit.kotlinextensions.fromEpochMilliseconds
import com.tweener.kmpship.domain.entity.AccountSubscription
import com.tweener.kmpship.domain.mapper.ModelToEntityMapper
import kotlinx.datetime.TimeZone

/**
 * @author Vivien Mahe
 * @since 17/01/2025
 */

data class RevenueCatAccountSubscriptionMapperData(
    val entitlementInfo: EntitlementInfo,
    val currentPackage: Package,
)

class RevenueCatAccountSubscriptionMapper(
    private val timeZone: TimeZone,
    private val revenueCatPaywallProductMapper: RevenueCatPaywallProductMapper,
    private val revenueCatAccountSubscriptionStoreMapper: RevenueCatAccountSubscriptionStoreMapper,
) : ModelToEntityMapper<RevenueCatAccountSubscriptionMapperData, AccountSubscription> {

    override fun convertToEntity(model: RevenueCatAccountSubscriptionMapperData): AccountSubscription {
        return AccountSubscription(
            id = model.entitlementInfo.identifier,
            paywallProduct = revenueCatPaywallProductMapper.convertToEntity(model.currentPackage),
            store = revenueCatAccountSubscriptionStoreMapper.convertToEntity(model.entitlementInfo.store),
            isTrial = model.entitlementInfo.periodType == PeriodType.TRIAL,
            willRenew = model.entitlementInfo.willRenew,
            expirationDate = model.entitlementInfo.expirationDateMillis?.fromEpochMilliseconds(timeZone = timeZone),
        )
    }
}
