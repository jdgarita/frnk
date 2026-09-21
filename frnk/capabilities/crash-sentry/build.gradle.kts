plugins {
    id("frnk.kmp.library.hosttest")
}

kotlin {
    android {
        namespace = "${libs.versions.frnk.groupId.get()}.backend.sentry"
    }
    sourceSets {
        commonMain.dependencies {
            api(projects.analyticsApi)
            implementation(libs.koin.core)
            implementation(libs.sentry.kmp)
        }
    }
}

// Sentry's Cocoa SDK is supplied by the consuming Xcode target (SPM product `Sentry`), so this
// module cannot link a standalone Kotlin/Native test executable. Its common tests still run via
// testAndroidHostTest; native linkage is verified by the demo Xcode integration build.
listOf("linkDebugTestIosSimulatorArm64", "iosSimulatorArm64Test").forEach { taskName ->
    tasks.named(taskName) {
        enabled = false
    }
}