package dev.jdgarita.frnk.monetization.usecase

import dev.jdgarita.frnk.backend.AnalyticsTracker
import dev.jdgarita.frnk.backend.CrashReporter
import dev.jdgarita.frnk.backend.ToolkitEvent
import dev.jdgarita.frnk.identity.AnonymousIdentityProvider
import dev.jdgarita.frnk.monetization.EntitlementManager
import dev.jdgarita.frnk.utils.AppResult
import dev.jdgarita.frnk.utils.CommonError
import dev.jdgarita.frnk.utils.fold

class DefaultSyncAuthUseCase(
    private val identityProvider: AnonymousIdentityProvider,
    private val entitlementManager: EntitlementManager,
    private val crashReporter: CrashReporter,
    private val analyticsTracker: AnalyticsTracker
) : SyncAuthUseCase {
    companion object {
        private const val STAGE_PARAM = "stage"
        private const val ERROR_PARAM = "error_type"
        private const val STAGE_SIGN_IN = "sign_in"
        private const val STAGE_ENTITLEMENT = "entitlement"
    }

    override suspend fun identify(): AppResult<Unit, CommonError> =
        identityProvider.ensureSignedIn().fold(
            onSuccess = { id -> syncIdentity(id) },
            onFailure = { error -> syncFailed(STAGE_SIGN_IN, error.name, error) }
        )

    private suspend fun syncIdentity(id: String): AppResult<Unit, CommonError> {
        // Best-effort sinks. Each logs its own failure internally and neither may gate the app, so
        // an unconfigured Firebase degrades telemetry rather than blocking a scan. Their results
        // are deliberately discarded — only the billing backend decides whether the sync succeeded,
        // because it is the one an outbound request actually depends on.
        crashReporter.identify(id)
        analyticsTracker.identify(id)

        return entitlementManager.identify(id).fold(
            onSuccess = {
                // Emitted here, not inside the analytics binding: the event describes the whole
                // sync, so it has to follow the step that determines success. Firebase attaches the
                // user id set above to it automatically.
                analyticsTracker.track(ToolkitEvent.IdentitySynced)
                AppResult.Success(Unit)
            },
            // IdentityError carries no cause worth forwarding, so the public contract reports the
            // honest thing: the sync failed for a reason this layer cannot name.
            onFailure = { error -> syncFailed(STAGE_ENTITLEMENT, error.name, CommonError.Unknown) }
        )
    }

    /**
     * Records a failed sync identically wherever it happened: a Crashlytics breadcrumb — not a
     * non-fatal, since no throwable survives either path and both causes (offline device,
     * unreachable billing backend) are expected rather than exceptional — plus an
     * [ToolkitEvent.IdentitySyncFailed] event.
     *
     * [stage] separates a Firebase-auth failure from a billing-backend one, which have completely
     * different remediations; both params stay low-cardinality, so they segment cleanly and pair
     * with [ToolkitEvent.IdentitySynced] to give a failure rate rather than a bare count.
     */
    private fun syncFailed(
        stage: String,
        errorName: String,
        error: CommonError
    ): AppResult<Unit, CommonError> {
        crashReporter.log("Identity sync failed at $stage: $errorName")
        analyticsTracker.track(
            event = ToolkitEvent.IdentitySyncFailed,
            params = mapOf(STAGE_PARAM to stage, ERROR_PARAM to errorName)
        )
        return AppResult.Failure(error)
    }
}