package dev.jdgarita.frnk.ui.theme.ext

import androidx.compose.runtime.Composable
import com.composeunstyled.theme.Theme
import dev.jdgarita.frnk.ui.theme.FrnkStringSource
import dev.jdgarita.frnk.ui.theme.strings

/**
 * Resolves this [FrnkStringSource] to a concrete `String` against the ambient [FrnkTheme]'s string
 * registry. Call from the leaf atom (`FrnkText`) so view states stay token-only; must run inside a
 * `FrnkTheme { … }` (it reads `Theme[strings]`).
 */
@Composable
fun FrnkStringSource.resolve(): String =
    when (this) {
        is FrnkStringSource.Token -> Theme[strings][token]
        is FrnkStringSource.Raw -> value
        is FrnkStringSource.Composite -> {
            // A plain for-loop, not joinToString { }: the transform lambda isn't @Composable, but a
            // loop body in a @Composable function may call composables (resolve() reads Theme).
            val resolved = ArrayList<String>(parts.size)
            for (part in parts) resolved.add(part.resolve())
            resolved.joinToString(separator)
        }
    }
