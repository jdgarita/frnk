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
    lint {
        // AGP 8.7's bundled lint embeds Kotlin Analysis API 2.0 and crashes on the project's
        // Kotlin 2.2.20 module metadata (NonNullableMutableLiveDataDetector hits
        // IncompatibleClassChangeError). This is a smoke harness, not a shipping app —
        // skipping release lint is the right scope. Bump AGP to 8.8+ to re-enable.
        checkReleaseBuilds = false
        abortOnError = false
    }
}

dependencies {
    // Toolkit surface. :androidApp re-exports :shared which aggregates every api + impl module.
    implementation(projects.androidApp)

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
