package dev.jdgarita.frnk.ui.app

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith

private const val ANIMATION_DURATION_IN_MILLIS = 250

/**
 * Forward navigation transition: new screen slides in from the right
 * while the current screen slides out to the left.
 */
internal fun defaultEnterTransition(): ContentTransform =
    slideInHorizontally(
        initialOffsetX = { it },
        animationSpec = tween(ANIMATION_DURATION_IN_MILLIS)
    ) togetherWith
        slideOutHorizontally(
            targetOffsetX = { -it },
            animationSpec = tween(ANIMATION_DURATION_IN_MILLIS)
        )

/**
 * Back navigation transition: previous screen slides in from the left
 * while the current screen slides out to the right.
 */
internal fun defaultExitTransition(): ContentTransform =
    slideInHorizontally(
        initialOffsetX = { -it },
        animationSpec = tween(ANIMATION_DURATION_IN_MILLIS)
    ) togetherWith
        slideOutHorizontally(
            targetOffsetX = { it },
            animationSpec = tween(ANIMATION_DURATION_IN_MILLIS)
        )