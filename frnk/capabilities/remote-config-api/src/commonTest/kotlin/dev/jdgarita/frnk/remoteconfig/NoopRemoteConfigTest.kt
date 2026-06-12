package dev.jdgarita.frnk.remoteconfig

import dev.jdgarita.frnk.utils.AppResult
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NoopRemoteConfigTest {
    private val config = NoopRemoteConfig()

    @Test
    fun fetchAndActivate_succeeds() =
        runTest {
            assertTrue(config.fetchAndActivate() is AppResult.Success)
        }

    @Test
    fun getters_return_the_supplied_default() {
        assertEquals("fallback", config.getString("key", "fallback"))
        assertEquals(true, config.getBoolean("key", true))
        assertEquals(42L, config.getLong("key", 42L))
        assertEquals(3.14, config.getDouble("key", 3.14))
    }
}
