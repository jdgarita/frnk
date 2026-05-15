plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
}

kotlin {
    jvmToolchain(17)
    androidTarget()
    listOf(iosX64(), iosArm64(), iosSimulatorArm64()).forEach { it.binaries.framework { baseName = "core_monetization_revenuecat" } }
    sourceSets {
        commonMain.dependencies {
            api(projects.coreMonetizationApi)
            implementation(libs.koin.core)
            implementation(libs.revenuecat.core)
            implementation(libs.revenuecat.result)
            implementation(libs.revenuecat.datetime)
        }
    }
}

android {
    namespace = "${ProjectConfiguration.GROUP_ID}.monetization.revenuecat"
    compileSdk = ProjectConfiguration.COMPILE_SDK
    defaultConfig { minSdk = ProjectConfiguration.MIN_SDK }
}
