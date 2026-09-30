package dev.jdgarita.frnk.ui.haptics

import dev.jdgarita.frnk.database.KeyValueStore
import org.koin.dsl.koinApplication
import org.koin.dsl.module
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertSame
import kotlin.test.assertTrue

/** A map-backed [KeyValueStore]; `values` lets a test read exactly what was written, under which key. */
private class MapKeyValueStore : KeyValueStore {
    val values = mutableMapOf<String, String>()

    override fun putString(
        key: String,
        value: String
    ) {
        values[key] = value
    }

    override fun getString(
        key: String,
        default: String?
    ): String? = values[key] ?: default

    override fun putBoolean(
        key: String,
        value: Boolean
    ) {
        values[key] = value.toString()
    }

    override fun getBoolean(
        key: String,
        default: Boolean
    ): Boolean = values[key]?.toBooleanStrictOrNull() ?: default

    override fun remove(key: String) {
        values.remove(key)
    }
}

private class RecordingEngine : HapticEngine {
    val emitted = mutableListOf<HapticType>()

    override fun emit(type: HapticType) {
        emitted += type
    }
}

class HapticsPreferenceTest {
    @Test
    fun an_unset_key_reads_as_on() {
        val preference = KeyValueHapticsPreference(MapKeyValueStore(), key = "app.haptics")

        assertTrue(preference.isEnabled.value)
    }

    @Test
    fun the_default_is_a_parameter() {
        val preference = KeyValueHapticsPreference(MapKeyValueStore(), key = "app.haptics", default = false)

        assertFalse(preference.isEnabled.value)
    }

    @Test
    fun a_stored_value_wins_over_the_default() {
        val store = MapKeyValueStore().apply { putBoolean("app.haptics", false) }

        assertFalse(KeyValueHapticsPreference(store, key = "app.haptics").isEnabled.value)
    }

    @Test
    fun setEnabled_writes_under_the_hosts_key_and_publishes() {
        val store = MapKeyValueStore()
        val preference = KeyValueHapticsPreference(store, key = "faint.haptics.enabled")

        preference.setEnabled(false)

        assertFalse(preference.isEnabled.value)
        assertEquals(mapOf("faint.haptics.enabled" to "false"), store.values)
    }

    @Test
    fun the_switch_survives_a_new_instance_over_the_same_store() {
        val store = MapKeyValueStore()
        KeyValueHapticsPreference(store, key = "app.haptics").setEnabled(false)

        assertFalse(KeyValueHapticsPreference(store, key = "app.haptics").isEnabled.value)
    }

    @Test
    fun a_blank_key_is_rejected() {
        assertFailsWith<IllegalArgumentException> { KeyValueHapticsPreference(MapKeyValueStore(), key = " ") }
    }

    @Test
    fun persistent_feedback_is_gated_by_the_preference() {
        val engine = RecordingEngine()
        val store = MapKeyValueStore().apply { putBoolean("app.haptics", false) }
        val haptics = PersistentHapticFeedback(engine, KeyValueHapticsPreference(store, key = "app.haptics"))

        haptics.perform(HapticType.Click)

        assertFalse(haptics.isEnabled.value)
        assertTrue(engine.emitted.isEmpty(), "perform must not emit while the stored switch is off")
    }

    @Test
    fun persistent_feedback_setEnabled_writes_through_to_the_store() {
        val engine = RecordingEngine()
        val store = MapKeyValueStore()
        val preference = KeyValueHapticsPreference(store, key = "app.haptics")
        val haptics = PersistentHapticFeedback(engine, preference)

        haptics.setEnabled(false)
        haptics.perform(HapticType.Click)
        assertFalse(preference.isEnabled.value)
        assertEquals("false", store.values["app.haptics"])
        assertTrue(engine.emitted.isEmpty())

        haptics.setEnabled(true)
        haptics.perform(HapticType.Success)
        assertEquals("true", store.values["app.haptics"])
        assertEquals(listOf(HapticType.Success), engine.emitted)
    }

    @Test
    fun persistent_feedback_follows_a_change_made_on_the_preference() {
        val engine = RecordingEngine()
        val preference = KeyValueHapticsPreference(MapKeyValueStore(), key = "app.haptics")
        val haptics = PersistentHapticFeedback(engine, preference)

        preference.setEnabled(false)

        assertFalse(haptics.isEnabled.value)
        haptics.perform(HapticType.Click)
        assertTrue(engine.emitted.isEmpty())
    }

    @Test
    fun the_module_binds_one_preference_under_the_given_key() {
        val store = MapKeyValueStore()
        val koin =
            koinApplication {
                modules(
                    module { single<KeyValueStore> { store } },
                    hapticsPreferenceModule(key = "still.haptics.enabled")
                )
            }.koin

        val preference = koin.get<HapticsPreference>()
        preference.setEnabled(false)

        assertSame(preference, koin.get<HapticsPreference>())
        assertEquals("false", store.values["still.haptics.enabled"])
    }

    @Test
    fun the_module_passes_the_default_through() {
        val koin =
            koinApplication {
                modules(
                    module { single<KeyValueStore> { MapKeyValueStore() } },
                    hapticsPreferenceModule(key = "app.haptics", default = false)
                )
            }.koin

        assertFalse(koin.get<HapticsPreference>().isEnabled.value)
    }
}