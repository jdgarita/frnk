enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

apply(from = "settings.shared.gradle.kts")

pluginManagement {
    includeBuild("config")
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Frnk"