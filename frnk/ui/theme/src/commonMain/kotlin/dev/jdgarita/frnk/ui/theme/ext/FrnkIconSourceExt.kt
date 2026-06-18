package dev.jdgarita.frnk.ui.theme.ext

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import com.composeunstyled.theme.Theme
import dev.jdgarita.frnk.ui.theme.FrnkIconSource
import dev.jdgarita.frnk.ui.theme.icons

/**
 * Resolves this [FrnkIconSource] to a concrete `ImageVector` against the ambient [FrnkTheme]'s icon
 * registry. Call from the leaf atom (`FrnkIcon`) so view states stay token-only; must run inside a
 * `FrnkTheme { … }` (it reads `Theme[icons]`).
 */
@Composable
fun FrnkIconSource.resolve(): ImageVector =
    when (this) {
        is FrnkIconSource.Token -> Theme[icons][token]
        is FrnkIconSource.Vector -> imageVector
    }