package dev.jdgarita.frnk.ui.app

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.runAndroidComposeUiTest
import dev.jdgarita.frnk.ui.atoms.RobolectricComposeTest
import dev.jdgarita.frnk.ui.haptics.HapticFeedback
import dev.jdgarita.frnk.ui.haptics.HapticsPreference
import dev.jdgarita.frnk.ui.haptics.LocalFrnkHaptics
import dev.jdgarita.frnk.ui.haptics.PersistentHapticFeedback
import dev.jdgarita.frnk.ui.nav.FrnkRootRoute
import dev.jdgarita.frnk.ui.nav.frnkRootNavConfig
import dev.jdgarita.frnk.ui.theme.AppearanceController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.core.module.Module
import org.koin.dsl.module
import org.koin.dsl.navigation3.navigation
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * [FrnkApp]'s haptics wiring: with a [HapticsPreference] in the graph the ambient `LocalFrnkHaptics`
 * reads and writes it; without one it is the in-memory default, as before.
 */
@OptIn(ExperimentalTestApi::class, KoinExperimentalAPI::class)
class FrnkAppHapticsTest : RobolectricComposeTest() {
    private class FakeHapticsPreference(
        initial: Boolean
    ) : HapticsPreference {
        val writes = mutableListOf<Boolean>()
        private val state = MutableStateFlow(initial)
        override val isEnabled: StateFlow<Boolean> = state

        override fun setEnabled(enabled: Boolean) {
            writes += enabled
            state.value = enabled
        }
    }

    private var ambient: HapticFeedback? = null

    private fun probeModule(): Module =
        module {
            navigation<FrnkRootRoute.Onboarding> { ambient = LocalFrnkHaptics.current }
        }

    @AfterTest
    fun stopAppKoin() {
        stopKoin()
    }

    @Test
    fun a_bound_preference_drives_the_ambient_haptics() =
        runAndroidComposeUiTest<ComponentActivity> {
            val preference = FakeHapticsPreference(initial = false)
            startKoin {
                modules(
                    module {
                        single { AppearanceController() }
                        single<HapticsPreference> { preference }
                    }
                )
            }
            setContent {
                FrnkApp(onSavedStateConfiguration = { frnkRootNavConfig() }) { probeModule() }
            }
            waitForIdle()

            val haptics = assertNotNull(ambient)
            assertIs<PersistentHapticFeedback>(haptics)
            assertFalse(haptics.isEnabled.value, "the ambient starts at the stored switch")

            runOnIdle { haptics.setEnabled(true) }

            assertEquals(listOf(true), preference.writes, "the Settings toggle's call reaches the preference")
            assertTrue(haptics.isEnabled.value)
        }

    @Test
    fun without_a_preference_haptics_stay_on_and_in_memory() =
        runAndroidComposeUiTest<ComponentActivity> {
            startKoin { modules(module { single { AppearanceController() } }) }
            setContent {
                FrnkApp(onSavedStateConfiguration = { frnkRootNavConfig() }) { probeModule() }
            }
            waitForIdle()

            val haptics = assertNotNull(ambient)
            assertFalse(haptics is PersistentHapticFeedback)
            assertTrue(haptics.isEnabled.value)
        }
}