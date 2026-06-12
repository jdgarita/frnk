plugins {
    id("frnk.kmp.library.hosttest")
}

kotlin {
    android {
        namespace = "${libs.versions.frnk.groupId.get()}.camera"
    }
    sourceSets {
        commonMain.dependencies {
            api(projects.sharedUtils)
            api(libs.kotlinx.coroutines.core)
            // cameraModule returns a Koin Module — part of the public surface.
            api(libs.koin.core)
        }
    }
}
