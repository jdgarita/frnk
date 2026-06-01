plugins {
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.compose.multiplatform) apply false
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.kotlin.multiplatform.library) apply false
    alias(libs.plugins.sqldelight) apply false
    alias(libs.plugins.ktlint) apply false
    alias(libs.plugins.google.services) apply false
    alias(libs.plugins.firebase.crashlytics) apply false
}

allprojects {
    apply(
        plugin =
            rootProject.libs.plugins.ktlint
                .get()
                .pluginId,
    )
    extensions.configure<org.jlleitschuh.gradle.ktlint.KtlintExtension> {
        version.set("1.3.1")
        ignoreFailures.set(false)
        filter { exclude { it.file.path.contains("build/") } }
    }
}

// Points git at .githooks/ so the pre-commit ktlintFormat hook activates for every clone.
// `git config core.hooksPath` is idempotent — re-runs are no-ops.
val gitDirExists = rootProject.file(".git").exists()
val hooksDirExists = rootProject.file(".githooks").isDirectory
val rootDirPath = rootDir

val installGitHooks by tasks.registering(Exec::class) {
    group = "git hooks"
    description = "Configure git to use .githooks/ as the hooks directory."
    workingDir = rootDirPath
    commandLine("git", "config", "core.hooksPath", ".githooks")
    enabled = gitDirExists && hooksDirExists
}

// Auto-run on IDE sync so contributors don't have to remember a manual step.
tasks
    .matching { it.name == "prepareKotlinBuildScriptModel" }
    .configureEach { dependsOn(installGitHooks) }
