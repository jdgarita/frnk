import org.gradle.plugin.use.PluginDependency

plugins { `kotlin-dsl` }

repositories {
    mavenCentral()
    google()
    gradlePluginPortal()
}

// Plugin-marker dependencies so the precompiled `frnk.kmp.library` convention plugin can apply the
// Kotlin Multiplatform + AGP-9 KMP-Android-library plugins. Versions resolve from the shared catalog
// (imported via settings.gradle.kts) — no version duplication.
dependencies {
    implementation(libs.plugins.kotlin.multiplatform.toMarker())
    implementation(libs.plugins.android.kotlin.multiplatform.library.toMarker())
}

// A plugin id `x.y.z` is published with the marker artifact `x.y.z:x.y.z.gradle.plugin:<version>`.
// This converts a catalog `[plugins]` alias into that marker coordinate so it can be a normal dep.
fun Provider<PluginDependency>.toMarker(): Provider<String> =
    map { "${it.pluginId}:${it.pluginId}.gradle.plugin:${it.version}" }
