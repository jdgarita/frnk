package dev.jdgarita.frnk.database

/** Thin port over multiplatform-settings so callers don't depend on the impl module. */
interface KeyValueStore {
    fun putString(
        key: String,
        value: String,
    )

    fun getString(
        key: String,
        default: String? = null,
    ): String?

    fun putBoolean(
        key: String,
        value: Boolean,
    )

    fun getBoolean(
        key: String,
        default: Boolean = false,
    ): Boolean

    fun remove(key: String)
}
