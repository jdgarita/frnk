package dev.jdgarita.frnk.ui.identity.splash

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import dev.jdgarita.frnk.presentation.identity.splash.SplashViewModelWrapper
import dev.jdgarita.frnk.presentation.identity.splash.SplashViewState


@Composable
fun SplashScreen(
    viewModel: SplashViewModelWrapper
) {
    val viewState = viewModel.viewStateFlow.collectAsState(SplashViewState()).value
    Box(modifier = Modifier.fillMaxSize()) {
        Text("Splash Screen")
        // todo define logo here.
    }
}