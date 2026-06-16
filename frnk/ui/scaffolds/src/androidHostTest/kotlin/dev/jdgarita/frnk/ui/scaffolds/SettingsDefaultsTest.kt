package dev.jdgarita.frnk.ui.scaffolds

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.runComposeUiTest
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Plus
import dev.jdgarita.frnk.ui.atoms.FrnkIconState
import dev.jdgarita.frnk.ui.atoms.RobolectricComposeTest
import dev.jdgarita.frnk.ui.atoms.setFrnkContent
import dev.jdgarita.frnk.ui.scaffolds.settings.SettingsAction
import dev.jdgarita.frnk.ui.scaffolds.settings.SettingsClickableRowState
import dev.jdgarita.frnk.ui.scaffolds.settings.SettingsExtraSectionsPlacement
import dev.jdgarita.frnk.ui.scaffolds.settings.SettingsScreenState
import dev.jdgarita.frnk.ui.scaffolds.settings.SettingsSectionState
import dev.jdgarita.frnk.ui.scaffolds.settings.rememberDefaultSettingsState
import dev.jdgarita.frnk.ui.theme.Appearance
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

/**
 * Verifies `rememberDefaultSettingsState`'s `extraSections` injection: each
 * [dev.jdgarita.frnk.ui.scaffolds.settings.SettingsExtraSectionsPlacement] slots the host sections at the right point in the default
 * catalogue (Appearance → Preferences → Subscription → Support → Legal). Needs a real composition
 * (theme-token resolution inside `remember`), so it runs as a Robolectric Compose host test.
 */
@OptIn(ExperimentalTestApi::class)
class SettingsDefaultsTest : RobolectricComposeTest() {
    private val extra =
        SettingsSectionState(
            title = "Host Section",
            rows =
                listOf(
                    SettingsClickableRowState(
                        id = "host_row",
                        icon = FrnkIconState.Content(imageVector = Lucide.Plus, contentDescription = null),
                        title = "Host Row",
                        action = SettingsAction.Custom("host_row"),
                    ),
                ),
        )

    private fun buildState(placement: SettingsExtraSectionsPlacement): SettingsScreenState {
        lateinit var state: SettingsScreenState
        runComposeUiTest {
            setFrnkContent {
                state =
                    rememberDefaultSettingsState(
                        version = "v1.0",
                        appearance = Appearance.System,
                        extraSections = listOf(extra),
                        extraSectionsPlacement = placement,
                    )
            }
        }
        return state
    }

    // Default catalogue order: [0] Appearance, [1] Preferences, [2] Subscription, [3] Support, [4] Legal.

    @Test
    fun after_appearance_inserts_at_index_1() {
        val state = buildState(SettingsExtraSectionsPlacement.AfterAppearance)
        assertEquals(1, state.sections.indexOf(extra))
        assertEquals(6, state.sections.size)
    }

    @Test
    fun before_subscription_inserts_after_preferences() {
        val state = buildState(SettingsExtraSectionsPlacement.BeforeSubscription)
        assertEquals(2, state.sections.indexOf(extra))
    }

    @Test
    fun before_legal_inserts_second_to_last() {
        val state = buildState(SettingsExtraSectionsPlacement.BeforeLegal)
        assertEquals(state.sections.size - 2, state.sections.indexOf(extra))
    }

    @Test
    fun end_inserts_last() {
        val state = buildState(SettingsExtraSectionsPlacement.End)
        assertEquals(state.sections.size - 1, state.sections.indexOf(extra))
    }

    @Test
    fun no_extras_keeps_the_default_catalogue_unchanged() {
        lateinit var state: SettingsScreenState
        runComposeUiTest {
            setFrnkContent {
                state = rememberDefaultSettingsState(version = "v1.0", appearance = Appearance.System)
            }
        }
        assertEquals(5, state.sections.size)
        assertFalse(extra in state.sections)
    }
}
