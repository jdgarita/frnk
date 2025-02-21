package dev.jdgarita.frnk

import io.sentry.kotlin.multiplatform.Sentry
import io.sentry.kotlin.multiplatform.SentryOptions

class CrashReporter {

    // Application context is only needed for Android targets
    fun initializeSentry(key: String) {
        val configuration: (SentryOptions) -> Unit = {
            it.dsn = key
        }
        Sentry.init(configuration)
    }

    fun captureError() {
        try {
            throw Exception("This is a test.")
        } catch (e: Exception) {
            Sentry.captureException(e)
        }
    }
}