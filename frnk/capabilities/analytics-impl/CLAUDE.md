# :analytics-impl

Firebase implementation of `:analytics-api`. Installed at runtime by passing its Koin modules (`firebaseBackendModule` / `firebaseObservabilityModule`) to `initializeFrnk(modules = …)`.

## Contents

- `FirestoreRemoteData.kt` — `RemoteData` over Firestore. (`FirebaseAuthService` was deleted with `AuthService` in restructure Stage 2.)
- `FirebaseAnalyticsTracker.kt` — analytics via Firebase Analytics (gitlive `firebase-analytics`).
- `FirebaseCrashReporter.kt` — crash reporting via Firebase Crashlytics (gitlive `firebase-crashlytics`). Every SDK call is wrapped in `runCatching` so an unconfigured Firebase degrades to a logged no-op (BACKLOG P1-5).
- `FirebaseBackendModule.kt` — exports `val firebaseBackendModule = module { ... }` (**remote data only**). Hosts that want the Firebase backend pass it to `initializeFrnk(...)`.
- `FirebaseObservabilityModule.kt` — exports `val firebaseObservabilityModule = module { ... }` (analytics + crash). **Separate from the backend module** so observability installs independently of the data backend (BACKLOG P1-5); install it XOR `noopObservabilityModule` (`:analytics-api`). The `CrashReporter` binding also calls `enableNativeCrashHandler()` on first resolve (see below).
- `NativeCrashHandler.kt` (+ `NativeCrashHandler.ios.kt` / `NativeCrashHandler.android.kt`) — `internal expect fun enableNativeCrashHandler()` (BACKLOG P1-5b). The **iOS** actual installs the CrashKiOS unhandled-exception hook (`enableCrashlytics()` + `setCrashlyticsUnhandledExceptionHook()`, `runCatching`-wrapped + idempotent) so *uncaught* Kotlin exceptions reach Crashlytics symbolicated — gitlive's `recordException` only reports exceptions you explicitly catch. The **Android** actual is a no-op (the Crashlytics Android SDK already hooks uncaught JVM exceptions). This is the module's only `expect/actual` and the only reason it has `iosMain`/`androidMain` Kotlin source sets.

## Rules

- This module is reachable **only** through Koin (host bootstrap or the demo's device-smoke overrides). No toolkit code should `import dev.jdgarita.frnk.backend.firebase.*` — that would defeat backend swap-ability.
- The Koin module name is `firebaseBackendModule` (lower-camel `val`); keep the `<provider>BackendModule` naming convention — hosts reference these names directly in their `initializeFrnk(...)` lists.
- Every binding here must satisfy an interface declared in `:analytics-api`. Return `AppResult` from every method — wrap Firebase exceptions in `AppResult.Failure(...)` rather than letting them propagate.
- Plugin: `kotlin.serialization` is applied (Firestore DTOs use `@Serializable`). Keep DTOs internal to this module — they should not leak into `*-api`.

## iOS note

Firebase iOS frameworks (`FirebaseFirestore`, `FirebaseCrashlytics`, …) are **not** bundled in any toolkit framework. The consumer Xcode project supplies them via CocoaPods/SPM; umbrella XCFrameworks (DemoKit, host frameworks) use `linkerOpts("-undefined", "dynamic_lookup")` so they link without them, and the host's link step resolves the symbols. CrashKiOS (`co.touchlab.crashkios:crashlytics`, `iosMain` only) follows the same rule — its native Crashlytics symbols resolve through the consumer's pod under `dynamic_lookup`; **do not** add per-framework `linkerOpts`.

### Crash symbolication (CrashKiOS)
The hook converts an uncaught Kotlin crash into a Crashlytics report, but Crashlytics still needs the **Kotlin framework dSYM** to symbolicate the Kotlin frames. Umbrella frameworks are `isStatic = true`, so Kotlin symbols link into the consumer app binary — the consumer uploads dSYMs at *their* archive step (the standard Firebase `upload-symbols` run-script, and/or the CrashKiOS `crashlyticslink` Gradle plugin). The toolkit cannot do this (it has no access to the consumer's archive). Recommend consumers upload **all** dSYMs (app-archive `dSYMs/` + the XCFramework's bundled `ios-*/dSYMs/`). The hook is installed before the consumer's `FirebaseApp.configure()` runs — that's fine: it only needs Crashlytics live at *crash* time.

> **CI caveat:** all `iosMain` CrashKiOS code is uncompiled by `compileAndroidMain`/`testAndroidHostTest` (Linux CI skips iOS targets). A CrashKiOS API/signature drift ships green — run a local macOS `./gradlew compileKotlinIosSimulatorArm64` (or `:shared-demo:assembleDemoKitDebugXCFramework`) before merging changes here.

## Dependencies

- `api(projects.analyticsApi)` — only api surface re-exported.
- `implementation`: `koin.core`, `firebase.{firestore,analytics,crashlytics}`. No Ktor, no UI.
- `iosMain` only: `crashkios.crashlytics` (must stay out of `commonMain` — it has no JVM variant and would break `compileAndroidMain`).
- `commonTest`: `kotlin.test` + `kotlinx.coroutines.test` (host tests opted in via `kotlin { android { withHostTest {} } }`; run with `./gradlew :analytics-impl:testAndroidHostTest`).
