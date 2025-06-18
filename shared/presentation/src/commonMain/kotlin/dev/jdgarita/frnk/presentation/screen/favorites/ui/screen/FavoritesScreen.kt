package dev.jdgarita.frnk.presentation.screen.favorites.ui.screen

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import dev.jdgarita.frnk.presentation._internal.kotlinextensions.findRootNavigator
import dev.jdgarita.frnk.presentation.screen.favorites.FavoritesViewModel
import dev.jdgarita.frnk.presentation.screen.favorites.ui.template.FavoritesTemplate
import org.koin.compose.koinInject

/**
 * @author Vivien Mahe
 * @since 19/02/2024
 */

@Composable
fun FavoritesScreen() {
    val viewModel: FavoritesViewModel = koinInject()
    val navigator = LocalNavigator.currentOrThrow.findRootNavigator()

    FavoritesTemplate()
}
