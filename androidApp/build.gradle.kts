import gradle.Secrets
import gradle.tasks.bumpVersionCode.BumpVersionCodeTask

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.google.services)
    alias(libs.plugins.firebase.crashlytics)
    alias(libs.plugins.firebase.performance)
    alias(libs.plugins.jetbrains.compose.compiler)
    alias(libs.plugins.google.play.publisher)
}

android {
    namespace = ProjectConfiguration.Frnk.Android.namespace
    compileSdk = ProjectConfiguration.Frnk.Android.compileSDK

    defaultConfig {
        applicationId = ProjectConfiguration.Frnk.Android.applicationId
        minSdk = ProjectConfiguration.Frnk.Android.minSDK
        targetSdk = ProjectConfiguration.Frnk.Android.targetSDK
        versionCode = Secrets.getVersionPropertyOrEnvVar(key = BumpVersionCodeTask.VERSION_CODE_PROPERTY, rootDir = rootDir)!!.toInt()
        versionName = ProjectConfiguration.Frnk.versionName
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    signingConfigs {
        create("default") {
            storeFile = file("$projectDir/config/keystore/my_app.keystore")
            keyAlias = Secrets.getLocalPropertyOrEnvVar(key = "KEY_ALIAS_RELEASE", rootDir = rootDir)
            keyPassword = Secrets.getLocalPropertyOrEnvVar(key = "KEY_PASSWORD_RELEASE", rootDir = rootDir)
            storePassword = Secrets.getLocalPropertyOrEnvVar(key = "STORE_PASSWORD_RELEASE", rootDir = rootDir)
        }
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            isDebuggable = false

            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }

        getByName("debug") {
            isDebuggable = true
        }
    }

    compileOptions {
        sourceCompatibility = ProjectConfiguration.Compiler.javaCompatibility
        targetCompatibility = ProjectConfiguration.Compiler.javaCompatibility

        isCoreLibraryDesugaringEnabled = true
    }

    kotlinOptions {
        jvmTarget = ProjectConfiguration.Compiler.jvmTarget
    }
}

dependencies {
    implementation(project(":shared"))

    coreLibraryDesugaring(libs.android.desugarjdklibs)

    // Tweener
    implementation(libs.tweener.czan)

    // Android
    implementation(libs.android.splashscreen)
    implementation(libs.android.activity)
    implementation(libs.android.activity.compose)
    implementation(libs.android.accompanist.permissions)

    // Firebase
    implementation(platform(libs.android.firebase.bom))
    implementation(libs.android.firebase.analytics)
    implementation(libs.android.firebase.performance)

    // DI
    implementation(libs.koin.core)
    implementation(libs.koin.android)
}

// region Google Play Publisher

tasks.register<BumpVersionCodeTask>("bumpVersionCode") {
    versionPropertiesFile.set(rootProject.layout.projectDirectory.file(BumpVersionCodeTask.VERSION_PROPERTIES_FILE))
}

afterEvaluate {
    tasks.named("publishReleaseBundle") {
        dependsOn("bumpVersionCode")
    }
}

play {
    serviceAccountCredentials.set(file("google-play-uploader.json"))
    track.set("internal") // or beta, production
    defaultToAppBundles.set(true)
}

// endregion Google Play Publisher
