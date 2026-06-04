plugins {
    id("frnk.kmp.base")
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.kotlin.compose)
}

kotlin {
    android {
        namespace = "${ProjectConfiguration.GROUP_ID}.android"
    }
    sourceSets.androidMain.dependencies {
        api(projects.shared)
    }
}
