package dev.jdgarita.frnk.ui.nav

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class FrnkPrimaryActionRegistryTest {
    @Test
    fun active_is_null_when_nothing_registered() {
        val registry = FrnkPrimaryActionRegistry()
        assertNull(registry.active.value)
    }

    @Test
    fun registered_handler_becomes_active_and_is_invocable() {
        val registry = FrnkPrimaryActionRegistry()
        var fired = 0
        registry.register { fired++ }

        registry.active.value?.invoke()

        assertEquals(1, fired)
    }

    @Test
    fun last_registered_handler_wins() {
        val registry = FrnkPrimaryActionRegistry()
        var fired = ""
        registry.register { fired = "first" }
        registry.register { fired = "second" }

        registry.active.value?.invoke()

        assertEquals("second", fired)
    }

    @Test
    fun unregister_restores_the_previous_handler() {
        val registry = FrnkPrimaryActionRegistry()
        var fired = ""
        registry.register { fired = "first" }
        val second = registry.register { fired = "second" }

        second.unregister()
        registry.active.value?.invoke()

        assertEquals("first", fired)
    }

    @Test
    fun unregistering_a_buried_handler_keeps_the_top_active() {
        val registry = FrnkPrimaryActionRegistry()
        var fired = ""
        val first = registry.register { fired = "first" }
        registry.register { fired = "second" }

        first.unregister()
        registry.active.value?.invoke()

        assertEquals("second", fired)
    }

    @Test
    fun unregistering_the_last_handler_clears_active() {
        val registry = FrnkPrimaryActionRegistry()
        val registration = registry.register { }

        registration.unregister()

        assertNull(registry.active.value)
    }

    @Test
    fun unregister_is_idempotent() {
        val registry = FrnkPrimaryActionRegistry()
        var fired = ""
        registry.register { fired = "first" }
        val second = registry.register { fired = "second" }

        second.unregister()
        second.unregister()
        registry.active.value?.invoke()

        assertEquals("first", fired)
    }
}
