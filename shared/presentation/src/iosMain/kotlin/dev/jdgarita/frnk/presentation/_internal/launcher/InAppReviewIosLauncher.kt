package dev.jdgarita.frnk.presentation._internal.launcher

import androidx.compose.runtime.Composable
import platform.StoreKit.SKStoreReviewController

/**
 * @author Vivien Mahe
 * @since 21/03/2025
 */
class InAppReviewIosLauncher : InAppReviewLauncher {

    @Composable
    override fun bindToView() {
        // Nothing to do here
    }

    override fun requestInAppReview() {
        SKStoreReviewController.requestReview()
    }
}
