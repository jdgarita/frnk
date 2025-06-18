package dev.jdgarita.frnk.presentation._internal.launcher

import androidx.compose.runtime.Composable

/**
 * @author Vivien Mahe
 * @since 21/03/2025
 */

interface InAppReviewLauncher {

    @Composable
    fun bindToView()

    fun requestInAppReview()
}
