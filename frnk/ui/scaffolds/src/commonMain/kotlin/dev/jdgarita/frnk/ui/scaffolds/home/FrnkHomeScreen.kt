package dev.jdgarita.frnk.ui.scaffolds.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.composeunstyled.theme.Theme
import dev.jdgarita.frnk.ui.mvi.FrnkScreen
import dev.jdgarita.frnk.ui.mvi.UiEffect
import dev.jdgarita.frnk.ui.scaffolds.FrnkScreenScaffold
import dev.jdgarita.frnk.ui.theme.spacing
import dev.jdgarita.frnk.ui.theme.spacingLg
import dev.jdgarita.frnk.ui.theme.spacingMd
import org.koin.compose.viewmodel.koinViewModel

/**
 * The toolkit's home-tab page template: a pinned [FrnkTopAppBar][dev.jdgarita.frnk.ui.atoms.FrnkTopAppBar]
 * over a scaffold-owned vertically-scrollable column the host fills through [content]. Resolves
 * [HomeViewModel] from Koin and drives it through [FrnkScreen] (which `attach`es the VM with
 * [frnkHomeArguments] and collects state); top-bar taps dispatch [HomeIntent], re-surfaced to the host
 * as [HomeEffect] through [onEffect]. Hosts needing a `LazyColumn` drop to
 * [FrnkScreenScaffold][dev.jdgarita.frnk.ui.scaffolds.FrnkScreenScaffold] directly.
 *
 * @param frnkHomeArguments runtime inputs the VM attaches with (notably the top-bar title).
 * @param contentPadding padding applied to the scrollable column; defaults to the large spacing token.
 * @param onEffect collector for the screen's one-shot [HomeEffect]s (single-consumer).
 * @param content the host's home-tab body, laid out in a scaffold-owned scrolling [ColumnScope].
 */
@Composable
fun FrnkHomeScreen(
    frnkHomeArguments: HomeArguments,
    contentPadding: PaddingValues = PaddingValues(Theme[spacing][spacingLg]),
    onEffect: (uiEffect: UiEffect) -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    val homeViewModel: HomeViewModel = koinViewModel()

    FrnkScreen(
        arguments = frnkHomeArguments,
        viewModel = homeViewModel,
        onEffect = onEffect
    ) { state ->

        FrnkScreenScaffold(
            topBar = state.topBar,
            contentPadding = contentPadding,
            onNavigationClick = { homeViewModel.send(HomeIntent.NavigationClicked) },
            onActionClick = { frnkTopAppAction ->
                homeViewModel.send(HomeIntent.TopBarActionClicked(frnkTopAppAction))
            }
        ) { mergedPadding ->
            Column(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(mergedPadding),
                verticalArrangement = Arrangement.spacedBy(Theme[spacing][spacingMd]),
                content = content
            )
        }
    }
}