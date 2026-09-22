// Firebase *build* wiring for an Android application host — the half a frnk library module cannot
// reach. frnk's Firebase-backed runtime bindings are `firebaseIdentityModule` (`:identity-impl`,
// Firebase Auth) and `remoteConfigModule` (`:remote-config-impl`, Remote Config); what only the
// host's application module can do is apply `google-services`, which generates the string resources
// `FirebaseInitProvider` reads to auto-initialize Firebase *before* `Application.onCreate` runs.
//
// Crash reporting is NOT Firebase's job anymore: every host ships Sentry (`:crash-sentry`), whose
// build half is the `frnk.android.sentry` plugin (R8 mapping upload). So no Crashlytics plugin here.
//
// `google-services.json` is deployment-specific and gitignored, so the plugin applies only when it
// is present. CI and fresh clones configure and build without it; the Firebase-backed bindings
// degrade to logged failures at runtime (every gitlive call is `runCatching`-wrapped).
pluginManager.withPlugin("com.android.application") {
    if (!file("google-services.json").exists()) {
        logger.warn(
            "[frnk.android.firebase] ${project.path}: no google-services.json — google-services plugin " +
                "skipped. Firebase Auth / Remote Config will fail at runtime (logged, non-fatal)."
        )
        return@withPlugin
    }

    apply(plugin = "com.google.gms.google-services")
}
