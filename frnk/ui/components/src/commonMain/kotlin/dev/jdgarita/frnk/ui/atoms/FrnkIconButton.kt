package dev.jdgarita.frnk.ui.atoms

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.composeunstyled.UnstyledButton
import com.composeunstyled.theme.Theme
import com.composeunstyled.theme.ThemeToken
import dev.jdgarita.frnk.ui.haptics.HapticType
import dev.jdgarita.frnk.ui.haptics.LocalFrnkHaptics
import dev.jdgarita.frnk.ui.theme.FrnkIconSource
import dev.jdgarita.frnk.ui.theme.shapeFull
import dev.jdgarita.frnk.ui.theme.shapes
import dev.jdgarita.frnk.ui.tokens.FrnkSpacing

/**
 * Sealed visual state for [FrnkIconButton]. [Content] is the interactive icon button; [Skeleton]
 * (an `object`) is the loading placeholder. [Content.contentDescription] is non-nullable so every
 * call site supplies a label readable by TalkBack/VoiceOver — an icon button without one is invisible
 * to assistive tech. Use the plain [FrnkIcon] (which accepts `null`) for decorative cases.
 * [Content.size] `null` (the default) uses the theme icon-size axis.
 */
sealed interface FrnkIconButtonState {
    /**
     * The glyph is supplied as a [FrnkIconSource] — either a theme [FrnkIconSource.Token] (so host
     * `iconOverrides` apply and a VM can author icon state without composition) or a host-supplied
     * [FrnkIconSource.Vector]. A `String`-free secondary constructor accepts a raw [ImageVector]
     * directly (wrapping it in [FrnkIconSource.Vector]), so existing call sites stay unchanged.
     */
    @Immutable
    data class Content(
        val icon: FrnkIconSource,
        val contentDescription: String,
        val size: Dp? = null,
        val tint: ThemeToken<Color>? = null,
        val contentPadding: PaddingValues = PaddingValues(FrnkSpacing.sm),
        val enabled: Boolean = true
    ) : FrnkIconButtonState {
        constructor(
            imageVector: ImageVector,
            contentDescription: String,
            size: Dp? = null,
            tint: ThemeToken<Color>? = null,
            contentPadding: PaddingValues = PaddingValues(FrnkSpacing.sm),
            enabled: Boolean = true
        ) : this(
            FrnkIconSource.Vector(imageVector),
            contentDescription,
            size,
            tint,
            contentPadding,
            enabled
        )
    }

    data object Skeleton : FrnkIconButtonState
}

@Composable
fun FrnkIconButton(
    state: FrnkIconButtonState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val content =
        when (state) {
            is FrnkIconButtonState.Content -> state
            FrnkIconButtonState.Skeleton -> {
                FrnkSkeletonBox(
                    modifier.defaultMinSize(minWidth = 48.dp, minHeight = 48.dp),
                    shape = shapeFull
                )
                return
            }
        }

    val haptics = LocalFrnkHaptics.current
    UnstyledButton(
        onClick = {
            haptics.perform(HapticType.Click)
            onClick()
        },
        enabled = content.enabled,
        modifier =
            modifier
                .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                .clip(Theme[shapes][shapeFull]),
        contentPadding = content.contentPadding
    ) {
        FrnkIcon(
            state =
                FrnkIconState.Content(
                    icon = content.icon,
                    contentDescription = content.contentDescription,
                    size = content.size,
                    tint = content.tint,
                    // Match FrnkButton's 0.4f disabled treatment so the icon visibly dims when
                    // the button is inactive.
                    tintAlpha = if (content.enabled) 1f else 0.4f
                )
        )
    }
}