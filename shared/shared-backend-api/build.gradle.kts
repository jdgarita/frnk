plugins {
    id("frnk.kmp.library.hosttest")
}

kotlin {
    android {
        namespace = "${ProjectConfiguration.GROUP_ID}.backend.api"
    }
    sourceSets {
        commonMain.dependencies {
            api(projects.sharedUtils)
            api(libs.kotlinx.coroutines.core)
        }
    }
}
