package dev.jdgarita.frnk.di

import org.koin.core.context.stopKoin
import org.koin.dsl.module
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertSame

class FrnkInitializerTest {
    @AfterTest
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun requireFrnkKoin_fails_fast_with_bootstrap_instruction_when_koin_not_started() {
        stopKoin() // Koin context is process-global; make sure no other test leaked one.
        val failure = assertFailsWith<IllegalStateException> { requireFrnkKoin() }
        assertEquals(true, failure.message?.contains("initializeFrnk"), "error must name the bootstrap call")
    }

    @Test
    fun initializeFrnk_starts_koin_with_the_passed_modules() {
        val hostModule = module { single { "host-binding" } }
        val app = initializeFrnk(modules = listOf(hostModule))
        assertSame(app.koin, requireFrnkKoin())
        assertEquals("host-binding", requireFrnkKoin().get<String>())
    }
}