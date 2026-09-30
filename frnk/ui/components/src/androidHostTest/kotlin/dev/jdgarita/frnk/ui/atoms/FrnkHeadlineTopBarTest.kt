package dev.jdgarita.frnk.ui.atoms

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.runComposeUiTest
import com.composables.icons.lucide.ArrowUpDown
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Plus
import com.composables.icons.lucide.Settings
import dev.jdgarita.frnk.ui.theme.FrnkIconSource
import dev.jdgarita.frnk.ui.theme.FrnkStringSource
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Ports Faint's `FaintHomeHeadlineUiTest` onto the generic [FrnkHeadlineTopBar], plus Still's case: a
 * plain trailing action (sort) where Faint has search.
 */
@OptIn(ExperimentalTestApi::class)
class FrnkHeadlineTopBarTest : RobolectricComposeTest() {
    private val settings =
        FrnkHeadlineAction(
            key = "settings",
            icon = FrnkIconSource.Vector(Lucide.Settings),
            contentDescription = FrnkStringSource.Raw("Settings")
        )
    private val add =
        FrnkHeadlineAction(
            key = "add",
            icon = FrnkIconSource.Vector(Lucide.Plus),
            contentDescription = FrnkStringSource.Raw("Add")
        )
    private val sort =
        FrnkHeadlineAction(
            key = "sort",
            icon = FrnkIconSource.Vector(Lucide.ArrowUpDown),
            contentDescription = FrnkStringSource.Raw("Sort")
        )
    private val pro =
        FrnkHeadlineBadge(
            key = "pro",
            label = FrnkStringSource.Raw("PRO"),
            contentDescription = FrnkStringSource.Raw("Upgrade to Pro")
        )

    private fun search(
        isActive: Boolean = false,
        query: String = ""
    ) = FrnkHeadlineTrailing.Search(
        isActive = isActive,
        query = query,
        openContentDescription = FrnkStringSource.Raw("Search"),
        closeContentDescription = FrnkStringSource.Raw("Close search")
    )

    private fun faintState(
        isSearching: Boolean = false,
        badge: FrnkHeadlineBadge? = pro
    ) = FrnkHeadlineTopBarState(
        title = FrnkStringSource.Raw("Faint"),
        leading = settings,
        badge = badge,
        actions = listOf(add),
        trailing = search(isActive = isSearching)
    )

    private fun stillState(sortEnabled: Boolean = true) =
        FrnkHeadlineTopBarState(
            title = FrnkStringSource.Raw("Still"),
            leading = settings,
            badge = pro,
            actions = listOf(add),
            trailing = FrnkHeadlineTrailing.Action(sort.copy(enabled = sortEnabled))
        )

    @Test
    fun the_badge_shows_when_present_and_not_searching() =
        runComposeUiTest {
            val clicks = mutableListOf<String>()
            setFrnkContent {
                FrnkHeadlineTopBar(state = faintState(), onActionClick = clicks::add)
            }

            onNode(hasContentDescription("Upgrade to Pro") and hasClickAction())
                .assertExists()
                .performClick()

            assertEquals(listOf("pro"), clicks)
        }

    @Test
    fun the_badge_and_actions_fold_away_while_searching() =
        runComposeUiTest {
            setFrnkContent {
                FrnkHeadlineTopBar(state = faintState(isSearching = true), onActionClick = {})
            }

            onNode(hasContentDescription("Upgrade to Pro")).assertDoesNotExist()
            onNode(hasContentDescription("Add")).assertDoesNotExist()
            onNode(hasContentDescription("Settings"))
                .assertExists()
                .assertIsNotEnabled()
        }

    @Test
    fun no_badge_is_drawn_when_the_state_has_none() =
        runComposeUiTest {
            setFrnkContent {
                FrnkHeadlineTopBar(state = faintState(badge = null), onActionClick = {})
            }

            onNode(hasContentDescription("Upgrade to Pro")).assertDoesNotExist()
            // The rest of the bar still renders.
            onNode(hasContentDescription("Add")).assertExists()
        }

    @Test
    fun leading_badge_and_actions_dispatch_their_keys() =
        runComposeUiTest {
            val clicks = mutableListOf<String>()
            setFrnkContent {
                FrnkHeadlineTopBar(state = faintState(), onActionClick = clicks::add)
            }

            onNode(hasContentDescription("Settings")).performClick()
            onNode(hasContentDescription("Upgrade to Pro")).performClick()
            onNode(hasContentDescription("Add")).performClick()

            assertEquals(listOf("settings", "pro", "add"), clicks)
        }

    @Test
    fun a_search_trailing_opens_types_and_closes() =
        runComposeUiTest {
            var opened = 0
            var closed = 0
            var lastQuery = ""
            setFrnkContent {
                // A controlled bar, as a host drives it: open flips the flag, typing feeds the query back.
                var isActive by remember { mutableStateOf(false) }
                var query by remember { mutableStateOf("") }
                FrnkHeadlineTopBar(
                    state = faintState().copy(trailing = search(isActive = isActive, query = query)),
                    onActionClick = {},
                    onSearchOpen = {
                        opened += 1
                        isActive = true
                    },
                    onSearchQueryChange = {
                        query = it
                        lastQuery = it
                    },
                    onSearchClose = {
                        closed += 1
                        isActive = false
                    }
                )
            }

            onNode(hasContentDescription("Search")).performClick()
            assertEquals(1, opened)

            onNode(hasSetTextAction()).performTextInput("yirga")
            assertEquals("yirga", lastQuery)

            onNode(hasContentDescription("Close search")).performClick()
            assertEquals(1, closed)
        }

    @Test
    fun an_action_trailing_dispatches_and_never_searches() =
        runComposeUiTest {
            val clicks = mutableListOf<String>()
            setFrnkContent {
                FrnkHeadlineTopBar(state = stillState(), onActionClick = clicks::add)
            }

            onNode(hasContentDescription("Sort")).performClick()

            assertEquals(listOf("sort"), clicks)
            onNode(hasSetTextAction()).assertDoesNotExist()
            onNode(hasContentDescription("Search")).assertDoesNotExist()
        }

    @Test
    fun a_disabled_trailing_action_does_not_dispatch() =
        runComposeUiTest {
            val clicks = mutableListOf<String>()
            setFrnkContent {
                FrnkHeadlineTopBar(state = stillState(sortEnabled = false), onActionClick = clicks::add)
            }

            onNode(hasContentDescription("Sort"))
                .assertIsNotEnabled()
                .performClick()

            assertTrue(clicks.isEmpty())
        }
}