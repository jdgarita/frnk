import gradle.tasks.renameProject.RenameProjectTask

plugins {
    alias(libs.plugins.android.application).apply(false)
    alias(libs.plugins.android.library).apply(false)
    alias(libs.plugins.jetbrains.compose).apply(false)
    alias(libs.plugins.jetbrains.compose.compiler).apply(false)
    alias(libs.plugins.kotlin.android).apply(false)
    alias(libs.plugins.kotlin.multiplatform).apply(false)
    alias(libs.plugins.kotlin.serialization).apply(false)
    alias(libs.plugins.google.services).apply(false)
    alias(libs.plugins.firebase.crashlytics).apply(false)
    alias(libs.plugins.firebase.performance).apply(false)
    alias(libs.plugins.ksp).apply(false)
    alias(libs.plugins.google.play.publisher).apply(false)
}

tasks.register("clean", Delete::class) {
    delete(rootProject.layout.buildDirectory)
}

tasks.register<RenameProjectTask>("renameProject") {
    projectName.set(project.findProperty("projectName")?.toString())
    packageName.set(project.findProperty("packageName")?.toString())
    dryRun.set(project.hasProperty("dryRun"))
    projectDir.set(layout.projectDirectory)
}
