pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

plugins {
    // Auto-provisions JDK 17 from the Foojay disco API when the JVM toolchain
    // requests a JDK Gradle can't find locally. Keeps Android Studio (bundled
    // JDK 21) and the CLI in sync without forcing a system-wide install.
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }
}

rootProject.name = "frnk"

// --- core ------------------------------------------------------------------
include(":core-common")
include(":core-network-api")
include(":core-network-impl")
include(":core-database-api")
include(":core-database-impl")
include(":core-ui-atoms")

// --- public entry points ---------------------------------------------------
include(":androidApp")
include(":iosApp")

// --- internal demos --------------------------------------------------------
include(":androidDemoApp")
include(":iosDemoApp")
