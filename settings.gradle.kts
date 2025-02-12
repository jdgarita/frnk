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
include(":frnk:common")
include(":frnk:ui:framework-ui")
include(":frnk:domain:framework-domain")
include(":frnk:presentation:mvi")
include(":frnk:presentation:component-core")
include(":frnk:presentation:frnk-resources")
include(":frnk:util:common")
include(":frnk:util:di")
