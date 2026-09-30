package dev.jdgarita.frnk.ui.atoms.previews

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.composables.icons.lucide.ArrowUpDown
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Plus
import com.composables.icons.lucide.Settings
import dev.jdgarita.frnk.ui.atoms.FrnkHeadlineAction
import dev.jdgarita.frnk.ui.atoms.FrnkHeadlineBadge
import dev.jdgarita.frnk.ui.atoms.FrnkHeadlineTopBar
import dev.jdgarita.frnk.ui.atoms.FrnkHeadlineTopBarState
import dev.jdgarita.frnk.ui.atoms.FrnkHeadlineTrailing
import dev.jdgarita.frnk.ui.theme.Appearance
import dev.jdgarita.frnk.ui.theme.FrnkIconSource
import dev.jdgarita.frnk.ui.theme.FrnkStringSource

private val previewSettings =
    FrnkHeadlineAction(
        key = "settings",
        icon = FrnkIconSource.Vector(Lucide.Settings),
        contentDescription = FrnkStringSource.Raw("Settings")
    )

private val previewAdd =
    FrnkHeadlineAction(
        key = "add",
        icon = FrnkIconSource.Vector(Lucide.Plus),
        contentDescription = FrnkStringSource.Raw("Add")
    )

private val previewBadge =
    FrnkHeadlineBadge(
        key = "pro",
        label = FrnkStringSource.Raw("PRO"),
        contentDescription = FrnkStringSource.Raw("Upgrade to Pro")
    )

private fun previewSearch(
    isActive: Boolean,
    query: String = ""
) = FrnkHeadlineTrailing.Search(
    isActive = isActive,
    query = query,
    openContentDescription = FrnkStringSource.Raw("Search"),
    closeContentDescription = FrnkStringSource.Raw("Close search")
)

private val previewIdle =
    FrnkHeadlineTopBarState(
        title = FrnkStringSource.Raw("Faint"),
        leading = previewSettings,
        badge = previewBadge,
        actions = listOf(previewAdd),
        trailing = previewSearch(isActive = false)
    )

@Preview(widthDp = 390)
@Composable
private fun FrnkHeadlineTopBar_Idle_Light() {
    PreviewSurface(appearance = Appearance.Light) {
        FrnkHeadlineTopBar(state = previewIdle, onActionClick = {})
    }
}

@Preview(widthDp = 390)
@Composable
private fun FrnkHeadlineTopBar_Searching_Light() {
    PreviewSurface(appearance = Appearance.Light) {
        FrnkHeadlineTopBar(
            state = previewIdle.copy(trailing = previewSearch(isActive = true, query = "Yirga")),
            onActionClick = {}
        )
    }
}

@Preview(widthDp = 390)
@Composable
private fun FrnkHeadlineTopBar_ActionTrailing_Dark() {
    PreviewSurface(appearance = Appearance.Dark) {
        FrnkHeadlineTopBar(
            state =
                previewIdle.copy(
                    title = FrnkStringSource.Raw("Still"),
                    trailing =
                        FrnkHeadlineTrailing.Action(
                            FrnkHeadlineAction(
                                key = "sort",
                                icon = FrnkIconSource.Vector(Lucide.ArrowUpDown),
                                contentDescription = FrnkStringSource.Raw("Sort")
                            )
                        )
                ),
            onActionClick = {}
        )
    }
}