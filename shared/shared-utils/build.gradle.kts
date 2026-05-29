plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
}

kotlin {
    jvmToolchain(17)
    android {
        namespace = "${ProjectConfiguration.GROUP_ID}.utils"
        compileSdk = ProjectConfiguration.COMPILE_SDK
        minSdk = ProjectConfiguration.MIN_SDK
        withHostTest {}
    }
    listOf(iosArm64(), iosSimulatorArm64()).forEach { it.binaries.framework { baseName = "shared_utils" } }
    sourceSets {
        commonMain.dependencies {
            api(libs.kotlinx.coroutines.core)
            api(libs.kotlinx.datetime)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}
