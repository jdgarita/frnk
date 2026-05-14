package dev.jdgarita.frnk.ui.atoms

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * NOTE: This is a stub — `compose-unstyled` does not ship a text primitive
 * because text rendering is platform-coupled. Replace with your own typed
 * text wrapper or use BasicText directly.
 */
@Composable
fun AppText(
    text: String,
    modifier: Modifier = Modifier,
) {
    androidx.compose.foundation.text
        .BasicText(text = text, modifier = modifier)
}
