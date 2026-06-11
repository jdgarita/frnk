package dev.jdgarita.frnk.database

/**
 * Canonical in-memory [KeyValueStore] for `:data-prefs-api` tests. Backs strings and booleans
 * with plain maps; no persistence. (Two other in-memory copies exist — one in
 * `shared-monetization-api`'s commonTest and one in `:shared-demo`'s commonMain — but test source
 * sets aren't shared across modules, so they can't be collapsed here.)
 */
class InMemoryKeyValueStore : KeyValueStore {
    private val strings = mutableMapOf<String, String>()
    private val booleans = mutableMapOf<String, Boolean>()

    override fun putString(
        key: String,
        value: String,
    ) {
        strings[key] = value
    }

    override fun getString(
        key: String,
        default: String?,
    ): String? = strings[key] ?: default

    override fun putBoolean(
        key: String,
        value: Boolean,
    ) {
        booleans[key] = value
    }

    override fun getBoolean(
        key: String,
        default: Boolean,
    ): Boolean = booleans[key] ?: default

    override fun remove(key: String) {
        strings.remove(key)
        booleans.remove(key)
    }
}
