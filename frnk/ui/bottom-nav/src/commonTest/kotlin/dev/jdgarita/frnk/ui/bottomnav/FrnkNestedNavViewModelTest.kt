package dev.jdgarita.frnk.ui.bottomnav

import dev.jdgarita.frnk.ui.nav.FrnkRoute
import dev.jdgarita.frnk.ui.nav.navigateTo
import dev.jdgarita.frnk.ui.theme.FrnkIconSource
import dev.jdgarita.frnk.ui.theme.iconNavComponent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

/**
 * Verifies [FrnkNestedNavViewModel]'s per-tab back-stack ownership: tab selection swaps the live
 * [FrnkNestedNavViewModel.backStack] in/out, each tab keeps its own history across switches, re-tapping
 * the active tab pops it to root, and back at a non-Home tab root returns to Home.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class FrnkNestedNavViewModelTest {
    private val dispatcher = UnconfinedTestDispatcher()

    private val componentsRoot = FrnkRoute.Custom("Components")
    private val componentDetail = FrnkRoute.Custom("Detail")

    private fun viewModel() =
        FrnkNestedNavViewModel().apply {
            attach(
                arguments =
                    FrnkNestedNavArguments(
                        customTab =
                            FrnkCustomTab(
                                route = componentsRoot,
                                icon = FrnkIconSource.Token(iconNavComponent),
                                iosSystemIcon = "square.grid.2x2",
                                label = "Components"
                            )
                    )
            )
        }

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun seeds_three_tabs_with_home_selected_and_home_root() =
        runTest(dispatcher) {
            val vm = viewModel()
            runCurrent()

            assertEquals(3, vm.state.value.items.size)
            assertEquals(0, vm.state.value.selectedIndex)
            assertContentEquals(listOf(FrnkRoute.Home), vm.backStack.toList())
        }

    @Test
    fun tapping_a_tab_switches_selection_and_swaps_to_that_tab_root() =
        runTest(dispatcher) {
            val vm = viewModel()

            vm.send(FrnkNestedNavIntent.Tap(index = 1))
            runCurrent()

            assertEquals(1, vm.state.value.selectedIndex)
            assertContentEquals(listOf(componentsRoot), vm.backStack.toList())
        }

    @Test
    fun each_tab_keeps_its_own_history_across_switches() =
        runTest(dispatcher) {
            val vm = viewModel()

            // Enter Components and push a detail (host within-tab navigation on the live stack).
            vm.send(FrnkNestedNavIntent.Tap(index = 1))
            runCurrent()
            vm.backStack.navigateTo(componentDetail)
            assertContentEquals(listOf(componentsRoot, componentDetail), vm.backStack.toList())

            // Switch to Settings — Components' history is saved, Settings shows its root.
            vm.send(FrnkNestedNavIntent.Tap(index = 2))
            runCurrent()
            assertContentEquals(listOf(FrnkRoute.Settings), vm.backStack.toList())

            // Back to Components — its detail is restored.
            vm.send(FrnkNestedNavIntent.Tap(index = 1))
            runCurrent()
            assertContentEquals(listOf(componentsRoot, componentDetail), vm.backStack.toList())
        }

    @Test
    fun re_tapping_the_active_tab_pops_it_to_root() =
        runTest(dispatcher) {
            val vm = viewModel()

            vm.send(FrnkNestedNavIntent.Tap(index = 1))
            runCurrent()
            vm.backStack.navigateTo(componentDetail)

            vm.send(FrnkNestedNavIntent.Tap(index = 1))
            runCurrent()

            assertEquals(1, vm.state.value.selectedIndex)
            assertContentEquals(listOf(componentsRoot), vm.backStack.toList())
        }

    @Test
    fun back_pops_within_tab_when_history_is_deeper_than_root() =
        runTest(dispatcher) {
            val vm = viewModel()

            vm.send(FrnkNestedNavIntent.Tap(index = 1))
            runCurrent()
            vm.backStack.navigateTo(componentDetail)

            vm.send(FrnkNestedNavIntent.Back)
            runCurrent()

            assertEquals(1, vm.state.value.selectedIndex)
            assertContentEquals(listOf(componentsRoot), vm.backStack.toList())
        }

    @Test
    fun back_at_a_non_home_tab_root_returns_to_home() =
        runTest(dispatcher) {
            val vm = viewModel()

            vm.send(FrnkNestedNavIntent.Tap(index = 2))
            runCurrent()

            vm.send(FrnkNestedNavIntent.Back)
            runCurrent()

            assertEquals(0, vm.state.value.selectedIndex)
            assertContentEquals(listOf(FrnkRoute.Home), vm.backStack.toList())
        }
}