# :remote-config-impl

Firebase Remote Config binding for `:remote-config-api`'s `RemoteConfigService`. Pulls a **native
Firebase SDK** (gitlive `dev.gitlive:firebase-config`, iOS cinterop) — so it is **never** part of any
toolkit module's common surface, and never in `:demo-shared`'s common deps. The host installs it via
`initializeFrnk(modules = …)`; everything else resolves it through Koin (same api/impl discipline as
`:analytics-impl`).

## Contents

- `FirebaseRemoteConfigService.kt` — `RemoteConfigService` over `Firebase.remoteConfig`. Honours the
  per-call default when a key is `ValueSource.Static` (unset); blank strings fall back too.
  `fetchAndActivate()` maps SDK failures to `AppResult.Failure(CommonError.Unknown)` and rethrows
  `CancellationException`.
- `RemoteConfigModule.kt` — `val remoteConfigModule` (`single<RemoteConfigService> { … }`).

## Dependencies

- `api(projects.remoteConfigApi)`, `implementation(libs.koin.core)`,
  `implementation(libs.firebase.config)`; `androidMain` adds the Firebase BOM `platform(...)` line.
- No sibling `*-api` → `*-api` dep: `RemoteConfigService` returns `AppResult` from `:shared-utils`.

## Host setup

The host supplies the native Firebase SDK (SPM/CocoaPods on iOS, `google-services` on Android) +
config file. Bundled defaults / fetch interval are the host's to register on the underlying SDK if
needed — the toolkit getter honours its own per-call default for unset keys.
