plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    jvmToolchain(17)
    android {
        namespace = "${ProjectConfiguration.GROUP_ID}.backend.supabase"
        compileSdk = ProjectConfiguration.COMPILE_SDK
        minSdk = ProjectConfiguration.MIN_SDK
    }
    listOf(iosX64(), iosArm64(), iosSimulatorArm64()).forEach { it.binaries.framework { baseName = "shared_backend_supabase" } }
    sourceSets {
        commonMain.dependencies {
            api(projects.sharedBackendApi)
            implementation(libs.koin.core)
            implementation(libs.supabase.postgrest)
            implementation(libs.supabase.auth)
            implementation(libs.supabase.storage)
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.client.serialization)
        }
        androidMain.dependencies { implementation(libs.ktor.client.android) }
        iosMain.dependencies { implementation(libs.ktor.client.darwin) }
    }
}
