plugins {
    id("frnk.kmp.library.hosttest")
}

kotlin {
    android {
        namespace = "${libs.versions.frnk.groupId.get()}.notifications.api"
    }
    sourceSets {
        commonMain.dependencies {
            // AppResult / AppError — the toolkit-wide result envelope.
            api(projects.sharedUtils)
            api(libs.kotlinx.coroutines.core)
        }
    }
}