package dev.jdgarita.frnk.ui.scaffolds.sheet

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf

/**
 * The sheet's ✕, as its content declares it: what a screen reader calls it and what a tap does.
 * The content description is per sheet on purpose ("Close Paywall", "Close Manual Entry") — the
 * control is the same, what it closes is not.
 */
internal class FrnkModalSheetClose(
    val contentDescription: String,
    val onClick: () -> Unit
)

/**
 * Declares the sheet's ✕ from inside its content, the way `BackHandler` declares back: the sheet
 * draws the control in its own corner for as long as this is composed, and a tap runs [onClose].
 * A no-op outside a [FrnkModalSheet] (no registry in scope), so content composed on its own — a
 * preview, a test of the body — never has to guard.
 *
 * One per sheet: a second handler in the same content replaces the first, which is the
 * `BackHandler` rule too. Read through `rememberUpdatedState`, so the registered action always
 * runs the latest lambda without re-registering.
 */
@Composable
fun FrnkModalSheetCloseHandler(
    contentDescription: String,
    onClose: () -> Unit
) {
    val registry = LocalFrnkModalSheetCloseRegistry.current ?: return
    val currentOnClose by rememberUpdatedState(onClose)
    DisposableEffect(registry, contentDescription) {
        registry.close = FrnkModalSheetClose(contentDescription) { currentOnClose() }
        onDispose { registry.close = null }
    }
}

/** The one slot a sheet's content can fill: its ✕. Snapshot state, so the surface redraws with it. */
internal class FrnkModalSheetCloseRegistry {
    var close: FrnkModalSheetClose? by mutableStateOf(null)
}

/** Provided by [FrnkModalSheet] to its content; null anywhere else, and the handler stands down. */
internal val LocalFrnkModalSheetCloseRegistry = staticCompositionLocalOf<FrnkModalSheetCloseRegistry?> { null }