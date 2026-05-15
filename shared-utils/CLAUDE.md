# shared-utils

KMP root module — the dependency floor every other `shared-*` module sits on. Pure-Kotlin; no Android, no Compose, no UI, no SDK bindings.

## Public surface

- `Logger.kt` — multiplatform log helper. Use this everywhere instead of `println` / `Log.d` / `NSLog`.
- `DateTimeFormat.kt` — `kotlinx-datetime` formatting helpers shared by every consumer.

`commonMain` `api`-exports `kotlinx.coroutines.core` and `kotlinx.datetime`, so downstream modules don't redeclare them.

## Rules

- This module **must not depend on anything in the project**. It is the root of the graph.
- Don't add SDK-specific code here — `BuildKonfig`-generated config is a future addition, but anything that needs Ktor / Firebase / Room belongs further down the graph.
- New helper goes here only if it's needed by **two or more** unrelated `shared-*` modules and has no platform-specific dependency. Otherwise put it in the module that owns it.
- All three iOS targets are configured (`iosX64`, `iosArm64`, `iosSimulatorArm64`) with `baseName = "shared_utils"` — keep that pattern for any new target you add.
