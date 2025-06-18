plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.room)
    alias(libs.plugins.ksp)
}

android {
    namespace = ProjectConfiguration.Frnk.packageName + ".data"
    compileSdk = ProjectConfiguration.Frnk.Android.compileSDK

    defaultConfig {
        minSdk = ProjectConfiguration.Frnk.Android.minSDK
    }

    compileOptions {
        sourceCompatibility = ProjectConfiguration.Compiler.javaCompatibility
        targetCompatibility = ProjectConfiguration.Compiler.javaCompatibility
    }
}

kotlin {
    applyDefaultHierarchyTemplate()

    androidTarget()

    iosX64()
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        commonMain.dependencies {
            implementation(project(":shared:domain"))

            implementation(libs.kotlin.coroutines.core)

            // Tweener
            implementation(libs.tweener.passage)
            implementation(libs.tweener.kmpkit)
            implementation(libs.tweener.firebase)

            // DI
            implementation(libs.koin.core)

            // Ktor
            implementation(libs.bundles.ktor.common)

            // Napier
            implementation(libs.napier)

            // Multiplatform Settings (equivalent to SharedPrefs but for all platforms)
            api(libs.bundles.multiplaform.settings)

            // Room
            api(libs.room.runtime)
            api(libs.sqlite.bundled)

            // RevenueCat
            implementation(libs.bundles.revenuecat)
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
        }

        androidMain.dependencies {
            // Ktor
            implementation(libs.ktor.client.android)
        }

        iosMain.dependencies {
            // Ktor
            implementation(libs.ktor.client.ios)
        }
    }
}

room {
    schemaDirectory("$projectDir/schemas")
}

dependencies {
    add("kspAndroid", libs.room.compiler)
    add("kspIosSimulatorArm64", libs.room.compiler)
    add("kspIosX64", libs.room.compiler)
    add("kspIosArm64", libs.room.compiler)
}
