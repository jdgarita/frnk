plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
}

kotlin {
    jvmToolchain(17)
    androidTarget()
    listOf(iosX64(), iosArm64(), iosSimulatorArm64()).forEach { it.binaries.framework { baseName = "shared_utils" } }
    sourceSets {
        commonMain.dependencies {
            api(libs.kotlinx.coroutines.core)
            api(libs.kotlinx.datetime)
        }
    }
}

android {
    namespace = "${ProjectConfiguration.GROUP_ID}.utils"
    compileSdk = ProjectConfiguration.COMPILE_SDK
    defaultConfig { minSdk = ProjectConfiguration.MIN_SDK }
}
