package dev.jdgarita.frnk.presentation.screen.detail.ui.component

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.tweener.czan.designsystem.atom.bars.TopBarWithBackButton
import frnk.shared.presentation.generated.resources.Res
import frnk.shared.presentation.generated.resources.detail_screen_title
import org.jetbrains.compose.resources.stringResource

/**
 * @author Vivien Mahe
 * @since 20/02/2024
 */

@Composable
fun DetailTopBar(
    onBackClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    TopBarWithBackButton(
        modifier = modifier,
        title = stringResource(resource = Res.string.detail_screen_title),
        textStyle = MaterialTheme.typography.titleLarge,
        onBackClicked = onBackClicked,
    )
}
