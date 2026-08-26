package dev.jdgarita.frnk.ui.atoms

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.composeunstyled.theme.Theme
import dev.jdgarita.frnk.ui.theme.FrnkIconSource
import dev.jdgarita.frnk.ui.theme.FrnkStringSource
import dev.jdgarita.frnk.ui.theme.bodyLarge
import dev.jdgarita.frnk.ui.theme.colorBackground
import dev.jdgarita.frnk.ui.theme.colorOnBackground
import dev.jdgarita.frnk.ui.theme.colorOnSurfaceVariant
import dev.jdgarita.frnk.ui.theme.colorPrimary
import dev.jdgarita.frnk.ui.theme.colors
import dev.jdgarita.frnk.ui.theme.ext.resolve
import dev.jdgarita.frnk.ui.theme.iconBack
import dev.jdgarita.frnk.ui.theme.iconClose
import dev.jdgarita.frnk.ui.theme.icons
import dev.jdgarita.frnk.ui.theme.spacing
import dev.jdgarita.frnk.ui.theme.spacingSm
import dev.jdgarita.frnk.ui.theme.spacingXs
import dev.jdgarita.frnk.ui.theme.spacingXxs
import dev.jdgarita.frnk.ui.theme.stringBack
import dev.jdgarita.frnk.ui.theme.stringSearch
import dev.jdgarita.frnk.ui.theme.stringSearchClear
import dev.jdgarita.frnk.ui.theme.stringSearchClose
import dev.jdgarita.frnk.ui.theme.strings
import dev.jdgarita.frnk.ui.theme.textStyles

/** A single trailing action rendered as an icon button on the right of [FrnkTopAppBar]. */
@Immutable
data class FrnkTopAppBarAction(
    val icon: FrnkIconSource,
    /** Doubles as the icon button's accessibility label; resolved at the bar, so tokens localize. */
    val contentDescription: FrnkStringSource,
    /** Stable identifier the host switches on in `onActionClick`. */
    val key: String
) {
    /**
     * Raw-string convenience mirroring [FrnkTextState]'s String constructors; [key] keeps its old
     * defaults-to-the-description behavior here, where the description is still a plain String.
     */
    constructor(
        icon: FrnkIconSource,
        contentDescription: String,
        key: String = contentDescription
    ) : this(icon, FrnkStringSource.Raw(contentDescription), key)
}

/**
 * View state for [FrnkTopAppBar].
 *
 * State shape — **Category C** (single-state `data class`, no `Skeleton`): the bar has one visual
 * state (search mode is a field, not a separate state) and never shows a loading placeholder. See the
 * component-state taxonomy in `docs/HOST_INTEGRATION.md` §9.
 */
@Immutable
data class FrnkTopAppBarState(
    val title: FrnkStringSource,
    /** Optional leading icon (e.g. a back arrow). `null` renders no leading button. */
    val navigationIcon: FrnkIconSource? = null,
    val navigationContentDescription: String? = null,
    val actions: List<FrnkTopAppBarAction> = emptyList(),
    /**
     * When true (default) the bar insets itself below the system status bar so its content never
     * draws under the clock/battery/wifi. Hosts that already consume the status-bar inset higher up
     * the tree should pass `false` to avoid double padding.
     */
    val applyStatusBarPadding: Boolean = true,
    /**
     * When true the bar swaps its title + [actions] for a focused search input (a leading "close
     * search" button, a text field, and a "clear" button once [searchQuery] is non-empty). The host
     * owns this flag — typically flipped on from an `onActionClick` that matches a search action, and
     * off from `onSearchClose`.
     */
    val isSearchActive: Boolean = false,
    val searchQuery: String = "",
    /** Placeholder shown while [searchQuery] is empty; `null` uses the [stringSearch] token. */
    val searchPlaceholder: String? = null
)

/** Shared layout metrics for [FrnkTopAppBar]. */
object FrnkTopAppBarDefaults {
    /**
     * Height of the bar's content row, **excluding** the status-bar inset the bar adds on top of it
     * when [FrnkTopAppBarState.applyStatusBarPadding] is set. Hosts that float the bar over edge-to-edge
     * content (and thus need to know how far to translate it to hide it, or how much top padding to
     * reserve) add the status-bar inset to this themselves.
     */
    val BarHeight: Dp = 56.dp
}

/**
 * Headless top app bar — a leading-aligned [title] with an optional [navigationIcon] on the left and
 * a row of [actions] on the right, all rendered through [FrnkIconButton] / [FrnkText]. Built on
 * foundation primitives (no Material3); colours resolve from [colorBackground] / [colorOnBackground].
 *
 * By default the bar applies the [WindowInsets.statusBars] inset, so it sits below the system status
 * bar in an edge-to-edge layout (the bar's background still fills behind the status bar). Set
 * [FrnkTopAppBarState.applyStatusBarPadding] to false if the host handles that inset itself.
 *
 * When [FrnkTopAppBarState.isSearchActive] is true the bar becomes a search field: the text field
 * auto-focuses, [onSearchQueryChange] streams edits (and clears on the trailing button), and the
 * leading button fires [onSearchClose] to exit search. Otherwise [onNavigationClick] fires for the
 * leading icon and [onActionClick] receives the tapped [FrnkTopAppBarAction].
 */
@Composable
fun FrnkTopAppBar(
    state: FrnkTopAppBarState,
    onNavigationClick: () -> Unit = {},
    onActionClick: (FrnkTopAppBarAction) -> Unit = {},
    onSearchQueryChange: (String) -> Unit = {},
    onSearchClose: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .background(Theme[colors][colorBackground])
                .then(
                    if (state.applyStatusBarPadding) {
                        Modifier.windowInsetsPadding(WindowInsets.statusBars)
                    } else {
                        Modifier
                    }
                )
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(FrnkTopAppBarDefaults.BarHeight)
                    .padding(horizontal = Theme[spacing][spacingXs]),
            horizontalArrangement = Arrangement.spacedBy(Theme[spacing][spacingXs]),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (state.isSearchActive) {
                SearchRow(
                    query = state.searchQuery,
                    placeholder = state.searchPlaceholder ?: Theme[strings][stringSearch],
                    onQueryChange = onSearchQueryChange,
                    onClose = onSearchClose
                )
            } else {
                state.navigationIcon?.let { icon ->
                    FrnkIconButton(
                        state =
                            FrnkIconButtonState.Content(
                                icon = icon,
                                contentDescription = state.navigationContentDescription ?: Theme[strings][stringBack],
                                tint = colorOnBackground
                            ),
                        onClick = onNavigationClick
                    )
                }

                FrnkText(
                    state =
                        FrnkTextState.Title(
                            content = state.title,
                            color = colorOnBackground,
                            singleLine = true
                        ),
                    // Material-style leading inset when there's no nav icon to provide it.
                    modifier =
                        Modifier
                            .weight(1f)
                            .padding(start = if (state.navigationIcon == null) Theme[spacing][spacingSm] else Theme[spacing][spacingXxs])
                )

                state.actions.forEach { action ->
                    FrnkIconButton(
                        state =
                            FrnkIconButtonState.Content(
                                icon = action.icon,
                                contentDescription = action.contentDescription.resolve(),
                                tint = colorOnBackground
                            ),
                        onClick = { onActionClick(action) }
                    )
                }
            }
        }
    }
}

/**
 * The bar's search layout: a leading "close search" button, an auto-focused single-line field, and a
 * trailing "clear" button shown only while [query] is non-empty. Laid out as siblings of the parent
 * [Row] so it inherits the bar's height and spacing.
 */
@Composable
private fun RowScope.SearchRow(
    query: String,
    placeholder: String,
    onQueryChange: (String) -> Unit,
    onClose: () -> Unit
) {
    FrnkIconButton(
        state =
            FrnkIconButtonState.Content(
                imageVector = Theme[icons][iconBack],
                contentDescription = Theme[strings][stringSearchClose],
                tint = colorOnBackground
            ),
        onClick = onClose
    )

    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    BasicTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier =
            Modifier
                .weight(1f)
                .focusRequester(focusRequester),
        textStyle = Theme[textStyles][bodyLarge].copy(color = Theme[colors][colorOnBackground]),
        singleLine = true,
        cursorBrush = SolidColor(Theme[colors][colorPrimary]),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        decorationBox = { innerTextField ->
            Box(contentAlignment = Alignment.CenterStart) {
                if (query.isEmpty()) {
                    FrnkText(
                        state =
                            FrnkTextState.Body(
                                text = placeholder,
                                color = colorOnSurfaceVariant,
                                singleLine = true
                            )
                    )
                }
                innerTextField()
            }
        }
    )

    if (query.isNotEmpty()) {
        FrnkIconButton(
            state =
                FrnkIconButtonState.Content(
                    imageVector = Theme[icons][iconClose],
                    contentDescription = Theme[strings][stringSearchClear],
                    tint = colorOnSurfaceVariant
                ),
            onClick = { onQueryChange("") }
        )
    }
}