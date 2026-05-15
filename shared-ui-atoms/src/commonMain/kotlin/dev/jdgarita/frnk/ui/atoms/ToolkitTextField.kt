package dev.jdgarita.frnk.ui.atoms

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp

@Composable
fun ToolkitTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
) {
    val colors = ToolkitTheme.colors
    val style: TextStyle = ToolkitTheme.typography.body.copy(color = colors.onSurface)
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        textStyle = style,
        modifier =
            modifier
                .clip(RoundedCornerShape(8.dp))
                .border(1.dp, colors.outline, RoundedCornerShape(8.dp))
                .padding(horizontal = 12.dp, vertical = 10.dp),
    )
}
