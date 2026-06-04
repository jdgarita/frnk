plugins {
    id("frnk.kmp.library")
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    android {
        namespace = "${ProjectConfiguration.GROUP_ID}.ui.api"
        withHostTest {}
    }
    sourceSets {
        commonMain.dependencies {
            api(projects.sharedUtils)
            api(libs.kotlinx.coroutines.core)
            api(libs.androidx.lifecycle.viewmodel)
            // Type-safe nav routes (ToolkitRoute is @Serializable). Core only — navigation-compose
            // encodes routes via its own SavedStateEncoder, so kotlinx-serialization-json is not needed.
            api(libs.kotlinx.serialization.core)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}
