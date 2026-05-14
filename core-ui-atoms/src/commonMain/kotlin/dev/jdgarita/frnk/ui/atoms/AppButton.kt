package dev.jdgarita.frnk.ui.atoms

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Headless button — no Material defaults. Caller supplies all visuals
 * (background, shape, ripple) via [modifier].
 */
@Composable
fun AppButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
    content: @Composable () -> Unit,
) {
    Box(
        modifier =
            modifier
                .clickable(onClick = onClick)
                .padding(contentPadding),
    ) { content() }
}
