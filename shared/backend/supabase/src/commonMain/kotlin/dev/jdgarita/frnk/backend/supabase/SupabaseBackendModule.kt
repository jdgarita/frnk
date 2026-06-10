package dev.jdgarita.frnk.backend.supabase

import dev.jdgarita.frnk.backend.AuthService
import dev.jdgarita.frnk.backend.RemoteData
import org.koin.dsl.module

/**
 * Auth + remote data only. Analytics / crash reporting are a **separate axis** (`ObservabilityChoice`
 * in `:shared`), not bound here — see `firebaseObservabilityModule` / `noopObservabilityModule`.
 */
val supabaseBackendModule =
    module {
        single<AuthService> { SupabaseAuthService() }
        single<RemoteData> { SupabaseRemoteData() }
    }
