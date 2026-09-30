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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.composeunstyled.ProvideContentColor
import com.composeunstyled.UnstyledButton
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
 * **Placement.** The dialog is drawn in the composition, not in a platform window: it fills the
 * space it is given, so compose it last in a full-screen `Box` (the app shell, above the screen it
 * interrupts) and remove it from the composition to dismiss it. It has no dismiss of its own — no
 * outside tap, no back handling — because every exit is an action the host chose; a host that wants
 * back to cancel installs its own `BackHandler` while the dialog is shown.
 *
 * **Gestures.** The scrim swallows every gesture that misses the card, so nothing underneath reacts
 * to a tap, long press or swipe while the dialog is up. The scrim is a sibling *below* the card, not
 * a modifier around it: an ancestor that consumed every pointer change would also cancel the card's
 * own buttons, since a press is cancelled by a move consumed anywhere on its path and a real finger
 * always drifts a pixel or two between down and up.
 *
 * Each action button fires `HapticType.Click` and reports its [FrnkDialogAction] through [onAction].
 *
 * @param extra optional content between the body and the actions (an error code chip, a checkbox).
 */
@Composable
fun FrnkDialog(
    state: FrnkDialogState,
    onAction: (FrnkDialogAction) -> Unit,
    modifier: Modifier = Modifier,
    extra: (@Composable () -> Unit)? = null
) {
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
            extra = extra
        )
    }
}

/** The card alone, without the scrim — what [FrnkDialog] centers, and what previews render. */
@Composable
internal fun FrnkDialogSurface(
    state: FrnkDialogState,
    onAction: (FrnkDialogAction) -> Unit,
    modifier: Modifier = Modifier,
    extra: (@Composable () -> Unit)? = null
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
                modifier = Modifier.padding(top = Theme[spacing][spacingXs])
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
                    state.actions.forEach { action ->
                        FrnkDialogButton(
                            action = action,
                            variant = state.variant,
                            onClick = { onAction(action) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

            FrnkDialogActionLayout.Stacked ->
                Column(modifier = actionsPadding, verticalArrangement = gap) {
                    state.actions.forEach { action ->
                        FrnkDialogButton(
                            action = action,
                            variant = state.variant,
                            onClick = { onAction(action) },
                            modifier = Modifier.fillMaxWidth()
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