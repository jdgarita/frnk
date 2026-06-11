package dev.jdgarita.frnk.backend.firebase

import dev.jdgarita.frnk.backend.RemoteData
import org.koin.dsl.module

/**
 * Remote data only. Analytics / crash reporting moved to [firebaseObservabilityModule] so
 * they are installed independently of this data-backend module (BACKLOG P1-5).
 */
val firebaseBackendModule =
    module {
        single<RemoteData> { FirestoreRemoteData() }
    }
