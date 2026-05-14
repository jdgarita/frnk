package dev.jdgarita.frnk.backend.firebase

import dev.jdgarita.frnk.backend.CrashReporter

internal class FirebaseCrashReporter : CrashReporter {
    override fun recordException(throwable: Throwable, extras: Map<String, String>) {
        // TODO: Firebase.crashlytics.recordException(throwable); extras.forEach { setCustomKey(it.key, it.value) }
    }

    override fun setUserId(id: String?) {
        // TODO: Firebase.crashlytics.setUserId(id.orEmpty())
    }

    override fun log(message: String) {
        // TODO: Firebase.crashlytics.log(message)
    }
}
