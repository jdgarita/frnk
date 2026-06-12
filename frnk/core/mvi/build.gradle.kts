plugins {
    id("frnk.kmp.library.hosttest")
}

kotlin {
    android {
        namespace = "${libs.versions.frnk.groupId.get()}.core.mvi"
    }
    sourceSets {
        commonMain.dependencies {
            api(projects.sharedUtils)
            api(libs.kotlinx.coroutines.core)
            api(libs.androidx.lifecycle.viewmodel)
        }
    }
}
