import dev.jdgarita.frnk.config.dependencies.Deps
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
}

kotlin {
    androidTarget {
        compilations.all {
            compileTaskProvider.configure {
                compilerOptions {
                    jvmTarget.set(JvmTarget.JVM_11)
                }
            }
        }
    }

    val xcf = XCFramework()
    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach {
        it.binaries.framework {
            baseName = "app"
            xcf.add(this)
            isStatic = true
        }
    }

    sourceSets {
        all {
            languageSettings.optIn("kotlinx.cinterop.ExperimentalForeignApi")
        }
        commonMain.dependencies {
            api(project(path = Deps.Main.Frnk.presentationFramework))
            api(project(path = Deps.Main.Frnk.presentationHomeApi))
            api(project(path = Deps.Main.Frnk.presentationIdentity))
            api(project(path = Deps.Main.Frnk.uiFramework))
            api(project(path = Deps.Main.Frnk.utilDi))
            api(project(path = Deps.Main.Frnk.subs))
            api(project(path = Deps.Main.Frnk.sdk))
            api(project(path = Deps.Main.Frnk.presentationMvi))
            api(project(path = Deps.Main.Frnk.domainFramework))
            api(project(path = Deps.Main.Frnk.presentationComponentCore))
            api(project(path = Deps.Main.Frnk.presentationFrnkResources))
            api(project(path = Deps.Main.Frnk.domainConfig))

            implementation(project(path = Deps.Main.Frnk.dataFramework))
            implementation(project(path = Deps.Main.Frnk.uiComponentLibrary))
            implementation(project(path = Deps.Main.Frnk.utilCommon))

            implementation(libs.koin.core)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        androidMain.dependencies {
            implementation(libs.sentry.kotlin.multiplatform)
        }
    }
}

android {
    namespace = "dev.jdgarita.frnk"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}