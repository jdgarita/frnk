package dev.jdgarita.frnk.database

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

private enum class TestTheme { System, Light, Dark }

class PreferenceTest {
    private val store = InMemoryKeyValueStore()

    // --- String ---

    @Test
    fun string_preference_round_trips() {
        val pref = store.stringPreference("name", default = "anon")
        pref.value = "frnk"
        assertEquals("frnk", pref.value)
        assertEquals("frnk", store.getString("name"))
    }

    @Test
    fun string_preference_returns_default_when_unset() {
        assertEquals("anon", store.stringPreference("name", default = "anon").value)
    }

    // --- Boolean ---

    @Test
    fun boolean_preference_round_trips() {
        val pref = store.booleanPreference("dark", default = false)
        pref.value = true
        assertTrue(pref.value)
        assertTrue(store.getBoolean("dark"))
    }

    @Test
    fun boolean_preference_returns_default_when_unset() {
        assertTrue(store.booleanPreference("dark", default = true).value)
    }

    // --- Int (encoded over String) ---

    @Test
    fun int_preference_round_trips() {
        val pref = store.intPreference("count", default = 0)
        pref.value = 42
        assertEquals(42, pref.value)
        assertEquals("42", store.getString("count"))
    }

    @Test
    fun int_preference_returns_default_when_unset() {
        assertEquals(7, store.intPreference("count", default = 7).value)
    }

    @Test
    fun int_preference_falls_back_to_default_on_corrupt_value() {
        store.putString("count", "not-a-number")
        assertEquals(7, store.intPreference("count", default = 7).value)
    }

    // --- Enum (encoded as name over String) ---

    @Test
    fun enum_preference_round_trips() {
        val pref = store.enumPreference("theme", default = TestTheme.System)
        pref.value = TestTheme.Dark
        assertEquals(TestTheme.Dark, pref.value)
        assertEquals("Dark", store.getString("theme"))
    }

    @Test
    fun enum_preference_returns_default_when_unset() {
        assertEquals(TestTheme.Light, store.enumPreference("theme", default = TestTheme.Light).value)
    }

    @Test
    fun enum_preference_falls_back_to_default_on_unknown_name() {
        store.putString("theme", "REMOVED_CONSTANT")
        assertEquals(TestTheme.System, store.enumPreference("theme", default = TestTheme.System).value)
    }

    // --- Delegation & imperative access ---

    @Test
    fun delegation_via_by_reads_and_writes() {
        var count by store.intPreference("count", default = 0)
        count = 9
        assertEquals("9", store.getString("count"))
        assertEquals(9, count)
    }

    @Test
    fun value_get_and_set_property() {
        val pref = store.stringPreference("name", default = "anon")
        assertEquals("anon", pref.value)
        pref.value = "frnk"
        assertEquals("frnk", pref.value)
    }

    // --- remove ---

    @Test
    fun remove_resets_to_default() {
        val pref = store.booleanPreference("dark", default = false)
        pref.value = true
        pref.remove()
        assertFalse(pref.value)
        assertNull(store.getString("dark"))
    }

    // --- independence ---

    @Test
    fun two_preferences_on_same_store_are_independent() {
        val a = store.intPreference("a", default = 0)
        val b = store.intPreference("b", default = 0)
        a.value = 1
        b.value = 2
        assertEquals(1, a.value)
        assertEquals(2, b.value)
    }
}
