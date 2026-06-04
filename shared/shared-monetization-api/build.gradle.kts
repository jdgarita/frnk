plugins {
    id("frnk.kmp.library.hosttest")
}

kotlin {
    android {
        namespace = "${ProjectConfiguration.GROUP_ID}.monetization.api"
    }
    sourceSets {
        commonMain.dependencies {
            api(projects.sharedBackendApi)
            api(projects.sharedDatabaseApi)
            api(libs.kotlinx.coroutines.core)
            api(libs.koin.core)
        }
    }
}
