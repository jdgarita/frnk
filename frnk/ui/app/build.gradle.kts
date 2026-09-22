plugins {
    id("frnk.kmp.library.compose")
    id("frnk.kmp.library.hosttest")
}

kotlin {
    android {
        namespace = "${libs.versions.frnk.groupId.get()}.ui.app"
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