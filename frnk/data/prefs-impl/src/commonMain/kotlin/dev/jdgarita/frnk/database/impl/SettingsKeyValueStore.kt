package dev.jdgarita.frnk.database.impl

import com.russhwolf.settings.Settings
import dev.jdgarita.frnk.database.KeyValueStore

internal class SettingsKeyValueStore(
    private val s: Settings
) : KeyValueStore {
    override fun putString(
        key: String,
        value: String
    ) {
        s.putString(key, value)
    }

    override fun getString(
        key: String,
        default: String?
    ): String? = s.getStringOrNull(key) ?: default

    override fun putBoolean(
        key: String,
        value: Boolean
    ) {
        s.putBoolean(key, value)
    }

    override fun getBoolean(
        key: String,
        default: Boolean
    ): Boolean = s.getBoolean(key, default)

    override fun remove(key: String) {
        s.remove(key)
    }
}