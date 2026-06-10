import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.getByType

plugins {
    id("frnk.kmp.library.hosttest")
}

private val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

kotlin {
    android {
        namespace = "dev.jdgarita.frnk.backend.api"
    }
    sourceSets {
        commonMain.dependencies {
            api(project(":shared-utils"))
            api(libs.findLibrary("kotlinx-coroutines-core").get())
        }
    }
}
