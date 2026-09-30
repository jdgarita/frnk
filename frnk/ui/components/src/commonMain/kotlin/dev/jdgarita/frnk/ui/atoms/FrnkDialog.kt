package dev.jdgarita.frnk.ui.atoms

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.composeunstyled.Modal
import com.composeunstyled.ProvideContentColor
import com.composeunstyled.UnstyledButton
import com.composeunstyled.rememberModalState
import com.composeunstyled.theme.Theme
import com.composeunstyled.theme.ThemeToken
import dev.jdgarita.frnk.ui.atoms.ext.defaultIconToken
import dev.jdgarita.frnk.ui.atoms.ext.onTintToken
import dev.jdgarita.frnk.ui.atoms.ext.tintToken
import dev.jdgarita.frnk.ui.haptics.HapticType
import dev.jdgarita.frnk.ui.haptics.LocalFrnkHaptics
import dev.jdgarita.frnk.ui.theme.FrnkIconSource
import dev.jdgarita.frnk.ui.theme.colorError
import dev.jdgarita.frnk.ui.theme.colorOnError
import dev.jdgarita.frnk.ui.theme.colorOnSurface
import dev.jdgarita.frnk.ui.theme.colorOnSurfaceVariant
import dev.jdgarita.frnk.ui.theme.colorOutline
import dev.jdgarita.frnk.ui.theme.colorOutlineVariant
import dev.jdgarita.frnk.ui.theme.colorScrim
import dev.jdgarita.frnk.ui.theme.colorSurface
import dev.jdgarita.frnk.ui.theme.colors
import dev.jdgarita.frnk.ui.theme.ext.resolve
import dev.jdgarita.frnk.ui.theme.iconSizeMd
import dev.jdgarita.frnk.ui.theme.iconSizes
import dev.jdgarita.frnk.ui.theme.labelLarge
import dev.jdgarita.frnk.ui.theme.labelMedium
import dev.jdgarita.frnk.ui.theme.shapeButton
import dev.jdgarita.frnk.ui.theme.shapeCard
import dev.jdgarita.frnk.ui.theme.shapeMedium
import dev.jdgarita.frnk.ui.theme.shapes
import dev.jdgarita.frnk.ui.theme.spacing
import dev.jdgarita.frnk.ui.theme.spacingLg
import dev.jdgarita.frnk.ui.theme.spacingMd
import dev.jdgarita.frnk.ui.theme.spacingSm
import dev.jdgarita.frnk.ui.theme.spacingXl
import dev.jdgarita.frnk.ui.theme.spacingXs
import dev.jdgarita.frnk.ui.theme.titleLarge

/** Alpha of the icon badge's tinted fill (the neutral variant uses [NEUTRAL_BADGE_FILL_ALPHA]). */
private const val BADGE_FILL_ALPHA = 0.12f
private const val NEUTRAL_BADGE_FILL_ALPHA = 0.05f

/**
 * The toolkit's alert dialog: one surface for confirmations (accent, destructive), failures
 * (warning), results (success) and information (neutral). A rounded card with a tinted icon badge,
 * an optional eyebrow, a title, optional body copy, an optional [extra] row and an action area,
 * centered over a scrim. Upstreamed from Faint's `PoAlert`; styled from theme tokens only, so each
 * host's `FrnkThemeConfig` decides how it looks.
 *
 * **Presentation.** Composing it shows it; removing it from the composition dismisses it. It is
 * presented through compose-unstyled's `Modal` — the same primitive `FrnkModalSheet` uses — so on
 * Android it sits in its own dialog window (inside a `ModalHost`, in that host's portal instead), marked
 * with dialog semantics. That keeps the screen beneath out of reach of TalkBack / VoiceOver and of
 * keyboard focus, not just of touch. Focus starts on the first action, and the title is a heading.
 *
 * **Back.** The dialog always consumes system back (and Escape) while it is shown, so back never pops
 * the screen beneath a still-visible dialog. It calls [onDismissRequest] when that is non-null; with
 * `null` (the default) back does nothing, and every exit is one of the [FrnkDialogState.actions]. There
 * is no outside-tap dismiss.
 *
 * **Gestures.** The scrim swallows every gesture that misses the card. The scrim is a sibling *below*
 * the card, not a modifier around it: an ancestor that consumed every pointer change would also cancel
 * the card's own buttons, since a press is cancelled by a move consumed anywhere on its path and a real
 * finger always drifts a pixel or two between down and up.
 *
 * Each action button fires `HapticType.Click` and reports its [FrnkDialogAction] through [onAction].
 *
 * @param onDismissRequest called on system back / Escape; `null` makes back a no-op while shown.
 * @param extra optional content between the body and the actions (an error code chip, a checkbox).
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun FrnkDialog(
    state: FrnkDialogState,
    onAction: (FrnkDialogAction) -> Unit,
    modifier: Modifier = Modifier,
    onDismissRequest: (() -> Unit)? = null,
    extra: (@Composable () -> Unit)? = null
) {
    val modalState = rememberModalState(initiallyVisible = true)
    val currentDismissRequest by rememberUpdatedState(onDismissRequest)
    Modal(
        state = modalState,
        onKeyEvent = { event ->
            if (event.key == Key.Escape) {
                if (event.type == KeyEventType.KeyUp) currentDismissRequest?.invoke()
                true
            } else {
                false
            }
        }
    ) {
        // Always enabled: a modal owns back while it is up, whether or not the host lets it dismiss.
        BackHandler(enabled = true) { currentDismissRequest?.invoke() }
        FrnkDialogOverlay(state = state, onAction = onAction, modifier = modifier, extra = extra)
    }
}

/**
 * The scrim and the centered card, filling the space given — what [FrnkDialog] draws inside its modal
 * window, and what the overlay preview renders (a platform window can't be previewed).
 */
@Composable
internal fun FrnkDialogOverlay(
    state: FrnkDialogState,
    onAction: (FrnkDialogAction) -> Unit,
    modifier: Modifier = Modifier,
    extra: (@Composable () -> Unit)? = null
) {
    val firstAction = remember { FocusRequester() }
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Box(
            Modifier
                .matchParentSize()
                .pointerInput(Unit) {
                    awaitEachGesture {
                        do {
                            val event = awaitPointerEvent()
                            event.changes.forEach { it.consume() }
                        } while (event.changes.any { it.pressed })
                    }
                }.background(Theme[colors][colorScrim])
        )
        FrnkDialogSurface(
            state = state,
            onAction = onAction,
            modifier = Modifier.padding(Theme[spacing][spacingXl]),
            extra = extra,
            firstActionFocus = firstAction
        )
    }
    if (state.actions.isNotEmpty()) {
        LaunchedEffect(firstAction) { firstAction.requestFocus() }
    }
}

/** The card alone, without the scrim — what [FrnkDialog] centers, and what previews render. */
@Composable
internal fun FrnkDialogSurface(
    state: FrnkDialogState,
    onAction: (FrnkDialogAction) -> Unit,
    modifier: Modifier = Modifier,
    extra: (@Composable () -> Unit)? = null,
    firstActionFocus: FocusRequester? = null
) {
    val shape = Theme[shapes][shapeCard]
    val tintToken = state.variant.tintToken()
    val tint = Theme[colors][tintToken]
    val title = state.title.resolve()

    Column(
        modifier
            .widthIn(max = FrnkDialogDefaults.MaxWidth)
            .fillMaxWidth()
            .clip(shape)
            .background(Theme[colors][colorSurface])
            .border(FrnkDialogDefaults.HairlineWidth, Theme[colors][colorOutlineVariant], shape)
            .semantics { paneTitle = title }
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(
                        start = Theme[spacing][spacingLg],
                        end = Theme[spacing][spacingLg],
                        top = Theme[spacing][spacingXl],
                        bottom = Theme[spacing][spacingMd]
                    ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            FrnkDialogIconBadge(
                icon = state.icon ?: FrnkIconSource.Token(state.variant.defaultIconToken()),
                tint = tint,
                neutral = state.variant == FrnkDialogVariant.Neutral,
                tintToken = tintToken
            )

            state.eyebrow?.let { eyebrow ->
                FrnkText(
                    state =
                        FrnkTextState.Raw(
                            text = eyebrow.resolve().uppercase(),
                            color = tintToken,
                            textAlign = TextAlign.Center,
                            style = labelMedium
                        ),
                    modifier = Modifier.padding(top = Theme[spacing][spacingSm])
                )
            }

            FrnkText(
                state =
                    FrnkTextState.Raw(
                        text = title,
                        color = colorOnSurface,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold,
                        style = titleLarge
                    ),
                modifier = Modifier.padding(top = Theme[spacing][spacingXs]).semantics { heading() }
            )

            state.body?.let { body ->
                FrnkText(
                    state =
                        FrnkTextState.BodyMedium(
                            content = body,
                            color = colorOnSurfaceVariant,
                            textAlign = TextAlign.Center
                        ),
                    modifier =
                        Modifier
                            .widthIn(max = FrnkDialogDefaults.BodyMaxWidth)
                            .padding(top = Theme[spacing][spacingSm])
                )
            }

            if (extra != null) {
                Box(Modifier.padding(top = Theme[spacing][spacingMd])) { extra() }
            }
        }

        FrnkDivider(
            state = FrnkDividerState.Horizontal(thickness = FrnkDialogDefaults.HairlineWidth, color = colorOutlineVariant)
        )

        val actionsPadding =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = Theme[spacing][spacingLg], vertical = Theme[spacing][spacingMd])
        val gap = Arrangement.spacedBy(Theme[spacing][spacingXs])
        when (state.actionLayout) {
            FrnkDialogActionLayout.Row ->
                Row(modifier = actionsPadding, horizontalArrangement = gap) {
                    state.actions.forEachIndexed { index, action ->
                        FrnkDialogButton(
                            action = action,
                            variant = state.variant,
                            onClick = { onAction(action) },
                            modifier = Modifier.weight(1f).focusFirst(index, firstActionFocus)
                        )
                    }
                }

            FrnkDialogActionLayout.Stacked ->
                Column(modifier = actionsPadding, verticalArrangement = gap) {
                    state.actions.forEachIndexed { index, action ->
                        FrnkDialogButton(
                            action = action,
                            variant = state.variant,
                            onClick = { onAction(action) },
                            modifier = Modifier.fillMaxWidth().focusFirst(index, firstActionFocus)
                        )
                    }
                }
        }
    }
}

@Composable
private fun FrnkDialogIconBadge(
    icon: FrnkIconSource,
    tint: Color,
    neutral: Boolean,
    tintToken: ThemeToken<Color>
) {
    val shape = Theme[shapes][shapeMedium]
    val fill =
        if (neutral) {
            Theme[colors][colorOnSurface].copy(alpha = NEUTRAL_BADGE_FILL_ALPHA)
        } else {
            tint.copy(alpha = BADGE_FILL_ALPHA)
        }
    Box(
        Modifier
            .size(FrnkDialogDefaults.IconBadgeSize)
            .clip(shape)
            .background(fill)
            .border(FrnkDialogDefaults.IconBadgeBorderWidth, tint, shape),
        contentAlignment = Alignment.Center
    ) {
        FrnkIcon(
            state =
                FrnkIconState.Content(
                    icon = icon,
                    contentDescription = null,
                    size = Theme[iconSizes][iconSizeMd],
                    tint = tintToken
                )
        )
    }
}

/** One action button: outlined for [FrnkDialogActionKind.Cancel], filled for the other two kinds. */
@Composable
private fun FrnkDialogButton(
    action: FrnkDialogAction,
    variant: FrnkDialogVariant,
    onClick: () -> Unit,
    modifier: Modifier
) {
    val shape = Theme[shapes][shapeButton]
    val haptics = LocalFrnkHaptics.current
    val fill: Color
    val content: Color
    when (action.kind) {
        FrnkDialogActionKind.Cancel -> {
            fill = Color.Transparent
            content = Theme[colors][colorOnSurface]
        }
        FrnkDialogActionKind.Primary -> {
            fill = Theme[colors][variant.tintToken()]
            content = Theme[colors][variant.onTintToken()]
        }
        FrnkDialogActionKind.Destructive -> {
            fill = Theme[colors][colorError]
            content = Theme[colors][colorOnError]
        }
    }
    val outlined =
        if (action.kind == FrnkDialogActionKind.Cancel) {
            Modifier.border(FrnkDialogDefaults.HairlineWidth, Theme[colors][colorOutline], shape)
        } else {
            Modifier
        }

    UnstyledButton(
        onClick = {
            haptics.perform(HapticType.Click)
            onClick()
        },
        modifier =
            modifier
                .defaultMinSize(minHeight = FrnkDialogDefaults.ActionMinHeight)
                .clip(shape)
                .background(fill)
                .then(outlined),
        contentPadding = PaddingValues(horizontal = Theme[spacing][spacingMd], vertical = Theme[spacing][spacingSm])
    ) {
        ProvideContentColor(content) {
            FrnkText(
                state =
                    FrnkTextState.Raw(
                        content = action.label,
                        textAlign = TextAlign.Center,
                        style = labelLarge
                    )
            )
        }
    }
}

/** Attaches [requester] to the first action only, so the dialog can move focus there when it opens. */
private fun Modifier.focusFirst(
    index: Int,
    requester: FocusRequester?
): Modifier = if (index == 0 && requester != null) focusRequester(requester) else this