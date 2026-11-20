package dev.jdgarita.frnk.monetization

import kotlin.math.roundToInt

/**
 * The yearly plan's saving over twelve months of the monthly one, as a whole percent — `$29.99`
 * against `$4.99 × 12` is 50 — or `null` when either price is missing, they are in different
 * currencies, or the yearly plan saves nothing. Computed from the two store prices, so a paywall's
 * "Save 50%" badge can never quote a figure the store no longer charges.
 */
fun yearlySavingsPercent(
    yearly: ProPrice?,
    monthly: ProPrice?
): Int? {
    if (yearly == null || monthly == null) return null
    if (yearly.currencyCode != monthly.currencyCode || monthly.amountMicros <= 0) return null
    val percent = ((1.0 - yearly.amountMicros.toDouble() / (monthly.amountMicros * MONTHS_PER_YEAR)) * 100).roundToInt()
    return percent.takeIf { it > 0 }
}

/** [yearlySavingsPercent] for an offering: its first [ProPlan.Yearly] against its first [ProPlan.Monthly]. */
fun List<ProProduct>.yearlySavingsPercent(): Int? =
    yearlySavingsPercent(
        yearly = firstOrNull { it.plan == ProPlan.Yearly }?.price,
        monthly = firstOrNull { it.plan == ProPlan.Monthly }?.price
    )

private const val MONTHS_PER_YEAR = 12