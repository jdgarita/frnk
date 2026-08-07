plugins {
    id("frnk.kmp.library.hosttest")
}

kotlin {
    android {
        namespace = "${libs.versions.frnk.groupId.get()}.monetization.api"
    }
    sourceSets {
        commonMain.dependencies {
            api(projects.analyticsApi)
            // AnonymousIdentityProvider — SyncAuthUseCase bridges the anonymous identity to the
            // billing backend, so the identity contract sits on this module's api surface.
            api(projects.identityApi)
            // KeyValueStore + the typed Preference layer — god-mode persistence in
            // DefaultEntitlementManager only needs key-value, never the SQL driver SPI.
            api(projects.dataPrefsApi)
            api(libs.kotlinx.coroutines.core)
            api(libs.koin.core)
        }
    }
}