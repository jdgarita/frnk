import dev.jdgarita.frnk.config.dependencies.Deps
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.serialization)
}

kotlin {
    androidTarget {
        compilations.all {
            compileTaskProvider.configure {
                compilerOptions {
                    jvmTarget.set(JvmTarget.JVM_1_8)
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
            baseName = "ui.framework"
            xcf.add(this)
            isStatic = true
        }
    }

    sourceSets {
        androidMain.dependencies {
            implementation(compose.preview)
        }

        commonMain.dependencies {
            implementation(project(path = Deps.Main.Frnk.presentationMvi))
            implementation(project(path = Deps.Main.Frnk.presentationComponentCore))
            implementation(project(path = Deps.Main.Frnk.presentationTabConfig))
            implementation(project(path = Deps.Main.Frnk.uiComponentLibrary))
            implementation(project(path = Deps.Main.Frnk.presentationFramework))
            implementation(libs.compose.navigation)
            implementation(libs.kotlinx.serialization)
            implementation(libs.compose.backhandler)
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.materialIconsExtended)
            implementation(compose.ui)
            implementation(compose.components.resources)

            implementation(compose.components.uiToolingPreview)
        }
        commonTest.dependencies {
        }
    }
}

android {
    namespace = "dev.jdgarita.frnk.ui.framework"
    compileSdk = 35
    defaultConfig {
        minSdk = 24
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}