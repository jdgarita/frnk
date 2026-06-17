package dev.jdgarita.frnk.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.vector.ImageVector
import com.composeunstyled.theme.ThemeToken

/**
 * A **Compose-free reference** to an icon, resolved to an `ImageVector` only at render time (see
 * `FrnkIconSource.resolve()` in `ext/FrnkIconSourceExt.kt`).
 *
 * Counterpart to [FrnkStringSource] for glyphs: view states hold a [Token] (a theme icon key the host
 * can re-skin via `FrnkThemeConfig.iconOverrides`) or a [Vector] (a host-supplied arbitrary
 * `ImageVector`, e.g. the feature-tab icon), and the leaf [dev.jdgarita.frnk.ui.atoms.FrnkIcon]
 * resolves it via `Theme[icons][token]`. Lets ViewModels author icon state without composition.
 */
@Immutable
sealed interface FrnkIconSource {
    /** A theme icon token (e.g. `iconUpgrade`), resolved via `Theme[icons][token]`. */
    @Immutable
    data class Token(
        val token: ThemeToken<ImageVector>,
    ) : FrnkIconSource

    /** An arbitrary, host-supplied vector the theme registry doesn't own. */
    @Immutable
    data class Vector(
        val imageVector: ImageVector,
    ) : FrnkIconSource
}
