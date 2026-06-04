plugins {
    id("frnk.kmp.library.hosttest")
}

kotlin {
    android {
        namespace = "${ProjectConfiguration.GROUP_ID}.database.api"
    }
    sourceSets {
        commonMain.dependencies {
            api(projects.sharedUtils)
            api(libs.sqldelight.runtime)
            api(libs.settings.core)
        }
    }
}
