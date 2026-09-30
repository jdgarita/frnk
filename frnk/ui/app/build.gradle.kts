plugins {
    id("frnk.kmp.library.compose")
    id("frnk.kmp.library.hosttest")
}

kotlin {
    android {
        namespace = "${libs.versions.frnk.groupId.get()}.ui.app"
        // FrnkStackShellTest drives a real composition (runComposeUiTest) under Robolectric, which needs
        // the merged Android resources/manifest to inflate the test host — as :ui-scaffolds does. The host
        // test is already created by frnk.kmp.library.hosttest (only one may exist), so configure it
        // through its compilation, the AGP-documented way to change an existing host test's options.
        compilations.withType(com.android.build.api.dsl.KotlinMultiplatformAndroidHostTestCompilation::class.java) {
            isIncludeAndroidResources = true
        }
    }
    sourceSets {
        commonMain.dependencies {
            // Apex of the ui column: FrnkAppShell + the adaptive bar (Material3 arrives transitively
            // from here — the accepted batteries-included trade), the paywall view layer, and the
            // observability contracts. EntitlementManager is resolved from Koin at runtime; hosts
            // install the monetization/database/prefs impl modules they choose.
            api(projects.uiBottomNav)
            api(projects.sharedMonetizationUi)
            api(projects.analyticsApi)
            // The ONE deliberate SDK-backed edge in the ui column: observability is not a choice
            // (every host ships PostHog + Sentry, no no-op), so frnkModules { observability(…) } takes
            // the two configs and installs the providers itself. `api` because the config types are
            // part of the builder's signature. Every host links both native SDKs regardless, so this
            // adds nothing a host would not otherwise carry.
            api(projects.analyticsPosthog)
            api(projects.crashSentry)
            // The bootstrap FrnkAppScaffold's fail-fast assertion points hosts at.
            api(projects.coreDi)
            // Multiplatform BackHandler — FrnkStackShell's host-window fallback for a visible sheet.
            implementation(libs.compose.ui.backhandler)
        }

        // The Compose host-test bundle :ui-scaffolds gets from frnk.kmp.library.composehosttest
        // (kotlin-test + coroutines-test arrive from commonTest via frnk.kmp.library.hosttest).
        getByName("androidHostTest").dependencies {
            implementation(libs.compose.ui.test)
            // Registers androidx.activity.ComponentActivity in the test manifest so runComposeUiTest
            // can launch it under Robolectric.
            implementation(libs.androidx.compose.ui.test.manifest)
            implementation(libs.robolectric)
        }
    }
}

// :analytics-posthog / :crash-sentry (now api deps) reference posthog-ios / sentry-cocoa symbols that
// only the consuming Xcode target supplies, so this module cannot link a standalone Kotlin/Native
// test executable either. Its common tests still run via testAndroidHostTest; native linkage is
// verified by the demo Xcode integration build (docs/plans/2026-07-28-native-backed-ios-test-policy.md).
listOf("linkDebugTestIosSimulatorArm64", "iosSimulatorArm64Test").forEach { taskName ->
    tasks.named(taskName) {
        enabled = false
    }
}