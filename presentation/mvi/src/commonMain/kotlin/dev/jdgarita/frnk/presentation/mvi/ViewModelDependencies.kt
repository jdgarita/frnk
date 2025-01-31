package dev.jdgarita.frnk.presentation.mvi

import dev.jdgarita.frnk.domain.framework.FrnkLogger
import dev.jdgarita.frnk.domain.framework.ScreenNavigationTracker
import dev.jdgarita.frnk.presentation.resources.FrnkStringProvider
import dev.jdgarita.frnk.util.common.Formatters

data class ViewModelDependencies(
    // todo provide AppConfigurationProvider
    // val appConfigurationProvider: AppConfigurationProvider,
    val logger: FrnkLogger,
    val formatters: Formatters,
    val stringProvider: FrnkStringProvider,
    val viewModelProvider: ViewModelProvider,
    val screenNavigationTracker: ScreenNavigationTracker
)