package dev.jdgarita.frnk.ios

import dev.jdgarita.frnk.shared.BackendChoice
import dev.jdgarita.frnk.shared.ObservabilityChoice
import dev.jdgarita.frnk.shared.initializeFrnk
import org.koin.core.KoinApplication

/**
 * iOS bootstrap entry point. Selecting [ObservabilityChoice.Firebase] also installs the CrashKiOS
 * unhandled-exception hook so uncaught Kotlin crashes reach Crashlytics symbolicated (the consumer
 * must supply the Firebase Crashlytics pod and call `FirebaseApp.configure()`).
 */
fun bootstrapFrnkKit(
    backend: BackendChoice = BackendChoice.Supabase,
    observability: ObservabilityChoice = ObservabilityChoice.None,
): KoinApplication = initializeFrnk(backend, observability)
