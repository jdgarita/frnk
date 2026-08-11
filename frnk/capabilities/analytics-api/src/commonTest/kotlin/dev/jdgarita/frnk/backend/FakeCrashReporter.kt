package dev.jdgarita.frnk.backend

import dev.jdgarita.frnk.identity.IdentityError
import dev.jdgarita.frnk.utils.AppResult

/**
 * Recording test double for [CrashReporter] — captures recorded exceptions (with their extras),
 * breadcrumb logs, and the last user id so tests can assert crash-reporting wiring without a real
 * SDK. Companion to [FakeAnalyticsTracker]; lives in `commonTest`, never ships.
 */
class FakeCrashReporter : CrashReporter {
    data class Recorded(
        val throwable: Throwable,
        val extras: Map<String, String>
    )

    val recorded = mutableListOf<Recorded>()
    val logs = mutableListOf<String>()
    var userId: String? = null
        private set

    override fun recordException(
        throwable: Throwable,
        extras: Map<String, String>
    ) {
        recorded += Recorded(throwable, extras)
    }

    override suspend fun identify(id: String): AppResult<Unit, IdentityError> {
        userId = id
        return AppResult.Success(Unit)
    }

    override fun log(message: String) {
        logs += message
    }
}