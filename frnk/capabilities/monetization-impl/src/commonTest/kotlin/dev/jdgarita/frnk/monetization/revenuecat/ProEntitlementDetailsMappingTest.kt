package dev.jdgarita.frnk.monetization.revenuecat

import com.revenuecat.purchases.kmp.models.EntitlementInfo
import com.revenuecat.purchases.kmp.models.OwnershipType
import com.revenuecat.purchases.kmp.models.PeriodType
import com.revenuecat.purchases.kmp.models.Store
import com.revenuecat.purchases.kmp.models.VerificationResult
import dev.jdgarita.frnk.monetization.ProEntitlementDetails
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Instant

/** [proEntitlementDetailsFor] over real SDK `EntitlementInfo` models (public constructor, no statics). */
class ProEntitlementDetailsMappingTest {
    @Test
    fun trial_entitlement_maps_trial_expiry_and_renewal() {
        val details =
            proEntitlementDetailsFor(
                mapOf("pro" to entitlement(periodType = PeriodType.TRIAL, expirationMillis = EXPIRY, willRenew = true)),
                proEntitlementId = "pro"
            )
        assertEquals(
            ProEntitlementDetails(isTrial = true, expiresAt = Instant.fromEpochMilliseconds(EXPIRY), willRenew = true),
            details
        )
    }

    @Test
    fun cancelled_paid_subscription_is_not_trial_and_will_not_renew() {
        val details =
            proEntitlementDetailsFor(
                mapOf("pro" to entitlement(periodType = PeriodType.NORMAL, expirationMillis = EXPIRY, willRenew = false)),
                proEntitlementId = "pro"
            )
        assertEquals(ProEntitlementDetails(isTrial = false, expiresAt = Instant.fromEpochMilliseconds(EXPIRY), willRenew = false), details)
    }

    @Test
    fun intro_offer_is_not_a_trial() {
        val details =
            proEntitlementDetailsFor(
                mapOf("pro" to entitlement(periodType = PeriodType.INTRO, expirationMillis = EXPIRY, willRenew = true)),
                proEntitlementId = "pro"
            )
        assertEquals(false, details.isTrial)
    }

    @Test
    fun lifetime_has_no_expiration() {
        val details =
            proEntitlementDetailsFor(
                mapOf("pro" to entitlement(periodType = PeriodType.NORMAL, expirationMillis = null, willRenew = false)),
                proEntitlementId = "pro"
            )
        assertEquals(ProEntitlementDetails.None, details)
    }

    @Test
    fun missing_pro_entitlement_maps_to_none() {
        assertEquals(ProEntitlementDetails.None, proEntitlementDetailsFor(emptyMap(), proEntitlementId = "pro"))
        assertEquals(
            ProEntitlementDetails.None,
            proEntitlementDetailsFor(
                mapOf("ads_free" to entitlement(periodType = PeriodType.TRIAL, expirationMillis = EXPIRY, willRenew = true)),
                proEntitlementId = "pro"
            )
        )
    }

    @Test
    fun honors_configurable_identifier() {
        val details =
            proEntitlementDetailsFor(
                mapOf("premium" to entitlement(periodType = PeriodType.TRIAL, expirationMillis = EXPIRY, willRenew = true)),
                proEntitlementId = "premium"
            )
        assertEquals(true, details.isTrial)
    }

    private fun entitlement(
        periodType: PeriodType,
        expirationMillis: Long?,
        willRenew: Boolean
    ) = EntitlementInfo(
        identifier = "pro",
        isActive = true,
        willRenew = willRenew,
        periodType = periodType,
        latestPurchaseDateMillis = 1_700_000_000_000,
        originalPurchaseDateMillis = 1_700_000_000_000,
        expirationDateMillis = expirationMillis,
        store = Store.PLAY_STORE,
        productIdentifier = "yearly",
        productPlanIdentifier = null,
        isSandbox = true,
        unsubscribeDetectedAtMillis = null,
        billingIssueDetectedAtMillis = null,
        ownershipType = OwnershipType.PURCHASED,
        verification = VerificationResult.NOT_REQUESTED
    )

    private companion object {
        const val EXPIRY = 1_800_000_000_000
    }
}