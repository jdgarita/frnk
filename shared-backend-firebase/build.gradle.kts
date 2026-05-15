plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    jvmToolchain(17)
    androidTarget()
    listOf(iosX64(), iosArm64(), iosSimulatorArm64()).forEach { it.binaries.framework { baseName = "shared_backend_firebase" } }
    sourceSets {
        commonMain.dependencies {
            api(projects.sharedBackendApi)
            implementation(libs.koin.core)
            implementation(libs.firebase.auth)
            implementation(libs.firebase.firestore)
            implementation(libs.firebase.analytics)
            implementation(libs.firebase.crashlytics)
        }
    }
}

android {
    namespace = "${ProjectConfiguration.GROUP_ID}.backend.firebase"
    compileSdk = ProjectConfiguration.COMPILE_SDK
    defaultConfig { minSdk = ProjectConfiguration.MIN_SDK }
}
