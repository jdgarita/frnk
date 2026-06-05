import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.compose.multiplatform)
}

// RevenueCat real-path smoke test (BACKLOG P3-2): the demo configures RevenueCat with a public
// Android SDK key read from local.properties (gitignored). When absent (e.g. CI, fresh checkout)
// the key is "" and DemoApplication falls back to the in-memory fake EntitlementManager.
val revenueCatAndroidApiKey: String =
    Properties()
        .apply {
            val f = rootProject.file("local.properties")
            if (f.exists()) f.inputStream().use { load(it) }
        }.getProperty("REVENUECAT_ANDROID_API_KEY", "")

// Real Firebase smoke test (BACKLOG P1-5): the google-services plugin processes
// google-services.json so Firebase auto-inits, enabling the real firebaseObservabilityModule
// wired in DemoApplication. google-services.json is gitignored, so these plugins are applied
// ONLY when it's present — locally that turns on the real SDK; on CI (no json) they're skipped
// and the demo compiles, with DemoApplication's Firebase path degrading to a logged no-op at
// runtime (every gitlive call is wrapped in runCatching).
if (rootProject.file("androidDemoApp/google-services.json").exists()) {
    apply(
        plugin =
            libs.plugins.google.services
                .get()
                .pluginId,
    )
    apply(
        plugin =
            libs.plugins.firebase.crashlytics
                .get()
                .pluginId,
    )
}

kotlin {
    jvmToolchain(17)
}

android {
    namespace = "${ProjectConfiguration.GROUP_ID}.demo"
    compileSdk =
        libs.versions.android.compileSdk
            .get()
            .toInt()
    defaultConfig {
        applicationId = "${ProjectConfiguration.GROUP_ID}.demo"
        minSdk =
            libs.versions.android.minSdk
                .get()
                .toInt()
        targetSdk =
            libs.versions.android.targetSdk
                .get()
                .toInt()
        versionCode = 1
        versionName = "0.1.0"
        buildConfigField("String", "REVENUECAT_ANDROID_API_KEY", "\"$revenueCatAndroidApiKey\"")
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    buildTypes {
        getByName("release") { isMinifyEnabled = false }
    }
}

dependencies {
    // Toolkit surface. :androidApp re-exports :shared which aggregates every api + impl module.
    implementation(projects.androidApp)
    // Shared demo Composable + MVI + Koin module (also consumed by iosDemoApp).
    implementation(projects.sharedDemo)

    // RevenueCat KMP SDK — only for the host's Purchases.configure(...) call (BACKLOG P3-2).
    // revenueCatModule / RevenueCatConfig arrive transitively via :androidApp → :shared (api),
    // so no direct :shared-monetization-revenuecat project dep is needed.
    implementation(libs.revenuecat.core)

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
