package dev.jdgarita.frnk.ui.scaffolds.previews

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.composeunstyled.theme.Theme
import dev.jdgarita.frnk.ui.scaffolds.sheet.FrnkModalSheetClose
import dev.jdgarita.frnk.ui.scaffolds.sheet.FrnkModalSheetScrim
import dev.jdgarita.frnk.ui.scaffolds.sheet.FrnkModalSheetSurface
import dev.jdgarita.frnk.ui.scaffolds.sheet.FrnkSheetHeader
import dev.jdgarita.frnk.ui.theme.Appearance
import dev.jdgarita.frnk.ui.theme.colorSurface
import dev.jdgarita.frnk.ui.theme.colors

@Composable
private fun SheetSurfaceSample() {
    FrnkModalSheetSurface(
        surfaceColor = Theme[colors][colorSurface],
        close = FrnkModalSheetClose(contentDescription = "Close", onClick = {}),
        modifier = Modifier.fillMaxWidth()
    ) {
        FrnkSheetHeader(eyebrow = "PRO", title = "Dial in the details")
        Box(modifier = Modifier.fillMaxWidth().height(160.dp))
    }
}

@Preview(widthDp = 390, heightDp = 240)
@Composable
private fun FrnkModalSheetScrim_Light() {
    PreviewSurface(appearance = Appearance.Light) {
        FrnkModalSheetScrim(modifier = Modifier.height(200.dp))
    }
}

@Preview(widthDp = 390, heightDp = 320)
@Composable
private fun FrnkModalSheetSurface_Light() {
    PreviewSurface(appearance = Appearance.Light) { SheetSurfaceSample() }
}

@Preview(widthDp = 390, heightDp = 320)
@Composable
private fun FrnkModalSheetSurface_Dark() {
    PreviewSurface(appearance = Appearance.Dark) { SheetSurfaceSample() }
}

@Preview(widthDp = 390)
@Composable
private fun FrnkSheetHeader_Leading() {
    PreviewSurface { FrnkSheetHeader(title = "Dial in the details") }
}

@Preview(widthDp = 390)
@Composable
private fun FrnkSheetHeader_Eyebrow() {
    PreviewSurface { FrnkSheetHeader(eyebrow = "PRO") }
}

@Preview(widthDp = 390)
@Composable
private fun FrnkSheetHeader_Centered() {
    PreviewSurface {
        FrnkSheetHeader(
            title = "Rate this cup",
            body = "How did it taste? Your rating stays on the card.",
            centered = true
        )
    }
}