package dev.jdgarita.frnk.ui.scaffolds.sheet

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.ZeroCornerSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.composeunstyled.theme.Theme
import dev.jdgarita.frnk.ui.atoms.FrnkIconButton
import dev.jdgarita.frnk.ui.atoms.FrnkIconButtonState
import dev.jdgarita.frnk.ui.theme.FrnkIconSource
import dev.jdgarita.frnk.ui.theme.colorOnSurface
import dev.jdgarita.frnk.ui.theme.colorOutline
import dev.jdgarita.frnk.ui.theme.colorScrim
import dev.jdgarita.frnk.ui.theme.colors
import dev.jdgarita.frnk.ui.theme.iconClose
import dev.jdgarita.frnk.ui.theme.shapeBottomSheet
import dev.jdgarita.frnk.ui.theme.shapeFull
import dev.jdgarita.frnk.ui.theme.shapes
import dev.jdgarita.frnk.ui.theme.spacing
import dev.jdgarita.frnk.ui.theme.spacingMd
import dev.jdgarita.frnk.ui.theme.spacingXs

/**
 * The sheet's visible body: a column clipped to the sheet's rounded top corners, with the drag
 * indicator on its own row above [content] — never hidden behind what the sheet presents, and
 * riding the sheet itself, so a drag that starts on it reaches the sheet's `anchoredDraggable`
 * and pulls the sheet down to dismiss. This is the whole of what [FrnkModalSheet] puts on screen —
 * the modal machinery around it adds behaviour, not pixels — so previewing this *is* previewing
 * the sheet's look.
 *
 * The corners come from `shapeBottomSheet`, with its bottom corners squared off: the sheet's bottom
 * edge is the window's.
 *
 * [surfaceColor] fills the column, extending the sheet's surface up under the indicator so the
 * rounded clip shapes real pixels. It defaults to transparent for content that brings its own
 * chrome — a floating self-contained card, with the indicator hovering above it over the scrim —
 * while an edge-to-edge sheet passes its surface tone. [surfaceBackdrop], when given, is drawn over
 * that tone and under everything else, across the whole sheet — the drag indicator's strip
 * included, which content alone can never reach.
 *
 * [dragIndication] receives the fully styled indicator modifier (placement, colour, pill shape,
 * size) and decides only what composable carries it: [FrnkModalSheet] passes the library's
 * `DragIndication`, whose entire visual comes from that modifier, and the default plain [Box]
 * renders pixel-identically for previews.
 *
 * [close], when given, is the ✕ in the sheet's top-end corner: a `FrnkIconButton` — the `iconClose`
 * token in `colorOnSurface` on its 48dp target — laid *over* the column rather than in it, so it
 * takes no row of its own and sits at the same place whatever the content puts first. Its box
 * starts [FrnkModalSheetDefaults.CloseInsetTop] under the sheet's edge, which lands its glyph level
 * with the first line of content under the grabber, and ends [FrnkModalSheetDefaults.CloseInsetEnd]
 * in from the side, which ends the glyph on the sheet gutter ([FrnkModalSheetDefaults.Gutter]).
 * [FrnkModalSheet] fills this from what its content registered; previews pass one directly.
 */
@Composable
internal fun FrnkModalSheetSurface(
    modifier: Modifier = Modifier,
    surfaceColor: Color = Color.Transparent,
    surfaceBackdrop: (@Composable BoxScope.() -> Unit)? = null,
    elevated: Boolean = false,
    close: FrnkModalSheetClose? = null,
    dragIndication: @Composable (Modifier) -> Unit = { indicator -> Box(modifier = indicator) },
    content: @Composable () -> Unit
) {
    val shape = Theme[shapes][shapeBottomSheet].topOnly()
    Box(
        modifier =
            modifier
                .then(if (elevated) Modifier.sheetEdgeShadow(shape) else Modifier)
                .clip(shape)
                .background(surfaceColor)
    ) {
        surfaceBackdrop?.invoke(this)
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            dragIndication(
                Modifier
                    .padding(top = Theme[spacing][spacingMd], bottom = Theme[spacing][spacingXs])
                    .background(Theme[colors][colorOutline], Theme[shapes][shapeFull])
                    .size(width = 32.dp, height = FrnkModalSheetDefaults.IndicatorHeight)
            )
            content()
            Spacer(modifier = Modifier.height(Theme[spacing][spacingMd]))
        }
        if (close != null) {
            FrnkIconButton(
                state =
                    FrnkIconButtonState.Content(
                        icon = FrnkIconSource.Token(iconClose),
                        contentDescription = close.contentDescription,
                        tint = colorOnSurface
                    ),
                onClick = close.onClick,
                modifier =
                    Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = FrnkModalSheetDefaults.CloseInsetTop, end = FrnkModalSheetDefaults.CloseInsetEnd)
            )
        }
    }
}

/**
 * The sheet's dimming overlay as a plain composable. The live sheet renders compose-unstyled's
 * `Scrim` instead — that one owns the modal wiring (visibility, the fade pair) and cannot compose
 * outside a modal — but both draw nothing more than a full-size fill of the `colorScrim` token, so
 * this mirror is what a preview (or a host that dims without the modal) uses.
 */
@Composable
internal fun FrnkModalSheetScrim(
    modifier: Modifier = Modifier,
    scrimColor: Color = Theme[colors][colorScrim]
) {
    Box(modifier = modifier.fillMaxSize().background(scrimColor))
}

/** The sheet shape with its bottom corners squared off; a non-corner-based shape passes as is. */
private fun Shape.topOnly(): Shape =
    when (this) {
        is CornerBasedShape -> copy(bottomStart = ZeroCornerSize, bottomEnd = ZeroCornerSize)
        else -> this
    }

/**
 * An elevated sheet's shadow, drawn rather than cast. A platform shadow falls *down* from a light
 * above the screen, so along a bottom sheet's top edge it is all but invisible — exactly the edge
 * that has to read as lifted. This one is a halo over the rounded top: the sheet reserves
 * [SHEET_SHADOW_REACH] of transparent room above its surface and draws [SHEET_SHADOW_LAYERS] rounded
 * rects into it, each spread further and all faint, packed near the edge (the spread grows with
 * the square of the layer), so the shade is darkest at the paper and trails off softly. The corner
 * radius follows [shape]'s top-start corner.
 */
private fun Modifier.sheetEdgeShadow(shape: Shape): Modifier =
    drawBehind {
        val reach = SHEET_SHADOW_REACH.toPx()
        val surfaceSize = Size(size.width, size.height - reach)
        val radius = (shape as? CornerBasedShape)?.topStart?.toPx(surfaceSize, this) ?: 0f
        val color = Color.Black.copy(alpha = SHEET_SHADOW_ALPHA / SHEET_SHADOW_LAYERS)
        for (layer in 1..SHEET_SHADOW_LAYERS) {
            val t = layer.toFloat() / SHEET_SHADOW_LAYERS
            val spread = reach * t * t
            drawRoundRect(
                color = color,
                topLeft = Offset(-spread, reach - spread),
                size = Size(size.width + spread * 2, size.height - reach + spread * 2),
                cornerRadius = CornerRadius(radius + spread)
            )
        }
    }.padding(top = SHEET_SHADOW_REACH)

private val SHEET_SHADOW_REACH = 28.dp
private const val SHEET_SHADOW_LAYERS = 14
private const val SHEET_SHADOW_ALPHA = 0.22f