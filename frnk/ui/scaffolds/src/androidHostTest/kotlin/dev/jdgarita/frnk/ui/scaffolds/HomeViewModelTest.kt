package dev.jdgarita.frnk.ui.scaffolds

import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Plus
import dev.jdgarita.frnk.ui.atoms.FrnkTopAppBarAction
import dev.jdgarita.frnk.ui.atoms.FrnkTopAppBarState
import dev.jdgarita.frnk.ui.scaffolds.home.HomeEffect
import dev.jdgarita.frnk.ui.scaffolds.home.HomeIntent
import dev.jdgarita.frnk.ui.scaffolds.home.HomeScreenState
import dev.jdgarita.frnk.ui.scaffolds.home.HomeViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Reducer/effect tests for [dev.jdgarita.frnk.ui.scaffolds.home.HomeViewModel] — the pass-through home-chrome state machine. Follows the
 * `MviViewModelTest` template (shared-ui-api): `Dispatchers.setMain` so `viewModelScope` drives the
 * intent collector. Lives in `androidHostTest` because this module has no `commonTest` source set.
 */
class HomeViewModelTest {
    private val dispatcher = UnconfinedTestDispatcher()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun initialState() = HomeScreenState(topBar = FrnkTopAppBarState(title = "Home"))

    @Test
    fun top_bar_action_re_emits_action_invoked_with_the_action_key() =
        runTest(dispatcher) {
            val vm = HomeViewModel(initialState())
            val effect = async { vm.effects.first() }

            vm.send(
                HomeIntent.TopBarActionClicked(
                    FrnkTopAppBarAction(icon = Lucide.Plus, contentDescription = "Upgrade", key = "upgrade"),
                ),
            )

            assertEquals(HomeEffect.ActionInvoked("upgrade"), effect.await())
        }

    @Test
    fun navigation_click_re_emits_navigation_invoked() =
        runTest(dispatcher) {
            val vm = HomeViewModel(initialState())
            val effect = async { vm.effects.first() }

            vm.send(HomeIntent.NavigationClicked)

            assertEquals(HomeEffect.NavigationInvoked, effect.await())
        }

    @Test
    fun intents_leave_the_chrome_state_untouched() =
        runTest(dispatcher) {
            val initial = initialState()
            val vm = HomeViewModel(initial)
            val effect = async { vm.effects.first() }

            vm.send(HomeIntent.NavigationClicked)
            effect.await()

            assertEquals(initial, vm.state.value)
        }
}
