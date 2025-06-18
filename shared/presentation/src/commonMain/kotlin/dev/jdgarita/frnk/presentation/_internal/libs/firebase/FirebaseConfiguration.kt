package dev.jdgarita.frnk.presentation._internal.libs.firebase

import com.tweener.firebase.analytics.FirebaseAnalyticsService
import com.tweener.firebase.crashlytics.FirebaseCrashlyticsService

/**
 * @author Vivien Mahe
 * @since 22/05/2024
 */
class FirebaseConfiguration(
    private val isDebug: Boolean,
    private val firebaseCrashlyticsService: FirebaseCrashlyticsService,
    private val firebaseAnalyticsService: FirebaseAnalyticsService,
) {

    fun init() {
        initializeFirebase()

        firebaseCrashlyticsService.getCrashlytics().setCrashlyticsCollectionEnabled(enabled = isDebug.not())
        firebaseAnalyticsService.getAnalytics().setAnalyticsCollectionEnabled(enabled = isDebug.not())
    }
}

expect fun initializeFirebase()
