plugins {
    id("frnk.kmp.library.hosttest")
}

kotlin {
    android {
        namespace = "${libs.versions.frnk.groupId.get()}.backend.api"
    }
    sourceSets {
        commonMain.dependencies {
            api(projects.sharedUtils)
            api(libs.kotlinx.coroutines.core)
            // noopObservabilityModule returns a Koin Module — part of the public surface.
            api(libs.koin.core)
            api(projects.identityApi)
        }
    }
}