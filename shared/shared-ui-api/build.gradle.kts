plugins {
    id("frnk.kmp.library.hosttest")
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    android {
        namespace = "${ProjectConfiguration.GROUP_ID}.ui.api"
    }
    sourceSets {
        commonMain.dependencies {
            api(projects.sharedUtils)
            api(libs.kotlinx.coroutines.core)
            api(libs.androidx.lifecycle.viewmodel)
            // Type-safe nav routes (ToolkitRoute is @Serializable). Core only — Navigation3 encodes
            // routes via savedstate's SavedStateEncoder, so kotlinx-serialization-json is not needed.
            api(libs.kotlinx.serialization.core)
            // Navigation3 runtime: the pure-Kotlin/multiplatform half of nav3 (NavKey, NavBackStack,
            // SavedStateConfiguration). No Compose — safe in this Compose-free contract module. `api` so
            // ToolkitRoute's `: NavKey` supertype and the back-stack helpers are visible to consumers.
            api(libs.androidx.navigation3.runtime)
        }
    }
}
