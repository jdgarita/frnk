enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
        mavenLocal()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        mavenLocal()
    }
}

rootProject.name = "KMPShip"
include(":androidApp")
include(":shared")
include(":shared:data")
include(":shared:domain")
include(":shared:presentation")
include(":shared:core")
