// Firebase *build* wiring for an Android application host — the half a frnk library module cannot
// reach. frnk already supplies the runtime bindings (`firebaseObservabilityModule` in
// `:analytics-impl`, over the `AnalyticsTracker`/`CrashReporter` contracts in `:analytics-api`);
// what only the host's application module can do is apply:
//
//  - `google-services`, which generates the string resources `FirebaseInitProvider` reads to
//    auto-initialize Firebase *before* `Application.onCreate` runs, and
//  - `firebase-crashlytics`, which uploads R8 mapping files so minified release stack traces
//    deobfuscate in the Crashlytics console. Without it a minified build reports unreadable frames.
//
// The host passes nothing, and there is nothing to tune: Crashlytics collection defaults to on for
// every build type, and mapping upload defaults to on wherever a mapping actually exists (release —
// debug isn't minified, so the upload task is never even created).
//
// `google-services.json` is deployment-specific and gitignored, so both plugins apply only when it
// is present. CI and fresh clones configure and build without it, and frnk's `runCatching`-wrapped
// reporter degrades to a logged no-op at runtime.
pluginManager.withPlugin("com.android.application") {
    if (!file("google-services.json").exists()) {
        logger.warn(
            "[frnk.android.firebase] ${project.path}: no google-services.json — Firebase build " +
                "plugins skipped. Crash reporting will no-op at runtime."
        )
        return@withPlugin
    }

    apply(plugin = "com.google.gms.google-services")
    apply(plugin = "com.google.firebase.crashlytics")
}
