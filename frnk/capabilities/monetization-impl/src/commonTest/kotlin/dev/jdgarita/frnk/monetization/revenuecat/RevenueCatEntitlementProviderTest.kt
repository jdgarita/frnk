package dev.jdgarita.frnk.monetization.revenuecat

import com.revenuecat.purchases.kmp.models.PurchasesError
import com.revenuecat.purchases.kmp.models.PurchasesErrorCode
import com.revenuecat.purchases.kmp.models.PurchasesException
import com.revenuecat.purchases.kmp.models.PurchasesTransactionException
import dev.jdgarita.frnk.monetization.EntitlementProvider
import dev.jdgarita.frnk.monetization.MonetizationError
import org.koin.dsl.koinApplication
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Unit coverage for the RevenueCat provider wiring (BACKLOG P3-2/P3-3). The SDK boundary
 * (`Purchases.sharedInstance`) is a static singleton that can't be faked in a host test, so the real
 * logic lives in the pure [isProFor] mapper (tested below) and the provider is covered by a
 * Koin-resolution test that proves the graph builds without touching the unconfigured SDK.
 */
class RevenueCatEntitlementProviderTest {
    @Test
    fun isProFor_true_when_pro_entitlement_active() {
        assertTrue(isProFor(activeEntitlementIds = setOf("pro"), proEntitlementId = "pro"))
        assertTrue(isProFor(activeEntitlementIds = setOf("ads_free", "pro"), proEntitlementId = "pro"))
    }

    @Test
    fun isProFor_false_when_pro_entitlement_absent() {
        assertFalse(isProFor(activeEntitlementIds = emptySet(), proEntitlementId = "pro"))
        assertFalse(isProFor(activeEntitlementIds = setOf("ads_free"), proEntitlementId = "pro"))
    }

    @Test
    fun isProFor_honors_configurable_identifier() {
        assertTrue(isProFor(activeEntitlementIds = setOf("premium"), proEntitlementId = "premium"))
        assertFalse(isProFor(activeEntitlementIds = setOf("premium"), proEntitlementId = "pro"))
    }

    @Test
    fun monetizationErrorFor_maps_cancellation_regardless_of_code() {
        assertEquals(
            MonetizationError.UserCancelled,
            monetizationErrorFor(PurchasesErrorCode.UnknownError, userCancelled = true)
        )
        assertEquals(
            MonetizationError.UserCancelled,
            monetizationErrorFor(PurchasesErrorCode.PurchaseCancelledError, userCancelled = false)
        )
    }

    @Test
    fun monetizationErrorFor_maps_already_owned_codes() {
        assertEquals(
            MonetizationError.AlreadyOwned,
            monetizationErrorFor(PurchasesErrorCode.ProductAlreadyPurchasedError, userCancelled = false)
        )
        assertEquals(
            MonetizationError.AlreadyOwned,
            monetizationErrorFor(PurchasesErrorCode.ReceiptAlreadyInUseError, userCancelled = false)
        )
    }

    @Test
    fun monetizationErrorFor_maps_network_error() {
        assertEquals(
            MonetizationError.NetworkUnavailable,
            monetizationErrorFor(PurchasesErrorCode.NetworkError, userCancelled = false)
        )
    }

    @Test
    fun monetizationErrorFor_defaults_to_unknown() {
        assertEquals(
            MonetizationError.Unknown,
            monetizationErrorFor(PurchasesErrorCode.StoreProblemError, userCancelled = false)
        )
    }

    @Test
    fun toMonetizationError_dispatches_on_exception_type() {
        assertEquals(
            MonetizationError.UserCancelled,
            PurchasesTransactionException(
                PurchasesError(PurchasesErrorCode.PurchaseCancelledError),
                userCancelled = true
            ).toMonetizationError()
        )
        assertEquals(
            MonetizationError.AlreadyOwned,
            PurchasesException(PurchasesError(PurchasesErrorCode.ProductAlreadyPurchasedError))
                .toMonetizationError()
        )
        assertEquals(
            MonetizationError.NetworkUnavailable,
            PurchasesException(PurchasesError(PurchasesErrorCode.NetworkError)).toMonetizationError()
        )
        assertEquals(MonetizationError.Unknown, IllegalStateException("boom").toMonetizationError())
    }

    @Test
    fun revenueCatModule_resolves_provider_without_throwing() {
        // Building/resolving must not touch Purchases.sharedInstance (throws while unconfigured);
        // the provider's entitlement state defaults to false until refresh().
        val app = koinApplication { modules(revenueCatModule) }
        try {
            val config = app.koin.get<RevenueCatConfig>()
            assertEquals("pro", config.proEntitlementId)

            val provider = app.koin.get<EntitlementProvider>()
            assertFalse(provider.isPro.value)
        } finally {
            app.close()
        }
    }
}