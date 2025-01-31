package dev.jdgarita.frnk.domain.framework

import kotlinx.coroutines.flow.StateFlow

interface ScreenNavigationTracker {
    val flow: StateFlow<ScreenState>
    val currentScreen: ScreenName?
    val previousScreen: ScreenName?

    fun onScreenChanged(screenName: ScreenName, screenAttributes: Map<String, String> = emptyMap())
}