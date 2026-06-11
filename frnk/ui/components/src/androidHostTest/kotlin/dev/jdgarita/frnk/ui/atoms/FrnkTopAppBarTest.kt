package dev.jdgarita.frnk.ui.atoms

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.runComposeUiTest
import com.composables.icons.lucide.House
import com.composables.icons.lucide.Lucide
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class FrnkTopAppBarTest : RobolectricComposeTest() {
    private val searchAction = FrnkTopAppBarAction(icon = Lucide.House, contentDescription = "Search")
    private val placeholder = "Search notes"

    @Test
    fun titleMode_showsTitleAndAction() =
        runComposeUiTest {
            setFrnkContent {
                FrnkTopAppBar(
                    state =
                        FrnkTopAppBarState(
                            title = "Settings",
                            actions = listOf(searchAction),
                            applyStatusBarPadding = false,
                        ),
                )
            }
            onNodeWithText("Settings").assertIsDisplayed()
            onNodeWithContentDescription("Search").assertIsDisplayed()
        }

    @Test
    fun searchMode_replacesTitleWithSearchAffordances() =
        runComposeUiTest {
            setFrnkContent {
                FrnkTopAppBar(
                    state =
                        FrnkTopAppBarState(
                            title = "Settings",
                            actions = listOf(searchAction),
                            isSearchActive = true,
                            searchPlaceholder = placeholder,
                            applyStatusBarPadding = false,
                        ),
                )
            }
            // Title + the trigger action are swapped out for the search field chrome.
            onNodeWithText("Settings").assertDoesNotExist()
            onNodeWithContentDescription("Search").assertDoesNotExist()
            onNodeWithContentDescription("Close search").assertIsDisplayed()
            onNodeWithText(placeholder).assertIsDisplayed()
        }

    @Test
    fun searchMode_emptyQuery_hidesClearButton() =
        runComposeUiTest {
            setFrnkContent {
                FrnkTopAppBar(
                    state =
                        FrnkTopAppBarState(
                            title = "Settings",
                            isSearchActive = true,
                            searchQuery = "",
                            searchPlaceholder = placeholder,
                            applyStatusBarPadding = false,
                        ),
                )
            }
            // Assert we're actually in search mode (not silently fallen back to the title layout),
            // then that the clear button is absent for an empty query.
            onNodeWithContentDescription("Close search").assertIsDisplayed()
            onNodeWithContentDescription("Clear search").assertDoesNotExist()
        }

    @Test
    fun searchMode_nonEmptyQuery_showsClearButtonAndHidesPlaceholder() =
        runComposeUiTest {
            setFrnkContent {
                FrnkTopAppBar(
                    state =
                        FrnkTopAppBarState(
                            title = "Settings",
                            isSearchActive = true,
                            searchQuery = "kotlin",
                            searchPlaceholder = placeholder,
                            applyStatusBarPadding = false,
                        ),
                )
            }
            onNodeWithContentDescription("Clear search").assertIsDisplayed()
            onNodeWithText(placeholder).assertDoesNotExist()
        }

    @Test
    fun typingInSearchField_streamsQueryChange() =
        runComposeUiTest {
            var lastQuery = ""
            setFrnkContent {
                // A genuinely controlled field: the state updates from onSearchQueryChange, mirroring
                // real host usage. This keeps the test correct whether the test runtime executes input
                // immediately (v1 runComposeUiTest) or queues it (v2) — it doesn't rely on a static value.
                var query by remember { mutableStateOf("") }
                FrnkTopAppBar(
                    state =
                        FrnkTopAppBarState(
                            title = "Settings",
                            isSearchActive = true,
                            searchQuery = query,
                            searchPlaceholder = placeholder,
                            applyStatusBarPadding = false,
                        ),
                    onSearchQueryChange = {
                        query = it
                        lastQuery = it
                    },
                )
            }

            onNode(hasSetTextAction()).performTextInput("abc")

            assertEquals("abc", lastQuery)
        }

    @Test
    fun clearButton_emitsEmptyQuery() =
        runComposeUiTest {
            var lastQuery: String? = null
            setFrnkContent {
                FrnkTopAppBar(
                    state =
                        FrnkTopAppBarState(
                            title = "Settings",
                            isSearchActive = true,
                            searchQuery = "kotlin",
                            searchPlaceholder = placeholder,
                            applyStatusBarPadding = false,
                        ),
                    onSearchQueryChange = { lastQuery = it },
                )
            }

            onNodeWithContentDescription("Clear search").performClick()

            assertEquals("", lastQuery)
        }

    @Test
    fun closeButton_invokesOnSearchClose() =
        runComposeUiTest {
            var closed = false
            setFrnkContent {
                FrnkTopAppBar(
                    state =
                        FrnkTopAppBarState(
                            title = "Settings",
                            isSearchActive = true,
                            searchPlaceholder = placeholder,
                            applyStatusBarPadding = false,
                        ),
                    onSearchClose = { closed = true },
                )
            }

            onNodeWithContentDescription("Close search").performClick()

            assertEquals(true, closed)
        }
}
