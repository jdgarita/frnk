package dev.jdgarita.frnk.demo.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import com.composeunstyled.theme.Theme
import dev.jdgarita.frnk.ui.atoms.FrnkText
import dev.jdgarita.frnk.ui.atoms.FrnkTextState
import dev.jdgarita.frnk.ui.theme.bodyMedium
import dev.jdgarita.frnk.ui.theme.colorOnSurface
import dev.jdgarita.frnk.ui.theme.colorSurface
import dev.jdgarita.frnk.ui.theme.colors
import dev.jdgarita.frnk.ui.theme.shapeFull
import dev.jdgarita.frnk.ui.theme.shapes
import dev.jdgarita.frnk.ui.theme.spacing
import dev.jdgarita.frnk.ui.theme.spacingMd
import dev.jdgarita.frnk.ui.theme.spacingSm
import kotlinx.coroutines.delay

/**
 * One transient message emitted by [DemoHomeEffect.Toast]. The [id] is what makes the *same* text shown
 * twice in a row re-trigger the overlay — keying only on the text would leave the second emission
 * invisible, since the state wouldn't change.
 */
@Immutable
internal data class DemoMessage(
    val id: Long,
    val text: String
)

/**
 * The demo's transient-message surface: a pill that fades in over the content and auto-dismisses.
 *
 * A **shared** overlay rather than a platform toast on purpose — `FrnkDemoApp` is the one composable
 * both `demo-android` and `iosDemoApp` mount, so building this from toolkit atoms is what gets the same
 * feedback on both platforms without an `expect`/`actual` (and keeps DemoKit cinterop-free).
 *
 * Stateless: the caller owns the current [message] and clears it via [onDismissed]. The only local state
 * is [shownText], which retains the last text so it doesn't blank out mid-fade when [message] goes null.
 */
@Composable
internal fun DemoMessageOverlay(
    message: DemoMessage?,
    onDismissed: () -> Unit,
    modifier: Modifier = Modifier
) {
    var shownText by remember { mutableStateOf("") }

    LaunchedEffect(message) {
        if (message == null) return@LaunchedEffect
        shownText = message.text
        delay(MESSAGE_VISIBLE_MS)
        onDismissed()
    }

    AnimatedVisibility(
        visible = message != null,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = modifier
    ) {
        FrnkText(
            state =
                FrnkTextState.Raw(
                    text = shownText,
                    style = bodyMedium,
                    color = colorOnSurface
                ),
            modifier =
                Modifier
                    .clip(Theme[shapes][shapeFull])
                    .background(Theme[colors][colorSurface])
                    .padding(
                        horizontal = Theme[spacing][spacingMd],
                        vertical = Theme[spacing][spacingSm]
                    )
        )
    }
}

private const val MESSAGE_VISIBLE_MS = 2_500L