package dev.jdgarita.frnk.ui.bottomnav

import androidx.compose.runtime.Immutable

@Immutable
data class FrnkTabbedNavViewState(
    val navBarItems: List<FrnkNavBarItem>,
    val navBarItemIndexSelected: Int,
)
