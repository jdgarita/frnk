package dev.jdgarita.frnk.ui.atoms.previews

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.composeunstyled.theme.Theme
import dev.jdgarita.frnk.ui.atoms.FrnkTopAppBar
import dev.jdgarita.frnk.ui.atoms.FrnkTopAppBarAction
import dev.jdgarita.frnk.ui.atoms.FrnkTopAppBarState
import dev.jdgarita.frnk.ui.theme.Appearance
import dev.jdgarita.frnk.ui.theme.iconBack
import dev.jdgarita.frnk.ui.theme.iconSearch
import dev.jdgarita.frnk.ui.theme.icons

@Preview
@Composable
private fun FrnkTopAppBar_TitleOnly_Light() {
    PreviewSurface(appearance = Appearance.Light) {
        FrnkTopAppBar(
            state = FrnkTopAppBarState(title = "Frnk Toolkit Demo", applyStatusBarPadding = false)
        )
    }
}

@Preview
@Composable
private fun FrnkTopAppBar_BackAndAction_Light() {
    PreviewSurface(appearance = Appearance.Light) {
        FrnkTopAppBar(
            state =
                FrnkTopAppBarState(
                    title = "Components",
                    navigationIcon = Theme[icons][iconBack],
                    navigationContentDescription = "Back",
                    actions =
                        listOf(
                            FrnkTopAppBarAction(icon = Theme[icons][iconSearch], contentDescription = "Search")
                        ),
                    applyStatusBarPadding = false
                )
        )
    }
}

@Preview
@Composable
private fun FrnkTopAppBar_SearchActive_Light() {
    PreviewSurface(appearance = Appearance.Light) {
        FrnkTopAppBar(
            state =
                FrnkTopAppBarState(
                    title = "Components",
                    isSearchActive = true,
                    searchQuery = "Button",
                    searchPlaceholder = "Search components",
                    applyStatusBarPadding = false
                )
        )
    }
}

@Preview
@Composable
private fun FrnkTopAppBar_BackAndAction_Dark() {
    PreviewSurface(appearance = Appearance.Dark) {
        FrnkTopAppBar(
            state =
                FrnkTopAppBarState(
                    title = "Components",
                    navigationIcon = Theme[icons][iconBack],
                    navigationContentDescription = "Back",
                    actions =
                        listOf(
                            FrnkTopAppBarAction(icon = Theme[icons][iconSearch], contentDescription = "Search")
                        ),
                    applyStatusBarPadding = false
                )
        )
    }
}