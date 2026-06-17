package dev.jdgarita.frnk.ui.scaffolds

import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Plus
import dev.jdgarita.frnk.ui.scaffolds.settings.SettingsAction
import dev.jdgarita.frnk.ui.scaffolds.settings.SettingsClickableRowState
import dev.jdgarita.frnk.ui.scaffolds.settings.SettingsExtraSectionsPlacement
import dev.jdgarita.frnk.ui.scaffolds.settings.SettingsScreenState
import dev.jdgarita.frnk.ui.scaffolds.settings.SettingsSectionState
import dev.jdgarita.frnk.ui.scaffolds.settings.defaultSettingsState
import dev.jdgarita.frnk.ui.theme.Appearance
import dev.jdgarita.frnk.ui.theme.FrnkIconSource
import dev.jdgarita.frnk.ui.theme.FrnkStringSource
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

/**
 * Verifies `defaultSettingsState`'s `extraSections` injection: each
 * [dev.jdgarita.frnk.ui.scaffolds.settings.SettingsExtraSectionsPlacement] slots the host sections at
 * the right point in the default catalogue (Appearance → Preferences → Subscription → Support → Legal).
 *
 * Since the catalogue now holds **theme-token refs** ([FrnkStringSource]/[FrnkIconSource]) and the
 * builder resolves nothing, this is a **plain** reducer test — no composition / Robolectric needed
 * (previously this had to run as a Robolectric Compose host test to resolve `Theme[...]` inside `remember`).
 */
class SettingsDefaultsTest {
    private val extra =
        SettingsSectionState(
            title = FrnkStringSource.Raw("Host Section"),
            rows =
                listOf(
                    SettingsClickableRowState(
                        id = "host_row",
                        icon = FrnkIconSource.Vector(Lucide.Plus),
                        title = FrnkStringSource.Raw("Host Row"),
                        action = SettingsAction.Custom("host_row"),
                    ),
                ),
        )

    private fun buildState(placement: SettingsExtraSectionsPlacement): SettingsScreenState =
        defaultSettingsState(
            version = "v1.0",
            appearance = Appearance.System,
            extraSections = listOf(extra),
            extraSectionsPlacement = placement,
        )

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
        val state = defaultSettingsState(version = "v1.0", appearance = Appearance.System)
        assertEquals(5, state.sections.size)
        assertFalse(extra in state.sections)
    }
}
