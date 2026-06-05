import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.getByType

// frnk KMP **core** convention plugin — the build config shared by every KMP-Android-library module of
// this project, regardless of its iOS shape: the Kotlin Multiplatform + AGP-9 KMP-Android-library
// plugins, `jvmToolchain(17)`, and the Android `compileSdk`/`minSdk` (from the catalog single source).
//
// It declares NO iOS targets and NO framework, so it serves both:
//  - the 12 standard `shared-*` modules via `frnk.kmp.library` (which applies this + adds the iOS
//    framework), and
//  - the special modules with a different iOS shape that previously hand-wrote this same toolchain/SDK
//    boilerplate: `:shared` (bare iOS targets, aggregated into FrnkKit by :iosApp — must NOT declare its
//    own framework), `:shared-demo` (a custom `XCFramework("DemoKit")`), and `:androidApp` (no iOS
//    targets at all). They apply `id("frnk.kmp.base")` and add only their own iOS/framework shape.

plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("com.android.kotlin.multiplatform.library")
}

private val versionCatalog = extensions.getByType<VersionCatalogsExtension>().named("libs")
private val catalogCompileSdk = versionCatalog.findVersion("android-compileSdk").get().requiredVersion.toInt()
private val catalogMinSdk = versionCatalog.findVersion("android-minSdk").get().requiredVersion.toInt()

// Advertise the Maven group so host apps consuming frnk as a Gradle composite build
// (`includeBuild("frnk")`) can substitute `dev.jdgarita.frnk:<module>` dependencies with the
// matching included-build project. Without a group, substitution can't match the coordinate and
// the host fails with "Could not find dev.jdgarita.frnk:<module>".
//
// Hard-coded (not ProjectConfiguration.GROUP_ID) because this `build-logic` convention plugin is a
// standalone included build that cannot see `buildSrc`. Keep in sync with
// ProjectConfiguration.GROUP_ID.
group = "dev.jdgarita.frnk"

kotlin {
    jvmToolchain(17)
    android {
        compileSdk = catalogCompileSdk
        minSdk = catalogMinSdk
    }
}
