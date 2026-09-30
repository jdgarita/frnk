package dev.jdgarita.frnk.ui.scaffolds.sheet

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.node.Ref
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.isSpecified
import com.composeunstyled.DragIndication
import com.composeunstyled.ModalBottomSheetProperties
import com.composeunstyled.ModalBottomSheetState
import com.composeunstyled.Scrim
import com.composeunstyled.Sheet
import com.composeunstyled.SheetDetent
import com.composeunstyled.UnstyledModalBottomSheet
import com.composeunstyled.rememberModalBottomSheetState
import com.composeunstyled.theme.Theme
import dev.jdgarita.frnk.ui.mvi.LocalFrnkBackHandledByHost
import dev.jdgarita.frnk.ui.scaffolds.sheet.ext.fadeScrimWithHide
import dev.jdgarita.frnk.ui.scaffolds.sheet.ext.fadeScrimWithSwipe
import dev.jdgarita.frnk.ui.scaffolds.sheet.ext.sheetWidth
import dev.jdgarita.frnk.ui.scaffolds.sheet.ext.showScrim
import dev.jdgarita.frnk.ui.theme.colorScrim
import dev.jdgarita.frnk.ui.theme.colors

/**
 * Modal sheet over the current screen, themed from the frnk theme.
 *
 * The sheet covers [heightFraction] of the window's height (rising from the bottom edge) and the
 * window's full width up to [maxWidth], centered horizontally — compose-unstyled itself never
 * centers a sheet, so the centering lives in here. On a phone the cap never binds and the sheet
 * spans edge to edge; on a tablet it stops at [FrnkModalSheetDefaults.MaxWidth] so the content keeps
 * a phone-like measure instead of stretching across the whole slab, with the scrim showing in the
 * gutters. The height defaults to **the content's own height** — with neither [heightFraction]
 * nor [topInset] given the detent is compose-unstyled's `SheetDetent.FullyExpanded`, which resolves
 * to the *sheet's* measured height, not the container's, so a short sheet rises only as far as its
 * content. A fraction makes the sheet that tall regardless of content; [topInset] then shaves a
 * fixed strip off the resolved height, which is how a near-full sheet stops exactly under the OS
 * status bar rather than guessing at a fraction. The exposed gutters and the area above a partial
 * sheet show the scrim and dismiss only when [dismissOnClickOutside] is set — a gutter tap on a
 * tablet counts as an outside tap, like the strip above a partial sheet.
 *
 * [visible] drives the sheet; [onDismiss] fires for every dismissal the sheet performs itself
 * (system back, swipe down, tap outside), so the caller can drop its own visibility state. A single
 * close can reach [onDismiss] more than once — back calls it, then compose-unstyled's settle
 * callback calls it again once the hidden sheet comes to rest — so hosts must treat it
 * idempotently: set `visible = false`, never count calls. That settle callback also runs after a
 * host's **own** programmatic hide (`visible = false`) once the sheet comes to rest, so [onDismiss]
 * is not proof the user dismissed anything: a host that logs "user dismissed" analytics from it must
 * first check its own intent (e.g. whether it had already set `visible = false` itself).
 * A swipe is reported by the sheet itself, from its own state ([FrnkSheetExitReporter]) — never
 * only by compose-unstyled's callback, which the modal's teardown can outrun. The content is
 * composed only while the sheet is on screen, so `remember` inside it is per-presentation.
 *
 * Every exit is one choreography: the sheet slides down on one spring and the scrim fades
 * **alongside** it, not after it. compose-unstyled's own `targetDetent = Hidden` path starts the
 * fade only once the slide has settled, and the spring settles some 300ms after the sheet has
 * visibly left — a dark, motionless scrim that reads as lag on every dismissal. So the fade is
 * started here the moment an exit begins: with the hide when [visible] drops
 * (`fadeScrimWithHide`), and on the release when a swipe the veto allows settles at Hidden
 * (`fadeScrimWithSwipe`). The modal still tears down only once *both* are done, so the content
 * stays composed for the whole slide.
 *
 * [canDismiss] is the veto a sheet with unsaved work needs. Back routes through [onDismiss],
 * which the content can answer with a confirmation of its own, but a **swipe down** never
 * reaches [onDismiss] until the sheet has already left — the gesture drives the sheet directly.
 * Returning false here refuses the drag's settle at the Hidden detent, so the sheet springs back
 * and the gesture becomes another way to *ask*, alongside back. An **outside tap** is held to
 * the same predicate: it fires [onDismiss] only when the veto lets it, and is otherwise dropped
 * once the predicate has reported it — so a sheet reads a tap on the scrim exactly as it reads a
 * swipe down, and the veto's refusal is the one signal either gesture leaves behind. Back is
 * deliberately not gated: a sheet that needs the veto also needs to hear about the attempt, and
 * the caller decides what to do with it. A host's own hide (`visible = false`) is never vetoed:
 * [canDismiss] only gates the user's gestures (swipe, scrim tap), so a host can always close a
 * vetoing sheet itself.
 *
 * [dismissSwipe] is how much of a swipe it takes to leave: the drag length, or the flick speed,
 * past which a release settles at Hidden rather than springing back. Every sheet takes
 * [FrnkSheetDismissSwipe.Default], the library's own feel; a sheet whose body scrolls under the
 * finger asks for [FrnkSheetDismissSwipe.Heavy], so a pull that began as the scroll's overscroll at
 * the top does not carry the sheet off with it — a swipe the user means is still one swipe, only a
 * longer or a faster one. It changes when a drag *counts*, never whether it may: [canDismiss] is
 * still the veto. The thresholds are captured when the sheet is first composed; a later change to
 * [dismissSwipe] is ignored.
 *
 * [scrimColor] is the dim over what the sheet covers, the theme's `colorScrim` by default. A sheet
 * whose content must read at full strength over what it covers passes transparent. A transparent
 * scrim is still the scrim — it still takes the outside tap and still blocks the screen underneath.
 * A sheet without a scrim should pass [elevated], which draws a soft shadow over its rounded top
 * edge so the sheet reads as lifted off the screen it covers rather than merged into it.
 *
 * **The sheet owns system back.** On Android the sheet gets its own dialog window, so a host-side
 * `BackHandler` never sees back while the sheet is up — the handler has to live in here. [content]
 * is composed with [LocalFrnkBackHandledByHost] set, so a `FrnkScreen` inside a sheet does not
 * install the leaf handler that would otherwise swallow back before the sheet sees it; a screen that
 * genuinely needs back (e.g. to log a dismissal) still opts in with `handleBackPressed = true` and
 * wins, being the deeper handler. [dismissOnBackPress] only controls compose-unstyled's own back
 * handler: while [visible], back always calls [onDismiss] through the sheet's handler, so a host
 * that must not close ignores it in [onDismiss].
 *
 * **The ✕ is the sheet's, not the content's.** A sheet that offers a close control draws it in
 * its own top-end corner, above the content and at one fixed geometry for every sheet, the way it
 * draws the grabber. The content asks for it with [FrnkModalSheetCloseHandler], modelled on
 * `BackHandler`: it names the control for a screen reader and says what a tap does, and the sheet
 * shows the glyph while the handler is composed. What the tap *means* stays the content's — which
 * is why the sheet never wires the ✕ to [onDismiss] itself.
 *
 * The sheet's look is layered out of two previewable pieces — `FrnkModalSheetScrim` (the dimming
 * overlay) and `FrnkModalSheetSurface` (the clipped surface column: drag indicator on top, content
 * below, the ✕ over its corner) — so each can be iterated on in isolation; this composable only
 * adds the modal wiring. Content that settles into motion of its own can wait on
 * [LocalFrnkModalSheetSettled].
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun FrnkModalSheet(
    visible: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    dismissOnBackPress: Boolean = true,
    dismissOnClickOutside: Boolean = false,
    maxWidth: Dp = FrnkModalSheetDefaults.MaxWidth,
    heightFraction: Float = 1f,
    topInset: Dp = Dp.Unspecified,
    surfaceColor: Color = Color.Transparent,
    surfaceBackdrop: (@Composable BoxScope.() -> Unit)? = null,
    scrimColor: Color = Theme[colors][colorScrim],
    elevated: Boolean = false,
    offsetForIme: Boolean = true,
    canDismiss: () -> Boolean = { true },
    dismissSwipe: FrnkSheetDismissSwipe = FrnkSheetDismissSwipe.Default,
    content: @Composable () -> Unit
) {
    // No dismissAnimationSpec: compose-unstyled's own `requestDismiss` path (tap outside, and
    // back where the library's EscapeHandler runs) would take it, and a second, faster spec made
    // tap-outside visibly skip the slide the other exits perform. Left null, that path falls
    // through to the sheet's default animation — the same [SHEET_SLIDE_SPRING] every
    // `targetDetent = Hidden` dismissal takes — so all exits share one slide, with the scrim
    // fading alongside it and the content composed until both are done. Size anything that has
    // to outlive the exit against the spring's settle (~400ms).

    // The fraction rides the identifier: SheetDetent equality is identifier-only, so a tweaked
    // fraction has to read as a *different* detent for the state to pick it up. The inset does
    // NOT ride it: the sheet state's detent list is fixed at creation, and insets report 0 until
    // the window is attached, so an identifier carrying the inset value would flip to an unknown
    // detent once the real inset lands — the state throws on the next present. Instead the detent
    // keeps one identity per call site (whether an inset applies at all is a call-site constant)
    // and its height lambda reads the *current* inset, which the sheet re-evaluates whenever it
    // recomputes anchors.
    val currentTopInset by rememberUpdatedState(topInset)
    val targetDetent =
        remember(heightFraction, topInset.isSpecified) {
            when {
                !topInset.isSpecified && heightFraction >= 1f -> SheetDetent.FullyExpanded
                !topInset.isSpecified ->
                    SheetDetent("fraction-$heightFraction") { containerHeight, _ ->
                        containerHeight * heightFraction
                    }

                else ->
                    SheetDetent("fraction-$heightFraction-minus-top-inset") { containerHeight, _ ->
                        (containerHeight * heightFraction - currentTopInset).coerceAtLeast(0.dp)
                    }
            }
        }

    // Read through rememberUpdatedState: the sheet state captures this lambda once, at creation,
    // and never rebuilds it — a predicate closed over stale state would keep vetoing (or keep
    // allowing) long after the content moved on.
    val currentCanDismiss by rememberUpdatedState(canDismiss)
    val currentVisible by rememberUpdatedState(visible)
    // The state cannot be named inside its own predicate, so the swipe hook reaches it through
    // this reference, filled in right after creation.
    val sheetStateRef = remember { Ref<ModalBottomSheetState>() }
    val sheetState =
        rememberModalBottomSheetState(
            initialDetent = SheetDetent.Hidden,
            detents = listOf(SheetDetent.Hidden, targetDetent),
            animationSpec = SHEET_SLIDE_SPRING,
            velocityThreshold = { dismissSwipe.velocityThreshold },
            positionalThreshold = dismissSwipe.positionalThreshold,
            confirmDetentChange = { detent ->
                if (detent != SheetDetent.Hidden) {
                    true
                } else {
                    // A host's own hide (visible = false) is never vetoed: the host has decided,
                    // and the veto only gates the user's gestures (swipe, scrim tap).
                    val allowed = !currentVisible || currentCanDismiss()
                    // A swipe the veto lets settle at Hidden is an exit that began on the
                    // release, so the scrim leaves from here rather than after the settle.
                    if (allowed) sheetStateRef.value?.fadeScrimWithSwipe()
                    allowed
                }
            }
        )
    sheetStateRef.value = sheetState

    LaunchedEffect(visible, targetDetent) {
        if (visible) {
            // A present that lands inside the previous exit finds the scrim mid-fade; it comes
            // back with the sheet. A no-op for a fresh presentation.
            sheetState.showScrim()
            sheetState.targetDetent = targetDetent
        } else {
            sheetState.fadeScrimWithHide()
            sheetState.targetDetent = SheetDetent.Hidden
        }
    }

    val currentOnDismiss by rememberUpdatedState(onDismiss)
    // The sheet's own exits (a swipe) reach the host from here, whether or not the library's
    // callback below survives the modal's teardown — see [FrnkSheetExitReporter]. Back and the
    // scrim tap call [onDismiss] directly, so their settle can reach it a second time (idempotent).
    val exitReporter = rememberFrnkSheetExitReporter(sheetState, visible, onDismiss)
    // Where the content registers its ✕ ([FrnkModalSheetCloseHandler]); the surface draws it.
    val closeRegistry = remember { FrnkModalSheetCloseRegistry() }

    UnstyledModalBottomSheet(
        state = sheetState,
        // The library's dismissOnClickOutside stays OFF even when [dismissOnClickOutside] is set:
        // its internal `requestDismiss` never goes through `targetDetent` — it animates the sheet
        // directly and collapses the scrim concurrently with the slide — so a tap-out read
        // differently from the back-gesture exit. Outside taps are caught on the scrim below
        // instead and routed through [onDismiss], the same entry the back handler uses, so every
        // exit plays the identical slide, scrim fading alongside. This also keeps a tap on
        // non-consuming sheet content (e.g. a camera preview) from counting as "outside": the
        // scrim never receives taps that land on the sheet.
        properties =
            ModalBottomSheetProperties(
                dismissOnBackPress = dismissOnBackPress,
                dismissOnClickOutside = false,
                // A near-full-height sheet has nowhere to rise to, so the library's IME offset
                // would push its header off the top of the window. Such a sheet turns this off and
                // pads its own scrolling content against the keyboard instead.
                offsetForIme = offsetForIme
            ),
        onDismiss = exitReporter::report,
        overlay = {
            // The library's [Scrim] rather than `FrnkModalSheetScrim`: it owns the modal wiring
            // (visibility off the modal state, the fade pair). Both fill with the same colour, so
            // the previewable mirror cannot drift from what ships.
            Scrim(
                modifier =
                    when {
                        dismissOnClickOutside ->
                            Modifier.pointerInput(Unit) {
                                detectTapGestures {
                                    // Gated on [visible]: the scrim outlives the exit, and a tap
                                    // during the slide must not fire a second dismissal. Then on
                                    // the veto, exactly like the swipe's settle: a refused tap
                                    // goes nowhere, and the predicate has already reported it.
                                    if (currentVisible && currentCanDismiss()) currentOnDismiss()
                                }
                            }

                        else -> Modifier
                    },
                scrimColor = scrimColor,
                enter = fadeIn(),
                exit = fadeOut()
            )
        }
    ) {
        // compose-unstyled top-starts the sheet in its wrapper and only ever offsets it
        // vertically, so a sheet narrower than the window (a capped one on a tablet) would hug the
        // left edge. This full-size Box rides that vertical offset and supplies the missing
        // horizontal centering; its empty regions carry no input handlers, so gutter taps still
        // fall through to the scrim's outside-tap detector rather than being swallowed here.
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.TopCenter
        ) {
            Sheet(
                modifier = modifier.sheetWidth(maxWidth)
            ) {
                // Composed before [content], so a handler the content installs itself is the deeper
                // one and takes back first. Disabled while dismissing, so this handler does not fire
                // [onDismiss] a second time during the exit animation. A second back then goes to
                // whatever handler is next: with the default dismissOnBackPress = true that is
                // compose-unstyled's own EscapeHandler, which calls [onDismiss] again (hence the
                // idempotency contract); only with it off does back fall through to the host.
                BackHandler(enabled = visible) { onDismiss() }

                FrnkModalSheetSurface(
                    surfaceColor = surfaceColor,
                    surfaceBackdrop = surfaceBackdrop,
                    elevated = elevated,
                    close = closeRegistry.close,
                    dragIndication = { indicator ->
                        DragIndication(
                            modifier = indicator,
                            // No ripple: the grabber reads as an affordance rather than a control.
                            indication = null
                        )
                    }
                ) {
                    CompositionLocalProvider(
                        LocalFrnkBackHandledByHost provides true,
                        LocalFrnkModalSheetCloseRegistry provides closeRegistry,
                        LocalFrnkModalSheetSettled provides (sheetState.isIdle && sheetState.currentDetent == targetDetent)
                    ) {
                        content()
                    }
                }
            }
        }
    }
}

/** The one slide every entrance and exit takes: a soft, barely-underdamped spring. */
private val SHEET_SLIDE_SPRING: SpringSpec<Float> =
    spring(dampingRatio = 0.9f, stiffness = Spring.StiffnessMediumLow, visibilityThreshold = 0.5f)