package dev.jdgarita.frnk.database.impl

import com.russhwolf.settings.MapSettings
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SettingsKeyValueStoreTest {
    private fun newStore() = SettingsKeyValueStore(MapSettings())

    @Test
    fun string_round_trips_and_falls_back_to_default() {
        val store = newStore()

        assertNull(store.getString("missing"))
        assertEquals("fallback", store.getString("missing", "fallback"))

        store.putString("key", "value")
        assertEquals("value", store.getString("key"))
        assertEquals("value", store.getString("key", "fallback"))
    }

    @Test
    fun boolean_round_trips_and_falls_back_to_default() {
        val store = newStore()

        assertFalse(store.getBoolean("missing"))
        assertTrue(store.getBoolean("missing", default = true))

        store.putBoolean("key", true)
        assertTrue(store.getBoolean("key"))
    }

    @Test
    fun remove_clears_the_value() {
        val store = newStore()
        store.putString("key", "value")
        store.putBoolean("flag", true)

        store.remove("key")
        store.remove("flag")

        assertNull(store.getString("key"))
        assertFalse(store.getBoolean("flag"))
    }
}
