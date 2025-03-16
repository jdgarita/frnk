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
include(":ui:component-library")
include(":domain:framework-domain")
include(":presentation:mvi")
include(":presentation:component-core")
include(":presentation:frnk-resources")
include(":util:common")
include(":util:di")
include(":data:framework-data")
include(":subs")
include(":presentation:framework-presentation")
include(":presentation:home-presentation-api")
include(":presentation:tab-config")
include(":sdk")
include(":domain:config-domain")
