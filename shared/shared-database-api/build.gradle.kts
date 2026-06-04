plugins {
    id("frnk.kmp.library")
}

kotlin {
    android {
        namespace = "${ProjectConfiguration.GROUP_ID}.database.api"
        withHostTest {}
    }
    sourceSets {
        commonMain.dependencies {
            api(projects.sharedUtils)
            api(libs.sqldelight.runtime)
            api(libs.settings.core)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}
