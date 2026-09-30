package dev.jdgarita.frnk.ui.scaffolds.sheet.ext

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.Velocity
import dev.jdgarita.frnk.ui.scaffolds.sheet.FrnkModalSheet
import dev.jdgarita.frnk.ui.scaffolds.sheet.FrnkSheetDismissSwipe

/**
 * For the scroller inside a [FrnkModalSheet]'s body: a release over it never counts as a flick
 * against the sheet, only as a drag. Apply it to the scrolling container **outside** its
 * `verticalScroll` (or lazy list), so it sits between the scroll and the sheet in the nested
 * scroll chain.
 *
 * Without it a flick that brings the body back to its top ends with velocity the scroll cannot
 * spend, and compose-unstyled's own connection hands that leftover straight to the sheet's
 * settle — with the sheet at rest, a pixel unmoved, judged on [FrnkSheetDismissSwipe.velocityThreshold]
 * alone. A scroll-to-top flick is routinely several thousand dp/s, so it clears even
 * [FrnkSheetDismissSwipe.Heavy] and the sheet leaves under a reader's thumb. This connection runs
 * first (an inner connection sees a post-fling before its parent) and consumes the whole
 * velocity, so the sheet's connection still settles, just with none: at rest that is a no-op, and
 * a pull that dragged the sheet down is judged on [FrnkSheetDismissSwipe.positionalThreshold] alone.
 * The drag delta passes through untouched, so a deliberate pull from the top still moves the
 * sheet under the finger and still leaves once it covers the distance. The sheet's own drag —
 * on its handle, or any part of it that does not scroll — never goes through nested scroll and
 * keeps its flick.
 */
fun Modifier.sheetBodyScroll(): Modifier = nestedScroll(SheetBodyScrollConnection)

private object SheetBodyScrollConnection : NestedScrollConnection {
    override suspend fun onPostFling(
        consumed: Velocity,
        available: Velocity
    ): Velocity = available
}

/**
 * The sheet's width contract: fill the window up to [maxWidth]. The order is load-bearing — the
 * cap has to be the *outer* modifier so it narrows the constraints `fillMaxWidth` then fills; the
 * other way round, `fillMaxWidth` fixes the minimum at the window's width before the cap can
 * shrink it, and the cap does nothing.
 */
internal fun Modifier.sheetWidth(maxWidth: Dp): Modifier = widthIn(max = maxWidth).fillMaxWidth()