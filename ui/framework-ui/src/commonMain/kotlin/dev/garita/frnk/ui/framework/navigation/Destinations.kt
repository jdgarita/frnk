package dev.garita.frnk.ui.framework.navigation

const val ROOT_DESTINATION = "root"
const val TABS_DESTINATION = "tabs"

fun createTabRoute(route: String) = "$TABS_DESTINATION/$route"

fun createRootRoute(route: String) = "$ROOT_DESTINATION/$route"