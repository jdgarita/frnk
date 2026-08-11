import org.gradle.plugin.use.PluginDependency

plugins { `kotlin-dsl` }

repositories {
    mavenCentral()
    google()
    gradlePluginPortal()
}

// Plugin-marker dependencies so the precompiled convention plugins can apply these plugins:
//  - frnk.kmp.library         → Kotlin Multiplatform + AGP-9 KMP-Android-library
//  - frnk.kmp.library.compose → + Compose Multiplatform plugin pair
//  - frnk.android.firebase    → google-services + Crashlytics for an application host
// Versions resolve from the shared catalog (imported via settings.gradle.kts) — no version duplication.
dependencies {
    implementation(libs.plugins.kotlin.multiplatform.toMarker())
    implementation(libs.plugins.android.kotlin.multiplatform.library.toMarker())
    implementation(libs.plugins.compose.multiplatform.toMarker())
    implementation(libs.plugins.kotlin.compose.toMarker())
    implementation(libs.plugins.kotlin.serialization.toMarker())
    implementation(libs.plugins.google.services.toMarker())
    implementation(libs.plugins.firebase.crashlytics.toMarker())
}

// A plugin id `x.y.z` is published with the marker artifact `x.y.z:x.y.z.gradle.plugin:<version>`.
// This converts a catalog `[plugins]` alias into that marker coordinate so it can be a normal dep.
fun Provider<PluginDependency>.toMarker(): Provider<String> =
    map { "${it.pluginId}:${it.pluginId}.gradle.plugin:${it.version}" }
