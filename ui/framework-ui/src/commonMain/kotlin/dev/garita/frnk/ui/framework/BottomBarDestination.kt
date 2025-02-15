package dev.garita.frnk.ui.framework

import androidx.compose.ui.graphics.vector.ImageVector

abstract class BottomBarDestination(
    open val screen: Screen,
    open val label: String,
    open val icon: ImageVector
)

interface Screen