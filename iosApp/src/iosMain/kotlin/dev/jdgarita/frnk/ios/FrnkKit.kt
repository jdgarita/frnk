package dev.jdgarita.frnk.ios

import dev.jdgarita.frnk.shared.BackendChoice
import dev.jdgarita.frnk.shared.MonetizationChoice
import dev.jdgarita.frnk.shared.ObservabilityChoice
import dev.jdgarita.frnk.shared.initializeFrnk
import org.koin.core.KoinApplication
import org.koin.core.module.Module

/**
 * iOS bootstrap entry point. Selecting [ObservabilityChoice.Firebase] also installs the CrashKiOS
 * unhandled-exception hook so uncaught Kotlin crashes reach Crashlytics symbolicated (the consumer
 * must supply the Firebase Crashlytics pod and call `FirebaseApp.configure()`).
 *
 * [monetization] selects the billing stack ([MonetizationChoice.None] omits RevenueCat for apps that
 * don't monetize or use a different provider). [additionalModules] registers the host's own Koin
 * modules (feature ViewModels, repositories, a custom `EntitlementProvider`, …).
 */
fun bootstrapFrnkKit(
    backend: BackendChoice = BackendChoice.Supabase,
    observability: ObservabilityChoice = ObservabilityChoice.None,
    monetization: MonetizationChoice = MonetizationChoice.RevenueCat,
    additionalModules: List<Module> = emptyList(),
): KoinApplication = initializeFrnk(backend, observability, monetization, additionalModules)
