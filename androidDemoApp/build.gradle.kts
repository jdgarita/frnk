plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.compose.compiler)
}

kotlin {
    jvmToolchain(17)
}

android {
    namespace = "dev.jdgarita.frnk.demo"
    compileSdk = ProjectConfiguration.Android.COMPILE_SDK
    defaultConfig {
        applicationId = "dev.jdgarita.frnk.demo"
        minSdk = ProjectConfiguration.Android.MIN_SDK
        targetSdk = ProjectConfiguration.Android.TARGET_SDK
        versionCode = 1
        versionName = "0.1.0"
    }
    buildFeatures { compose = true }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    implementation(project(":androidApp"))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.koin.android)
    implementation(libs.koin.compose)
}
