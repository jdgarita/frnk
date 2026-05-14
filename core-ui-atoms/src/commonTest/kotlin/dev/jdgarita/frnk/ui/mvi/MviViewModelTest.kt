package dev.jdgarita.frnk.ui.mvi

import app.cash.turbine.test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

private data class CounterState(
    val count: Int = 0,
) : UiState

private sealed interface CounterAction : UiAction {
    data object Increment : CounterAction
}

private sealed interface CounterEffect : UiEffect {
    data object Maxed : CounterEffect
}

private class CounterVm : MviViewModel<CounterState, CounterAction, CounterEffect>(CounterState()) {
    override fun reduce(
        current: CounterState,
        action: CounterAction,
    ): CounterState =
        when (action) {
            CounterAction.Increment -> current.copy(count = current.count + 1)
        }

    override suspend fun onAction(action: CounterAction) {
        if (state.value.count >= 2) emitEffect(CounterEffect.Maxed)
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class MviViewModelTest {
    @BeforeTest fun setUp() = kotlinx.coroutines.Dispatchers.setMain(UnconfinedTestDispatcher())

    @AfterTest fun tearDown() = kotlinx.coroutines.Dispatchers.resetMain()

    @Test
    fun reducerAndEffects() =
        runTest {
            val vm = CounterVm()
            vm.state.test {
                assertEquals(0, awaitItem().count)
                vm.dispatch(CounterAction.Increment)
                assertEquals(1, awaitItem().count)
            }
        }
}
