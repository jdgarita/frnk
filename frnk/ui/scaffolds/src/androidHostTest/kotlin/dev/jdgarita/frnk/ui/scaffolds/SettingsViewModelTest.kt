package dev.jdgarita.frnk.ui.scaffolds

import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Plus
import dev.jdgarita.frnk.monetization.usecase.ObserveProStatusUseCase
import dev.jdgarita.frnk.ui.atoms.FrnkIconState
import dev.jdgarita.frnk.ui.scaffolds.settings.SettingsAction
import dev.jdgarita.frnk.ui.scaffolds.settings.SettingsClickableRowState
import dev.jdgarita.frnk.ui.scaffolds.settings.SettingsIntent
import dev.jdgarita.frnk.ui.scaffolds.settings.SettingsRowState
import dev.jdgarita.frnk.ui.scaffolds.settings.SettingsScreenState
import dev.jdgarita.frnk.ui.scaffolds.settings.SettingsSectionState
import dev.jdgarita.frnk.ui.scaffolds.settings.SettingsStatusRowState
import dev.jdgarita.frnk.ui.scaffolds.settings.SettingsThemeRowState
import dev.jdgarita.frnk.ui.scaffolds.settings.SettingsToggleRowState
import dev.jdgarita.frnk.ui.scaffolds.settings.SettingsViewModel
import dev.jdgarita.frnk.ui.theme.Appearance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Reducer tests for [SettingsViewModel.SettingsIntent.ConfigChanged][dev.jdgarita.frnk.ui.scaffolds.settings.SettingsIntent.ConfigChanged] —
 * the merge that replaces the old "re-key the ViewModel on isPro" approach. Verifies the VM adopts an
 * externally-recomputed catalogue (Free→Pro subscription rows) while preserving its own interaction
 * state (toggle `checked`, theme selection, dev-reveal). Follows the `MviViewModelTest` template:
 * `Dispatchers.setMain` so `viewModelScope` drives the intent collector. Lives in `androidHostTest`
 * because this module has no `commonTest` source set.
 */
class SettingsViewModelTest {
    private val dispatcher = UnconfinedTestDispatcher()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private val icon = FrnkIconState.Content(imageVector = Lucide.Plus, contentDescription = null)

    private fun freeSubscriptionRows() =
        listOf(
            SettingsClickableRowState(
                id = "upgrade_to_pro",
                icon = icon,
                title = "Upgrade",
                action = SettingsAction.UpgradeToPro,
            ),
            SettingsClickableRowState(
                id = "restore_purchases",
                icon = icon,
                title = "Restore",
                action = SettingsAction.RestorePurchases,
            ),
        )

    private fun proSubscriptionRows() =
        listOf(
            SettingsStatusRowState(id = "pro_member", icon = icon, title = "Pro Member", badge = "PRO"),
            SettingsClickableRowState(
                id = "manage_subscription",
                icon = icon,
                title = "Manage",
                action = SettingsAction.ManageSubscription,
            ),
        )

    private fun state(
        subscriptionRows: List<SettingsRowState>,
        notificationsChecked: Boolean = true,
        theme: Appearance = Appearance.System,
    ) = SettingsScreenState(
        title = "",
        sections =
            listOf(
                SettingsSectionState(
                    rows =
                        listOf(
                            SettingsThemeRowState(
                                title = "Appearance",
                                selected = theme,
                                optionLabels = listOf("System", "Light", "Dark"),
                            ),
                        ),
                ),
                SettingsSectionState(
                    title = "Preferences",
                    rows =
                        listOf(
                            SettingsToggleRowState(
                                id = "notifications",
                                icon = icon,
                                title = "Notifications",
                                checked = notificationsChecked,
                            ),
                        ),
                ),
                SettingsSectionState(title = "Subscription", rows = subscriptionRows),
            ),
    )

    private fun SettingsScreenState.row(id: String): SettingsRowState? = sections.flatMap { it.rows }.firstOrNull { it.id == id }

    private fun SettingsScreenState.theme(): Appearance =
        sections
            .flatMap { it.rows }
            .filterIsInstance<SettingsThemeRowState>()
            .first()
            .selected

    // Stand-in for the Koin-resolved use case; the reducer tests don't care about its value.
    private fun proStatus(isPro: Boolean = false) = ObserveProStatusUseCase { MutableStateFlow(isPro) }

    @Test
    fun config_changed_adopts_the_new_subscription_rows() =
        runTest(dispatcher) {
            val vm = SettingsViewModel(state(freeSubscriptionRows()), proStatus())

            vm.send(SettingsIntent.ConfigChanged(state(proSubscriptionRows())))
            runCurrent()

            val s = vm.state.value
            assertTrue(s.row("pro_member") is SettingsStatusRowState)
            assertEquals(SettingsAction.ManageSubscription, (s.row("manage_subscription") as SettingsClickableRowState).action)
            assertNull(s.row("upgrade_to_pro"))
        }

    @Test
    fun config_changed_preserves_a_user_flipped_toggle_over_the_incoming_default() =
        runTest(dispatcher) {
            val vm = SettingsViewModel(state(freeSubscriptionRows(), notificationsChecked = true), proStatus())
            vm.send(SettingsIntent.ToggleChanged("notifications", false))
            runCurrent()

            // Upstream recomputes (e.g. isPro flipped) carrying the *default* (true) — the VM's value wins.
            vm.send(SettingsIntent.ConfigChanged(state(proSubscriptionRows(), notificationsChecked = true)))
            runCurrent()

            assertFalse((vm.state.value.row("notifications") as SettingsToggleRowState).checked)
            assertTrue(vm.state.value.row("pro_member") is SettingsStatusRowState)
        }

    @Test
    fun toggling_a_developer_section_row_updates_state_and_survives_config_changed() =
        runTest(dispatcher) {
            val devSection = { checked: Boolean ->
                SettingsSectionState(
                    title = "Developer",
                    rows =
                        listOf(
                            SettingsToggleRowState(
                                id = "god_mode",
                                icon = icon,
                                title = "God mode",
                                checked = checked,
                            ),
                        ),
                )
            }
            val vm =
                SettingsViewModel(
                    state(freeSubscriptionRows()).copy(
                        developerSection = devSection(false),
                        showDeveloperSection = true,
                    ),
                    proStatus(),
                )

            // Tapping the dev-section toggle must flip it in state (regression: mapRows skipped developerSection).
            vm.send(SettingsIntent.ToggleChanged("god_mode", true))
            runCurrent()
            assertTrue(
                (
                    vm.state.value.developerSection
                        ?.rows
                        ?.first() as SettingsToggleRowState
                ).checked,
            )

            // The round-trip then re-supplies the catalogue (god mode → Pro). The merge keeps the toggle on.
            vm.send(
                SettingsIntent.ConfigChanged(
                    state(proSubscriptionRows()).copy(developerSection = devSection(true), showDeveloperSection = true),
                ),
            )
            runCurrent()
            assertTrue(
                (
                    vm.state.value.developerSection
                        ?.rows
                        ?.first() as SettingsToggleRowState
                ).checked,
            )
            assertTrue(vm.state.value.row("pro_member") is SettingsStatusRowState)
        }

    @Test
    fun config_changed_preserves_dev_reveal_progress() =
        runTest(dispatcher) {
            val vm = SettingsViewModel(state(freeSubscriptionRows()), proStatus())
            repeat(SettingsScreenState.DEVELOPER_REVEAL_TAPS) { vm.send(SettingsIntent.VersionTapped) }
            runCurrent()
            assertTrue(vm.state.value.developerRevealed)

            vm.send(SettingsIntent.ConfigChanged(state(proSubscriptionRows())))
            runCurrent()

            assertTrue(vm.state.value.developerRevealed)
            assertEquals(SettingsScreenState.DEVELOPER_REVEAL_TAPS, vm.state.value.versionTapCount)
        }

    @Test
    fun config_changed_adopts_the_incoming_theme_so_external_appearance_changes_apply() =
        runTest(dispatcher) {
            val vm = SettingsViewModel(state(freeSubscriptionRows(), theme = Appearance.Dark), proStatus())

            // Appearance changed outside the Settings toggle (the controller is the source of truth) and
            // arrives via the recomputed catalogue — the segmented control must follow it.
            vm.send(SettingsIntent.ConfigChanged(state(proSubscriptionRows(), theme = Appearance.Light)))
            runCurrent()

            assertEquals(Appearance.Light, vm.state.value.theme())
        }

    @Test
    fun is_pro_reflects_the_injected_use_case() =
        runTest(dispatcher) {
            val pro = SettingsViewModel(state(proSubscriptionRows()), proStatus(isPro = true))
            val free = SettingsViewModel(state(freeSubscriptionRows()), proStatus(isPro = false))

            assertTrue(pro.isPro.value)
            assertFalse(free.isPro.value)
        }

    @Test
    fun a_theme_tap_round_trips_through_incoming_without_reverting() =
        runTest(dispatcher) {
            val vm = SettingsViewModel(state(freeSubscriptionRows(), theme = Appearance.System), proStatus())
            // Optimistic feedback from the ThemeSelected reducer.
            vm.send(SettingsIntent.ThemeSelected(Appearance.Dark))
            runCurrent()
            assertEquals(Appearance.Dark, vm.state.value.theme())

            // The controller catches up and the catalogue recomputes carrying Dark — no revert.
            vm.send(SettingsIntent.ConfigChanged(state(proSubscriptionRows(), theme = Appearance.Dark)))
            runCurrent()
            assertEquals(Appearance.Dark, vm.state.value.theme())
        }
}
