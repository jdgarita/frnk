plugins {
    id("frnk.kmp.library.hosttest")
}

kotlin {
    android {
        namespace = "${libs.versions.frnk.groupId.get()}.identity.firebase"
    }
    sourceSets {
        commonMain.dependencies {
            api(projects.identityApi)
            implementation(libs.koin.core)
            implementation(libs.firebase.auth)
        }
        androidMain.dependencies {
            implementation(project.dependencies.platform(libs.firebase.bom))
        }
    }
}

// Firebase's Apple SDK is supplied by the consuming Xcode target. Common tests run on Android;
// native linkage is verified by the host application's Xcode integration build.
listOf("linkDebugTestIosSimulatorArm64", "iosSimulatorArm64Test").forEach { taskName ->
    tasks.named(taskName) {
        enabled = false
    }
}