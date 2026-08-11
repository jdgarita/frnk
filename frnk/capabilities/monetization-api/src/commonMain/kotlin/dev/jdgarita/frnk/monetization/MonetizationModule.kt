package dev.jdgarita.frnk.monetization

import dev.jdgarita.frnk.monetization.usecase.DefaultObserveProStatusUseCase
import dev.jdgarita.frnk.monetization.usecase.DefaultPaywallPurchaseUseCase
import dev.jdgarita.frnk.monetization.usecase.DefaultSyncAuthUseCase
import dev.jdgarita.frnk.monetization.usecase.ObserveProStatusUseCase
import dev.jdgarita.frnk.monetization.usecase.PaywallPurchaseUseCase
import dev.jdgarita.frnk.monetization.usecase.SyncAuthUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import org.koin.core.module.dsl.onClose
import org.koin.core.module.dsl.withOptions
import org.koin.core.qualifier.named
import org.koin.dsl.module

internal val FRNK_APPLICATION_SCOPE = named("frnkApplicationScope")

/**
 * frnk-owned monetization bindings, independent of any billing SDK. Binds the canonical
 * [EntitlementManager] over whatever [EntitlementProvider] the host installs (RevenueCat in `:shared`,
 * a fake in `:shared-demo`) plus [FeatureGate].
 *
 * Requires an [EntitlementProvider], a [dev.jdgarita.frnk.database.KeyValueStore] (for god-mode
 * persistence), an [dev.jdgarita.frnk.backend.AnalyticsTracker], and an
 * [dev.jdgarita.frnk.identity.AnonymousIdentityProvider] (for [SyncAuthUseCase]) to be present in
 * the graph.
 */
val monetizationModule =
    module {
        single<CoroutineScope>(FRNK_APPLICATION_SCOPE) {
            CoroutineScope(SupervisorJob() + Dispatchers.Default)
        } withOptions {
            onClose { scope -> scope?.cancel() }
        }
        // Deliberately NOT createdAtStart: eager instantiation would crash startKoin before
        // validateFrnkBootstrap can name a missing dependency. The validated path resolves the
        // manager at bootstrap anyway (isBound getOrNull), which triggers its construction-time
        // provider.refresh() hydration; unvalidated hosts hydrate on first injection.
        single<EntitlementManager> {
            DefaultEntitlementManager(
                provider = get(),
                keyValueStore = get(),
                analytics = get(),
                scope = get(FRNK_APPLICATION_SCOPE)
            )
        }
        single { FeatureGate(get(), get()) }
        single<ObserveProStatusUseCase> { DefaultObserveProStatusUseCase(get()) }
        single<PaywallPurchaseUseCase> { DefaultPaywallPurchaseUseCase(get()) }
        single<SyncAuthUseCase> {
            DefaultSyncAuthUseCase(
                identityProvider = get(),
                entitlementManager = get(),
                crashReporter = get(),
                analyticsTracker = get()
            )
        }
    }