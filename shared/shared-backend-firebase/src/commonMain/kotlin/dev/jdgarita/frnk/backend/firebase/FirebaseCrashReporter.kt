package dev.jdgarita.frnk.backend.firebase

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.crashlytics.crashlytics
import dev.jdgarita.frnk.backend.CrashReporter
import dev.jdgarita.frnk.utils.PrintLogger

/**
 * Firebase Crashlytics binding (gitlive `firebase-crashlytics`).
 *
 * As with [FirebaseAnalyticsTracker], every SDK call is wrapped in [runCatching] so an
 * unconfigured Firebase degrades to a logged no-op rather than crashing (BACKLOG P1-5).
 */
internal class FirebaseCrashReporter : CrashReporter {
    override fun recordException(
        throwable: Throwable,
        extras: Map<String, String>,
    ) {
        runCatching {
            extras.forEach { (key, value) -> Firebase.crashlytics.setCustomKey(key, value) }
            Firebase.crashlytics.recordException(throwable)
        }.onFailure { PrintLogger.w(TAG, "recordException skipped: ${it.message}") }
    }

    override fun setUserId(id: String?) {
        runCatching { Firebase.crashlytics.setUserId(id.orEmpty()) }
            .onFailure { PrintLogger.w(TAG, "setUserId skipped: ${it.message}") }
    }

    override fun log(message: String) {
        runCatching { Firebase.crashlytics.log(message) }
            .onFailure { PrintLogger.w(TAG, "log skipped: ${it.message}") }
    }

    private companion object {
        const val TAG = "FirebaseCrash"
    }
}
