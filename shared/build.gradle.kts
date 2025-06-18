import com.codingfeline.buildkonfig.compiler.FieldSpec.Type.BOOLEAN
import com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING
import gradle.Secrets
import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.jetbrains.compose)
    alias(libs.plugins.jetbrains.compose.compiler)
    alias(libs.plugins.buildkonfig)
}

android {
    namespace = ProjectConfiguration.MyProject.packageName
    compileSdk = ProjectConfiguration.MyProject.Android.compileSDK

    defaultConfig {
        minSdk = ProjectConfiguration.MyProject.Android.minSDK
    }

    buildFeatures {
        compose = true
    }

    compileOptions {
        sourceCompatibility = ProjectConfiguration.Compiler.javaCompatibility
        targetCompatibility = ProjectConfiguration.Compiler.javaCompatibility
    }
}

kotlin {
    applyDefaultHierarchyTemplate()

    androidTarget()

    // region iOS configuration

    val xcFramework = XCFramework()

    listOf(
        iosX64(), iosArm64(), iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "shared"
            isStatic = true
            xcFramework.add(this)

            export(project(":shared:core"))
            export(libs.tweener.passage)
        }
    }

    // endregion iOS configuration

    sourceSets {
        commonMain.dependencies {
            api(project(":shared:data"))
            api(project(":shared:domain"))
            api(project(":shared:presentation"))
            api(project(":shared:core"))

            implementation(libs.kotlin.coroutines.core)

            // Tweener
            implementation(libs.tweener.passage)
            implementation(libs.tweener.kmpkit)
            implementation(libs.tweener.firebase)
            implementation(libs.tweener.alarmee)

            // DI
            implementation(libs.koin.core)

            // RevenueCat
            implementation(libs.bundles.revenuecat)
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
        }

        androidMain.dependencies {
            // DI
            implementation(libs.koin.android)

            // Preferences
            implementation(libs.android.preferences)

            // Compose
            implementation(compose.ui)
        }
    }
}

buildkonfig {
    packageName = ProjectConfiguration.MyProject.packageName

    // Release
    defaultConfigs {
        buildConfigField(BOOLEAN, "DEBUG", "false")
        buildConfigField(STRING, "VERSION_NAME", ProjectConfiguration.MyProject.versionName)
        buildConfigField(STRING, "APPLICATION_ID", ProjectConfiguration.MyProject.Android.applicationId)

        buildConfigField(STRING, "GOOGLE_SIGN_IN_WEB_CLIENT_ID", Secrets.getLocalPropertyOrEnvVar("GOOGLE_SIGN_IN_WEB_CLIENT_ID", rootDir) ?: "")

        buildConfigField(STRING, "REVENUECAT_ANDROID_API_KEY", Secrets.getLocalPropertyOrEnvVar("REVENUECAT_ANDROID_API_KEY", rootDir) ?: "tmp")
        buildConfigField(STRING, "REVENUECAT_IOS_API_KEY", Secrets.getLocalPropertyOrEnvVar("REVENUECAT_IOS_API_KEY", rootDir) ?: "tmp")
    }

    // Debug
    defaultConfigs("debug") {
        buildConfigField(BOOLEAN, "DEBUG", "true")
    }
}
