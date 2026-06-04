plugins {
    id("frnk.kmp.library")
}

kotlin {
    android {
        namespace = "${ProjectConfiguration.GROUP_ID}.monetization.api"
        withHostTest {}
    }
    sourceSets {
        commonMain.dependencies {
            api(projects.sharedBackendApi)
            api(projects.sharedDatabaseApi)
            api(libs.kotlinx.coroutines.core)
            api(libs.koin.core)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}
