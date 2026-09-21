package dev.jdgarita.frnk.ui.app

import dev.jdgarita.frnk.backend.noopAnalyticsModule
import dev.jdgarita.frnk.backend.noopCrashReportingModule
import dev.jdgarita.frnk.database.KeyValueStore
import dev.jdgarita.frnk.identity.AnonymousIdentityProvider
import dev.jdgarita.frnk.identity.IdentityError
import dev.jdgarita.frnk.monetization.EntitlementProvider
import dev.jdgarita.frnk.monetization.MonetizationError
import dev.jdgarita.frnk.monetization.ProMetadata
import dev.jdgarita.frnk.monetization.ProProduct
import dev.jdgarita.frnk.monetization.WebPurchaseRedemptionError
import dev.jdgarita.frnk.remoteconfig.noopRemoteConfigModule
import dev.jdgarita.frnk.utils.AppResult
import dev.jdgarita.frnk.utils.CommonError
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.koin.core.module.Module
import org.koin.dsl.koinApplication
import org.koin.dsl.module
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

/**
 * Starts a Koin graph and runs [validateFrnkBootstrap]: a complete frnk-UI stack passes, and dropping
 * any required module fails fast with a message naming the exact module to install. Mirrors
 * `FrnkInitializerTest`'s "the error must name the fix" contract.
 */
class FrnkBootstrapValidationTest {
    // monetizationModule needs an EntitlementProvider + KeyValueStore in the graph to construct, and
    // its SyncAuthUseCase an AnonymousIdentityProvider — a real provider module (revenueCatModule)
    // binds the identity alongside the entitlements, so the fake plays both parts too.
    private val identityModule = module { single<AnonymousIdentityProvider> { FakeIdentityProvider() } }
    private val fakesModule =
        module {
            single<EntitlementProvider> { FakeEntitlementProvider() }
            single<KeyValueStore> { FakeKeyValueStore() }
            includes(identityModule)
        }

    private fun validModules(): List<Module> =
        frnkModules {
            monetization(provider = fakesModule)
        }.plus(fakesModule)

    private fun validateMissing(modules: List<Module>): IllegalStateException {
        val app = koinApplication { modules(modules) }
        try {
            return assertFailsWith<IllegalStateException> { app.koin.validateFrnkBootstrap() }
        } finally {
            app.close()
        }
    }

    @Test
    fun complete_stack_passes() {
        val app = koinApplication { modules(validModules()) }
        try {
            app.koin.validateFrnkBootstrap() // must not throw
        } finally {
            app.close()
        }
    }

    @Test
    fun missing_analytics_names_the_module() {
        val failure = validateMissing(validModules().filterNot { it === noopAnalyticsModule })
        assertEquals(true, failure.message?.contains("analytics"), "names the missing axis")
        assertEquals(true, failure.message?.contains("noopAnalyticsModule"), "names the module to install")
        assertEquals(false, failure.message?.contains("crash reporting"), "the other slot is still bound")
        assertEquals(true, failure.message?.contains("initializeFrnk"), "names the bootstrap call")
    }

    @Test
    fun missing_crash_reporting_names_the_module() {
        val failure = validateMissing(validModules().filterNot { it === noopCrashReportingModule })
        assertEquals(true, failure.message?.contains("crash reporting"), "names the missing axis")
        assertEquals(true, failure.message?.contains("noopCrashReportingModule"), "names the module to install")
        assertEquals(false, failure.message?.contains("analytics —"), "the other slot is still bound")
    }

    @Test
    fun missing_identity_under_monetization_names_the_module() {
        val withoutIdentity =
            module {
                single<EntitlementProvider> { FakeEntitlementProvider() }
                single<KeyValueStore> { FakeKeyValueStore() }
            }
        val failure = validateMissing(frnkModules { monetization(provider = withoutIdentity) })
        assertEquals(true, failure.message?.contains("identity"), "names the missing axis")
        assertEquals(true, failure.message?.contains("revenueCatModule"), "names the module that binds it")
        assertEquals(false, failure.message?.contains("monetization —"), "the stack itself is present")
    }

    @Test
    fun missing_remote_config_names_the_module() {
        val failure = validateMissing(validModules().filterNot { it === noopRemoteConfigModule })
        assertEquals(true, failure.message?.contains("remote config"), "names the missing axis")
        assertEquals(true, failure.message?.contains("remoteConfigModule"), "names the module to install")
    }

    @Test
    fun missing_monetization_names_the_module_and_settings() {
        // frnkModules { } with no monetization(provider) → no monetizationModule in the graph.
        val failure = validateMissing(frnkModules { })
        assertEquals(true, failure.message?.contains("monetization"), "names the missing axis")
        assertEquals(true, failure.message?.contains("monetizationModule"), "names the module to install")
        assertEquals(true, failure.message?.contains("Settings"), "explains why it's required")
        assertEquals(false, failure.message?.contains("identity"), "identity is only required once the stack is")
    }
}

private class FakeIdentityProvider : AnonymousIdentityProvider {
    override val uid: StateFlow<String?> = MutableStateFlow("fake-uid")

    override suspend fun ensureSignedIn(): AppResult<String, CommonError> = AppResult.Success("fake-uid")
}

private class FakeEntitlementProvider : EntitlementProvider {
    override val isPro: StateFlow<Boolean> = MutableStateFlow(false)

    override suspend fun refresh() = Unit

    override suspend fun identify(id: String): AppResult<Unit, IdentityError> = AppResult.Success(Unit)

    override suspend fun offerings(): AppResult<List<ProProduct>, MonetizationError> = AppResult.Success(emptyList())

    override suspend fun purchase(productId: String): AppResult<Boolean, MonetizationError> = AppResult.Success(false)

    override suspend fun restore(): AppResult<Boolean, MonetizationError> = AppResult.Success(false)

    override suspend fun syncPurchases(): AppResult<Boolean, MonetizationError> = AppResult.Success(false)

    override suspend fun managementUrl(): AppResult<String?, MonetizationError> = AppResult.Success(null)

    override suspend fun fetchMetadata(): AppResult<ProMetadata, MonetizationError> = AppResult.Success(ProMetadata.DUMMY)

    override suspend fun redeemWebPurchase(url: String): AppResult<Boolean, WebPurchaseRedemptionError> =
        AppResult.Failure(WebPurchaseRedemptionError.NotARedemptionLink)
}

private class FakeKeyValueStore : KeyValueStore {
    private val strings = mutableMapOf<String, String>()
    private val booleans = mutableMapOf<String, Boolean>()

    override fun putString(
        key: String,
        value: String
    ) {
        strings[key] = value
    }

    override fun getString(
        key: String,
        default: String?
    ): String? = strings[key] ?: default

    override fun putBoolean(
        key: String,
        value: Boolean
    ) {
        booleans[key] = value
    }

    override fun getBoolean(
        key: String,
        default: Boolean
    ): Boolean = booleans[key] ?: default

    override fun remove(key: String) {
        strings.remove(key)
        booleans.remove(key)
    }
}