# :remote-config-api

Pure-interface remote-config contract. **No Firebase, no native SDK.** Feature/host code depends on
this interface; the concrete Firebase binding lives in `:remote-config-impl`.

Its own capability pair, **sibling of `:analytics-*`, kept separate from it** (restructure Stage 11 /
OQ-1). It replaced the old generic Firestore-shaped `RemoteData` stub (deleted at Stage 11).

## Contents

- `RemoteConfigService.kt` — read-only, typed key→value surface with a fetch/activate lifecycle:
  `fetchAndActivate()` + `getString/getBoolean/getLong/getDouble(key, default)`. Not a CRUD store —
  there is no `set`.
- `NoopRemoteConfig.kt` — the SDK-free default: every getter returns the passed default,
  `fetchAndActivate` is a successful no-op. Same precedent as `NoopObservability` in `:analytics-api`.
- `RemoteConfigModule.kt` — `val noopRemoteConfigModule`, the Koin binding of `NoopRemoteConfig`.
  Hosts install it XOR `remoteConfigModule` (`:remote-config-impl`).

## Rules

- **`fetchAndActivate()` returns `AppResult<Unit, CommonError>`. Never throw.** The getters are
  total (always return the default on a miss) so they don't need `AppResult`.
- **No SDK dependencies.** Anything reaching for `dev.gitlive.firebase.*` belongs in
  `:remote-config-impl`.

## Dependencies

- `api(projects.sharedUtils)` (for `AppResult`/`CommonError`), `api(libs.kotlinx.coroutines.core)`
  (the interface is `suspend`), `api(libs.koin.core)` (for `noopRemoteConfigModule`). That's it.
- `commonTest`: `kotlin-test` + `kotlinx-coroutines-test` (run with
  `./gradlew :remote-config-api:testAndroidHostTest`).

## Candidate first consumer

`still` resolves legal URLs (privacy/terms) + feedback email from Android Remote Config natively
today; it's the intended first real consumer of this pair. Wiring it is **future work**, not Stage 11.
