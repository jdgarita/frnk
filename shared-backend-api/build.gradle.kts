plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
}

kotlin {
    jvmToolchain(17)
    androidTarget()
    listOf(iosX64(), iosArm64(), iosSimulatorArm64()).forEach { it.binaries.framework { baseName = "shared_backend_api" } }
    sourceSets {
        commonMain.dependencies {
            api(projects.sharedUtils)
            api(libs.kotlinx.coroutines.core)
        }
    }
}

android {
    namespace = "${ProjectConfiguration.GROUP_ID}.backend.api"
    compileSdk = ProjectConfiguration.COMPILE_SDK
    defaultConfig { minSdk = ProjectConfiguration.MIN_SDK }
}
