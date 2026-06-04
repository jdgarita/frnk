import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.getByType

// frnk KMP-library + host-test convention plugin.
//
// Composes the `frnk.kmp.library` base with the standard JVM host-test setup every tested module shares:
// the `withHostTest {}` opt-in plus the `commonTest` `kotlin-test` + `kotlinx-coroutines-test` deps. A
// tested module declares `plugins { id("frnk.kmp.library.hosttest") }` instead of repeating the
// `withHostTest {}` line and the identical `commonTest.dependencies { … }` block. Compose-bearing tested
// modules apply this alongside `id("frnk.kmp.library.compose")` (plugin application is idempotent, so the
// base plugin is still applied exactly once).
//
// Modules that DON'T fit stay on the base `frnk.kmp.library`: :shared-ui-atoms (needs the
// `withHostTest { isIncludeAndroidResources = true }` variant + an androidHostTest source set, not
// commonTest) and :shared-backend-supabase (no tests).

plugins {
    id("frnk.kmp.library")
}

// Catalog lookup by dashed alias mirrors the base plugin's `findVersion("android-compileSdk")`.
private val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

kotlin {
    android {
        withHostTest {}
    }
    sourceSets {
        commonTest.dependencies {
            implementation(libs.findLibrary("kotlin-test").get())
            implementation(libs.findLibrary("kotlinx-coroutines-test").get())
        }
    }
}
