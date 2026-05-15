@file:Suppress("UnstableApiUsage")

pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex(".*google.*")
                includeGroupByRegex(".*android.*")
            }
        }
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
    ":shared",
    ":shared-utils",
    ":shared-ui-api",
    ":shared-ui-atoms",
    ":shared-database-api",
    ":shared-database-impl",
    ":shared-backend-api",
    ":shared-backend-firebase",
    ":shared-backend-supabase",
    ":shared-monetization-api",
    ":shared-monetization-revenuecat",
    ":shared-demo",
    ":androidApp",
    ":iosApp",
    ":androidDemoApp",
)
