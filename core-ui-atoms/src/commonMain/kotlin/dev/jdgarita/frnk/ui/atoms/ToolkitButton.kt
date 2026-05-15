package dev.jdgarita.frnk.ui.atoms

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

/** Atomic button. Pure layout + tokens — no Material chrome. */
@Composable
fun ToolkitButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val colors = ToolkitTheme.colors
    val typography = ToolkitTheme.typography
    BasicText(
        text = label,
        style = typography.button.copy(color = colors.onPrimary),
        modifier =
            modifier
                .clip(RoundedCornerShape(12.dp))
                .background(colors.primary)
                .clickable(enabled = enabled, onClick = onClick)
                .padding(horizontal = 20.dp, vertical = 12.dp),
    )
}
