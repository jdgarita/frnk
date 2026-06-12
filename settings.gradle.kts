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
    ":core-mvi",
    ":core-nav",
    ":haptics",
    ":ui-theme",
    ":ui-components",
    ":ui-scaffolds",
    ":ui-bottom-nav",
    ":data-db-api",
    ":data-db-impl",
    ":data-prefs-api",
    ":data-prefs-impl",
    ":analytics-api",
    ":analytics-impl",
    ":monetization-api",
    ":monetization-impl",
    ":shared-monetization-ui",
    ":ui-app",
    ":shared-demo",
    ":androidDemoApp",
)

// Restructure Stage 3: modules physically live at their final layered locations under frnk/
// (core/data/ui/capabilities) + demo/, while most Gradle project NAMES stayed unchanged until the
// rename stages. Composite-build substitution matches group:name, so paths are free to move.
// Stage 9 (the coordinate flip) deleted the two transient facades (shared-ui-api at
// frnk/core/ui-api-facade, shared-ui-atoms at frnk/ui/atoms-facade) and renamed the last three
// still-visible coordinates to their final flat names: :shared-ui-nav → :ui-bottom-nav,
// :shared-monetization-api → :monetization-api, :shared-monetization-revenuecat → :monetization-impl.
// Remaining legacy names (:shared-utils, :shared-monetization-ui, :shared-demo, :androidDemoApp) flip
// in later stages.
mapOf(
    "core-di" to "frnk/core/di",
    "shared-utils" to "frnk/core/util",
    "core-mvi" to "frnk/core/mvi",
    "core-nav" to "frnk/core/nav",
    "haptics" to "frnk/capabilities/haptics",
    "ui-theme" to "frnk/ui/theme",
    "ui-components" to "frnk/ui/components",
    "ui-scaffolds" to "frnk/ui/scaffolds",
    "data-db-api" to "frnk/data/db-api",
    "data-db-impl" to "frnk/data/db-impl",
    "data-prefs-api" to "frnk/data/prefs-api",
    "data-prefs-impl" to "frnk/data/prefs-impl",
    "ui-bottom-nav" to "frnk/ui/bottom-nav",
    "ui-app" to "frnk/ui/app",
    "analytics-api" to "frnk/capabilities/analytics-api",
    "analytics-impl" to "frnk/capabilities/analytics-impl",
    "monetization-api" to "frnk/capabilities/monetization-api",
    "monetization-impl" to "frnk/capabilities/monetization-impl",
    "shared-monetization-ui" to "frnk/capabilities/monetization-ui",
    "shared-demo" to "demo/shared",
    "androidDemoApp" to "demo/android-app",
).forEach { (name, dir) ->
    project(":$name").projectDir = file(dir)
}
