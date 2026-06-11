import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework

plugins {
    id("frnk.kmp.base")
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    // Demo-owned SQLDelight schema (restructure Stage 4 / OQ-2): the demo carries its own DemoDB,
    // built through :data-db-api's SqlDriverFactory exactly the way a real host injects a schema.
    alias(libs.plugins.sqldelight)
}

// Demo-bundled drawable for the Components tab icon under the adaptive-nav-bar engine (which takes a
// DrawableResource, unlike Calf's ImageVector). Pin the generated accessor package for deterministic imports.
compose.resources {
    publicResClass = true
    packageOfResClass = "${ProjectConfiguration.GROUP_ID}.demo.generated.resources"
}

kotlin {
    android {
        namespace = "${ProjectConfiguration.GROUP_ID}.demo.shared"
        withHostTest {}
    }

    val xcf = XCFramework("DemoKit")
    listOf(iosArm64(), iosSimulatorArm64()).forEach { t ->
        t.binaries.framework {
            baseName = "DemoKit"
            xcf.add(this)
            isStatic = true
            // Only api-only toolkit modules are exported. The demo's common surface deliberately
            // avoids the *-impl modules (shared-monetization-revenuecat / :data-db-impl and their
            // native cinterops), so DemoKit stays free of RevenueCat / SQLite symbols.
            // EXCEPTION (BACKLOG P1-5b): the iosMain set adds the lightweight CrashKiOS cinterop so
            // the demo's "Force crash" panic button can be reported to Firebase Crashlytics. That
            // makes iosDemoApp require the native Firebase Crashlytics SDK (via SPM/CocoaPods) +
            // GoogleService-Info.plist — it no longer launches on a bare simulator with no Firebase.
            export(projects.sharedUtils)
            // shared-ui-api was split (restructure Stage 6); Kotlin/Native `export` is non-transitive, so
            // the src-less facade would carry no Swift symbols — export the three successors directly.
            export(projects.coreMvi)
            export(projects.coreNav)
            export(projects.haptics)
            // shared-ui-atoms was split (restructure Stage 7); the src-less facade carries no Swift
            // symbols (export is non-transitive) — export the three successors directly.
            export(projects.uiTheme)
            export(projects.uiComponents)
            export(projects.uiScaffolds)
            export(projects.sharedUiNav)
            export(projects.analyticsApi)
            export(projects.dataDbApi)
            export(projects.dataPrefsApi)
            export(projects.sharedMonetizationApi)
            export(projects.sharedMonetizationUi)
            // CrashKiOS resolves the native Crashlytics symbols through the host's Firebase SDK at the
            // app link step; defer them here (same approach as :iosApp / FrnkKit).
            linkerOpts("-undefined", "dynamic_lookup")
        }
    }

    sourceSets {
        commonMain.dependencies {
            api(projects.sharedUtils)
            // shared-ui-api split (Stage 6) — depend on the successors directly (matches the iOS export
            // list above; `export` requires the module to be a direct api dependency).
            api(projects.coreMvi)
            api(projects.coreNav)
            api(projects.haptics)
            // shared-ui-atoms split (Stage 7) — depend on the successors directly (matches the iOS export
            // list above; `export` requires the module to be a direct api dependency).
            api(projects.uiTheme)
            api(projects.uiComponents)
            api(projects.uiScaffolds)
            // Platform-adaptive bottom nav (Calf-backed). The demo consumes the toolkit default from here
            // rather than carrying any nav-bar implementation itself.
            api(projects.sharedUiNav)
            api(projects.analyticsApi)
            api(projects.dataDbApi)
            api(projects.dataPrefsApi)
            api(projects.sharedMonetizationApi)
            api(projects.sharedMonetizationUi)
            api(compose.runtime)
            api(compose.foundation)
            api(compose.ui)
            // Multiplatform BackHandler (androidx.compose.ui.backhandler) — bridges the Android
            // system back button / predictive-back gesture (and iOS swipe-back) to the demo's
            // in-app navigation state. Not transitively on compose.ui's Android classpath here.
            implementation(libs.compose.ui.backhandler)
            api(libs.koin.compose)
            api(libs.koin.compose.viewmodel)
            implementation(libs.koin.core)
            // The demo acts as a host and references a Lucide glyph at the call site (the bottom-nav
            // middle "Stats" tab). Real consumers only need this if they likewise name Lucide icons
            // directly; otherwise every icon is overridable through FrnkThemeConfig.
            implementation(libs.icons.lucide)
            // Demo-bundled drawable for the Components tab under the adaptive-nav-bar engine (resource icons).
            implementation(compose.components.resources)
        }
        // iOS-only native SDKs the demo opts into (kept out of commonMain so the Android demo +
        // DemoKit's common surface stay SDK-free):
        //  - CrashKiOS for the Crashlytics panic-button test (BACKLOG P1-5b).
        //  - RevenueCat (P3-3): so DemoKit can install the REAL revenueCatModule over the fake and
        //    iosDemoApp exercises the same RevenueCat Test Store path androidDemoApp does. The native
        //    purchases-ios SDK is supplied by the consumer (iosDemoApp) via SPM under dynamic_lookup.
        iosMain.dependencies {
            implementation(libs.crashkios.crashlytics)
            implementation(projects.sharedMonetizationRevenuecat)
            implementation(libs.revenuecat.core)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
        }
        // The NoteStore round-trip test runs on the JVM host, so it uses the JDBC SQLite driver
        // (JdbcSqliteDriver.IN_MEMORY). The android/native drivers can't run in a host test.
        getByName("androidHostTest").dependencies { implementation(libs.sqldelight.sqlite.driver) }
    }
}

sqldelight {
    databases {
        create("DemoDB") {
            packageName.set("${ProjectConfiguration.GROUP_ID}.demo.sql")
        }
    }
}
