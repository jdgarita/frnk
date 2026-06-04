plugins {
    id("frnk.kmp.library")
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    android {
        namespace = "${ProjectConfiguration.GROUP_ID}.backend.firebase"
        withHostTest {}
    }
    sourceSets {
        commonMain.dependencies {
            api(projects.sharedBackendApi)
            implementation(libs.koin.core)
            implementation(libs.firebase.auth)
            implementation(libs.firebase.firestore)
            implementation(libs.firebase.analytics)
            implementation(libs.firebase.crashlytics)
        }
        // gitlive-firebase 2.x delegates Android transitive versions to the Firebase BOM.
        androidMain.dependencies { implementation(project.dependencies.platform(libs.firebase.bom)) }
        // CrashKiOS is iOS-only: it installs the Kotlin/Native unhandled-exception hook that
        // forwards uncaught Kotlin crashes to Crashlytics symbolicated. Android needs nothing
        // (the Crashlytics Android SDK already hooks uncaught JVM exceptions). It has no JVM
        // variant, so it must stay out of commonMain (would break compileAndroidMain).
        iosMain.dependencies { implementation(libs.crashkios.crashlytics) }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}
