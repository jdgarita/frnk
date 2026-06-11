/*
 * Copyright (c) 2025 RevenueCat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

// Vendored from RevenueCat/placeholder-compose (Apache-2.0). Slimmed to the single
// Modifier.placeholder entry point; the Material-coupled placeholderText variant and the
// LocalPlaceholderTheme defaults were dropped — callers pass color/shape/highlight explicitly
// (frnk resolves them from theme tokens via Modifier.frnkSkeleton). See NOTICE.
package dev.jdgarita.frnk.ui.placeholder

import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.spring
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalInspectionMode

/**
 * Draws a placeholder effect over content while it is loading.
 *
 * This modifier displays an animated placeholder overlay that covers the content
 * when [enabled] is true, and smoothly transitions to reveal the actual content
 * when [enabled] becomes false.
 *
 * @param enabled Whether the placeholder should be visible. When false, the actual content is shown.
 * @param color The background color of the placeholder.
 * @param shape The shape of the placeholder. Defaults to [RectangleShape].
 * @param highlight The highlight animation effect to apply. Defaults to [PlaceholderDefaults.fade].
 *   Set to null to disable highlight animations.
 * @param placeholderFadeTransitionSpec Animation spec for fading the placeholder in/out.
 * @param contentFadeTransitionSpec Animation spec for fading the content in/out.
 * @return A [Modifier] that draws the placeholder effect
 */
@Composable
internal fun Modifier.placeholder(
    enabled: Boolean = true,
    color: Color,
    shape: Shape = RectangleShape,
    highlight: PlaceholderHighlight? = PlaceholderDefaults.fade,
    placeholderFadeTransitionSpec: () -> FiniteAnimationSpec<Float> = { spring() },
    contentFadeTransitionSpec: () -> FiniteAnimationSpec<Float> = { spring() },
): Modifier {
    val coordinator = LocalPlaceholderCoordinator.current
    val placeholder =
        rememberPlaceholder(
            visible = enabled,
            color = color,
            shape = shape,
            highlight = highlight,
            coordinator = coordinator,
            placeholderFadeTransitionSpec = placeholderFadeTransitionSpec,
            contentFadeTransitionSpec = contentFadeTransitionSpec,
        )

    return this then PlaceholderElement(placeholder = placeholder)
}

/**
 * Internal placeholder systems to remember placeholder and running & stopping the placeholder.
 *
 * @param visible whether the placeholder should be visible or not.
 * @param color the color used to draw the placeholder UI.
 * @param shape desired shape of the placeholder. Defaults to [RectangleShape].
 * @param highlight optional highlight animation.
 * @param placeholderFadeTransitionSpec The transition spec to use when fading the placeholder
 * on/off screen. The boolean parameter defined for the transition is [visible].
 * @param contentFadeTransitionSpec The transition spec to use when fading the content
 * on/off screen. The boolean parameter defined for the transition is [visible].
 */
@Composable
internal fun rememberPlaceholder(
    visible: Boolean,
    color: Color,
    shape: Shape = RectangleShape,
    highlight: PlaceholderHighlight? = null,
    coordinator: PlaceholderCoordinator? = null,
    placeholderFadeTransitionSpec: () -> FiniteAnimationSpec<Float> = { spring() },
    contentFadeTransitionSpec: () -> FiniteAnimationSpec<Float> = { spring() },
): Placeholder {
    val placeholder: Placeholder =
        remember(
            keys =
                arrayOf(
                    visible,
                    color,
                    shape,
                    highlight,
                    coordinator,
                    placeholderFadeTransitionSpec,
                    contentFadeTransitionSpec,
                ),
        ) {
            Placeholder(
                visible = visible,
                color = color,
                shape = shape,
                highlight = highlight,
                coordinator = coordinator,
                placeholderFadeTransitionSpec = placeholderFadeTransitionSpec,
                contentFadeTransitionSpec = contentFadeTransitionSpec,
            )
        }

    val inPreviewMode = LocalInspectionMode.current
    LaunchedEffect(key1 = placeholder) {
        if (visible && !inPreviewMode) {
            placeholder.startAnimation()
        } else {
            placeholder.stopAnimation()
        }
    }

    return placeholder
}
