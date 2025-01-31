enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
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
include(":common")
include(":domain:framework-domain")
include(":presentation:mvi")
include(":presentation:component-core")
include(":presentation:frnk-resources")
include(":util:common")
include(":util:di")
