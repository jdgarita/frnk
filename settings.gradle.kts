@file:Suppress("UnstableApiUsage")

pluginManagement {
    // frnk's Gradle convention plugins (frnk.kmp.library, …) live in a standalone included build so
    // their plugin classpath attaches only to projects that apply them — avoiding the buildSrc
    // whole-build classpath leak that clashes with `alias(...) apply false` version requests.
    includeBuild("build-logic")
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
    ":core-di",
    ":shared-utils",
    ":shared-ui-api",
    ":shared-ui-atoms",
    ":shared-ui-nav",
    ":shared-database-api",
    ":shared-database-impl",
    ":shared:backend",
    ":shared:backend:api",
    ":shared:backend:firebase",
    ":shared-monetization-api",
    ":shared-monetization-revenuecat",
    ":shared-monetization-ui",
    ":ui-app",
    ":shared-demo",
    ":androidDemoApp",
)

// Restructure Stage 3: modules physically live at their final layered locations under frnk/
// (core/data/ui/capabilities) + demo/, while every Gradle project NAME stays unchanged until the
// rename stages (5, 9, 10). Composite-build substitution matches group:name, so paths are free
// to move. Modules that later split (shared-ui-api, shared-ui-atoms, shared-database-*) are
// parked at the directory of their main successor per docs/RESTRUCTURE_PLAN.md §3.
mapOf(
    "core-di" to "frnk/core/di",
    "shared-utils" to "frnk/core/util",
    "shared-ui-api" to "frnk/core/mvi",
    "shared-database-api" to "frnk/data/db-api",
    "shared-database-impl" to "frnk/data/db-impl",
    "shared-ui-atoms" to "frnk/ui/components",
    "shared-ui-nav" to "frnk/ui/bottom-nav",
    "ui-app" to "frnk/ui/app",
    "shared-monetization-api" to "frnk/capabilities/monetization-api",
    "shared-monetization-revenuecat" to "frnk/capabilities/monetization-impl",
    "shared-monetization-ui" to "frnk/capabilities/monetization-ui",
    "shared-demo" to "demo/shared",
    "androidDemoApp" to "demo/android-app",
).forEach { (name, dir) ->
    project(":$name").projectDir = file(dir)
}

// :shared and :shared:backend are build-file-less hull projects that survive only as the Gradle
// path parents of :shared:backend:{api,firebase} until Stage 5 re-flattens those to
// :analytics-api/:analytics-impl. Their default projectDirs (shared/, shared/backend/) no longer
// exist, so park them on existing, build-file-free directories to keep Gradle happy.
project(":shared").projectDir = file("frnk")
project(":shared:backend").projectDir = file("frnk/capabilities")
project(":shared:backend:api").projectDir = file("frnk/capabilities/analytics-api")
project(":shared:backend:firebase").projectDir = file("frnk/capabilities/analytics-impl")
