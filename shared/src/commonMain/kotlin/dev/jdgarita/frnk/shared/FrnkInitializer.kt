package dev.jdgarita.frnk.shared

import org.koin.core.KoinApplication
import org.koin.core.context.startKoin

fun initializeFrnk(
    backend: BackendChoice = BackendChoice.Supabase,
    observability: ObservabilityChoice = ObservabilityChoice.None,
    extraConfig: KoinApplication.() -> Unit = {},
): KoinApplication =
    startKoin {
        modules(frnkModules(backend, observability))
        extraConfig()
    }
