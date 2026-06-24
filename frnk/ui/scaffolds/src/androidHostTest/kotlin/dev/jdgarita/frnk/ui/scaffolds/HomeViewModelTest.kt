package dev.jdgarita.frnk.ui.scaffolds

import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Plus
import dev.jdgarita.frnk.monetization.usecase.ObserveProStatusUseCase
import dev.jdgarita.frnk.ui.atoms.FrnkTopAppBarAction
import dev.jdgarita.frnk.ui.scaffolds.home.HomeEffect
import dev.jdgarita.frnk.ui.scaffolds.home.HomeIntent
import dev.jdgarita.frnk.ui.scaffolds.home.HomeViewModel
import dev.jdgarita.frnk.ui.theme.FrnkIconSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
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
 * Reducer/effect tests for [dev.jdgarita.frnk.ui.scaffolds.home.HomeViewModel] — the home-chrome state
 * machine. Model-first: the VM is constructed with the [ObserveProStatusUseCase] and seeds its model
 * from [dev.jdgarita.frnk.ui.scaffolds.home.HomeModelStateFactory] (top-bar title "Home", Free), so the
 * derived [dev.jdgarita.frnk.ui.scaffolds.home.HomeScreenState] is valid without an `attach`. Follows
 * the `MviViewModelTest` template: `Dispatchers.setMain` so `viewModelScope` drives the intent
 * collector. Lives in `androidHostTest` because this module has no `commonTest` source set.
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

    // Stand-in for the Koin-resolved use case; the chrome reducer only mirrors it into `isPro`.
    private fun proStatus(isPro: Boolean = false) = ObserveProStatusUseCase { MutableStateFlow(isPro) }

    @Test
    fun top_bar_action_re_emits_action_invoked_with_the_action_key() =
        runTest(dispatcher) {
            val vm = HomeViewModel(proStatus())
            val effect = async { vm.effects.first() }

            vm.send(
                HomeIntent.TopBarActionClicked(
                    FrnkTopAppBarAction(icon = FrnkIconSource.Vector(Lucide.Plus), contentDescription = "Upgrade", key = "upgrade")
                )
            )

            assertEquals(HomeEffect.ActionInvoked("upgrade"), effect.await())
        }

    @Test
    fun navigation_click_re_emits_navigation_invoked() =
        runTest(dispatcher) {
            val vm = HomeViewModel(proStatus())
            val effect = async { vm.effects.first() }

            vm.send(HomeIntent.NavigationClicked)

            assertEquals(HomeEffect.NavigationInvoked, effect.await())
        }

    @Test
    fun intents_leave_the_chrome_state_untouched() =
        runTest(dispatcher) {
            val vm = HomeViewModel(proStatus())
            val before = vm.state.value
            val effect = async { vm.effects.first() }

            vm.send(HomeIntent.NavigationClicked)
            effect.await()

            assertEquals(before, vm.state.value)
        }
}