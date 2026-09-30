package dev.jdgarita.frnk.ui.app.shell

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * The sheet-presentation reducer behind [FrnkStackShell]: which sheets are up, and how many times each
 * has been presented (the key that gives every presentation a fresh ViewModel store). Follows the
 * `MviViewModelTest` template — `Dispatchers.setMain` so `viewModelScope` drives the intent collector.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class FrnkShellViewModelTest {
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
    fun present_shows_the_sheet_and_counts_the_presentation() =
        runTest(dispatcher) {
            val vm = FrnkShellViewModel()
            assertFalse(vm.state.value.isVisible(SHEET_A))
            assertEquals(0, vm.state.value.presentation(SHEET_A))

            vm.send(FrnkShellIntent.Present(SHEET_A))
            // A double-fired present (a rapid double tap) is one presentation, not a re-key of the
            // content already on screen.
            vm.send(FrnkShellIntent.Present(SHEET_A))

            assertTrue(vm.state.value.isVisible(SHEET_A))
            assertEquals(1, vm.state.value.presentation(SHEET_A))
        }

    @Test
    fun dismiss_hides_only_that_sheet() =
        runTest(dispatcher) {
            val vm = FrnkShellViewModel()
            vm.send(FrnkShellIntent.Present(SHEET_A))
            vm.send(FrnkShellIntent.Present(SHEET_B))

            vm.send(FrnkShellIntent.Dismiss(SHEET_A))
            // A single close can reach the shell more than once (see FrnkModalSheet); the second is a no-op.
            vm.send(FrnkShellIntent.Dismiss(SHEET_A))

            assertFalse(vm.state.value.isVisible(SHEET_A))
            assertTrue(vm.state.value.isVisible(SHEET_B))
            // Dismissing never re-keys: the content stays composed, unchanged, through its exit slide.
            assertEquals(1, vm.state.value.presentation(SHEET_A))
        }

    @Test
    fun presenting_again_bumps_the_presentation() =
        runTest(dispatcher) {
            val vm = FrnkShellViewModel()
            vm.send(FrnkShellIntent.Present(SHEET_A))
            vm.send(FrnkShellIntent.Dismiss(SHEET_A))

            vm.send(FrnkShellIntent.Present(SHEET_A))

            assertTrue(vm.state.value.isVisible(SHEET_A))
            assertEquals(2, vm.state.value.presentation(SHEET_A))
            assertEquals(0, vm.state.value.presentation(SHEET_B))
        }

    private companion object {
        const val SHEET_A = "a"
        const val SHEET_B = "b"
    }
}