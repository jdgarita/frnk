# :shared:backend:firebase

Firebase implementation of `:shared:backend:api`. Bundled inside `:shared` and selected at runtime via `BackendChoice.Firebase`.

## Contents

- `FirebaseAuthService.kt` — `AuthService` over Firebase Auth.
- `FirestoreRemoteData.kt` — `RemoteData` over Firestore.
- `FirebaseAnalyticsTracker.kt` — analytics via Firebase Analytics (gitlive `firebase-analytics`).
- `FirebaseCrashReporter.kt` — crash reporting via Firebase Crashlytics (gitlive `firebase-crashlytics`). Every SDK call is wrapped in `runCatching` so an unconfigured Firebase degrades to a logged no-op (BACKLOG P1-5).
- `FirebaseBackendModule.kt` — exports `val firebaseBackendModule = module { ... }` (**auth + remote data only**). `:shared`'s `frnkModules(BackendChoice.Firebase)` installs this.
- `FirebaseObservabilityModule.kt` — exports `val firebaseObservabilityModule = module { ... }` (analytics + crash). **Separate from the backend module** so it can be selected via `ObservabilityChoice.Firebase` independently of `BackendChoice` (BACKLOG P1-5). `:shared`'s `frnkModules(observability = Firebase)` installs this. The `CrashReporter` binding also calls `enableNativeCrashHandler()` on first resolve (see below).
- `NativeCrashHandler.kt` (+ `NativeCrashHandler.ios.kt` / `NativeCrashHandler.android.kt`) — `internal expect fun enableNativeCrashHandler()` (BACKLOG P1-5b). The **iOS** actual installs the CrashKiOS unhandled-exception hook (`enableCrashlytics()` + `setCrashlyticsUnhandledExceptionHook()`, `runCatching`-wrapped + idempotent) so *uncaught* Kotlin exceptions reach Crashlytics symbolicated — gitlive's `recordException` only reports exceptions you explicitly catch. The **Android** actual is a no-op (the Crashlytics Android SDK already hooks uncaught JVM exceptions). This is the module's only `expect/actual` and the only reason it has `iosMain`/`androidMain` Kotlin source sets.

## Rules

- This module is reachable from `:shared` **only** through Koin. Nothing else in the project should `import dev.jdgarita.frnk.backend.firebase.*` — that would defeat backend swap-ability.
- The Koin module name is `firebaseBackendModule` (lower-camel `val`). The Supabase mirror is `supabaseBackendModule`. Keep both names symmetric; `:shared/FrnkModules.kt` `when`-switches on them.
- Every binding here must satisfy an interface declared in `:shared:backend:api`. Return `AppResult` from every method — wrap Firebase exceptions in `AppResult.Failure(...)` rather than letting them propagate.
- Plugin: `kotlin.serialization` is applied (Firestore DTOs use `@Serializable`). Keep DTOs internal to this module — they should not leak into `*-api`.

## iOS note

Firebase iOS frameworks (`FirebaseAuth`, `FirebaseFirestore`, …) are **not** bundled in `FrnkKit.xcframework`. The consumer Xcode project supplies them via CocoaPods/SPM; `:iosApp` uses `linkerOpts("-undefined", "dynamic_lookup")` so the toolkit links without them, and the host's link step resolves the symbols. CrashKiOS (`co.touchlab.crashkios:crashlytics`, `iosMain` only) follows the same rule — its native Crashlytics symbols resolve through the consumer's pod under `dynamic_lookup`; **do not** add per-framework `linkerOpts`.

### Crash symbolication (CrashKiOS)
The hook converts an uncaught Kotlin crash into a Crashlytics report, but Crashlytics still needs the **Kotlin framework dSYM** to symbolicate the Kotlin frames. `FrnkKit.xcframework` is `isStatic = true`, so Kotlin symbols link into the consumer app binary — the consumer uploads dSYMs at *their* archive step (the standard Firebase `upload-symbols` run-script, and/or the CrashKiOS `crashlyticslink` Gradle plugin). The toolkit cannot do this (it ships a prebuilt framework and has no access to the consumer's archive). Recommend consumers upload **all** dSYMs (app-archive `dSYMs/` + the XCFramework's bundled `ios-*/dSYMs/`). The hook is installed before the consumer's `FirebaseApp.configure()` runs — that's fine: it only needs Crashlytics live at *crash* time.

> **CI caveat:** all `iosMain` CrashKiOS code is uncompiled by `compileAndroidMain`/`testAndroidHostTest` (Linux CI skips iOS targets). A CrashKiOS API/signature drift ships green — run a local macOS `./gradlew :iosApp:assembleFrnkKitReleaseXCFramework` (or at least `compileKotlinIosSimulatorArm64`) before merging changes here.

## Dependencies

- `api(projects.shared.backend.api)` — only api surface re-exported.
- `implementation`: `koin.core`, `firebase.{auth,firestore,analytics,crashlytics}`. No Ktor, no Supabase, no UI.
- `iosMain` only: `crashkios.crashlytics` (must stay out of `commonMain` — it has no JVM variant and would break `compileAndroidMain`).
- `commonTest`: `kotlin.test` + `kotlinx.coroutines.test` (host tests opted in via `kotlin { android { withHostTest {} } }`; run with `./gradlew :shared:backend:firebase:testAndroidHostTest`).
