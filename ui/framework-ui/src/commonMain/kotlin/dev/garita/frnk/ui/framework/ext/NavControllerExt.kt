package dev.garita.frnk.ui.framework.ext

import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.Navigator
import dev.jdgarita.frnk.util.common.Log

fun NavController.navigateSafe(
    route: String,
    navOptions: NavOptions? = null,
    navigatorExtras: Navigator.Extras? = null
) = try {
    navigate(route, navOptions, navigatorExtras)
} catch (exception: Exception) {
    Log.e("Invalid navigation route $route", exception)
}