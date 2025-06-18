package com.tweener.kmpship.presentation._internal.launcher

import android.app.Activity
import android.content.Context
import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import com.google.android.play.core.review.ReviewException
import com.google.android.play.core.review.ReviewManagerFactory
import com.google.android.play.core.review.model.ReviewErrorCode
import io.github.aakira.napier.Napier

/**
 * @author Vivien Mahe
 * @since 21/03/2025
 */

class InAppReviewAndroidLauncher(private val context: Context) : InAppReviewLauncher {

    private var activity: Activity? = null

    @Composable
    override fun bindToView() {
        activity = LocalActivity.current
    }

    override fun requestInAppReview() {
        activity?.let { activity ->
            val reviewManager = ReviewManagerFactory.create(context)

            reviewManager
                .requestReviewFlow()
                .addOnCompleteListener { requestInfo ->
                    Napier.d { "Request review flow completed" }

                    if (requestInfo.isSuccessful) {
                        val reviewInfo = requestInfo.result

                        reviewManager
                            .launchReviewFlow(activity, reviewInfo)
                            .addOnCompleteListener { _ ->
                                Napier.d { "Review flow completed" }
                            }
                    } else {
                        @ReviewErrorCode val reviewErrorCode = (requestInfo.exception as ReviewException).errorCode
                        Napier.d { "Request review flow failed with error code: $reviewErrorCode" }
                    }
                }
        }
    }
}
