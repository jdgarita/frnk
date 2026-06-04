plugins {
    id("frnk.kmp.library")
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    android {
        namespace = "${ProjectConfiguration.GROUP_ID}.backend.supabase"
    }
    sourceSets {
        commonMain.dependencies {
            api(projects.sharedBackendApi)
            implementation(libs.koin.core)
            implementation(libs.supabase.postgrest)
            implementation(libs.supabase.auth)
            implementation(libs.supabase.storage)
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.client.serialization)
        }
        androidMain.dependencies { implementation(libs.ktor.client.android) }
        iosMain.dependencies { implementation(libs.ktor.client.darwin) }
    }
}
