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

// The shared library modules physically live under shared/ for project-tree tidiness, while
// keeping flat Gradle paths for the top-level domains that have not yet moved to nested module
// groups. (The :shared aggregator itself was deleted at restructure Stage 1 — the path segment
// survives only as the implicit, build-file-less parent of :shared:backend:* until Stage 5
// re-flattens those.)
listOf(
    "core-di",
    "shared-utils",
    "shared-ui-api",
    "shared-ui-atoms",
    "shared-ui-nav",
    "shared-database-api",
    "shared-database-impl",
    "shared-monetization-api",
    "shared-monetization-revenuecat",
    "shared-monetization-ui",
    "ui-app",
    "shared-demo",
).forEach { name ->
    project(":$name").projectDir = file("shared/$name")
}
