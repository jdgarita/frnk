import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework

plugins {
    id("frnk.kmp.base")
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    // Demo-owned Room schema (restructure Stage 4 / OQ-2): the demo carries its own DemoDatabase,
    // opened through :data-db-api's DatabaseFactory exactly the way a real host opens its schema.
    // The Room + KSP plugins belong to whichever module owns the entities — here, as in a host.
    alias(libs.plugins.androidx.room)
    alias(libs.plugins.ksp)
}

// Demo-bundled drawable for the Components tab icon: the adaptive bottom bar takes resource-based icons
// (DrawableResource). Pin the generated accessor package for deterministic imports.
compose.resources {
    publicResClass = true
    packageOfResClass = "${libs.versions.frnk.groupId.get()}.demo.generated.resources"
}

kotlin {
    android {
        namespace = "${libs.versions.frnk.groupId.get()}.demo.shared"
        withHostTest {}
    }

    val xcf = XCFramework("DemoKit")
    listOf(iosArm64(), iosSimulatorArm64()).forEach { t ->
        t.binaries.framework {
            baseName = "DemoKit"
            xcf.add(this)
            isStatic = true
            // Only api-only toolkit modules are exported. The demo's common surface avoids the
            // optional *-impl modules (monetization-impl / :data-db-impl and their native cinterops),
            // so DemoKit stays free of RevenueCat / bundled-SQLite symbols (:data-db-api carries only
            // Room's pure-Kotlin runtime). Observability is the deliberate exception: PostHog + Sentry
            // are mandatory for every host, the demo included, so :ui-app (and this module's
            // commonMain) carry them and iosDemoApp links the `PostHog` + `Sentry` SPM products.
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
            export(projects.uiBottomNav)
            export(projects.analyticsApi)
            // Stage 11 capability scaffolds — api modules only (their impls, if any, stay out of the
            // common surface so DemoKit links no extra native cinterop).
            export(projects.remoteConfigApi)
            export(projects.camera)
            export(projects.permissions)
            export(projects.dataDbApi)
            export(projects.dataPrefsApi)
            export(projects.monetizationApi)
            export(projects.sharedMonetizationUi)
            // Batteries-included app root (FrnkAppScaffold/FrnkAppConfig). Kotlin/Native `export` is
            // non-transitive, so export it directly to keep the Swift surface consistent with the api() list.
            export(projects.uiApp)
            // The Sentry / PostHog / RevenueCat native symbols resolve through the host's SPM products
            // at the app link step; defer them here (same approach as a host's own umbrella framework).
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
            // Platform-adaptive bottom nav. The demo consumes the toolkit default from here rather than
            // carrying any nav-bar implementation itself.
            api(projects.uiBottomNav)
            api(projects.analyticsApi)
            // Stage 11 capability scaffolds (matches the iOS export list above). Only the *-api
            // modules — :remote-config-impl is installed by androidDemoApp via Koin, never here.
            api(projects.remoteConfigApi)
            api(projects.camera)
            api(projects.permissions)
            api(projects.dataDbApi)
            api(projects.dataPrefsApi)
            api(projects.monetizationApi)
            api(projects.sharedMonetizationUi)
            // The batteries-included app root. :ui-app carries the mandatory observability pair
            // (:analytics-posthog + :crash-sentry, native SDKs supplied by iosDemoApp via SPM under
            // dynamic_lookup) and otherwise no *-impl, so it adds no RevenueCat/SQLite/Firebase symbols.
            api(projects.uiApp)
            // The demo bootstraps observability itself (bootstrapDemoKoin takes the two configs).
            implementation(projects.analyticsPosthog)
            implementation(projects.crashSentry)
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
        // iOS-only native SDK the demo opts into (kept out of commonMain so DemoKit's common surface
        // stays free of it): RevenueCat (P3-3), so DemoKit can install the REAL revenueCatModule over
        // the fake and iosDemoApp exercises the same RevenueCat Test Store path demo-android does. The
        // native purchases-ios SDK is supplied by the consumer (iosDemoApp) via SPM under dynamic_lookup.
        iosMain.dependencies {
            implementation(projects.monetizationImpl)
            implementation(libs.revenuecat.core)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
        }
        // The NoteStore round-trip test opens an in-memory Room database under Robolectric (Room's
        // Android builder needs a Context) on the framework driver, which runs on the JVM.
        getByName("androidHostTest").dependencies { implementation(libs.robolectric) }
    }
}

dependencies {
    add("kspAndroid", libs.androidx.room.compiler)
    add("kspIosArm64", libs.androidx.room.compiler)
    add("kspIosSimulatorArm64", libs.androidx.room.compiler)
}

room {
    schemaDirectory("$projectDir/schemas")
}

// Room's KSP task for the host-test compilation writes the generated source directories that
// AGP's lint tasks for that variant read as inputs, but nothing wires the two under the KMP
// Android plugin's `withHostTest {}`, so `check` fails Gradle's task validation ("property has
// implicit dependency"). Declare it; a host applying Room + KSP to a module with host tests and
// running `check` (not just `testAndroidHostTest`) needs the same two lines.
tasks
    .matching { it.name == "generateAndroidHostTestLintModel" || it.name == "lintAnalyzeAndroidHostTest" }
    .configureEach { dependsOn("kspAndroidHostTest") }