plugins {
    id("frnk.kmp.library.hosttest")
}

kotlin {
    android {
        namespace = "${ProjectConfiguration.GROUP_ID}.remoteconfig.api"
    }
    sourceSets {
        commonMain.dependencies {
            api(projects.sharedUtils)
            api(libs.kotlinx.coroutines.core)
            // noopRemoteConfigModule returns a Koin Module — part of the public surface.
            api(libs.koin.core)
        }
    }
}
