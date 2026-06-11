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

// Vendored from RevenueCat/placeholder-compose (Apache-2.0). Trimmed to the shimmer + fade
// highlights only; the pulse / lightReveal / circularReveal defaults were dropped. See NOTICE.
package dev.jdgarita.frnk.ui.placeholder

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.ui.graphics.Color

/**
 * Contains the default [PlaceholderHighlight] implementations for common placeholder effects.
 *
 * These defaults provide pre-configured highlight animations that can be used directly
 * with the [placeholder] modifier.
 *
 * @see shimmer for a sweeping gradient effect
 * @see fade for a simple opacity fade in/out
 */
internal object PlaceholderDefaults {
    /**
     * A shimmer effect that sweeps a gradient highlight across the placeholder.
     *
     * Default configuration:
     * - Highlight color: White with 50% opacity
     * - Animation duration: 1700ms with 200ms delay
     * - Repeat mode: Restart
     */
    val shimmer: Shimmer =
        Shimmer(
            highlightColor = Color.White.copy(alpha = 0.5f),
            animationSpec =
                infiniteRepeatable(
                    animation = tween(durationMillis = 1700, delayMillis = 200),
                    repeatMode = RepeatMode.Restart,
                ),
        )

    /**
     * A fade effect that smoothly transitions the highlight opacity in and out.
     *
     * Default configuration:
     * - Highlight color: Gray with 60% opacity
     * - Animation duration: 600ms with 200ms delay
     * - Repeat mode: Reverse (ping-pong effect)
     */
    val fade: Fade =
        Fade(
            highlightColor = Color.Gray.copy(alpha = 0.6f),
            animationSpec =
                infiniteRepeatable(
                    animation = tween(delayMillis = 200, durationMillis = 600),
                    repeatMode = RepeatMode.Reverse,
                ),
        )
}
