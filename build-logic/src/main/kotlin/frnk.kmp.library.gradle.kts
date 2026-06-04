import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.getByType

// frnk KMP-library convention plugin.
//
// Centralizes the build config every standard `shared-*` library module shares, so modules don't
// each repeat the toolchain / SDK / iOS-framework boilerplate. A module applies this with
// `plugins { id("frnk.kmp.library") }` and then only declares what is genuinely module-specific:
// its Android `namespace`, `withHostTest {}` opt-in, any extra plugins (compose / serialization /
// sqldelight), and its source-set dependencies.
//
// What stays out of here on purpose:
//  - `namespace` — every module differs and it doesn't map 1:1 from the module name
//    (e.g. :shared-ui-nav → ...ui.bottomnav), so it's set per-module.
//  - `withHostTest {}` — most but not all modules opt in, and :shared-ui-atoms needs the
//    `isIncludeAndroidResources = true` variant; configuring it twice would double-register.
//  - :shared-demo — it ships a custom `XCFramework("DemoKit")` with explicit exports + linkerOpts,
//    so it keeps its hand-written build and does NOT apply this plugin.

plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("com.android.kotlin.multiplatform.library")
}

// SDK targets come from the shared version catalog (gradle/libs.versions.toml) — the single source of
// truth, also read by the special modules' build scripts and inherited by composite-build host apps.
private val versionCatalog = extensions.getByType<VersionCatalogsExtension>().named("libs")
private val catalogCompileSdk = versionCatalog.findVersion("android-compileSdk").get().requiredVersion.toInt()
private val catalogMinSdk = versionCatalog.findVersion("android-minSdk").get().requiredVersion.toInt()

kotlin {
    jvmToolchain(17)

    android {
        compileSdk = catalogCompileSdk
        minSdk = catalogMinSdk
    }

    // iOS framework baseName mirrors the long-standing convention `shared_<module>` (underscores),
    // which for every standard module is exactly the Gradle module name with `-` → `_`.
    val frameworkBaseName = project.name.replace('-', '_')
    listOf(iosArm64(), iosSimulatorArm64()).forEach { target ->
        target.binaries.framework { baseName = frameworkBaseName }
    }
}
