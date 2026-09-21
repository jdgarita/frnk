import io.sentry.android.gradle.extensions.SentryPluginExtension

// Sentry *build* wiring for an Android application host — the half a frnk library module cannot
// reach. frnk supplies the runtime binding (`sentryCrashReportingModule` in `:crash-sentry`); what
// only the host's application module can do is apply Sentry's Android Gradle plugin, which
// records the R8 mapping's UUID in the manifest and uploads the mapping so minified release stack
// traces symbolicate in Sentry. Without it a minified build reports unreadable frames.
//
// The plugin is applied unconditionally — it needs no secret to build — but the UPLOAD runs only
// when `SENTRY_AUTH_TOKEN` is in the environment (the release machine's; never CI's). Org and
// project come from sentry-cli's own sources: `SENTRY_ORG` / `SENTRY_PROJECT` env vars or a
// gitignored `sentry.properties` in the application module.
//
// `autoInstallation` stays OFF: it would add sentry-android at the plugin's own version, above the
// one `sentry-kotlin-multiplatform` bundles, and the two disagree at runtime (KMP issue #450).
// Bytecode instrumentation stays off too — a Compose Multiplatform app gains nothing from the
// OkHttp/Room/Compose-Navigation hooks, and the KMP SDK has no Navigation 3 integration.
pluginManager.withPlugin("com.android.application") {
    apply(plugin = "io.sentry.android.gradle")

    val authToken: String? = System.getenv("SENTRY_AUTH_TOKEN")?.takeIf { it.isNotBlank() }
    if (authToken == null) {
        logger.info("[frnk.android.sentry] ${project.path}: no SENTRY_AUTH_TOKEN — mapping upload skipped.")
    }

    extensions.configure<SentryPluginExtension>("sentry") {
        autoInstallation { enabled.set(false) }
        tracingInstrumentation { enabled.set(false) }
        includeProguardMapping.set(true)
        autoUploadProguardMapping.set(authToken != null)
        authToken?.let(this.authToken::set)
    }
}
