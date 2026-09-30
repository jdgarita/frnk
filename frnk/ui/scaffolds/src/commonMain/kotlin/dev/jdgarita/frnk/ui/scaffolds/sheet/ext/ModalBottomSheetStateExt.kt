package dev.jdgarita.frnk.ui.scaffolds.sheet.ext

import com.composeunstyled.ModalBottomSheetState
import com.composeunstyled.SheetDetent

/**
 * Starts the scrim's fade for a hide the host has decided (`visible` dropping), so it runs
 * alongside the slide that `targetDetent = Hidden` starts next rather than after its settle.
 * Unconditional: the library flips the same flag itself once the slide is done, and flipping it
 * on a modal that is not showing changes nothing.
 */
internal fun ModalBottomSheetState.fadeScrimWithHide() {
    modalState.transitionState.targetState = false
}

/**
 * Starts the scrim's fade for a swipe settling at Hidden. Called from the detent predicate, which
 * Foundation also consults while a still-hidden sheet's anchors are first laid out; the guard on
 * the *settled* detent is what keeps that call from cancelling a presentation on its way in.
 */
internal fun ModalBottomSheetState.fadeScrimWithSwipe() {
    if (bottomSheetState.currentDetent != SheetDetent.Hidden) {
        modalState.transitionState.targetState = false
    }
}

/**
 * Targets the scrim shown again, for a present that lands while the previous exit's fade is
 * still running — the library only re-targets it for a modal that is fully hidden.
 */
internal fun ModalBottomSheetState.showScrim() {
    modalState.transitionState.targetState = true
}