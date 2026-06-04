plugins {
    id("frnk.kmp.library")
    alias(libs.plugins.sqldelight)
}

kotlin {
    android {
        namespace = "${ProjectConfiguration.GROUP_ID}.database.impl"
        withHostTest {}
    }
    sourceSets {
        commonMain.dependencies {
            api(projects.sharedDatabaseApi)
            implementation(libs.koin.core)
            implementation(libs.settings.core)
            implementation(libs.settings.coroutines)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
        }
        androidMain.dependencies { implementation(libs.sqldelight.android.driver) }
        iosMain.dependencies { implementation(libs.sqldelight.native.driver) }
        // The round-trip test runs on the JVM host, so it uses the JDBC SQLite driver
        // (JdbcSqliteDriver.IN_MEMORY). The android/native drivers can't run in a host test.
        getByName("androidHostTest").dependencies { implementation(libs.sqldelight.sqlite.driver) }
    }
}

sqldelight {
    databases {
        create(ProjectConfiguration.DATABASE_NAME) {
            packageName.set(ProjectConfiguration.DATABASE_PACKAGE)
        }
    }
}
