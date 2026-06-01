# shared-backend-firebase

Firebase implementation of `:shared-backend-api`. Bundled inside `:shared` and selected at runtime via `BackendChoice.Firebase`.

## Contents

- `FirebaseAuthService.kt` — `AuthService` over Firebase Auth.
- `FirestoreRemoteData.kt` — `RemoteData` over Firestore.
- `FirebaseAnalyticsTracker.kt` — analytics via Firebase Analytics (gitlive `firebase-analytics`).
- `FirebaseCrashReporter.kt` — crash reporting via Firebase Crashlytics (gitlive `firebase-crashlytics`). Every SDK call is wrapped in `runCatching` so an unconfigured Firebase degrades to a logged no-op (BACKLOG P1-5).
- `FirebaseBackendModule.kt` — exports `val firebaseBackendModule = module { ... }` (**auth + remote data only**). `:shared`'s `frnkModules(BackendChoice.Firebase)` installs this.
- `FirebaseObservabilityModule.kt` — exports `val firebaseObservabilityModule = module { ... }` (analytics + crash). **Separate from the backend module** so it can be selected via `ObservabilityChoice.Firebase` independently of `BackendChoice` (BACKLOG P1-5). `:shared`'s `frnkModules(observability = Firebase)` installs this.

## Rules

- This module is reachable from `:shared` **only** through Koin. Nothing else in the project should `import dev.jdgarita.frnk.backend.firebase.*` — that would defeat backend swap-ability.
- The Koin module name is `firebaseBackendModule` (lower-camel `val`). The Supabase mirror is `supabaseBackendModule`. Keep both names symmetric; `:shared/FrnkModules.kt` `when`-switches on them.
- Every binding here must satisfy an interface declared in `:shared-backend-api`. Return `AppResult` from every method — wrap Firebase exceptions in `AppResult.Failure(...)` rather than letting them propagate.
- Plugin: `kotlin.serialization` is applied (Firestore DTOs use `@Serializable`). Keep DTOs internal to this module — they should not leak into `*-api`.

## iOS note

Firebase iOS frameworks (`FirebaseAuth`, `FirebaseFirestore`, …) are **not** bundled in `FrnkKit.xcframework`. The consumer Xcode project supplies them via CocoaPods/SPM; `:iosApp` uses `linkerOpts("-undefined", "dynamic_lookup")` so the toolkit links without them, and the host's link step resolves the symbols.

## Dependencies

- `api(projects.sharedBackendApi)` — only api surface re-exported.
- `implementation`: `koin.core`, `firebase.{auth,firestore,analytics,crashlytics}`. No Ktor, no Supabase, no UI.
