import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.getByType

plugins {
    id("frnk.backend.impl")
    id("frnk.kmp.library.hosttest")
}

private val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

kotlin {
    android {
        namespace = "dev.jdgarita.frnk.backend.firebase"
    }
    sourceSets {
        commonMain.dependencies {
            implementation(libs.findLibrary("firebase-auth").get())
            implementation(libs.findLibrary("firebase-firestore").get())
            implementation(libs.findLibrary("firebase-analytics").get())
            implementation(libs.findLibrary("firebase-crashlytics").get())
        }
        // gitlive-firebase 2.x delegates Android transitive versions to the Firebase BOM.
        androidMain.dependencies {
            implementation(project.dependencies.platform(libs.findLibrary("firebase-bom").get()))
        }
        // CrashKiOS is iOS-only: it installs the Kotlin/Native unhandled-exception hook that
        // forwards uncaught Kotlin crashes to Crashlytics symbolicated.
        iosMain.dependencies {
            implementation(libs.findLibrary("crashkios-crashlytics").get())
        }
    }
}
