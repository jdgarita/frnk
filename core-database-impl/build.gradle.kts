plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.sqldelight)
}

kotlin {
    androidTarget {
        compilations.all {
            kotlinOptions.jvmTarget = "17"
        }
    }
    listOf(iosX64(), iosArm64(), iosSimulatorArm64()).forEach {
        it.binaries.framework {
            baseName = "core_database_impl"
            isStatic = true
        }
    }
    sourceSets {
        commonMain.dependencies {
            api(project(":core-database-api"))
            api(project(":core-common"))
            implementation(libs.sqldelight.runtime)
            implementation(libs.sqldelight.coroutines)
            implementation(libs.multiplatform.settings)
            implementation(libs.koin.core)
        }
        androidMain.dependencies {
            implementation(libs.sqldelight.android.driver)
            implementation(libs.androidx.datastore.preferences)
        }
        iosMain.dependencies {
            implementation(libs.sqldelight.native.driver)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.turbine)
            implementation(libs.assertk)
        }
    }
}

sqldelight {
    databases {
        create("FrnkDB") {
            packageName.set("dev.jdgarita.frnk.database.sql")
        }
    }
}

android {
    namespace = "dev.jdgarita.frnk.database.impl"
    compileSdk = ProjectConfiguration.Android.COMPILE_SDK
    defaultConfig { minSdk = ProjectConfiguration.Android.MIN_SDK }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
