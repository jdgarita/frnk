package dev.jdgarita.frnk.demo.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.composeunstyled.theme.Theme
import dev.jdgarita.frnk.demo.ui.home.DemoHomeIntent
import dev.jdgarita.frnk.demo.ui.home.DemoHomeViewModel
import dev.jdgarita.frnk.ui.atoms.FrnkDivider
import dev.jdgarita.frnk.ui.atoms.FrnkDividerState
import dev.jdgarita.frnk.ui.atoms.FrnkIcon
import dev.jdgarita.frnk.ui.atoms.FrnkIconState
import dev.jdgarita.frnk.ui.atoms.FrnkText
import dev.jdgarita.frnk.ui.atoms.FrnkTextState
import dev.jdgarita.frnk.ui.atoms.FrnkTopAppBarAction
import dev.jdgarita.frnk.ui.atoms.FrnkTopAppBarState
import dev.jdgarita.frnk.ui.scaffolds.FrnkScreenScaffold
import dev.jdgarita.frnk.ui.theme.FrnkIconSource
import dev.jdgarita.frnk.ui.theme.FrnkStringSource
import dev.jdgarita.frnk.ui.theme.colorOnSurfaceVariant
import dev.jdgarita.frnk.ui.theme.iconChevronRight
import dev.jdgarita.frnk.ui.theme.iconSearch
import dev.jdgarita.frnk.ui.theme.icons
import dev.jdgarita.frnk.ui.tokens.FrnkIconSize
import dev.jdgarita.frnk.ui.tokens.FrnkSpacing
import org.koin.compose.viewmodel.koinViewModel

private val componentNames =
    listOf(
        "FrnkText",
        "FrnkButton",
        "FrnkIcon / FrnkIconButton",
        "FrnkDivider",
        "FrnkSwitch",
        "FrnkSegmentedControl",
        "FrnkBottomFloatingBar",
        "Ripple",
        "FrnkListRow",
        "FrnkSwipeable",
        "FrnkLabeledValue",
        "FrnkEmptyState",
        "FrnkListSection",
        "FrnkProfileHeader"
    )

@Composable
internal fun ComponentsListScreen(onOpenComponent: (String) -> Unit) {
    val viewModel: DemoHomeViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val trimmedQuery = state.searchQuery.trim()
    val matches = componentNames.filter { it.contains(trimmedQuery, ignoreCase = true) }

    FrnkScreenScaffold(
        topBar =
            FrnkTopAppBarState(
                title = FrnkStringSource.Raw("Components"),
                actions =
                    listOf(
                        FrnkTopAppBarAction(icon = FrnkIconSource.Token(iconSearch), contentDescription = "Search")
                    ),
                isSearchActive = state.searchActive,
                searchQuery = state.searchQuery,
                searchPlaceholder = "Search components"
            ),
        onActionClick = { viewModel.send(DemoHomeIntent.SearchOpened) },
        onSearchQueryChange = { viewModel.send(DemoHomeIntent.SearchQueryChanged(it)) },
        onSearchClose = { viewModel.send(DemoHomeIntent.SearchClosed) }
    ) { padding ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(padding)
        ) {
            if (matches.isEmpty()) {
                FrnkText(
                    state =
                        FrnkTextState.Body(
                            text = "No components match \"${state.searchQuery}\".",
                            color = colorOnSurfaceVariant
                        )
                )
            } else {
                matches.forEachIndexed { index, name ->
                    if (index > 0) {
                        FrnkDivider(state = FrnkDividerState.Horizontal())
                    }
                    ComponentRow(name = name, onClick = { onOpenComponent(name) })
                }
            }
        }
    }
}

@Composable
private fun ComponentRow(
    name: String,
    onClick: () -> Unit
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(vertical = FrnkSpacing.md),
        verticalAlignment = Alignment.CenterVertically
    ) {
        FrnkText(
            state = FrnkTextState.TitleMedium(text = name),
            modifier = Modifier.weight(1f)
        )
        FrnkIcon(
            state =
                FrnkIconState.Content(
                    imageVector = Theme[icons][iconChevronRight],
                    contentDescription = null,
                    size = FrnkIconSize.md,
                    tint = colorOnSurfaceVariant
                )
        )
    }
}