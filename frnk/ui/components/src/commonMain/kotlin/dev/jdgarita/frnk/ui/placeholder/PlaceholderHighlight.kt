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

// Vendored from RevenueCat/placeholder-compose (Apache-2.0). The androidx.annotation.FloatRange
// annotations were dropped to keep this commonMain-safe. See NOTICE.
package dev.jdgarita.frnk.ui.placeholder

import androidx.compose.animation.core.InfiniteRepeatableSpec
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush

/**
 * Defines the visual highlight animation applied to a placeholder.
 *
 * A placeholder highlight provides animated visual effects that appear on top of
 * placeholder content to indicate loading state. The vendored implementations are
 * [Shimmer] and [Fade].
 *
 * Implementations should define:
 * - How the highlight is drawn via [brush]
 * - The opacity of the highlight via [alpha]
 * - The animation timing via [animationSpec]
 *
 * @see PlaceholderDefaults for default highlight implementations
 */
internal interface PlaceholderHighlight {
    /**
     * The animation specification for the highlight effect.
     *
     * Defines the timing, easing, and repeat behavior of the highlight animation.
     * If null, the highlight will be static (no animation).
     */
    val animationSpec: InfiniteRepeatableSpec<Float>?

    /**
     * Returns the brush to draw the highlight at the current animation progress.
     *
     * @param progress The current animation progress in the range 0.0 to 1.0
     * @param size The size of the placeholder being drawn
     * @return A [Brush] defining the highlight appearance
     */
    fun brush(
        progress: Float,
        size: Size
    ): Brush

    /**
     * Returns the alpha (opacity) of the highlight at the current animation progress.
     *
     * This allows the highlight to fade in/out during the animation cycle.
     *
     * @param progress The current animation progress in the range 0.0 to 1.0
     * @return The alpha value in the range 0.0 (transparent) to 1.0 (opaque)
     */
    fun alpha(progress: Float): Float
}