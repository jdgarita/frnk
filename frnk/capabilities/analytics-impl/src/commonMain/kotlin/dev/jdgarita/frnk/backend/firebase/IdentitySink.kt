package dev.jdgarita.frnk.backend.firebase

import dev.jdgarita.frnk.identity.IdentityError
import dev.jdgarita.frnk.utils.AppResult
import dev.jdgarita.frnk.utils.PrintLogger

/**
 * Shared `IdentitySource.identify` body for the Firebase observability bindings.
 *
 * Both sinks do the same thing — hand the uid to a synchronous, fire-and-forget SDK setter — so
 * they share one failure policy: log to the terminal and surface [IdentityError.Error]. In
 * practice the only reachable failure is an unconfigured Firebase (`Default FirebaseApp is not
 * initialized`), since neither setter touches the network.
 *
 * The result is reported honestly but is **not** meant to gate anything: `DefaultSyncAuthUseCase`
 * lets the entitlement backend decide whether an auth sync succeeded, so a missing
 * `google-services.json` degrades telemetry without ever blocking the app.
 */
internal fun identitySinkResult(
    tag: String,
    block: () -> Unit
): AppResult<Unit, IdentityError> =
    runCatching(block).fold(
        onSuccess = { AppResult.Success(Unit) },
        onFailure = {
            PrintLogger.w(tag, "identify skipped: ${it.message}")
            AppResult.Failure(IdentityError.Error)
        }
    )