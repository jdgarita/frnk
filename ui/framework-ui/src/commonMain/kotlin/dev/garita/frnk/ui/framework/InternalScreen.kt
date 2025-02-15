package dev.garita.frnk.ui.framework

import kotlinx.serialization.Serializable

sealed class InternalScreen : Screen {
    @Serializable
    data object Root : InternalScreen()

    @Serializable
    data object HomeGraph : InternalScreen()

    @Serializable
    data object Home : InternalScreen()

    @Serializable
    data object Settings : InternalScreen()
}