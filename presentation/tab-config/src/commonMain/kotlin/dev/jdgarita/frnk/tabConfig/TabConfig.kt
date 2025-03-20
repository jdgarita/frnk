package dev.jdgarita.frnk.tabConfig

import dev.jdgarita.frnk.presentation.framework.navigation.NavigationContext
import dev.jdgarita.frnk.presentation.home.api.navigation.HomeNavigationContext
import dev.jdgarita.frnk.presentation.resources.images.SemanticIcon
import dev.jdgarita.frnk.presentation.resources.text.SemanticText
import dev.jdgarita.frnk.presentation.resources.text.asSemanticText

sealed class TabConfig(
    val icon: SemanticIcon,
    val text: SemanticText,
    val navigationContext: NavigationContext
) {
    data object Home : TabConfig(
        icon = SemanticIcon.TabHome,
        // text = Strings.bottom_navigation_item_home.asSemanticText(),
        text = "Home".asSemanticText(),
        navigationContext = HomeNavigationContext
    )

    companion object {
        fun getConfigFor(
            contexts: List<NavigationContext>,
            availableConfigs: List<TabConfig>
        ): List<TabConfig> = availableConfigs.filter { it.navigationContext in contexts }
    }
}