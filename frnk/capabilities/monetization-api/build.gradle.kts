plugins {
    id("frnk.kmp.library.hosttest")
}

kotlin {
    android {
        namespace = "${ProjectConfiguration.GROUP_ID}.monetization.api"
    }
    sourceSets {
        commonMain.dependencies {
            api(projects.shared.backend.api)
            // KeyValueStore + the typed Preference layer — god-mode persistence in
            // DefaultEntitlementManager only needs key-value, never the SQL driver SPI.
            api(projects.dataPrefsApi)
            api(libs.kotlinx.coroutines.core)
            api(libs.koin.core)
        }
    }
}
