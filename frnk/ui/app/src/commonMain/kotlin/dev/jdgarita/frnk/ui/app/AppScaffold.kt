package dev.jdgarita.frnk.ui.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.composeunstyled.ProvideContentColor
import com.composeunstyled.theme.Theme
import dev.jdgarita.frnk.ui.theme.colorBackground
import dev.jdgarita.frnk.ui.theme.colorOnBackground
import dev.jdgarita.frnk.ui.theme.colors

@Composable
fun AppScaffold(
    backgroundColor: Color = Theme[colors][colorBackground],
    contentColor: Color = Theme[colors][colorOnBackground],
    contentPadding: PaddingValues = PaddingValues(0.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    Column(Modifier.fillMaxSize().background(backgroundColor).padding(contentPadding)) {
        ProvideContentColor(contentColor) {
            content()
        }
    }
}