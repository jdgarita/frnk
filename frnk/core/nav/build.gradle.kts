plugins {
    id("frnk.kmp.library.hosttest")
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    android {
        namespace = "${libs.versions.frnk.groupId.get()}.core.nav"
    }
    sourceSets {
        commonMain.dependencies {
            api(libs.kotlinx.coroutines.core)
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