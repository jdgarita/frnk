package dev.jdgarita.frnk.ui.scaffolds

import dev.jdgarita.frnk.database.KeyValueStore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Tests for [OnboardingGate] — the first-launch persistence wrapper. Pure (no Compose), driven over an
 * in-memory [KeyValueStore] fake. Lives in `androidHostTest` because `:ui-scaffolds` has no
 * `commonTest` source set.
 */
class OnboardingGateTest {
    private class InMemoryKeyValueStore : KeyValueStore {
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

    @Test
    fun unseen_by_default() {
        val gate = OnboardingGate(InMemoryKeyValueStore())
        assertFalse(gate.seen, "a fresh gate must report unseen")
    }

    @Test
    fun markSeen_persists() {
        val store = InMemoryKeyValueStore()
        OnboardingGate(store).markSeen()

        // A fresh gate over the same store must observe the persisted flag.
        assertTrue(OnboardingGate(store).seen, "markSeen must persist across gate instances")
    }

    @Test
    fun custom_key_is_isolated() {
        val store = InMemoryKeyValueStore()
        OnboardingGate(store, key = "flow.a").markSeen()

        assertTrue(OnboardingGate(store, key = "flow.a").seen)
        assertFalse(OnboardingGate(store, key = "flow.b").seen, "independent keys must not bleed")
    }

    @Test
    fun default_key_is_namespaced() {
        assertEquals("frnk.onboarding.seen", OnboardingGate.DEFAULT_KEY)
    }
}
