package dev.jdgarita.frnk.ui.mvi

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.withContext
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
    fun initial_model_is_created_exactly_once() =
        runTest(dispatcher) {
            val factory = CountingSampleModelFactory()

            SampleViewModel(factory)

            assertEquals(1, factory.invocationCount)
        }

    @Test
    fun effects_retain_the_feature_effect_type() =
        runTest(dispatcher) {
            val vm = SampleViewModel()

            val typedEffects: Flow<SampleEffect> = vm.effects

            val effect = async { typedEffects.first() }
            vm.send(SampleIntent.Reset)
            runCurrent()
            assertEquals(SampleEffect.DidReset, effect.await())
        }

    @Test
    fun intents_are_processed_in_order_without_dropping_bursts() =
        runTest(dispatcher) {
            val vm = OrderedIntentViewModel()

            vm.send(OrderedIntent.Value(0))
            vm.firstIntentStarted.await()
            (1..64).forEach { vm.send(OrderedIntent.Value(it)) }
            vm.releaseFirstIntent.complete(Unit)
            advanceUntilIdle()

            assertEquals((0..64).toList(), vm.received)
        }

    @Test
    fun concurrent_model_updates_are_atomic() =
        runTest(dispatcher) {
            val vm = SampleViewModel()

            vm.incrementConcurrently(1_000)
            advanceUntilIdle()

            assertEquals(1_007, vm.state.value.count)
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

private class CountingSampleModelFactory : ModelStateFactory<SampleModel> {
    var invocationCount = 0
        private set

    override fun initialModelState(): SampleModel {
        invocationCount += 1
        return SampleModel()
    }
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

private class SampleViewModel(
    factory: ModelStateFactory<SampleModel> = SampleModelFactory
) : MviViewModel<SampleArguments, SampleModel, SampleUiState, SampleIntent, SampleEffect>(
        factory = factory,
        mapper = { SampleUiState(count = it.count, label = "Count: ${it.count}") }
    ) {
    suspend fun incrementConcurrently(times: Int) {
        withContext(Dispatchers.Default) {
            coroutineScope {
                repeat(times) {
                    launch { updateModel { copy(count = count + 1) } }
                }
            }
        }
    }

    override fun onAttached(arguments: SampleArguments) {
        updateModel { copy(count = arguments.startCount) }
    }

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

private data object OrderedArguments : Arguments

private data class OrderedModel(
    val count: Int = 0
) : ModelState

private data class OrderedState(
    val count: Int
) : UiState

private object OrderedModelFactory : ModelStateFactory<OrderedModel> {
    override fun initialModelState() = OrderedModel()
}

private sealed interface OrderedIntent : UiIntent {
    data class Value(
        val value: Int
    ) : OrderedIntent
}

private data object OrderedEffect : UiEffect

private class OrderedIntentViewModel :
    MviViewModel<OrderedArguments, OrderedModel, OrderedState, OrderedIntent, OrderedEffect>(
        factory = OrderedModelFactory,
        mapper = { OrderedState(it.count) }
    ) {
    val firstIntentStarted = CompletableDeferred<Unit>()
    val releaseFirstIntent = CompletableDeferred<Unit>()
    val received = mutableListOf<Int>()

    override suspend fun onIntent(intent: OrderedIntent) {
        when (intent) {
            is OrderedIntent.Value -> {
                if (intent.value == 0) {
                    firstIntentStarted.complete(Unit)
                    releaseFirstIntent.await()
                }
                received += intent.value
            }
        }
    }
}