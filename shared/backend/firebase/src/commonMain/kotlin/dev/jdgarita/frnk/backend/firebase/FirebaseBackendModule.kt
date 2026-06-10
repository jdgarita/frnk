package dev.jdgarita.frnk.backend.firebase

import dev.jdgarita.frnk.backend.AuthService
import dev.jdgarita.frnk.backend.RemoteData
import org.koin.dsl.module

/**
 * Auth + remote data only. Analytics / crash reporting moved to [firebaseObservabilityModule] so
 * they can be selected independently of [dev.jdgarita.frnk.shared.BackendChoice] (BACKLOG P1-5).
 * Host picks ONE of firebase/supabase backend modules to satisfy the backend-api contracts.
 */
val firebaseBackendModule =
    module {
        single<AuthService> { FirebaseAuthService() }
        single<RemoteData> { FirestoreRemoteData() }
    }
