plugins {
    id("frnk.kmp.library.hosttest")
}

kotlin {
    android {
        namespace = "${libs.versions.frnk.groupId.get()}.backend.posthog"
    }
    sourceSets {
        commonMain.dependencies {
            api(projects.analyticsApi)
            implementation(libs.koin.core)
            implementation(libs.posthog.kmp)
        }
        // PostHog's Android setup needs the Application; it comes from Koin's androidContext(),
        // which the Android initializeFrnk overload registers.
        androidMain.dependencies {
            implementation(libs.koin.android)
        }
    }
}

// posthog-ios is supplied by the consuming Xcode target (SPM product `PostHog`), so this module
// cannot link a standalone Kotlin/Native test executable. Its common tests still run via
// testAndroidHostTest; native linkage is verified by the demo Xcode integration build.
listOf("linkDebugTestIosSimulatorArm64", "iosSimulatorArm64Test").forEach { taskName ->
    tasks.named(taskName) {
        enabled = false
    }
}