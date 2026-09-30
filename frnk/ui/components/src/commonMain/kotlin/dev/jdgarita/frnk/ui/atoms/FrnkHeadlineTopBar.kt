package dev.jdgarita.frnk.ui.atoms

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.composeunstyled.UnstyledButton
import com.composeunstyled.theme.Theme
import com.composeunstyled.theme.ThemeToken
import dev.jdgarita.frnk.ui.haptics.HapticType
import dev.jdgarita.frnk.ui.haptics.LocalFrnkHaptics
import dev.jdgarita.frnk.ui.theme.FrnkIconSource
import dev.jdgarita.frnk.ui.theme.colorOnBackground
import dev.jdgarita.frnk.ui.theme.colorOnPrimary
import dev.jdgarita.frnk.ui.theme.colorOnSurfaceVariant
import dev.jdgarita.frnk.ui.theme.colorPrimary
import dev.jdgarita.frnk.ui.theme.colors
import dev.jdgarita.frnk.ui.theme.ext.resolve
import dev.jdgarita.frnk.ui.theme.headlineLarge
import dev.jdgarita.frnk.ui.theme.iconClose
import dev.jdgarita.frnk.ui.theme.iconSearch
import dev.jdgarita.frnk.ui.theme.labelSmall
import dev.jdgarita.frnk.ui.theme.shapeFull
import dev.jdgarita.frnk.ui.theme.shapes
import dev.jdgarita.frnk.ui.theme.spacing
import dev.jdgarita.frnk.ui.theme.spacingXs
import dev.jdgarita.frnk.ui.theme.spacingXxs
import dev.jdgarita.frnk.ui.theme.textStyles
import kotlinx.coroutines.delay

/** Duration of every headline swap: the title/field crossfade, the folds, and the corner glyph. */
private const val HEADLINE_SWAP_MILLIS = 220

/** Fast out, long settle — the headline's one easing curve. */
private val MECHANICAL_DECEL = CubicBezierEasing(0.2f, 0.8f, 0.2f, 1f)

/** How small the corner glyph is at the far end of its swap — visible, never a pop from nothing. */
private const val CORNER_GLYPH_REST_SCALE = 0.6f

/**
 * Headless large-headline top bar: a [FrnkHeadlineTopBarState.leading] action, a
 * `headlineLarge` title, then an optional badge, the actions and a corner slot. Built on foundation
 * primitives (no Material3); every tap except search goes through [onActionClick] by key.
 *
 * The bar draws no background and applies no horizontal inset or status-bar padding: the edge
 * buttons sit flush with the caller's bounds, and the caller owns the insets.
 *
 * With a [FrnkHeadlineTrailing.Search] corner, entering search is a choreography rather than a cut:
 * the leading action disables in place, the title crossfades into a field that carries it as
 * placeholder, the badge and actions fold toward the corner, and the search glyph gives way to the
 * close glyph. [onSearchOpen] / [onSearchClose] ask the host to flip
 * [FrnkHeadlineTrailing.Search.isActive]; [onSearchQueryChange] streams edits. A
 * [FrnkHeadlineTrailing.Action] corner never searches.
 *
 * Haptics: every button fires the standard Click through [LocalFrnkHaptics].
 */
@Composable
fun FrnkHeadlineTopBar(
    state: FrnkHeadlineTopBarState,
    onActionClick: (key: String) -> Unit,
    onSearchOpen: () -> Unit = {},
    onSearchQueryChange: (String) -> Unit = {},
    onSearchClose: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val trailing = state.trailing
    val isSearching = trailing is FrnkHeadlineTrailing.Search && trailing.isActive
    val title = state.title.resolve()

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        HeadlineActionButton(
            action = state.leading,
            enabled = state.leading.enabled && !isSearching,
            tint = colorOnSurfaceVariant,
            onActionClick = onActionClick
        )

        // The field's placeholder is the title in muted ink at the same size and place, so the
        // crossfade reads as the title going quiet and a cursor appearing after it.
        Crossfade(
            targetState = isSearching,
            modifier = Modifier.weight(1f),
            animationSpec = tween(HEADLINE_SWAP_MILLIS, easing = MECHANICAL_DECEL),
            label = "FrnkHeadlineTopBar.headline"
        ) { searching ->
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterStart) {
                if (searching && trailing is FrnkHeadlineTrailing.Search) {
                    HeadlineSearchField(
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = title,
                        query = trailing.query,
                        onQueryChange = onSearchQueryChange
                    )
                } else {
                    FrnkText(
                        state =
                            FrnkTextState.Raw(
                                text = title,
                                style = headlineLarge,
                                color = colorOnBackground,
                                singleLine = true
                            )
                    )
                }
            }
        }

        HeadlineTrailingActions(
            state = state,
            isSearching = isSearching,
            onActionClick = onActionClick,
            onSearchOpen = onSearchOpen,
            onSearchClose = onSearchClose
        )
    }
}

/**
 * The badge and actions fold away toward the corner as the search field claims their width, and fold
 * back the same way; the corner slot swaps its glyph with a fade and a scale so the two read as one
 * control changing state rather than two buttons taking turns.
 */
@Composable
private fun HeadlineTrailingActions(
    state: FrnkHeadlineTopBarState,
    isSearching: Boolean,
    onActionClick: (key: String) -> Unit,
    onSearchOpen: () -> Unit,
    onSearchClose: () -> Unit
) {
    val swap = tween<Float>(HEADLINE_SWAP_MILLIS, easing = MECHANICAL_DECEL)
    val fold = tween<IntSize>(HEADLINE_SWAP_MILLIS, easing = MECHANICAL_DECEL)
    val enter = fadeIn(swap) + expandHorizontally(fold, expandFrom = Alignment.End)
    val exit = fadeOut(swap) + shrinkHorizontally(fold, shrinkTowards = Alignment.End)

    Row(verticalAlignment = Alignment.CenterVertically) {
        state.badge?.let { badge ->
            AnimatedVisibility(
                visible = !isSearching,
                enter = enter,
                exit = exit,
                label = "FrnkHeadlineTopBar.badge"
            ) {
                HeadlineBadge(badge = badge, onClick = { onActionClick(badge.key) })
            }
        }

        state.actions.forEach { action ->
            AnimatedVisibility(
                visible = !isSearching,
                enter = enter,
                exit = exit,
                label = "FrnkHeadlineTopBar.action"
            ) {
                HeadlineActionButton(action = action, enabled = action.enabled, onActionClick = onActionClick)
            }
        }

        val trailing = state.trailing
        AnimatedContent(
            targetState = isSearching,
            transitionSpec = {
                (fadeIn(swap) + scaleIn(swap, initialScale = CORNER_GLYPH_REST_SCALE))
                    .togetherWith(fadeOut(swap) + scaleOut(swap, targetScale = CORNER_GLYPH_REST_SCALE))
                    .using(sizeTransform = null)
            },
            label = "FrnkHeadlineTopBar.corner"
        ) { searching ->
            when (trailing) {
                is FrnkHeadlineTrailing.Action ->
                    HeadlineActionButton(
                        action = trailing.action,
                        enabled = trailing.action.enabled,
                        onActionClick = onActionClick
                    )

                is FrnkHeadlineTrailing.Search ->
                    if (searching) {
                        FrnkIconButton(
                            state =
                                FrnkIconButtonState.Content(
                                    icon = FrnkIconSource.Token(iconClose),
                                    contentDescription = trailing.closeContentDescription.resolve(),
                                    tint = colorOnBackground
                                ),
                            onClick = onSearchClose
                        )
                    } else {
                        FrnkIconButton(
                            state =
                                FrnkIconButtonState.Content(
                                    icon = FrnkIconSource.Token(iconSearch),
                                    contentDescription = trailing.openContentDescription.resolve(),
                                    tint = colorOnBackground
                                ),
                            onClick = onSearchOpen
                        )
                    }
            }
        }
    }
}

@Composable
private fun HeadlineActionButton(
    action: FrnkHeadlineAction,
    enabled: Boolean,
    onActionClick: (key: String) -> Unit,
    tint: ThemeToken<Color> = colorOnBackground
) {
    FrnkIconButton(
        state =
            FrnkIconButtonState.Content(
                icon = action.icon,
                contentDescription = action.contentDescription.resolve(),
                tint = tint,
                enabled = enabled
            ),
        onClick = { onActionClick(action.key) }
    )
}

/**
 * The badge: a [colorPrimary] pill inside a 48 dp touch target. Its label is hidden from assistive
 * tech so the pill announces its [FrnkHeadlineBadge.contentDescription] (what a tap does) instead.
 */
@Composable
private fun HeadlineBadge(
    badge: FrnkHeadlineBadge,
    onClick: () -> Unit
) {
    val haptics = LocalFrnkHaptics.current
    val description = badge.contentDescription.resolve()
    UnstyledButton(
        onClick = {
            haptics.perform(HapticType.Click)
            onClick()
        },
        modifier =
            Modifier
                .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                .semantics { contentDescription = description }
    ) {
        Box(
            modifier =
                Modifier
                    .clearAndSetSemantics {}
                    .clip(Theme[shapes][shapeFull])
                    .background(Theme[colors][colorPrimary])
                    .padding(PaddingValues(horizontal = Theme[spacing][spacingXs], vertical = Theme[spacing][spacingXxs]))
        ) {
            FrnkText(
                state =
                    FrnkTextState.Raw(
                        content = badge.label,
                        style = labelSmall,
                        color = colorOnPrimary,
                        singleLine = true
                    )
            )
        }
    }
}

@Composable
private fun HeadlineSearchField(
    placeholder: String,
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val focusRequester = remember { FocusRequester() }

    // Ask for focus only once the headline swap has landed: focus raises the keyboard, and the IME's
    // own inset animation on top of the actions folding away is what drops frames on the way in.
    LaunchedEffect(Unit) {
        delay(HEADLINE_SWAP_MILLIS.toLong())
        focusRequester.requestFocus()
    }

    BasicTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier.focusRequester(focusRequester),
        textStyle = Theme[textStyles][headlineLarge].copy(color = Theme[colors][colorOnBackground]),
        cursorBrush = SolidColor(Theme[colors][colorPrimary]),
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        decorationBox = { innerTextField ->
            Box(contentAlignment = Alignment.CenterStart) {
                if (query.isEmpty()) {
                    FrnkText(
                        state =
                            FrnkTextState.Raw(
                                text = placeholder,
                                style = headlineLarge,
                                color = colorOnSurfaceVariant,
                                singleLine = true
                            )
                    )
                }
                innerTextField()
            }
        }
    )
}