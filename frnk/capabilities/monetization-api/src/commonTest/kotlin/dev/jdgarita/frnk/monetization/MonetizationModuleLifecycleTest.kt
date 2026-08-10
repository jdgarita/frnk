package dev.jdgarita.frnk.monetization

import dev.jdgarita.frnk.backend.AnalyticsTracker
import dev.jdgarita.frnk.backend.ToolkitEvent
import dev.jdgarita.frnk.database.KeyValueStore
import dev.jdgarita.frnk.utils.AppResult
import dev.jdgarita.frnk.utils.CommonError
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MonetizationModuleLifecycleTest {
    @AfterTest
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun application_scope_is_cancelled_when_koin_stops() {
        val application =
            startKoin {
                modules(
                    module {
                        single<EntitlementProvider> { LifecycleTestProvider() }
                        single<KeyValueStore> { LifecycleTestKeyValueStore() }
                        single<AnalyticsTracker> { LifecycleTestAnalytics() }
                    },
                    monetizationModule
                )
            }

        application.koin.get<EntitlementManager>()
        val scope = application.koin.get<CoroutineScope>(FRNK_APPLICATION_SCOPE)
        assertTrue(scope.isActive)

        application.close()

        assertFalse(scope.isActive)
    }
}

private class LifecycleTestProvider : EntitlementProvider {
    override val isPro: StateFlow<Boolean> = MutableStateFlow(false)

    override suspend fun refresh() = Unit

    override suspend fun identify(userId: String): AppResult<Unit, CommonError> = AppResult.Success(Unit)

    override suspend fun offerings(): AppResult<List<ProProduct>, MonetizationError> = AppResult.Success(emptyList())

    override suspend fun purchase(productId: String): AppResult<Boolean, MonetizationError> = AppResult.Success(true)

    override suspend fun restore(): AppResult<Boolean, MonetizationError> = AppResult.Success(false)

    override suspend fun syncPurchases(): AppResult<Boolean, MonetizationError> = AppResult.Success(false)

    override suspend fun managementUrl(): AppResult<String?, MonetizationError> = AppResult.Success(null)

    override suspend fun fetchMetadata(): AppResult<ProMetadata, MonetizationError> = AppResult.Success(ProMetadata.DUMMY)
}

private class LifecycleTestKeyValueStore : KeyValueStore {
    private val values = mutableMapOf<String, Any>()

    override fun putString(
        key: String,
        value: String
    ) {
        values[key] = value
    }

    override fun getString(
        key: String,
        default: String?
    ): String? = values[key] as? String ?: default

    override fun putBoolean(
        key: String,
        value: Boolean
    ) {
        values[key] = value
    }

    override fun getBoolean(
        key: String,
        default: Boolean
    ): Boolean = values[key] as? Boolean ?: default

    override fun remove(key: String) {
        values.remove(key)
    }
}

private class LifecycleTestAnalytics : AnalyticsTracker {
    override fun track(
        event: ToolkitEvent,
        params: Map<String, Any?>
    ) = Unit

    override fun trackCustom(
        name: String,
        params: Map<String, Any?>
    ) = Unit

    override fun setUserProperty(
        key: String,
        value: String?
    ) = Unit
}