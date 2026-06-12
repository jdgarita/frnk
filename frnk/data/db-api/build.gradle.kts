plugins {
    id("frnk.kmp.library")
}

kotlin {
    android {
        namespace = "${libs.versions.frnk.groupId.get()}.database.api"
    }
    sourceSets {
        commonMain.dependencies {
            // SqlDriver/SqlSchema appear in the SqlDriverFactory signature.
            api(libs.sqldelight.runtime)
        }
    }
}
