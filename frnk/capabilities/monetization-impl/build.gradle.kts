plugins {
    id("frnk.kmp.library.hosttest")
}

kotlin {
    android {
        namespace = "${libs.versions.frnk.groupId.get()}.monetization.revenuecat"
    }
    sourceSets {
        commonMain.dependencies {
            api(projects.monetizationApi)
            implementation(libs.koin.core)
            implementation(libs.revenuecat.core)
            implementation(libs.revenuecat.result)
        }
    }
}

// RevenueCat's Apple SDK is supplied by the consuming Xcode target, so this module cannot link a
// standalone Kotlin/Native test executable. Its common tests still run via testAndroidHostTest;
// native linkage is verified by the demo Xcode integration build.
listOf("linkDebugTestIosSimulatorArm64", "iosSimulatorArm64Test").forEach { taskName ->
    tasks.named(taskName) {
        enabled = false
    }
}