package dev.jdgarita.frnk.monetization.usecase

import dev.jdgarita.frnk.backend.AnalyticsTracker
import dev.jdgarita.frnk.backend.CrashReporter
import dev.jdgarita.frnk.backend.ToolkitEvent
import dev.jdgarita.frnk.identity.AnonymousIdentityProvider
import dev.jdgarita.frnk.identity.IdentityError
import dev.jdgarita.frnk.monetization.EntitlementManager
import dev.jdgarita.frnk.monetization.EntitlementStatus
import dev.jdgarita.frnk.monetization.MonetizationError
import dev.jdgarita.frnk.monetization.ProMetadata
import dev.jdgarita.frnk.monetization.ProProduct
import dev.jdgarita.frnk.utils.AppResult
import dev.jdgarita.frnk.utils.CommonError
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultSyncAuthUseCaseTest {
    @Test
    fun successful_sign_in_identifies_the_uid_with_the_entitlement_manager() =
        runTest {
            val identity = ResultIdentityProvider(AppResult.Success("uid-42"))
            val entitlements = RecordingEntitlementManager()

            val result = useCase(identity, entitlements).identify()

            assertEquals(AppResult.Success(Unit), result)
            assertEquals(listOf("uid-42"), entitlements.identifiedUserIds)
        }

    @Test
    fun sign_in_failure_propagates_without_touching_the_entitlement_manager() =
        runTest {
            val identity = ResultIdentityProvider(AppResult.Failure(CommonError.Network))
            val entitlements = RecordingEntitlementManager()

            val result = useCase(identity, entitlements).identify()

            assertEquals(AppResult.Failure(CommonError.Network), result)
            assertEquals(emptyList(), entitlements.identifiedUserIds)
        }

    @Test
    fun entitlement_identify_failure_propagates() =
        runTest {
            val identity = ResultIdentityProvider(AppResult.Success("uid-42"))
            val entitlements =
                RecordingEntitlementManager(identifyResult = AppResult.Failure(IdentityError.Error))

            val result = useCase(identity, entitlements).identify()

            assertEquals(AppResult.Failure(CommonError.Unknown), result)
            assertEquals(listOf("uid-42"), entitlements.identifiedUserIds)
        }

    @Test
    fun successful_sign_in_attributes_the_uid_and_tracks_the_sync() =
        runTest {
            val crash = RecordingCrashReporter()
            val analytics = RecordingAnalyticsTracker()

            useCase(
                ResultIdentityProvider(AppResult.Success("uid-42")),
                RecordingEntitlementManager(),
                crash,
                analytics
            ).identify()

            assertEquals("uid-42", crash.userIds.single())
            assertEquals("uid-42", analytics.userIds.single())
            // The uid rides the reserved User-ID field, never the event params.
            assertEquals(TrackedEvent("identity_synced", emptyMap()), analytics.tracked.single())
        }

    @Test
    fun sign_in_failure_logs_a_breadcrumb_and_tracks_the_error_variant() =
        runTest {
            val crash = RecordingCrashReporter()
            val analytics = RecordingAnalyticsTracker()

            useCase(
                ResultIdentityProvider(AppResult.Failure(CommonError.Network)),
                RecordingEntitlementManager(),
                crash,
                analytics
            ).identify()

            assertEquals("Identity sync failed at sign_in: Network", crash.logs.single())
            assertEquals(
                TrackedEvent(
                    "identity_sync_failed",
                    mapOf("stage" to "sign_in", "error_type" to "Network")
                ),
                analytics.tracked.single()
            )
            // A breadcrumb, not a non-fatal — there is no surviving throwable worth recording.
            assertEquals(0, crash.recordedExceptions)
            // A transient failure must not clear a uid a previous successful sync established.
            assertTrue(crash.userIds.isEmpty())
        }

    @Test
    fun entitlement_failure_reports_the_entitlement_stage_and_skips_the_success_event() =
        runTest {
            val crash = RecordingCrashReporter()
            val analytics = RecordingAnalyticsTracker()

            useCase(
                ResultIdentityProvider(AppResult.Success("uid-42")),
                RecordingEntitlementManager(identifyResult = AppResult.Failure(IdentityError.Error)),
                crash,
                analytics
            ).identify()

            assertEquals("Identity sync failed at entitlement: Error", crash.logs.single())
            // Exactly one funnel event per call: the success event must not fire for a sync that
            // reached the billing backend and failed there.
            assertEquals(
                TrackedEvent(
                    "identity_sync_failed",
                    mapOf("stage" to "entitlement", "error_type" to "Error")
                ),
                analytics.tracked.single()
            )
            // The observability sinks still ran — they are attributed before the gating step.
            assertEquals("uid-42", crash.userIds.single())
            assertEquals("uid-42", analytics.userIds.single())
        }

    @Test
    fun a_failing_observability_sink_never_fails_the_sync() =
        runTest {
            val analytics = RecordingAnalyticsTracker(identifyResult = AppResult.Failure(IdentityError.Error))

            val result =
                useCase(
                    ResultIdentityProvider(AppResult.Success("uid-42")),
                    RecordingEntitlementManager(),
                    RecordingCrashReporter(identifyResult = AppResult.Failure(IdentityError.Error)),
                    analytics
                ).identify()

            // Telemetry is best-effort: an unconfigured Firebase must not block a scan.
            assertEquals(AppResult.Success(Unit), result)
            assertEquals(TrackedEvent("identity_synced", emptyMap()), analytics.tracked.single())
        }

    private fun useCase(
        identity: AnonymousIdentityProvider,
        entitlements: EntitlementManager,
        crash: CrashReporter = RecordingCrashReporter(),
        analytics: AnalyticsTracker = RecordingAnalyticsTracker()
    ) = DefaultSyncAuthUseCase(identity, entitlements, crash, analytics)
}

private class RecordingCrashReporter(
    private val identifyResult: AppResult<Unit, IdentityError> = AppResult.Success(Unit)
) : CrashReporter {
    val userIds = mutableListOf<String?>()
    val logs = mutableListOf<String>()
    var recordedExceptions: Int = 0
        private set

    override fun recordException(
        throwable: Throwable,
        extras: Map<String, String>
    ) {
        recordedExceptions += 1
    }

    override suspend fun identify(id: String): AppResult<Unit, IdentityError> {
        userIds += id
        return identifyResult
    }

    override fun log(message: String) {
        logs += message
    }
}

private data class TrackedEvent(
    val key: String,
    val params: Map<String, Any?>
)

private class RecordingAnalyticsTracker(
    private val identifyResult: AppResult<Unit, IdentityError> = AppResult.Success(Unit)
) : AnalyticsTracker {
    val userIds = mutableListOf<String?>()
    val tracked = mutableListOf<TrackedEvent>()
    val userProperties = mutableMapOf<String, String?>()

    override fun track(
        event: ToolkitEvent,
        params: Map<String, Any?>
    ) {
        tracked += TrackedEvent(event.key, params)
    }

    override fun trackCustom(
        name: String,
        params: Map<String, Any?>
    ) {
        tracked += TrackedEvent(name, params)
    }

    override fun setUserProperty(
        key: String,
        value: String?
    ) {
        userProperties[key] = value
    }

    override suspend fun identify(id: String): AppResult<Unit, IdentityError> {
        userIds += id
        return identifyResult
    }
}

private class ResultIdentityProvider(
    private val signInResult: AppResult<String, CommonError>
) : AnonymousIdentityProvider {
    override val uid: StateFlow<String?> = MutableStateFlow(null)

    override suspend fun ensureSignedIn(): AppResult<String, CommonError> = signInResult

    override suspend fun idToken(forceRefresh: Boolean): AppResult<String, CommonError> = AppResult.Failure(CommonError.Unauthorized)
}

private class RecordingEntitlementManager(
    private val identifyResult: AppResult<Unit, IdentityError> = AppResult.Success(Unit)
) : EntitlementManager {
    val identifiedUserIds = mutableListOf<String>()
    override val status: StateFlow<EntitlementStatus> = MutableStateFlow(EntitlementStatus.Free)
    override val isPro: StateFlow<Boolean> = MutableStateFlow(false)
    override val isGodMode: StateFlow<Boolean> = MutableStateFlow(false)

    override suspend fun identify(id: String): AppResult<Unit, IdentityError> {
        identifiedUserIds += id
        return identifyResult
    }

    override fun setGodMode(enabled: Boolean) = Unit

    override suspend fun refresh() = Unit

    override suspend fun offerings(): AppResult<List<ProProduct>, MonetizationError> = AppResult.Success(emptyList())

    override suspend fun purchase(productId: String): AppResult<Boolean, MonetizationError> = AppResult.Success(false)

    override suspend fun restorePurchases(): AppResult<Boolean, MonetizationError> = AppResult.Success(false)

    override suspend fun syncPurchases(): AppResult<Boolean, MonetizationError> = AppResult.Success(false)

    override suspend fun managementUrl(): AppResult<String?, MonetizationError> = AppResult.Success(null)

    override suspend fun fetchMetadata(): AppResult<ProMetadata, MonetizationError> = AppResult.Success(ProMetadata.DUMMY)
}