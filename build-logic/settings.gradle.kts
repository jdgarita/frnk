// build-logic is a standalone included build that publishes frnk's Gradle convention plugins.
// It is consumed by the root build via `includeBuild("build-logic")` in pluginManagement, so the
// plugins it contributes are version-checked through the plugin DSL and their classpath is attached
// only to the projects that actually apply them — unlike buildSrc, which leaks onto every project's
// buildscript classpath and clashes with `alias(...) apply false` version requests.
rootProject.name = "build-logic"

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        google()
        gradlePluginPortal()
    }
    // Reuse the project's single source of truth for plugin/SDK versions.
    versionCatalogs {
        create("libs") {
            from(files("../gradle/libs.versions.toml"))
        }
    }
}
