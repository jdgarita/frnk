package dev.jdgarita.frnk.domain.framework

data class ScreenState(
    val currentScreen: ScreenName?,
    val currentScreenAttributes: Map<String, String> = emptyMap(),
    val previousScreen: ScreenName?
)