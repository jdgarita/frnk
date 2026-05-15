plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.kotlin.compose)
}

kotlin {
    jvmToolchain(17)
    androidTarget()
    sourceSets.androidMain.dependencies {
        api(projects.coreUtils)
        api(projects.coreUiApi)
        api(projects.coreUiAtoms)
        api(projects.coreDatabaseApi)
        api(projects.coreBackendApi)
        api(projects.coreMonetizationApi)
        // Host apps may include impl modules selectively; toolkit only re-exports API surface here.
    }
}

android {
    namespace = "${ProjectConfiguration.GROUP_ID}.android"
    compileSdk = ProjectConfiguration.COMPILE_SDK
    defaultConfig { minSdk = ProjectConfiguration.MIN_SDK }
}
