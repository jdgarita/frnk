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
 * Verifies the [MviViewModel] engine via a minimal in-test subclass: the initial UiState is the
 * factory's model mapped through [MviViewModel.mapToUiState]; mutating the model via
 * [MviViewModel.updateModel] re-derives the UiState automatically; and an intent emits a one-shot
 * effect. The reusable template for testing real model-first reducers.
 */
class MviViewModelTest {
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
    fun initial_ui_state_is_mapped_from_the_factory_model() =
        runTest(dispatcher) {
            val vm = SampleViewModel()

            // Factory seeds count = 7; the mapper decorates it as "Count: 7".
            assertEquals(7, vm.state.value.count)
            assertEquals("Count: 7", vm.state.value.label)
        }

    @Test
    fun updating_the_model_re_derives_the_ui_state() =
        runTest(dispatcher) {
            val vm = SampleViewModel()

            vm.send(SampleIntent.Increment)
            vm.send(SampleIntent.Increment)
            runCurrent()

            assertEquals(9, vm.state.value.count)
            assertEquals("Count: 9", vm.state.value.label)
        }

    @Test
    fun attach_seeds_the_model_from_arguments() =
        runTest(dispatcher) {
            val vm = SampleViewModel()

            // Before attach the model is the factory seed (7); attach overwrites it from the arguments.
            assertEquals(7, vm.state.value.count)
            vm.attach(SampleArguments(startCount = 20))
            runCurrent()

            assertEquals(20, vm.state.value.count)
            assertEquals("Count: 20", vm.state.value.label)
        }

    @Test
    fun attach_runs_only_once() =
        runTest(dispatcher) {
            val vm = SampleViewModel()

            vm.attach(SampleArguments(startCount = 20))
            vm.attach(SampleArguments(startCount = 99)) // ignored — already attached
            runCurrent()

            assertEquals(20, vm.state.value.count)
        }

    @Test
    fun reset_intent_re_derives_state_and_emits_effect() =
        runTest(dispatcher) {
            val vm = SampleViewModel()
            vm.send(SampleIntent.Increment)
            runCurrent()

            val effect = async { vm.effects.first() }
            runCurrent()

            vm.send(SampleIntent.Reset)
            runCurrent()

            assertEquals(0, vm.state.value.count)
            assertEquals("Count: 0", vm.state.value.label)
            assertEquals(SampleEffect.DidReset, effect.await())
        }
}

// --- Test fixtures: a trivial MviViewModel exercising model → UiState mapping + effects. ---

private data class SampleArguments(
    val startCount: Int
) : Arguments

private data class SampleModel(
    val count: Int = 7
) : ModelState

private object SampleModelFactory : ModelStateFactory<SampleModel> {
    override fun initialModelState() = SampleModel()
}

private data class SampleUiState(
    val count: Int,
    val label: String
) : UiState

private sealed interface SampleIntent : UiIntent {
    data object Increment : SampleIntent

    data object Reset : SampleIntent
}

private sealed interface SampleEffect : UiEffect {
    data object DidReset : SampleEffect
}

private class SampleViewModel :
    MviViewModel<SampleArguments, SampleModel, SampleUiState, SampleIntent, SampleEffect>(SampleModelFactory) {
    override fun onAttached(arguments: SampleArguments) {
        updateModel { copy(count = arguments.startCount) }
    }

    override fun mapToUiState(modelState: SampleModel) = SampleUiState(count = modelState.count, label = "Count: ${modelState.count}")

    override suspend fun onIntent(intent: SampleIntent) {
        when (intent) {
            SampleIntent.Increment -> updateModel { copy(count = count + 1) }
            SampleIntent.Reset -> {
                updateModel { copy(count = 0) }
                emit(SampleEffect.DidReset)
            }
        }
    }
}