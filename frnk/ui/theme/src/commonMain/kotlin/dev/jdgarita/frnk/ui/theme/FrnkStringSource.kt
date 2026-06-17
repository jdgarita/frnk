package dev.jdgarita.frnk.ui.theme

import androidx.compose.runtime.Immutable
import com.composeunstyled.theme.ThemeToken

/**
 * A **Compose-free reference** to a piece of display text, resolved to a `String` only at render time
 * (see `FrnkStringSource.resolve()` in `ext/FrnkStringSourceExt.kt`).
 *
 * The point is that ViewModels and view states can author/update text **without composition**: they
 * hold a [Token] (a theme key), a [Raw] literal, or a [Composite] of the two, and the UI layer maps it
 * to the actual string via `Theme[strings][token]` at the leaf atom. This keeps state locale- and
 * override-independent (a host's `stringOverrides` / a locale change re-resolves automatically) and
 * keeps the hoisted-state rule intact — no `@Composable remember*State` builder needed to bake copy in.
 *
 * Mirrors the long-standing token-in-state precedent already used for `FrnkTextState.color/style`
 * (`ThemeToken<Color>/<TextStyle>`).
 */
@Immutable
sealed interface FrnkStringSource {
    /** A theme string token (e.g. `stringProBadge`), resolved via `Theme[strings][token]`. */
    @Immutable
    data class Token(
        val token: ThemeToken<String>,
    ) : FrnkStringSource

    /** A literal, dynamic string the theme can't supply (a version name, a user's input, a count). */
    @Immutable
    data class Raw(
        val value: String,
    ) : FrnkStringSource

    /**
     * An ordered composition of [parts] joined by [separator] — for interpolated copy assembled from
     * several tokens/literals (e.g. a paywall title "<prefix> <appName> <proName>").
     */
    @Immutable
    data class Composite(
        val parts: List<FrnkStringSource>,
        val separator: String = " ",
    ) : FrnkStringSource
}
