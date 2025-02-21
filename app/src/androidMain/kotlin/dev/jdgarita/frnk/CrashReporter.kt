package dev.jdgarita.frnk

import io.sentry.kotlin.multiplatform.Sentry
import io.sentry.kotlin.multiplatform.SentryOptions

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual class CrashReporter actual constructor() {

    // Application context is only needed for Android targets
    actual fun init(key: String) {
        val configuration: (SentryOptions) -> Unit = {
            it.dsn = key
        }
        Sentry.init(configuration)
    }

    actual fun captureError() {
        try {
            throw Exception("This is a test.")
        } catch (e: Exception) {
            Sentry.captureException(e)
        }
    }
}