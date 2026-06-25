plugins {
    id("frnk.kmp.library.hosttest")
}

kotlin {
    android {
        namespace = "${libs.versions.frnk.groupId.get()}.database.api"
    }
    sourceSets {
        commonMain.dependencies {
            // SqlDriver/SqlSchema appear in the SqlDriverFactory signature.
            api(libs.sqldelight.runtime)
            // Module receiver of the inline databaseSingle helper — in its public signature.
            api(libs.koin.core)
        }
    }
}