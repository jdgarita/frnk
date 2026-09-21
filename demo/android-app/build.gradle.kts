import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.compose.multiplatform)
    // Sentry's Android Gradle plugin (R8 mapping upload when SENTRY_AUTH_TOKEN is set) — the host
    // half of :crash-sentry, applied the way a real host applies it.
    id("frnk.android.sentry")
}

// RevenueCat real-path smoke test (BACKLOG P3-2): the demo configures RevenueCat with a public
// Android SDK key read from local.properties (gitignored). When absent (e.g. CI, fresh checkout)
// the key is "" and DemoApplication falls back to the in-memory fake EntitlementManager.
val localProperties: Properties =
    Properties().apply {
        val f = rootProject.file("local.properties")
        if (f.exists()) f.inputStream().use { load(it) }
    }
val revenueCatAndroidApiKey: String = localProperties.getProperty("REVENUECAT_ANDROID_API_KEY", "")

// Sentry + PostHog real-path smoke test: public client keys read from local.properties (gitignored).
// Blank = DemoApplication keeps the Firebase bindings for that slot (until Firebase is retired).
val sentryDsn: String = localProperties.getProperty("SENTRY_DSN", "")
val postHogApiKey: String = localProperties.getProperty("POSTHOG_API_KEY", "")
val postHogHost: String = localProperties.getProperty("POSTHOG_HOST", "")

// Real Firebase smoke test (BACKLOG P1-5): the google-services plugin processes
// google-services.json so Firebase auto-inits, enabling the real firebaseObservabilityModule
// wired in DemoApplication. google-services.json is gitignored, so these plugins are applied
// ONLY when it's present — locally that turns on the real SDK; on CI (no json) they're skipped
// and the demo compiles, with DemoApplication's Firebase path degrading to a logged no-op at
// runtime (every gitlive call is wrapped in runCatching).
if (rootProject.file("demo/android-app/google-services.json").exists()) {
    apply(
        plugin =
            libs.plugins.google.services
                .get()
                .pluginId
    )
    apply(
        plugin =
            libs.plugins.firebase.crashlytics
                .get()
                .pluginId
    )
}

kotlin {
    jvmToolchain(17)
}

android {
    namespace = "${libs.versions.frnk.groupId.get()}.demo"
    compileSdk =
        libs.versions.android.compileSdk
            .get()
            .toInt()
    defaultConfig {
        applicationId = "${libs.versions.frnk.groupId.get()}.demo"
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
        buildConfigField("String", "SENTRY_DSN", "\"$sentryDsn\"")
        buildConfigField("String", "POSTHOG_API_KEY", "\"$postHogApiKey\"")
        buildConfigField("String", "POSTHOG_HOST", "\"$postHogHost\"")
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
    // Toolkit surface — the individual modules this harness actually uses, like a real host
    // (the :shared/:androidApp aggregators died at restructure Stage 1). Atoms/theme/scaffolds/utils
    // arrive transitively via :demo-shared's api() deps.
    implementation(projects.uiApp) // FrnkAppScaffold — now wrapped by :demo-shared's DemoScreen (also transitive)
    implementation(projects.analyticsImpl) // firebaseAnalyticsModule / firebaseCrashReportingModule fallbacks
    implementation(projects.analyticsPosthog) // postHogAnalyticsModule when POSTHOG_API_KEY is set
    implementation(projects.crashSentry) // sentryCrashReportingModule when SENTRY_DSN is set
    implementation(projects.monetizationImpl) // revenueCatModule override
    implementation(projects.dataDbImpl) // databaseModule override — real DatabaseFactory for DemoDatabase
    implementation(projects.remoteConfigImpl) // remoteConfigModule override — real Firebase Remote Config
    implementation(projects.coreDi) // DatabaseContext seam (the demo bypasses initializeFrnk)
    // Shared demo Composable + MVI + Koin module (also consumed by iosDemoApp).
    implementation(projects.demoShared)

    // RevenueCat KMP SDK — for the host's Purchases.configure(...) call (BACKLOG P3-2).
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