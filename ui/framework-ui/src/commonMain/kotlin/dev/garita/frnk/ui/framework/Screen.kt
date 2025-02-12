package dev.garita.frnk.ui.framework

import kotlinx.serialization.Serializable

sealed class Screen {
    @Serializable
    data object Root : Screen()

    @Serializable
    data object HomeGraph : Screen()

    @Serializable
    data object Settings : Screen()
}