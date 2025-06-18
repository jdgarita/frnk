package dev.jdgarita.frnk.domain.entity

/**
 * @author Vivien Mahe
 * @since 20/03/2024
 */

data class Amount(
    val value: Double,
    val currency: String,
) : Comparable<Amount> {

    operator fun plus(other: Amount) = this.copy(value = value + other.value)

    operator fun plus(other: Double) = this.copy(value = value + other)

    operator fun minus(other: Amount) = this.copy(value = value - other.value)

    operator fun minus(other: Double) = this.copy(value = value - other)

    operator fun div(other: Amount) = this.copy(value = value / other.value)

    operator fun div(other: Double) = this.copy(value = value / other)

    operator fun times(other: Amount) = this.copy(value = value * other.value)

    operator fun times(other: Double) = this.copy(value = value * other)

    override fun compareTo(other: Amount): Int {
        // Check if the currencies are the same
        if (currency != other.currency) {
            throw IllegalArgumentException("Cannot compare amounts with different currencies")
        }

        // Compare based on the value
        return value.compareTo(other.value)
    }

    fun compareTo(other: Double): Int = value.compareTo(other)
}
