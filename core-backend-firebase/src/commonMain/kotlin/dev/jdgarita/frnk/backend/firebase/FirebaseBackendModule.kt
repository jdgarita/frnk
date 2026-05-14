package dev.jdgarita.frnk.backend.firebase

import dev.jdgarita.frnk.backend.AnalyticsTracker
import dev.jdgarita.frnk.backend.AuthService
import dev.jdgarita.frnk.backend.CrashReporter
import dev.jdgarita.frnk.backend.RemoteData
import org.koin.dsl.module

/** Host picks ONE of firebase/supabase modules to satisfy the backend-api contracts. */
val firebaseBackendModule = module {
    single<AuthService> { FirebaseAuthService() }
    single<AnalyticsTracker> { FirebaseAnalyticsTracker() }
    single<CrashReporter> { FirebaseCrashReporter() }
    single<RemoteData> { FirestoreRemoteData() }
}
