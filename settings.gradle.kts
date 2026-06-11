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
    ":shared-ui-api",
    ":ui-theme",
    ":shared-ui-atoms",
    ":shared-ui-nav",
    ":data-db-api",
    ":data-db-impl",
    ":data-prefs-api",
    ":data-prefs-impl",
    ":analytics-api",
    ":analytics-impl",
    ":shared-monetization-api",
    ":shared-monetization-revenuecat",
    ":shared-monetization-ui",
    ":ui-app",
    ":shared-demo",
    ":androidDemoApp",
)

// Restructure Stage 3: modules physically live at their final layered locations under frnk/
// (core/data/ui/capabilities) + demo/, while every Gradle project NAME stays unchanged until the
// rename stages (9, 10) — except the data modules (final names since the Stage 4 split) and the
// analytics pair (re-flattened from :shared:backend:* at Stage 5, per OQ-6). Composite-build
// substitution matches group:name, so paths are free to move. Modules that later split
// (shared-ui-atoms) are parked at the directory of their main successor per docs/RESTRUCTURE_PLAN.md §3.
// Stage 6 split shared-ui-api → :core-mvi (kept frnk/core/mvi) + :core-nav + :haptics; shared-ui-api is
// now a src-less facade parked at frnk/core/ui-api-facade (deleted at Stage 9).
mapOf(
    "core-di" to "frnk/core/di",
    "shared-utils" to "frnk/core/util",
    "core-mvi" to "frnk/core/mvi",
    "core-nav" to "frnk/core/nav",
    "haptics" to "frnk/capabilities/haptics",
    "shared-ui-api" to "frnk/core/ui-api-facade",
    "ui-theme" to "frnk/ui/theme",
    "data-db-api" to "frnk/data/db-api",
    "data-db-impl" to "frnk/data/db-impl",
    "data-prefs-api" to "frnk/data/prefs-api",
    "data-prefs-impl" to "frnk/data/prefs-impl",
    "shared-ui-atoms" to "frnk/ui/components",
    "shared-ui-nav" to "frnk/ui/bottom-nav",
    "ui-app" to "frnk/ui/app",
    "analytics-api" to "frnk/capabilities/analytics-api",
    "analytics-impl" to "frnk/capabilities/analytics-impl",
    "shared-monetization-api" to "frnk/capabilities/monetization-api",
    "shared-monetization-revenuecat" to "frnk/capabilities/monetization-impl",
    "shared-monetization-ui" to "frnk/capabilities/monetization-ui",
    "shared-demo" to "demo/shared",
    "androidDemoApp" to "demo/android-app",
).forEach { (name, dir) ->
    project(":$name").projectDir = file(dir)
}
