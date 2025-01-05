package com.renamecompanyname.renameappname.ui.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import com.google.android.play.core.review.ReviewException
import com.google.android.play.core.review.ReviewManagerFactory
import com.google.android.play.core.review.model.ReviewErrorCode

/**
 * Composable that handles requesting user reviews through the Google Play In-App Review API.
 *
 * This composable will trigger the review flow when [key] becomes true. The review dialog will be shown
 * to the user based on Google Play Store's quotas and restrictions. Note that there is no guarantee
 * the review dialog will be shown, and the API does not provide information about whether the user
 * actually submitted a review.
 *
 * @param key Boolean flag that triggers the review flow when true
 * @param onSuccess Callback function that will be invoked after the review flow completes, regardless
 *                 of whether the user submitted a review or not
 */
@Composable
fun AskForGooglePlayStoreUserReview(key: Boolean, onSuccess: () -> Unit) {
    val context = LocalContext.current
    LaunchedEffect(key) {
        if (key) {
            try {
                // Starting review request flow
                val manager = ReviewManagerFactory.create(context)
                val request = manager.requestReviewFlow()
                request.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // Review request successful, launching review flow
                        val reviewInfo = task.result
                        context.getActivity()?.let {
                            val flow = manager.launchReviewFlow(it, reviewInfo)
                            flow.addOnCompleteListener { _ ->
                                // The flow has finished. The API does not indicate whether the user
                                // reviewed or not, or even whether the review dialog was shown. Thus, no
                                // matter the result, we continue our app flow.
                                onSuccess()
                            }
                        }
                    } else {
                        @ReviewErrorCode val reviewErrorCode =
                            (task.exception as ReviewException).errorCode
                        //    "InAppReview", "Failed to request review: error code $reviewErrorCode",
                    }
                }
            } catch (e: Exception) {
                // "InAppReview", "Exception during review flow: ${e.message}"
            }
        } else {
            // Review flow skipped - key is false
        }
    }
}