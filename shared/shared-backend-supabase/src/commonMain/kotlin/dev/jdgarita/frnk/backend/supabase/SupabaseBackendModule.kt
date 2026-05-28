package dev.jdgarita.frnk.backend.supabase

import dev.jdgarita.frnk.backend.AnalyticsTracker
import dev.jdgarita.frnk.backend.AuthService
import dev.jdgarita.frnk.backend.CrashReporter
import dev.jdgarita.frnk.backend.RemoteData
import org.koin.dsl.module

val supabaseBackendModule =
    module {
        single<AuthService> { SupabaseAuthService() }
        single<AnalyticsTracker> { NoopAnalyticsTracker() }
        single<CrashReporter> { NoopCrashReporter() }
        single<RemoteData> { SupabaseRemoteData() }
    }
