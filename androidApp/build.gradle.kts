plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.compose.compiler)
}

// Pin both Kotlin and Java to JDK 17 regardless of the launcher JVM (Android
// Studio bundles 21, the CLI here is on 17). Without this, the Kotlin task
// inherits the launcher's JVM target and AGP's javac (forced to 17 by
// compileOptions below) disagrees.
kotlin {
    jvmToolchain(17)
}

android {
    namespace = "dev.jdgarita.frnk.android"
    compileSdk = ProjectConfiguration.Android.COMPILE_SDK
    defaultConfig {
        minSdk = ProjectConfiguration.Android.MIN_SDK
        consumerProguardFiles("consumer-rules.pro")
    }
    buildFeatures { compose = true }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    api(project(":core-common"))
    api(project(":core-network-api"))
    api(project(":core-network-impl"))
    api(project(":core-database-api"))
    api(project(":core-database-impl"))
    api(project(":core-ui-atoms"))

    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.koin.android)
    implementation(libs.koin.compose)
    implementation(libs.gitlive.firebase.app)
    implementation(libs.gitlive.firebase.auth)
}
