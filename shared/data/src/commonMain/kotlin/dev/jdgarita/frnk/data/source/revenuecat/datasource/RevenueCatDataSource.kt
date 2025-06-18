package dev.jdgarita.frnk.data.source.revenuecat.datasource

import com.revenuecat.purchases.kmp.Purchases
import com.revenuecat.purchases.kmp.models.CacheFetchPolicy
import com.revenuecat.purchases.kmp.models.CustomerInfo
import com.revenuecat.purchases.kmp.models.EntitlementInfo
import com.revenuecat.purchases.kmp.models.Offering
import com.revenuecat.purchases.kmp.models.Package
import com.tweener.kmpkit.thread.resumeIfActive
import com.tweener.kmpkit.thread.resumeWithExceptionIfActive
import dev.jdgarita.frnk.data.source.revenuecat.error.RevenueCatProductPurchaseUserCanceledException
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine

/**
 * @author Vivien Mahe
 * @since 15/01/2025
 */
class RevenueCatDataSource(
    private val purchases: Purchases,
) {

    private val scope = CoroutineScope(Dispatchers.IO)
    private val _customerInfo = MutableSharedFlow<CustomerInfo>(replay = 1)

    init {
        purchases.delegate = RevenueCatPurchasesDelegateWrapper(onCustomerInfoUpdated = { customerInfo ->
            Napier.d { "RevenueCat customer info updated! appUserID: ${customerInfo.originalAppUserId}" }

            scope.launch {
                _customerInfo.emit(customerInfo)
            }
        })
    }

    /**
     * Logs in the user to RevenueCat.
     *
     * @param userId The user ID to log in.
     * @param email The email of the user.
     * @param deviceVersion The version of the device.
     */
    fun logIn(userId: String, email: String, deviceVersion: String) {
        // Only login with a new userId if the current userId is different
        if (purchases.appUserID != userId) {
            purchases.logIn(
                newAppUserID = userId,
                onSuccess = { customerInfo, created -> Napier.d { "RevenueCat user logged in! user created: $created <> appUserID: ${customerInfo.originalAppUserId}" } },
                onError = { Napier.e { "RevenueCat user log in error: $it" } },
            )
        }

        purchases.setAttributes(
            mapOf(
                "\$email" to email,
                "\$deviceVersion" to deviceVersion,
            ),
        )
    }

    /**
     * Logs out the user from RevenueCat.
     */
    fun logOut() {
        purchases.logOut(
            onSuccess = { Napier.d { "RevenueCat user logged out!" } },
            onError = { Napier.e { "RevenueCat user log out error: $it" } },
        )
    }

    /**
     * Fetches the current subscription status, if any.
     */
    suspend fun getCurrentSubscription(forceFetch: Boolean): EntitlementInfo? = suspendCancellableCoroutine { continuation ->
        try {
            purchases.getCustomerInfo(
                fetchPolicy = if (forceFetch) CacheFetchPolicy.FETCH_CURRENT else CacheFetchPolicy.default(),
                onSuccess = { customerInfo ->
                    val activeSubscription = customerInfo.entitlements.active.values.firstOrNull()
                    Napier.d { "RevenueCat current subscription updated! activeSubscription? ${activeSubscription?.getFullProductId()}" }
                    continuation.resumeIfActive(activeSubscription)
                },
                onError = {
                    Napier.e { "RevenueCat current subscription error: $it" }
                    continuation.resumeWithExceptionIfActive(Throwable(it.message))
                },
            )
        } catch (throwable: Throwable) {
            Napier.e(throwable) { "RevenueCat current subscription error." }
            continuation.resumeWithExceptionIfActive(throwable)
        }
    }

    /**
     * Fetches the user's subscription status as a flow.
     */
    fun isUserSubscribedAsFlow(): Flow<Boolean> =
        _customerInfo.map {
            val isSubscribed = it.entitlements.active.isNotEmpty()
            Napier.d { "RevenueCat user subscription updated! isSubscribed: $isSubscribed" }
            isSubscribed
        }

    suspend fun isUserSubscribed(): Boolean = suspendCancellableCoroutine { continuation ->
        purchases.getCustomerInfo(
            onSuccess = { customerInfo ->
                val isSubscribed = customerInfo.entitlements.active.isNotEmpty()
                Napier.d { "RevenueCat isSubscribed? $isSubscribed" }
                continuation.resumeIfActive(isSubscribed)
            },
            onError = {
                Napier.e { "RevenueCat isSubscribed error: $it" }
                continuation.resumeWithExceptionIfActive(Throwable(it.message))
            },
        )
    }

    /**
     * Gets the latest expiration date in milliseconds from any of the user's past subscriptions.
     */
    suspend fun getLastExpirationDateInMillis(): Long? = suspendCancellableCoroutine { continuation ->
        purchases.getCustomerInfo(
            onSuccess = { customerInfo ->
                val latestExpirationDate = customerInfo.latestExpirationDateMillis
                Napier.d { "RevenueCat latest expiration date? $latestExpirationDate" }
                continuation.resumeIfActive(latestExpirationDate)
            },
            onError = {
                Napier.e { "RevenueCat latest expiration date error: $it" }
                continuation.resumeWithExceptionIfActive(Throwable(it.message))
            },
        )
    }

    /**
     * Fetches the current offering.
     */
    suspend fun getCurrentOffering(): Offering? = suspendCancellableCoroutine { continuation ->
        try {
            purchases.getOfferings(
                onSuccess = { offerings ->
                    Napier.d { "RevenueCat offerings fetched!" }
                    continuation.resumeIfActive(offerings.current)
                },
                onError = {
                    Napier.e { "RevenueCat offerings error: $it" }
                    continuation.resumeWithExceptionIfActive(Throwable(it.message))
                },
            )
        } catch (throwable: Throwable) {
            Napier.e(throwable) { "RevenueCat offerings error." }
            continuation.resumeWithExceptionIfActive(throwable)
        }
    }

    suspend fun getCurrentPackageFromSubscription(subscription: EntitlementInfo): Package? =
        getCurrentOffering()
            ?.availablePackages
            ?.firstOrNull { it.storeProduct.id == subscription.getFullProductId() }

    /**
     * Purchases a product with the given product ID. If the product is not found, nothing happens.
     *
     * @param productId The product ID to purchase.
     */
    suspend fun purchaseProduct(productId: String): Result<Unit> {
        // Find the store product corresponding to the product ID and make the purchase
        val offering = getCurrentOffering()

        return suspendCancellableCoroutine { continuation ->
            offering?.availablePackages?.firstOrNull { it.identifier == productId }?.storeProduct?.let { storeProduct ->
                purchases.purchase(
                    storeProduct = storeProduct,
                    onSuccess = { transaction, customerInfo ->
                        Napier.d { "RevenueCat product purchased! transaction: $transaction <> customerInfo: $customerInfo" }
                        continuation.resumeIfActive(Result.success(Unit))
                    },
                    onError = { error, userCancelled ->
                        Napier.e { "RevenueCat product purchase error: $error <> userCancelled: $userCancelled" }
                        val exception = when (userCancelled) {
                            true -> RevenueCatProductPurchaseUserCanceledException(productId = productId)
                            else -> Exception(error.message)
                        }
                        continuation.resumeIfActive(Result.failure(exception))
                    },
                )
            } ?: continuation.resumeIfActive(Result.failure(NoSuchElementException("Product (ID: $productId) not found")))
        }
    }

    /**
     * Restores the user's purchases. If the user has no purchases, nothing happens.
     */
    suspend fun restorePurchase(): Result<CustomerInfo> = suspendCancellableCoroutine { continuation ->
        purchases.restorePurchases(
            onSuccess = { customerInfo ->
                Napier.d { "RevenueCat purchases restored! customerInfo: $customerInfo" }
                continuation.resumeIfActive(Result.success(customerInfo))
            },
            onError = {
                Napier.e { "RevenueCat purchases restore error: $it" }
                continuation.resumeIfActive(Result.failure(Error(it.message)))
            },
        )
    }

    private fun EntitlementInfo.getFullProductId(): String =
        productPlanIdentifier?.let { "${productIdentifier}:$it" } ?: productIdentifier

}
