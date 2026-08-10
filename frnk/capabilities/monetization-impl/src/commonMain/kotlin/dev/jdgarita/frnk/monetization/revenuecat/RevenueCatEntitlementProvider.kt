package dev.jdgarita.frnk.monetization.revenuecat

import com.revenuecat.purchases.kmp.Purchases
import com.revenuecat.purchases.kmp.PurchasesDelegate
import com.revenuecat.purchases.kmp.models.CustomerInfo
import com.revenuecat.purchases.kmp.models.Offering
import com.revenuecat.purchases.kmp.models.Package
import com.revenuecat.purchases.kmp.models.PackageType
import com.revenuecat.purchases.kmp.models.PurchasesError
import com.revenuecat.purchases.kmp.models.PurchasesErrorCode
import com.revenuecat.purchases.kmp.models.PurchasesException
import com.revenuecat.purchases.kmp.models.PurchasesTransactionException
import com.revenuecat.purchases.kmp.models.StoreProduct
import com.revenuecat.purchases.kmp.models.StoreTransaction
import com.revenuecat.purchases.kmp.result.awaitCustomerInfoResult
import com.revenuecat.purchases.kmp.result.awaitLogInResult
import com.revenuecat.purchases.kmp.result.awaitOfferingsResult
import com.revenuecat.purchases.kmp.result.awaitPurchaseResult
import com.revenuecat.purchases.kmp.result.awaitRestoreResult
import com.revenuecat.purchases.kmp.result.awaitSyncPurchasesResult
import dev.jdgarita.frnk.monetization.EntitlementProvider
import dev.jdgarita.frnk.monetization.MonetizationError
import dev.jdgarita.frnk.monetization.ProBenefit
import dev.jdgarita.frnk.monetization.ProMetadata
import dev.jdgarita.frnk.monetization.ProPlan
import dev.jdgarita.frnk.monetization.ProProduct
import dev.jdgarita.frnk.utils.AppResult
import dev.jdgarita.frnk.utils.CommonError
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * [EntitlementProvider] backed by the RevenueCat KMP SDK.
 *
 * The toolkit never calls `Purchases.configure(...)` — the host does (platform context + public SDK key +
 * native iOS pod) before the gate is used. Every SDK access is `runCatching`/`Result`-guarded, so an
 * unconfigured `Purchases.sharedInstance` degrades to a safe failure/no-op instead of throwing.
 */
internal class RevenueCatEntitlementProvider(
    private val config: RevenueCatConfig
) : EntitlementProvider {
    private val _isPro = MutableStateFlow(false)
    override val isPro: StateFlow<Boolean> = _isPro.asStateFlow()

    override suspend fun identify(userId: String): AppResult<Unit, CommonError> {
        installListenerOnce()
        // An unconfigured SDK throws on the appUserID read — that's the documented
        // graceful-degradation mode (missing/placeholder key), so identify succeeds as a no-op
        // rather than blocking dev builds that never configured Purchases.
        val activeUserId =
            runCatching { Purchases.sharedInstance.appUserID }.getOrNull()
                ?: return AppResult.Success(Unit)
        // Skip the network logIn when already identified as [userId].
        if (activeUserId == userId) return AppResult.Success(Unit)
        val result =
            sdkCall { Purchases.sharedInstance.awaitLogInResult(userId) }
                ?: return AppResult.Failure(CommonError.Unknown)
        return result.fold(
            onSuccess = {
                updateFrom(customerInfo = it.customerInfo)
                AppResult.Success(Unit)
            },
            onFailure = { AppResult.Failure(CommonError.Unknown) }
        )
    }

    private var listenerInstalled = false

    /** Last fetched packages, keyed by identifier — so [purchase] can resolve the `Package` to buy. */
    private var packagesById: Map<String, Package> = emptyMap()

    override suspend fun refresh() {
        installListenerOnce()
        sdkCall { Purchases.sharedInstance.awaitCustomerInfoResult() }
            ?.onSuccess(::updateFrom)
    }

    override suspend fun offerings(): AppResult<List<ProProduct>, MonetizationError> {
        installListenerOnce()
        val result =
            sdkCall { Purchases.sharedInstance.awaitOfferingsResult() }
                ?: return AppResult.Failure(MonetizationError.StoreUnavailable)
        return result.fold(
            onSuccess = { offerings ->
                val packages = offerings.current?.availablePackages.orEmpty()
                packagesById = packages.associateBy { it.identifier }
                AppResult.Success(mapProducts(packages))
            },
            onFailure = { AppResult.Failure(MonetizationError.NoOfferings) }
        )
    }

    override suspend fun purchase(productId: String): AppResult<Boolean, MonetizationError> {
        installListenerOnce()
        val pkg =
            packagesById[productId]
                ?: run {
                    // Cache cold (purchase before offerings) — populate then retry the lookup.
                    offerings()
                    packagesById[productId]
                }
                ?: return AppResult.Failure(MonetizationError.NoOfferings)

        val result =
            sdkCall { Purchases.sharedInstance.awaitPurchaseResult(pkg) }
                ?: return AppResult.Failure(MonetizationError.StoreUnavailable)
        return result.fold(
            onSuccess = { success ->
                updateFrom(success.customerInfo)
                AppResult.Success(_isPro.value)
            },
            onFailure = { AppResult.Failure(it.toMonetizationError()) }
        )
    }

    override suspend fun restore(): AppResult<Boolean, MonetizationError> {
        installListenerOnce()
        val result =
            sdkCall { Purchases.sharedInstance.awaitRestoreResult() }
                ?: return AppResult.Failure(MonetizationError.StoreUnavailable)
        return result.fold(
            onSuccess = { info ->
                updateFrom(info)
                // Computed from the restore's own CustomerInfo — `_isPro.value` would conflate a
                // pre-existing Pro state (e.g. god-mode-free Pro before a no-op restore) with a
                // successful restore, telling an already-Pro user "restored" when nothing happened.
                AppResult.Success(isProFor(info.entitlements.active.keys, config.proEntitlementId))
            },
            onFailure = { AppResult.Failure(it.toMonetizationError()) }
        )
    }

    override suspend fun syncPurchases(): AppResult<Boolean, MonetizationError> {
        installListenerOnce()
        val result =
            sdkCall { Purchases.sharedInstance.awaitSyncPurchasesResult() }
                ?: return AppResult.Failure(MonetizationError.StoreUnavailable)
        return result.fold(
            onSuccess = { info ->
                updateFrom(info)
                AppResult.Success(isProFor(info.entitlements.active.keys, config.proEntitlementId))
            },
            onFailure = { AppResult.Failure(it.toMonetizationError()) }
        )
    }

    override suspend fun managementUrl(): AppResult<String?, MonetizationError> {
        installListenerOnce()
        val result =
            sdkCall { Purchases.sharedInstance.awaitCustomerInfoResult() }
                ?: return AppResult.Failure(MonetizationError.StoreUnavailable)
        return result.fold(
            onSuccess = { AppResult.Success(it.managementUrlString) },
            onFailure = { AppResult.Failure(MonetizationError.Unknown) }
        )
    }

    override suspend fun fetchMetadata(): AppResult<ProMetadata, MonetizationError> {
        installListenerOnce()
        val result =
            sdkCall { Purchases.sharedInstance.awaitOfferingsResult() }
                ?: return AppResult.Failure(MonetizationError.StoreUnavailable)
        return result.fold(
            onSuccess = { offerings ->
                offerings.current
                    ?.let { AppResult.Success(extractPaywallMetadata(it)) }
                    ?: AppResult.Failure(MonetizationError.NoOfferings)
            },
            onFailure = { AppResult.Failure(MonetizationError.Unknown) }
        )
    }

    private fun updateFrom(customerInfo: CustomerInfo) {
        _isPro.value = isProFor(customerInfo.entitlements.active.keys, config.proEntitlementId)
    }

    private fun extractPaywallMetadata(offering: Offering): ProMetadata {
        // 1. Define your offline fallbacks
        val defaultTitle = "Faint Pro"
        val defaultSubtitle = "Elevate your coffee journal."
        val defaultBenefits =
            listOf(
                ProBenefit("SCANS", "Unlimited label scans"),
                ProBenefit("NOTES", "AI-read tasting notes from any bag"),
                ProBenefit("PRIVACY", "Your cards stay on-device, always")
            )

        return try {
            // 2. Extract Title and Subtitle Strings
            val title = offering.metadata["title"] as? String ?: defaultTitle
            val subtitle = offering.metadata["subtitle"] as? String ?: defaultSubtitle

            // 3. Extract the Benefits Array
            @Suppress("UNCHECKED_CAST")
            val metadataList = offering.metadata["benefits"] as? List<Map<String, String>>

            val benefits =
                metadataList
                    ?.mapNotNull { item ->
                        val id = item["key"]
                        val text = item["value"]
                        if (id != null && text != null) ProBenefit(id, text) else null
                    }?.takeIf { it.isNotEmpty() } ?: defaultBenefits

            ProMetadata(title, subtitle, benefits)
        } catch (e: Exception) {
            // todo: add crashlytics logging
            // Safe fallback if the JSON is malformed
            ProMetadata(defaultTitle, defaultSubtitle, defaultBenefits)
        }
    }

    /** Install the delegate once, lazily — see [RevenueCatConfig] / P3-2 notes. Doesn't clobber a host delegate. */
    private fun installListenerOnce() {
        if (listenerInstalled) return
        runCatching {
            val purchases = Purchases.sharedInstance
            if (purchases.delegate == null) {
                purchases.delegate = EntitlementDelegate(::updateFrom)
            }
            listenerInstalled = true
        }
    }
}

/** Minimal [PurchasesDelegate] that forwards customer-info updates; ignores App Store promo purchases. */
private class EntitlementDelegate(
    private val onUpdate: (CustomerInfo) -> Unit
) : PurchasesDelegate {
    override fun onCustomerInfoUpdated(customerInfo: CustomerInfo) = onUpdate(customerInfo)

    override fun onPurchasePromoProduct(
        product: StoreProduct,
        startPurchase: (
            onError: (error: PurchasesError, userCancelled: Boolean) -> Unit,
            onSuccess: (storeTransaction: StoreTransaction, customerInfo: CustomerInfo) -> Unit
        ) -> Unit
    ) = Unit
}

private fun mapProducts(packages: List<Package>): List<ProProduct> {
    // Per-month price of the annual plan vs the monthly plan → savings badge.
    val monthlyMicros =
        packages
            .firstOrNull { it.packageType == PackageType.MONTHLY }
            ?.storeProduct
            ?.price
            ?.amountMicros
    return packages.map { pkg ->
        val product = pkg.storeProduct
        val plan = pkg.packageType.toProPlan()
        val perMonthMicros = product.pricePerMonth?.amountMicros
        ProProduct(
            id = pkg.identifier,
            plan = plan,
            title = product.title,
            priceFormatted = product.price.formatted,
            pricePerMonthFormatted = product.pricePerMonth?.formatted,
            hasFreeTrial = product.introductoryDiscount != null,
            badge = savingsBadge(plan, monthlyMicros, perMonthMicros)
        )
    }
}

private fun savingsBadge(
    plan: ProPlan,
    monthlyMicros: Long?,
    perMonthMicros: Long?
): String? {
    if (plan != ProPlan.Yearly || monthlyMicros == null || perMonthMicros == null || monthlyMicros <= 0L) return null
    val savings = ((1.0 - perMonthMicros.toDouble() / monthlyMicros.toDouble()) * 100).toInt()
    return if (savings in 1..99) "Save $savings%" else null
}

private fun PackageType.toProPlan(): ProPlan =
    when (this) {
        PackageType.WEEKLY -> ProPlan.Weekly
        PackageType.MONTHLY -> ProPlan.Monthly
        PackageType.ANNUAL -> ProPlan.Yearly
        PackageType.LIFETIME -> ProPlan.Lifetime
        else -> ProPlan.Other
    }

/**
 * Runs an SDK access, converting any throw (typically an unconfigured `Purchases.sharedInstance` —
 * the documented graceful-degradation mode) into `null` — except [CancellationException], which must
 * propagate so a cancelled caller isn't handed a bogus `StoreUnavailable`. This replaces the old
 * `runCatching { ... }.getOrNull()` wrappers, which swallowed cancellation.
 */
private inline fun <T> sdkCall(block: () -> T): T? =
    try {
        block()
    } catch (e: CancellationException) {
        throw e
    } catch (_: Throwable) {
        null
    }

internal fun Throwable.toMonetizationError(): MonetizationError =
    when (this) {
        is PurchasesTransactionException -> monetizationErrorFor(code, userCancelled)
        is PurchasesException -> monetizationErrorFor(code, userCancelled = false)
        else -> MonetizationError.Unknown
    }

/**
 * Pure, statics-free error-code → [MonetizationError] mapping, split out (like [isProFor]) so it is
 * unit-testable without `Purchases.sharedInstance`. `ProductAlreadyPurchasedError` (Play: the user
 * already owns the subscription under this store account) and `ReceiptAlreadyInUseError` (the
 * receipt belongs to another app user) both mean "the store says you own this" — surfaced as
 * [MonetizationError.AlreadyOwned] so callers can fall through to a restore instead of dead-ending.
 */
internal fun monetizationErrorFor(
    code: PurchasesErrorCode,
    userCancelled: Boolean
): MonetizationError =
    when {
        userCancelled || code == PurchasesErrorCode.PurchaseCancelledError -> MonetizationError.UserCancelled
        code == PurchasesErrorCode.ProductAlreadyPurchasedError -> MonetizationError.AlreadyOwned
        code == PurchasesErrorCode.ReceiptAlreadyInUseError -> MonetizationError.AlreadyOwned
        code == PurchasesErrorCode.NetworkError -> MonetizationError.NetworkUnavailable
        else -> MonetizationError.Unknown
    }

/**
 * Pure, SDK-free Free-vs-Pro decision, split out so it is unit-testable without the static
 * `Purchases.sharedInstance`. A user is Pro when [proEntitlementId] is among their active entitlements.
 */
internal fun isProFor(
    activeEntitlementIds: Set<String>,
    proEntitlementId: String
): Boolean = proEntitlementId in activeEntitlementIds