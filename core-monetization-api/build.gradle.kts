plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
}

kotlin {
    jvmToolchain(17)
    androidTarget()
    listOf(iosX64(), iosArm64(), iosSimulatorArm64()).forEach { it.binaries.framework { baseName = "core_monetization_api" } }
    sourceSets {
        commonMain.dependencies {
            api(projects.coreBackendApi)
            api(libs.kotlinx.coroutines.core)
            api(libs.koin.core)
        }
    }
}

android {
    namespace = "${ProjectConfiguration.GROUP_ID}.monetization.api"
    compileSdk = ProjectConfiguration.COMPILE_SDK
    defaultConfig { minSdk = ProjectConfiguration.MIN_SDK }
}
