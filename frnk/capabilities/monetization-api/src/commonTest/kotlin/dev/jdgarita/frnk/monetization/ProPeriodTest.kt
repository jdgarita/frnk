package dev.jdgarita.frnk.monetization

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class ProPeriodTest {
    @Test
    fun approximate_days_are_exact_for_days_and_weeks() {
        assertEquals(7, ProPeriod(7, ProPeriodUnit.Day).approximateDays)
        assertEquals(7, ProPeriod(1, ProPeriodUnit.Week).approximateDays)
        assertEquals(14, ProPeriod(2, ProPeriodUnit.Week).approximateDays)
    }

    @Test
    fun approximate_days_count_a_month_as_30_and_a_year_as_365() {
        assertEquals(30, ProPeriod(1, ProPeriodUnit.Month).approximateDays)
        assertEquals(365, ProPeriod(1, ProPeriodUnit.Year).approximateDays)
    }

    @Test
    fun a_period_is_at_least_one_unit() {
        assertFailsWith<IllegalArgumentException> { ProPeriod(0, ProPeriodUnit.Day) }
    }

    @Test
    fun a_product_has_no_trial_length_by_default() {
        assertNull(ProProduct("monthly", ProPlan.Monthly, "Monthly", "$4.99").freeTrialPeriod)
    }
}