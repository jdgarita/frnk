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