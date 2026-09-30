package dev.jdgarita.frnk.ui.scaffolds.sheet

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.composeunstyled.theme.Theme
import dev.jdgarita.frnk.ui.theme.spacing
import dev.jdgarita.frnk.ui.theme.spacingLg
import dev.jdgarita.frnk.ui.theme.spacingMd
import dev.jdgarita.frnk.ui.theme.spacingSm
import dev.jdgarita.frnk.ui.theme.spacingXs

/** Geometry shared by [FrnkModalSheet], its surface and [FrnkSheetHeader]. */
object FrnkModalSheetDefaults {
    /**
     * Widest any modal sheet gets. Below it a sheet spans the window edge to edge, which is every
     * phone; above it — tablets, and a phone-sized window is never this wide — the sheet holds this
     * measure centered so its content keeps phone proportions rather than stretching. Material's
     * bottom sheets cap at the same width, so a sheet sits where a tablet user already expects one.
     */
    val MaxWidth: Dp = 640.dp

    /**
     * What the sheet's surface adds around its content, top and bottom: the drag indicator's row
     * above it and the spacer under it. A content-height sheet's full height is its content's plus
     * this — what a caller measuring its content needs to know how much of the window the sheet takes.
     */
    val ChromeHeight: Dp
        @Composable get() = Theme[spacing][spacingMd] + IndicatorHeight + Theme[spacing][spacingXs] + Theme[spacing][spacingMd]

    /**
     * Every modal sheet's horizontal gutter. One value for the header and the body alike, so a
     * sheet's title, its controls and its ✕ all share one content edge ([CloseInsetEnd] is derived
     * from it).
     */
    val Gutter: Dp
        @Composable get() = Theme[spacing][spacingLg]

    /**
     * How far the ✕'s 48dp box sits under the sheet's top edge. Its 24dp glyph is then centred 40dp
     * down — level with the eyebrow or headline a [FrnkSheetHeader] puts first — while the box clears
     * the grabber's row entirely on the side, so the two never contend for a touch.
     */
    internal val CloseInsetTop: Dp
        @Composable get() = Theme[spacing][spacingMd]

    /**
     * How far the ✕'s box sits in from the sheet's end edge: the box overhangs the gutter so its
     * glyph, not its hit box, ends on the content edge — 12dp of box inset plus the button's own
     * 12dp glyph inset make the 24dp [Gutter].
     */
    internal val CloseInsetEnd: Dp
        @Composable get() = Theme[spacing][spacingSm]

    /** The drag indicator's height; its width is fixed at 32dp. */
    internal val IndicatorHeight: Dp = 4.dp
}

/**
 * Whether the hosting [FrnkModalSheet] has come to rest at its shown detent — false while it is
 * still sliding up (and again once it starts to leave or is dragged). Content that plays an
 * entrance of its own waits on this, so the sheet's slide and the content's motion never run at
 * once. `true` outside any sheet, so a preview or a screen plays straight away. Non-static: it
 * flips twice per presentation, and only its readers should pay for that.
 */
val LocalFrnkModalSheetSettled: ProvidableCompositionLocal<Boolean> = compositionLocalOf { true }