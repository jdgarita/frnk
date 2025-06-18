plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
}

android {
    namespace = ProjectConfiguration.Frnk.packageName + ".domain"
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
            implementation(libs.kotlin.coroutines.core)

            // Tweener
            implementation(libs.tweener.kmpkit)

            // DI
            implementation(libs.koin.core)

            // Napier
            implementation(libs.napier)
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
        }

        iosMain.dependencies {
        }
    }
}
