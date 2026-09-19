plugins {
    id("frnk.kmp.library.hosttest")
}

kotlin {
    android {
        namespace = "${libs.versions.frnk.groupId.get()}.database.api"
    }
    sourceSets {
        commonMain.dependencies {
            // RoomDatabase / RoomDatabase.Builder appear in the DatabaseFactory signature.
            api(libs.androidx.room.runtime)
            // Module receiver of the inline databaseSingle helper — in its public signature.
            api(libs.koin.core)
        }
        androidMain.dependencies {
            // DatabaseContext — the bootstrap-owned Android Context seam Room's builder needs.
            implementation(projects.coreDi)
        }
    }
}