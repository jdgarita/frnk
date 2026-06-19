import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.getByType

// frnk KMP-library + Compose convention plugin.
//
// Composes the `frnk.kmp.library` base with the Compose Multiplatform plugin pair AND the baseline
// `api(compose.runtime/foundation/ui)` exports every Compose module needs — so a Compose-bearing module
// declares `plugins { id("frnk.kmp.library.compose") }` instead of repeating the base id, the
// `compose.multiplatform` + `kotlin.compose` aliases, and the three `api(compose.*)` lines. Plugin
// application is idempotent, so a module can also apply `id("frnk.kmp.library.hosttest")` alongside this —
// the base plugin is still applied once.

plugins {
    id("frnk.kmp.library")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
}

// The Compose deps are pinned from the shared catalog (the `compose-multiplatform` version) and exposed
// as `api` so they're on every Compose module's public surface uniformly (rather than each module
// re-declaring them, or relying on a transitive export from a sibling). Catalog lookup by dashed alias
// mirrors the sibling plugins (`frnk.kmp.library.hosttest`, `…composehosttest`) — precompiled script
// plugins don't get type-safe `libs.*` accessors, so the catalog is resolved manually.
private val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(libs.findLibrary("compose-runtime").get())
            api(libs.findLibrary("compose-foundation").get())
            api(libs.findLibrary("compose-ui").get())
        }
    }
}
