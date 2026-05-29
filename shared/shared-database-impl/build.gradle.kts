plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.sqldelight)
}

kotlin {
    jvmToolchain(17)
    android {
        namespace = "${ProjectConfiguration.GROUP_ID}.database.impl"
        compileSdk = ProjectConfiguration.COMPILE_SDK
        minSdk = ProjectConfiguration.MIN_SDK
        withHostTest {}
    }
    listOf(iosArm64(), iosSimulatorArm64()).forEach { it.binaries.framework { baseName = "shared_database_impl" } }
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
