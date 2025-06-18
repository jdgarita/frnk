package com.tweener.kmpship.data.repository

import com.tweener.kmpkit.kotlinextensions.fromEpochMilliseconds
import com.tweener.kmpship.data.source.firebase.firestore.datasource.FirestoreSubscriptionsDiscountDataSource
import com.tweener.kmpship.data.source.firebase.firestore.mapper.FirestoreSubscriptionsDiscountModelMapper
import com.tweener.kmpship.data.source.revenuecat.datasource.RevenueCatDataSource
import com.tweener.kmpship.data.source.revenuecat.error.RevenueCatProductPurchaseUserCanceledException
import com.tweener.kmpship.data.source.revenuecat.mapper.RevenueCatAccountSubscriptionMapper
import com.tweener.kmpship.data.source.revenuecat.mapper.RevenueCatAccountSubscriptionMapperData
import com.tweener.kmpship.data.source.revenuecat.mapper.RevenueCatPaywallProductMapper
import com.tweener.kmpship.domain.entity.Paywall
import com.tweener.kmpship.domain.error.NoCurrentActiveSubscriptionException
import com.tweener.kmpship.domain.error.PaywallNoPurchaseToRestoreException
import com.tweener.kmpship.domain.error.PaywallNotFoundException
import com.tweener.kmpship.domain.error.PaywallProductNotFoundException
import com.tweener.kmpship.domain.error.PaywallProductPurchaseCanceledByUserException
import com.tweener.kmpship.domain.error.PaywallProductPurchaseException
import com.tweener.kmpship.domain.repository.AccountSubscriptionRepository
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.TimeZone

/**
 * @author Vivien Mahe
 * @since 16/01/2025
 */
class AccountSubscriptionRepositoryImpl(
    private val timeZone: TimeZone,
    private val revenueCatPaywallProductMapper: RevenueCatPaywallProductMapper,
    private val revenueCatAccountSubscriptionMapper: RevenueCatAccountSubscriptionMapper,
    private val firestoreSubscriptionsDiscountModelMapper: FirestoreSubscriptionsDiscountModelMapper,
    private val revenueCatDataSource: RevenueCatDataSource,
    private val firestoreSubscriptionsDiscountDataSource: FirestoreSubscriptionsDiscountDataSource,
) : AccountSubscriptionRepository {

    override suspend fun isUserSubscribed(): Flow<AccountSubscriptionRepository.OutputParams.IsUserSubscribed> =
        revenueCatDataSource
            .isUserSubscribedAsFlow()
            .map { isSubscribed -> AccountSubscriptionRepository.OutputParams.IsUserSubscribed(isSubscribed = isSubscribed) }

    override suspend fun getPaywall(): Result<AccountSubscriptionRepository.OutputParams.GetPaywall> =
        revenueCatDataSource.getCurrentOffering()?.let { offering ->
            val weeklyProduct = offering.weekly?.let { runCatching { revenueCatPaywallProductMapper.convertToEntity(it) }.getOrNull() }
            val monthlyProduct = offering.monthly?.let { runCatching { revenueCatPaywallProductMapper.convertToEntity(it) }.getOrNull() }
            val yearlyProduct = offering.annual?.let { runCatching { revenueCatPaywallProductMapper.convertToEntity(it) }.getOrNull() }
            val lifetimeProduct = offering.lifetime?.let { runCatching { revenueCatPaywallProductMapper.convertToEntity(it) }.getOrNull() }

            val paywall = Paywall(
                id = offering.identifier,
                description = offering.serverDescription,
                weeklyProduct = weeklyProduct,
                monthlyProduct = monthlyProduct,
                yearlyProduct = yearlyProduct,
                lifetimeProduct = lifetimeProduct,
            )

            val outputParams = AccountSubscriptionRepository.OutputParams.GetPaywall(paywall = paywall)

            Result.success(outputParams)
        } ?: Result.failure(PaywallNotFoundException())

    override suspend fun purchaseProduct(inputParams: AccountSubscriptionRepository.InputParams.PurchaseProduct): Result<Unit> =
        revenueCatDataSource
            .purchaseProduct(productId = inputParams.productId)
            .fold(
                onSuccess = { Result.success(Unit) },
                onFailure = {
                    val exception = when (it) {
                        is NoSuchElementException -> PaywallProductNotFoundException(id = inputParams.productId)
                        is RevenueCatProductPurchaseUserCanceledException -> PaywallProductPurchaseCanceledByUserException(id = inputParams.productId)
                        else -> PaywallProductPurchaseException(id = inputParams.productId, errorMessage = it.message)
                    }

                    Result.failure(exception)
                }
            )

    override suspend fun restorePurchase(): Result<Unit> =
        revenueCatDataSource
            .restorePurchase()
            .fold(
                onSuccess = {
                    when (revenueCatDataSource.isUserSubscribed()) {
                        true -> Result.success(Unit)
                        false -> Result.failure(NoCurrentActiveSubscriptionException())
                    }
                },
                onFailure = { Result.failure(PaywallNoPurchaseToRestoreException(errorMessage = it.message)) }
            )

    override suspend fun getCurrentSubscription(inputParams: AccountSubscriptionRepository.InputParams.GetCurrentSubscription): Result<AccountSubscriptionRepository.OutputParams.GetCurrentSubscription> =
        revenueCatDataSource
            .getCurrentSubscription(forceFetch = inputParams.forceFetch)
            ?.let { currentSubscription ->
                revenueCatDataSource
                    .getCurrentPackageFromSubscription(subscription = currentSubscription)
                    ?.let {
                        runCatching {
                            revenueCatAccountSubscriptionMapper.convertToEntity(RevenueCatAccountSubscriptionMapperData(entitlementInfo = currentSubscription, currentPackage = it))
                        }.getOrNull()
                    }
                    ?.let { accountSubscription -> AccountSubscriptionRepository.OutputParams.GetCurrentSubscription(accountSubscription = accountSubscription) }
                    ?.let { Result.success(it) }
                    ?: Result.failure(NoCurrentActiveSubscriptionException())
            }
            ?: Result.failure(NoCurrentActiveSubscriptionException())

    override suspend fun wasUserPreviouslySubscribed(): AccountSubscriptionRepository.OutputParams.WasUserPreviouslySubscribed {
        val isUserSubscribed = revenueCatDataSource.isUserSubscribed()
        val latestExpirationDate = revenueCatDataSource.getLastExpirationDateInMillis()?.fromEpochMilliseconds(timeZone = timeZone)

        // A user was previously subscribed if he is not currently subscribed but has a past subscription
        val wasUserPreviouslySubscribed = isUserSubscribed.not() && latestExpirationDate != null

        return AccountSubscriptionRepository.OutputParams.WasUserPreviouslySubscribed(userWasAlreadySubscribed = wasUserPreviouslySubscribed)
    }

    override suspend fun fetchDiscounts(): AccountSubscriptionRepository.OutputParams.FetchDiscounts {
        Napier.d { "${this::class.simpleName}#fetchDiscounts()" }

        return firestoreSubscriptionsDiscountDataSource
            .getAllDiscounts()
            .mapNotNull { runCatching { firestoreSubscriptionsDiscountModelMapper.convertToEntity(it) }.getOrNull() }
            .let { AccountSubscriptionRepository.OutputParams.FetchDiscounts(discounts = it) }
    }
}
