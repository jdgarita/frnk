package com.tweener.kmpship.presentation.screen.home.ui.screen

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.tweener.czan._internal.kotlinextensions.subscribe
import com.tweener.kmpship.presentation._internal.kotlinextensions.findRootNavigator
import com.tweener.kmpship.presentation.screen.detail.ui.screen.DetailScreen
import com.tweener.kmpship.presentation.screen.home.HomeViewModel
import com.tweener.kmpship.presentation.screen.home.ui.template.HomeTemplate
import org.koin.compose.koinInject

/**
 * @author Vivien Mahe
 * @since 19/02/2024
 */

@Composable
fun HomeScreen() {
    val viewModel: HomeViewModel = koinInject()
    val navigator = LocalNavigator.currentOrThrow.findRootNavigator()

    viewModel.openDetailScreen.subscribe { id -> navigator.push(item = DetailScreen(id = id)) }

    HomeTemplate(
        onAction = { viewModel.onUiAction(uiAction = it) },
    )
}
