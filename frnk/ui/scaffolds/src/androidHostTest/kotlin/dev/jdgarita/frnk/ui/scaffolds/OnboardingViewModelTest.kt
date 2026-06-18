package dev.jdgarita.frnk.ui.scaffolds

import dev.jdgarita.frnk.ui.atoms.FrnkTextState
import dev.jdgarita.frnk.ui.scaffolds.onboarding.OnboardingArguments
import dev.jdgarita.frnk.ui.scaffolds.onboarding.OnboardingEffect
import dev.jdgarita.frnk.ui.scaffolds.onboarding.OnboardingIntent
import dev.jdgarita.frnk.ui.scaffolds.onboarding.OnboardingPageState
import dev.jdgarita.frnk.ui.scaffolds.onboarding.OnboardingViewModel
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
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

/**
 * Reducer/effect tests for the model-first [OnboardingViewModel]. The pages arrive as
 * [OnboardingArguments] at attach time (no constructor seed), so every test `attach`es first. Follows the
 * `ModelMviViewModelTest`/`HomeViewModelTest` template: `Dispatchers.setMain` so `viewModelScope` drives
 * the intent collector. Lives in `androidHostTest` because this module has no `commonTest` source set.
 */
class OnboardingViewModelTest {
    private val dispatcher = UnconfinedTestDispatcher()

    private fun pages(count: Int): List<OnboardingPageState> =
        List(count) { OnboardingPageState(title = FrnkTextState.Title(text = "Page $it")) }

    private fun attachedVm(pageCount: Int = 3): OnboardingViewModel =
        OnboardingViewModel().also { it.attach(OnboardingArguments(pages = pages(pageCount))) }

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun empty_pages_fails_fast_at_construction() {
        // The non-empty invariant is enforced at the input boundary (OnboardingArguments), so an
        // onboarding flow with no pages fails loudly here instead of producing a broken 0-page pager.
        assertFailsWith<IllegalArgumentException> { OnboardingArguments(pages = pages(0)) }
    }

    @Test
    fun attach_seeds_pages_and_starts_on_the_first_page() =
        runTest(dispatcher) {
            val vm = attachedVm(pageCount = 3)
            runCurrent()

            assertEquals(3, vm.state.value.pages.size)
            assertEquals(0, vm.state.value.currentPageIndex)
            assertTrue(vm.state.value.isFirstPage)
        }

    @Test
    fun next_advances_one_page() =
        runTest(dispatcher) {
            val vm = attachedVm(pageCount = 3)

            vm.send(OnboardingIntent.NextClicked)
            runCurrent()

            assertEquals(1, vm.state.value.currentPageIndex)
            assertEquals(false, vm.state.value.isLastPage)
        }

    @Test
    fun next_on_the_last_page_emits_completed() =
        runTest(dispatcher) {
            val vm = attachedVm(pageCount = 2)
            vm.send(OnboardingIntent.NextClicked) // → page 1 (last)
            runCurrent()
            assertTrue(vm.state.value.isLastPage)

            val effect = async { vm.effects.first() }
            runCurrent()
            vm.send(OnboardingIntent.NextClicked) // last page → Completed, index unchanged
            runCurrent()

            assertEquals(OnboardingEffect.Completed, effect.await())
            assertEquals(1, vm.state.value.currentPageIndex)
        }

    @Test
    fun previous_clamps_at_the_first_page() =
        runTest(dispatcher) {
            val vm = attachedVm(pageCount = 3)

            vm.send(OnboardingIntent.PreviousClicked)
            runCurrent()

            assertEquals(0, vm.state.value.currentPageIndex)
        }

    @Test
    fun page_selected_jumps_to_the_clamped_index() =
        runTest(dispatcher) {
            val vm = attachedVm(pageCount = 3)

            vm.send(OnboardingIntent.PageSelected(index = 5)) // beyond range → clamps to lastIndex (2)
            runCurrent()

            assertEquals(2, vm.state.value.currentPageIndex)
            assertTrue(vm.state.value.isLastPage)
        }

    @Test
    fun close_emits_close_requested() =
        runTest(dispatcher) {
            val vm = attachedVm(pageCount = 3)

            val effect = async { vm.effects.first() }
            runCurrent()
            vm.send(OnboardingIntent.CloseClicked)
            runCurrent()

            assertEquals(OnboardingEffect.CloseRequested, effect.await())
        }
}
