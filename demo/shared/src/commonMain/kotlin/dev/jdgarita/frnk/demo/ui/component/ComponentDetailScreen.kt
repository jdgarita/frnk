package dev.jdgarita.frnk.demo.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.jdgarita.frnk.ui.atoms.FrnkTopAppBarState
import dev.jdgarita.frnk.ui.scaffolds.FrnkScreenScaffold
import dev.jdgarita.frnk.ui.theme.FrnkIconSource
import dev.jdgarita.frnk.ui.theme.FrnkStringSource
import dev.jdgarita.frnk.ui.theme.iconBack
import dev.jdgarita.frnk.ui.tokens.FrnkSpacing

@Composable
fun ComponentDetailScreen(
    name: String,
    onBack: () -> Unit,
    content: @Composable () -> Unit
) {
    FrnkScreenScaffold(
        topBar =
            FrnkTopAppBarState(
                title = FrnkStringSource.Raw(name),
                navigationIcon = FrnkIconSource.Token(iconBack),
                navigationContentDescription = "Back"
            ),
        onNavigationClick = onBack
    ) { padding ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(padding),
            verticalArrangement = Arrangement.spacedBy(FrnkSpacing.md)
        ) {
            content()
        }
    }
}