import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.getByType

plugins {
    id("frnk.kmp.library")
    id("org.jetbrains.kotlin.plugin.serialization")
}

private val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(project(":shared:backend:api"))
            implementation(libs.findLibrary("koin-core").get())
        }
    }
}
