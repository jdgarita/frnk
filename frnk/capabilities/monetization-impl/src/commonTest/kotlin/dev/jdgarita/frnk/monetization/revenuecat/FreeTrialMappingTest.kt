package dev.jdgarita.frnk.monetization.revenuecat

import com.revenuecat.purchases.kmp.models.DiscountPaymentMode
import com.revenuecat.purchases.kmp.models.OfferPaymentMode
import com.revenuecat.purchases.kmp.models.Period
import com.revenuecat.purchases.kmp.models.PeriodUnit
import com.revenuecat.purchases.kmp.models.Price
import com.revenuecat.purchases.kmp.models.PricingPhase
import com.revenuecat.purchases.kmp.models.RecurrenceMode
import dev.jdgarita.frnk.monetization.ProPeriod
import dev.jdgarita.frnk.monetization.ProPeriodUnit
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * The free-trial length RevenueCat reports, mapped onto `ProProduct.freeTrialPeriod`: from a Play
 * subscription option's pricing phases (Android) and from a StoreKit introductory discount (iOS).
 */
class FreeTrialMappingTest {
    private fun phase(
        value: Int,
        unit: PeriodUnit,
        micros: Long,
        mode: OfferPaymentMode? = null,
        cycles: Int? = 1,
        recurrence: RecurrenceMode = RecurrenceMode.FINITE_RECURRING
    ) = PricingPhase(
        billingPeriod = Period(value, unit),
        recurrenceMode = recurrence,
        billingCycleCount = cycles,
        price = Price(formatted = if (micros == 0L) "Free" else "$4.99", amountMicros = micros, currencyCode = "USD"),
        offerPaymentMode = mode
    )

    private val basePlan = phase(1, PeriodUnit.MONTH, 4_990_000, cycles = null, recurrence = RecurrenceMode.INFINITE_RECURRING)

    @Test
    fun a_play_free_phase_before_the_base_plan_is_the_trial() {
        val phases = listOf(phase(7, PeriodUnit.DAY, 0, OfferPaymentMode.FREE_TRIAL), basePlan)

        assertEquals(ProPeriod(7, ProPeriodUnit.Day), freeTrialOf(phases))
    }

    @Test
    fun a_zero_priced_phase_counts_even_without_the_payment_mode_and_its_cycles_multiply() {
        val phases = listOf(phase(1, PeriodUnit.WEEK, 0, mode = null, cycles = 2), basePlan)

        assertEquals(ProPeriod(2, ProPeriodUnit.Week), freeTrialOf(phases))
    }

    @Test
    fun a_base_plan_alone_or_a_paid_intro_phase_is_no_trial() {
        assertNull(freeTrialOf(listOf(basePlan)))
        assertNull(freeTrialOf(listOf(phase(1, PeriodUnit.MONTH, 990_000, OfferPaymentMode.DISCOUNTED_RECURRING_PAYMENT), basePlan)))
        assertNull(freeTrialOf(emptyList()))
    }

    @Test
    fun the_free_phase_is_found_after_a_paid_one() {
        val phases =
            listOf(
                phase(3, PeriodUnit.DAY, 0, OfferPaymentMode.FREE_TRIAL),
                phase(1, PeriodUnit.MONTH, 990_000, OfferPaymentMode.DISCOUNTED_RECURRING_PAYMENT),
                basePlan
            )

        assertEquals(ProPeriod(3, ProPeriodUnit.Day), freeTrialOf(phases))
    }

    @Test
    fun an_unknown_unit_reports_no_length() {
        assertNull(freeTrialOf(listOf(phase(7, PeriodUnit.UNKNOWN, 0, OfferPaymentMode.FREE_TRIAL), basePlan)))
    }

    @Test
    fun an_ios_free_trial_discount_lasts_its_period_times_its_count() {
        assertEquals(
            ProPeriod(1, ProPeriodUnit.Week),
            freeTrialOf(DiscountPaymentMode.FREE_TRIAL, Period(1, PeriodUnit.WEEK), numberOfPeriods = 1)
        )
        assertEquals(
            ProPeriod(3, ProPeriodUnit.Month),
            freeTrialOf(DiscountPaymentMode.FREE_TRIAL, Period(1, PeriodUnit.MONTH), numberOfPeriods = 3)
        )
    }

    @Test
    fun a_paid_ios_introductory_offer_is_no_trial() {
        val week = Period(1, PeriodUnit.WEEK)

        assertNull(freeTrialOf(DiscountPaymentMode.PAY_AS_YOU_GO, week, numberOfPeriods = 4))
        assertNull(freeTrialOf(DiscountPaymentMode.PAY_UP_FRONT, week, numberOfPeriods = 1))
    }
}