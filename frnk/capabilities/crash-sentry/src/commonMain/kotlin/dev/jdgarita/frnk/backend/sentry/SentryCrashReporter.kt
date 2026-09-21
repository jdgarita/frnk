package dev.jdgarita.frnk.backend.sentry

import dev.jdgarita.frnk.backend.CrashReporter
import dev.jdgarita.frnk.identity.IdentityError
import dev.jdgarita.frnk.utils.AppResult
import dev.jdgarita.frnk.utils.PrintLogger
import io.sentry.kotlin.multiplatform.Sentry
import io.sentry.kotlin.multiplatform.protocol.Breadcrumb
import io.sentry.kotlin.multiplatform.protocol.User

/** The four SDK calls the reporter makes, behind a seam so the mapping is testable without `Sentry.init`. */
internal interface SentryGateway {
    fun captureException(
        throwable: Throwable,
        extras: Map<String, String>
    )

    fun addBreadcrumb(message: String)

    fun setUser(id: String)
}

internal object SentrySdkGateway : SentryGateway {
    override fun captureException(
        throwable: Throwable,
        extras: Map<String, String>
    ) {
        // Scoped to this one event: nothing leaks into the next report, unlike Crashlytics' global keys.
        Sentry.captureException(throwable) { scope ->
            extras.forEach { (key, value) -> scope.setExtra(key, value) }
        }
    }

    override fun addBreadcrumb(message: String) {
        Sentry.addBreadcrumb(Breadcrumb.info(message))
    }

    override fun setUser(id: String) {
        Sentry.setUser(User(id = id))
    }
}

/**
 * [CrashReporter] over the Sentry KMP SDK.
 *
 * `recordException` is a Sentry event with [extras] set on a per-event scope; `log` is an info
 * breadcrumb, attached to whatever event comes next; `identify` sets the Sentry user id. Every SDK
 * call is wrapped in [runCatching] so an SDK that failed to start degrades to a logged no-op.
 *
 * On Apple targets `Sentry.init` installs the SDK's own Kotlin unhandled-exception hook, and
 * `captureException` carries the throwable's real Kotlin frames, so nothing here replaces CrashKiOS —
 * there is no native hook to install.
 */
internal class SentryCrashReporter(
    private val gateway: SentryGateway = SentrySdkGateway
) : CrashReporter {
    override fun recordException(
        throwable: Throwable,
        extras: Map<String, String>
    ) {
        runCatching { gateway.captureException(throwable, extras) }
            .onFailure { PrintLogger.w(TAG, "recordException skipped: ${it.message}") }
    }

    override fun log(message: String) {
        runCatching { gateway.addBreadcrumb(message) }
            .onFailure { PrintLogger.w(TAG, "log skipped: ${it.message}") }
    }

    override suspend fun identify(id: String): AppResult<Unit, IdentityError> =
        runCatching { gateway.setUser(id) }
            .fold(
                onSuccess = { AppResult.Success(Unit) },
                onFailure = {
                    PrintLogger.w(TAG, "identify skipped: ${it.message}")
                    AppResult.Failure(IdentityError.Error)
                }
            )

    private companion object {
        const val TAG = "SentryCrash"
    }
}