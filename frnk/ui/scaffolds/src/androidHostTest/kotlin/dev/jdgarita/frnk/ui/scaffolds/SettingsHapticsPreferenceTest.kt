package dev.jdgarita.frnk.ui.scaffolds

import dev.jdgarita.frnk.monetization.usecase.ObserveProStatusUseCase
import dev.jdgarita.frnk.ui.haptics.HAPTICS_TOGGLE_ID
import dev.jdgarita.frnk.ui.haptics.HapticsPreference
import dev.jdgarita.frnk.ui.scaffolds.settings.SettingsArguments
import dev.jdgarita.frnk.ui.scaffolds.settings.SettingsIntent
import dev.jdgarita.frnk.ui.scaffolds.settings.SettingsScreenState
import dev.jdgarita.frnk.ui.scaffolds.settings.SettingsToggleRowState
import dev.jdgarita.frnk.ui.scaffolds.settings.SettingsViewModel
import dev.jdgarita.frnk.ui.scaffolds.settings.settingsScaffoldModule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.koin.dsl.koinApplication
import org.koin.dsl.module
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * The Settings haptics row and the stored switch (A5) must agree: a fresh ViewModel shows the stored
 * value (not the catalogue's hardcoded "on"), follows later writes, and keeps it across a
 * `ConfigChanged` whose seed says otherwise.
 */
class SettingsHapticsPreferenceTest {
    private val dispatcher = UnconfinedTestDispatcher()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private class FakePreference(
        initial: Boolean
    ) : HapticsPreference {
        private val state = MutableStateFlow(initial)
        override val isEnabled: StateFlow<Boolean> = state

        override fun setEnabled(enabled: Boolean) {
            state.value = enabled
        }
    }

    private val proStatus = ObserveProStatusUseCase { MutableStateFlow(false) }

    private fun SettingsScreenState.hapticsChecked(): Boolean =
        sections
            .flatMap { it.rows }
            .filterIsInstance<SettingsToggleRowState>()
            .single { it.id == HAPTICS_TOGGLE_ID }
            .checked

    @Test
    fun a_fresh_view_model_shows_the_stored_switch() {
        val vm = SettingsViewModel(proStatus, hapticsPreference = FakePreference(initial = false))

        assertFalse(vm.state.value.hapticsChecked(), "the row starts at the stored value, before attach")
    }

    @Test
    fun the_row_follows_a_write_to_the_preference() {
        val preference = FakePreference(initial = true)
        val vm = SettingsViewModel(proStatus, hapticsPreference = preference).also { it.attach(SettingsArguments) }

        preference.setEnabled(false)
        assertFalse(vm.state.value.hapticsChecked())

        preference.setEnabled(true)
        assertTrue(vm.state.value.hapticsChecked())
    }

    @Test
    fun a_config_change_seeded_on_keeps_the_stored_off() {
        val vm = SettingsViewModel(proStatus, hapticsPreference = FakePreference(initial = false)).also { it.attach(SettingsArguments) }
        vm.send(SettingsIntent.ConfigChanged(vm.state.value.withHaptics(true)))

        assertFalse(vm.state.value.hapticsChecked())
    }

    @Test
    fun without_a_preference_the_row_keeps_the_catalogue_default() {
        val vm = SettingsViewModel(proStatus)

        assertTrue(vm.state.value.hapticsChecked())
    }

    @Test
    fun the_scaffold_module_injects_a_bound_preference() {
        val koin =
            koinApplication {
                modules(
                    module {
                        single { proStatus }
                        single<HapticsPreference> { FakePreference(initial = false) }
                    },
                    settingsScaffoldModule
                )
            }.koin

        assertEquals(
            false,
            koin
                .get<SettingsViewModel>()
                .state.value
                .hapticsChecked()
        )
    }

    private fun SettingsScreenState.withHaptics(checked: Boolean): SettingsScreenState =
        copy(
            sections =
                sections.map { section ->
                    section.copy(
                        rows =
                            section.rows.map { row ->
                                if (row is SettingsToggleRowState && row.id == HAPTICS_TOGGLE_ID) row.copy(checked = checked) else row
                            }
                    )
                }
        )
}