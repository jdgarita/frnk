@file:Suppress("UnstableApiUsage")

pluginManagement {
    repositories {
        google { content { includeGroupByRegex(".*google.*"); includeGroupByRegex(".*android.*") } }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }
}

plugins { id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0" }

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

rootProject.name = "frnk"

include(
    ":core-utils",
    ":core-ui-api",
    ":core-ui-atoms",
    ":core-database-api",
    ":core-database-impl",
    ":core-backend-api",
    ":core-backend-firebase",
    ":core-backend-supabase",
    ":core-monetization-api",
    ":core-monetization-revenuecat",
    ":androidApp",
    ":iosApp",
    ":androidDemoApp",
)
