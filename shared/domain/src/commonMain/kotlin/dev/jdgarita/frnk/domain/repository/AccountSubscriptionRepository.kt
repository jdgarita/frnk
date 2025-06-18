package dev.jdgarita.frnk.domain.repository

import dev.jdgarita.frnk.domain.entity.AccountSubscription
import dev.jdgarita.frnk.domain.entity.Paywall
import dev.jdgarita.frnk.domain.entity.SubscriptionDiscount
import kotlinx.coroutines.flow.Flow

/**
 * @author Vivien Mahe
 * @since 16/01/2025
 */
interface AccountSubscriptionRepository {

    sealed class InputParams {
        data class PurchaseProduct(val productId: String) : InputParams()
        data class GetCurrentSubscription(val forceFetch: Boolean) : InputParams()
    }

    sealed class OutputParams {
        data class IsUserSubscribed(val isSubscribed: Boolean) : OutputParams()
        data class GetPaywall(val paywall: Paywall) : OutputParams()
        data class GetCurrentSubscription(val accountSubscription: AccountSubscription) : OutputParams()
        data class WasUserPreviouslySubscribed(val userWasAlreadySubscribed: Boolean) : OutputParams()
        data class FetchDiscounts(val discounts: List<SubscriptionDiscount>) : OutputParams()
    }

    suspend fun isUserSubscribed(): Flow<OutputParams.IsUserSubscribed>

    suspend fun getPaywall(): Result<OutputParams.GetPaywall>

    suspend fun purchaseProduct(inputParams: InputParams.PurchaseProduct): Result<Unit>

    suspend fun restorePurchase(): Result<Unit>

    suspend fun getCurrentSubscription(inputParams: InputParams.GetCurrentSubscription): Result<OutputParams.GetCurrentSubscription>

    suspend fun wasUserPreviouslySubscribed(): OutputParams.WasUserPreviouslySubscribed

    suspend fun fetchDiscounts(): OutputParams.FetchDiscounts
}
