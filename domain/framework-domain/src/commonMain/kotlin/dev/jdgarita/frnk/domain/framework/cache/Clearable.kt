package dev.jdgarita.frnk.domain.framework.cache

/**
 * To be implemented by any class that needs to clear cached data.
 * In order to be cleared, the class must be multibound to the Clearable Set with the [clearablesQualifier].
 *
 * @see dev.jdgarita.frnk.util.di.clearablesQualifier
 */
interface Clearable {

    /**
     * Clears any cached data. Executed automatically when user logs out.
     */
    suspend fun clear()
}