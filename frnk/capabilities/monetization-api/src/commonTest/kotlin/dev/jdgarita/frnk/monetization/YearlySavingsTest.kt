package dev.jdgarita.frnk.monetization

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class YearlySavingsTest {
    private fun usd(amount: Double) = ProPrice(amountMicros = (amount * 1_000_000).toLong(), currencyCode = "USD")

    @Test
    fun yearly_against_twelve_monthly_payments_rounds_to_a_whole_percent() {
        assertEquals(50, yearlySavingsPercent(yearly = usd(29.99), monthly = usd(4.99)))
        assertEquals(37, yearlySavingsPercent(yearly = usd(29.99), monthly = usd(3.99)))
    }

    @Test
    fun no_saving_when_a_price_is_missing_or_the_currencies_differ() {
        assertNull(yearlySavingsPercent(yearly = null, monthly = usd(4.99)))
        assertNull(yearlySavingsPercent(yearly = usd(29.99), monthly = null))
        assertNull(yearlySavingsPercent(yearly = usd(29.99), monthly = ProPrice(4_990_000, "EUR")))
        assertNull(yearlySavingsPercent(yearly = usd(29.99), monthly = usd(0.0)))
    }

    @Test
    fun a_yearly_plan_that_costs_as_much_or_more_saves_nothing() {
        assertNull(yearlySavingsPercent(yearly = usd(59.88), monthly = usd(4.99)))
        assertNull(yearlySavingsPercent(yearly = usd(70.0), monthly = usd(4.99)))
    }

    @Test
    fun an_offering_reads_its_yearly_and_monthly_plans() {
        val offering =
            listOf(
                ProProduct(id = "\$rc_annual", plan = ProPlan.Yearly, title = "Yearly", priceFormatted = "$29.99", price = usd(29.99)),
                ProProduct(id = "\$rc_monthly", plan = ProPlan.Monthly, title = "Monthly", priceFormatted = "$4.99", price = usd(4.99))
            )
        assertEquals(50, offering.yearlySavingsPercent())
        assertNull(offering.filter { it.plan == ProPlan.Monthly }.yearlySavingsPercent())
    }
}