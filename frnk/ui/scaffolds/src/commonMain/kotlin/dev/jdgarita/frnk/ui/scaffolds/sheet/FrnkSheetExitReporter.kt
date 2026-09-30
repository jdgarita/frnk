package dev.jdgarita.frnk.ui.scaffolds.sheet

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import com.composeunstyled.ModalBottomSheetState
import com.composeunstyled.SheetDetent
import dev.jdgarita.frnk.ui.scaffolds.sheet.ext.fadeScrimWithSwipe

/**
 * Reports a sheet's exit to its host **at most once per presentation through this reporter**,
 * read off the sheet state itself rather than waited for from compose-unstyled's callback. The
 * host's `onDismiss` can still hear the same close twice: back and the scrim tap call it directly,
 * bypassing [report], and the library's settle callback then arrives here — so hosts must treat
 * `onDismiss` idempotently (set `visible = false`; don't count).
 *
 * The library reports a swipe through `UnstyledModalBottomSheet`'s `onDismiss`, from a
 * `LaunchedEffect` that lives *inside* the modal and fires once the sheet has settled at Hidden.
 * Since the scrim leaves at the gesture's release ([fadeScrimWithSwipe]) rather than after the
 * settle, the modal's lifetime no longer waits for that callback: when the fade has finished
 * before the slide, the settle is also the moment the modal's last fragment goes, and the modal
 * can tear down before the effect body runs — the effect is cancelled, the callback never comes,
 * and the host's `visible` flag stays true against a sheet that has already left. Every later present is then a
 * no-op. The library also drops the callback when its `dismissRequested` guard is still set from
 * a previous exit in the same mount.
 *
 * So the host composition watches the sheet directly ([rememberFrnkSheetExitReporter]): a sheet
 * that was up — or on its way up — while the host held it visible, and is now settled at Hidden,
 * has been dismissed by the user, and [report] tells the host. The library's callback is routed
 * through [report] as well, so whichever of the two arrives first is the one the host hears and a
 * swipe is never counted twice. A programmatic hide (`visible` already false) is not a user exit
 * and is never detected here; the library's own callback for it still passes through, as before.
 *
 * Plain fields, not snapshot state: the reporter is read only from effects and never drives
 * composition.
 */
internal class FrnkSheetExitReporter(
    private val onDismiss: () -> Unit
) {
    private var reported = false
    private var wasUp = false

    /** Forwards the exit to the host unless this presentation's exit has already been reported. */
    fun report() {
        if (reported) return
        reported = true
        onDismiss()
    }

    /** A new presentation: its exit is still to come. */
    fun presented() {
        reported = false
        wasUp = false
    }

    /**
     * One observation of the sheet: whether the host holds it [visible], and whether it is settled
     * at Hidden ([settledHidden]). The exit is the first settled-hidden observation after the
     * sheet was seen anywhere else while visible.
     */
    fun observe(
        visible: Boolean,
        settledHidden: Boolean
    ) {
        when {
            !visible -> wasUp = false
            !settledHidden -> wasUp = true
            wasUp -> {
                wasUp = false
                report()
            }
        }
    }
}

/**
 * A [FrnkSheetExitReporter] bound to [sheetState] and the host's [visible] flag. Pass its
 * [FrnkSheetExitReporter.report] as the library's `onDismiss`.
 */
@Composable
internal fun rememberFrnkSheetExitReporter(
    sheetState: ModalBottomSheetState,
    visible: Boolean,
    onDismiss: () -> Unit
): FrnkSheetExitReporter {
    val currentOnDismiss by rememberUpdatedState(onDismiss)
    val currentVisible by rememberUpdatedState(visible)
    val reporter = remember { FrnkSheetExitReporter { currentOnDismiss() } }
    LaunchedEffect(visible) {
        if (visible) reporter.presented()
    }
    LaunchedEffect(sheetState) {
        val sheet = sheetState.bottomSheetState
        snapshotFlow {
            val settledHidden =
                sheet.isIdle &&
                    sheet.currentDetent == SheetDetent.Hidden &&
                    sheet.targetDetent == SheetDetent.Hidden
            currentVisible to settledHidden
        }.collect { (isVisible, settledHidden) -> reporter.observe(isVisible, settledHidden) }
    }
    return reporter
}