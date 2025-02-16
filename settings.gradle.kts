enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

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
include(":app")
include(":ui:framework-ui")
include(":domain:framework-domain")
include(":presentation:mvi")
include(":presentation:component-core")
include(":presentation:frnk-resources")
include(":util:common")
include(":util:di")