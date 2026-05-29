# shared-utils

KMP root module — the dependency floor every other `shared-*` module sits on. Pure-Kotlin; no Android, no Compose, no UI, no SDK bindings.

## Public surface

- `AppResult.kt` — the **toolkit-wide result envelope**: `sealed interface AppResult<out D, out E : AppError>` (`Success(data)` / `Failure(error)`), the `AppError` interface (`val message: String`), the `CommonError` enum (`Network`, `Unauthorized`, `NotFound`, `Unknown`), and the `fold(onSuccess, onFailure)` extension. Every `*-api` interface returns this and never throws. It lives here (the neutral root) rather than in `shared-backend-api` so backend, database (`NoteStore`), and monetization apis can all return it without a sibling `*-api`→`*-api` dependency. Tested by `AppResultTest` in `commonTest`.
- `Logger.kt` — multiplatform log helper. Use this everywhere instead of `println` / `Log.d` / `NSLog`.
- `DateTimeFormat.kt` — `kotlinx-datetime` formatting helpers shared by every consumer.
- `PlatformInfo.kt` — the module's **only `expect/actual`**. `expect object PlatformInfo` (`osName` / `osVersion` / `deviceModel`) with an `androidMain` actual (reads `android.os.Build` / `Build.VERSION`) and an `iosMain` actual (reads `UIDevice`). No `Context` or composition needed, so it's safe to read from anywhere in common code. It lives here — not in `shared-ui-atoms` — so the UI module stays platform-free; the `FeedbackEmailLauncher` scaffold consumes it through `FeedbackEmail`.
- `FeedbackEmail.kt` — pure-Kotlin `EmailDraft(recipient, subject, body)` with `toMailtoUri()` (RFC-6068 percent-encoded), plus `FeedbackEmail.draft(appName, appVersion, …)` which builds the toolkit's default "Send Feedback" e-mail (prompt + app/OS/device diagnostics block, the last two from `PlatformInfo`). `DEFAULT_RECIPIENT = "hello@jdgarita.dev"`. The host decides how to open the URI (the `shared-ui-atoms` `rememberFeedbackEmailLauncher` hands it to a Compose `UriHandler`).

`commonMain` `api`-exports `kotlinx.coroutines.core` and `kotlinx.datetime`, so downstream modules don't redeclare them.

## Rules

- This module **must not depend on anything in the project**. It is the root of the graph.
- Don't add SDK-specific code here — `BuildKonfig`-generated config is a future addition, but anything that needs Ktor / Firebase / Room belongs further down the graph.
- New helper goes here only if it's needed by **two or more** unrelated `shared-*` modules. Pure-Kotlin helpers stay in `commonMain`. If a helper genuinely needs the platform SDK (like `PlatformInfo`), expose a **narrow `expect/actual`** that returns plain data — never leak a `Context` / `UIViewController` up the graph. Otherwise put it in the module that owns it.
- All three iOS targets are configured (`iosX64`, `iosArm64`, `iosSimulatorArm64`) with `baseName = "shared_utils"` — keep that pattern for any new target you add. Any `expect` in `commonMain` needs an `actual` in **both** `androidMain` and the iOS source set.
