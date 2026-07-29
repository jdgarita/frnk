plugins {
    id("frnk.kmp.library.hosttest")
}

kotlin {
    android {
        namespace = "${libs.versions.frnk.groupId.get()}.backend.firebase"
    }
    sourceSets {
        commonMain.dependencies {
            api(projects.analyticsApi)
            implementation(libs.koin.core)
            implementation(libs.firebase.analytics)
            implementation(libs.firebase.crashlytics)
        }
        // gitlive-firebase 2.x delegates Android transitive versions to the Firebase BOM.
        androidMain.dependencies {
            implementation(project.dependencies.platform(libs.firebase.bom))
        }
        // CrashKiOS is iOS-only: it installs the Kotlin/Native unhandled-exception hook that
        // forwards uncaught Kotlin crashes to Crashlytics symbolicated.
        iosMain.dependencies {
            implementation(libs.crashkios.crashlytics)
        }
    }
}

// Firebase's Apple SDK is supplied by the consuming Xcode target, so this module cannot link a
// standalone Kotlin/Native test executable. Its common tests still run via testAndroidHostTest;
// native linkage is verified by the demo Xcode integration build.
listOf("linkDebugTestIosSimulatorArm64", "iosSimulatorArm64Test").forEach { taskName ->
    tasks.named(taskName) {
        enabled = false
    }
}