package dev.jdgarita.frnk.presentation.framework.navigation

import dev.jdgarita.frnk.domain.framework.ScreenName

interface NavigationComponent

interface NavigationScreen : NavigationComponent {
    val screenName: ScreenName
}

data class ExternalUrlScreen(val url: String, override val screenName: ScreenName) : NavigationScreen

data class EmailComposerScreen(val email: String, override val screenName: ScreenName) : NavigationScreen

data class ShareContentScreen(
    val title: String,
    val content: String,
    override val screenName: ScreenName
) : NavigationScreen

data class SystemScreen(
    val type: SystemScreenType,
    override val screenName: ScreenName
) : NavigationScreen

enum class SystemScreenType {
    AppSettings,
    AppMarket
}