package dev.jdgarita.frnk.ui.mvi

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Verifies the [MviViewModel] engine via a minimal in-test subclass: an intent reduces state
 * through [MviViewModel.setState], and an intent emits a one-shot effect through the effects
 * channel. This is the reusable template for testing real feature reducers.
 */
class MviViewModelTest {
    // Shared scheduler so the ViewModel's `viewModelScope` (Main) and the test body advance together.
    private val dispatcher = UnconfinedTestDispatcher()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun intent_reduces_state() =
        runTest(dispatcher) {
            val vm = CounterViewModel()

            vm.send(CounterIntent.Increment)
            vm.send(CounterIntent.Increment)
            runCurrent()

            assertEquals(2, vm.state.value.count)
        }

    @Test
    fun reset_intent_restores_state_and_emits_effect() =
        runTest(dispatcher) {
            val vm = CounterViewModel()
            vm.send(CounterIntent.Increment)
            runCurrent()

            // Start awaiting the effect before triggering it.
            val effect = async { vm.effects.first() }
            runCurrent()

            vm.send(CounterIntent.Reset)
            runCurrent()

            assertEquals(0, vm.state.value.count)
            assertEquals(CounterEffect.DidReset, effect.await())
        }
}

// --- Test fixtures: a trivial MviViewModel subclass exercising state + effects. ---

private data class CounterState(
    val count: Int = 0,
) : UiState

private sealed interface CounterIntent : UiIntent {
    data object Increment : CounterIntent

    data object Reset : CounterIntent
}

private sealed interface CounterEffect : UiEffect {
    data object DidReset : CounterEffect
}

private class CounterViewModel : MviViewModel<CounterState, CounterIntent, CounterEffect>(CounterState()) {
    override suspend fun onIntent(intent: CounterIntent) {
        when (intent) {
            CounterIntent.Increment -> setState { copy(count = count + 1) }
            CounterIntent.Reset -> {
                setState { copy(count = 0) }
                emit(CounterEffect.DidReset)
            }
        }
    }
}
