plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.compose.multiplatform)
}

kotlin {
    jvmToolchain(17)
}

android {
    namespace = "${ProjectConfiguration.GROUP_ID}.demo"
    compileSdk = ProjectConfiguration.COMPILE_SDK
    defaultConfig {
        applicationId = "${ProjectConfiguration.GROUP_ID}.demo"
        minSdk = ProjectConfiguration.MIN_SDK
        targetSdk = ProjectConfiguration.TARGET_SDK
        versionCode = 1
        versionName = "0.1.0"
    }
    buildFeatures { compose = true }
    buildTypes {
        getByName("release") { isMinifyEnabled = false }
    }
}

dependencies {
    // Toolkit surface. The androidApp aggregator re-exports every public api module.
    implementation(projects.androidApp)
    implementation(projects.coreUiAtoms)
    implementation(projects.coreUiApi)
    implementation(projects.coreMonetizationApi)
    implementation(projects.coreBackendApi)
    implementation(projects.coreDatabaseImpl)

    // Compose runtime + UI primitives (multiplatform artifacts).
    implementation(compose.runtime)
    implementation(compose.foundation)
    implementation(compose.ui)

    // Android entry-point.
    implementation(libs.androidx.activity.compose)

    // DI.
    implementation(libs.koin.core)
    implementation(libs.koin.compose)
    implementation(libs.koin.compose.viewmodel)
}
