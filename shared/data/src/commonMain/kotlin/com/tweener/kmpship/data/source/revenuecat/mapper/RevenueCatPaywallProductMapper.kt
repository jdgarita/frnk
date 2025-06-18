package com.tweener.kmpship.data.source.revenuecat.mapper

import com.revenuecat.purchases.kmp.models.Package
import com.tweener.kmpkit.Platform
import com.tweener.kmpkit.currentPlatform
import com.tweener.kmpship.domain.entity.Amount
import com.tweener.kmpship.domain.entity.PaywallProduct
import com.tweener.kmpship.domain.mapper.ModelToEntityMapper

/**
 * @author Vivien Mahe
 * @since 16/01/2025
 */
class RevenueCatPaywallProductMapper(
    private val revenueCatPaywallProductTypeMapper: RevenueCatPaywallProductTypeMapper,
    private val revenueCatPaywallProductPeriodMapper: RevenueCatPaywallProductPeriodMapper,
    private val revenueCatPaywallProductGoogleTrialMapper: RevenueCatPaywallProductGoogleTrialMapper,
    private val revenueCatPaywallProductAppleTrialMapper: RevenueCatPaywallProductAppleTrialMapper,
) : ModelToEntityMapper<Package, PaywallProduct> {

    override fun convertToEntity(model: Package): PaywallProduct {
        val storeProduct = model.storeProduct

        val amount = Amount(value = storeProduct.price.amountMicros.toDouble() / 1_000_000, currency = storeProduct.price.currencyCode)

        val discountAmount = storeProduct.subscriptionOptions?.defaultOffer?.pricingPhases?.firstOrNull()?.price?.let { price ->
            Amount(value = price.amountMicros.toDouble() / 1_000_000, currency = price.currencyCode)
        }

        val googleTrial = storeProduct.subscriptionOptions?.freeTrial?.let { revenueCatPaywallProductGoogleTrialMapper.convertToEntity(it) }
        val appleTrial = storeProduct.introductoryDiscount?.let { revenueCatPaywallProductAppleTrialMapper.convertToEntity(it) }

        // Match the right trial with the current platform
        val trial = when (currentPlatform) {
            Platform.ANDROID -> googleTrial
            Platform.IOS -> appleTrial
            else -> null
        }

        return PaywallProduct(
            id = model.identifier,
            title = storeProduct.title,
            amount = amount,
            discountAmount = discountAmount,
            type = revenueCatPaywallProductTypeMapper.convertToEntity(storeProduct.type),
            period = revenueCatPaywallProductPeriodMapper.convertToEntity(model.packageType),
            trial = trial,
        )
    }
}
