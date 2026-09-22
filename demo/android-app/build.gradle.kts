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

// Sentry DSN: the demo's own Sentry project (public client key), read from local.properties
// (gitignored). REQUIRED — the demo is a real host, and a blank DSN fails at bootstrap inside the
// config that names it. PostHog needs nothing here: its key is the toolkit-wide project's, shipped
// in :analytics-posthog.
val sentryDsn: String = localProperties.getProperty("SENTRY_DSN", "")

// Real Firebase Remote Config smoke test (restructure Stage 11): the google-services plugin processes
// google-services.json so Firebase auto-inits, enabling the real remoteConfigModule wired in
// DemoApplication. google-services.json is gitignored, so the plugin is applied ONLY when it's
// present — locally that turns on the real SDK; on CI (no json) it's skipped and the demo compiles,
// with the Remote Config path degrading to a logged failure at runtime (every gitlive call is
// wrapped in runCatching).
if (rootProject.file("demo/android-app/google-services.json").exists()) {
    apply(
        plugin =
            libs.plugins.google.services
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
    implementation(projects.analyticsPosthog) // PostHogAnalyticsConfig(debug = …) — the key itself ships in the module
    implementation(projects.crashSentry) // SentryCrashReportingConfig — the demo's own DSN from local.properties
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