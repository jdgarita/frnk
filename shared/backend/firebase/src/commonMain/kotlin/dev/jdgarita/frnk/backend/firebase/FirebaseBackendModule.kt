package dev.jdgarita.frnk.backend.firebase

import dev.jdgarita.frnk.backend.RemoteData
import org.koin.dsl.module

/**
 * Remote data only. Analytics / crash reporting moved to [firebaseObservabilityModule] so
 * they can be selected independently of [dev.jdgarita.frnk.shared.BackendChoice] (BACKLOG P1-5).
 */
val firebaseBackendModule =
    module {
        single<RemoteData> { FirestoreRemoteData() }
    }
