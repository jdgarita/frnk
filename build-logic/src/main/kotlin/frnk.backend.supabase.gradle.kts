import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.getByType

plugins {
    id("frnk.backend.impl")
}

private val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

kotlin {
    android {
        namespace = "dev.jdgarita.frnk.backend.supabase"
    }
    sourceSets {
        commonMain.dependencies {
            implementation(libs.findLibrary("supabase-postgrest").get())
            implementation(libs.findLibrary("supabase-auth").get())
            implementation(libs.findLibrary("supabase-storage").get())
            implementation(libs.findLibrary("ktor-client-core").get())
            implementation(libs.findLibrary("ktor-client-content-negotiation").get())
            implementation(libs.findLibrary("ktor-client-serialization").get())
        }
        androidMain.dependencies {
            implementation(libs.findLibrary("ktor-client-android").get())
        }
        iosMain.dependencies {
            implementation(libs.findLibrary("ktor-client-darwin").get())
        }
    }
}
