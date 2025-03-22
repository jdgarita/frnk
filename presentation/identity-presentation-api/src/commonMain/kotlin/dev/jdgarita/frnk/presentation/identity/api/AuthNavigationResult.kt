package dev.jdgarita.frnk.presentation.identity.api

import dev.jdgarita.frnk.presentation.framework.navigation.NavigationRouterResult

sealed class AuthNavigationResult : NavigationRouterResult {
    object Succeeded : AuthNavigationResult()
}