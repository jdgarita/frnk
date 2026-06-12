# :permissions

Capability **scaffold** (restructure Stage 11 / D4) — api-only. A minimal cross-platform runtime
permission contract + a no-op default + a Koin module. **No `expect/actual`, no native cinterop**, so
the XCFrameworks stay clean. A real platform implementation is future feature work, out of scope.

## Contents

- `PermissionController.kt` — `fun status(p): PermissionStatus` + `suspend fun request(p):
  PermissionStatus`, plus the `Permission` (Camera/Microphone/Notifications/Location) and
  `PermissionStatus` (Granted/Denied/NotDetermined) enums.
- `NoopPermissionController.kt` — `status` → `NotDetermined`, `request` → `Denied`.
- `PermissionModule.kt` — `val permissionsModule`, binding `NoopPermissionController`.

## Rules

- Keep the surface coarse and platform-neutral; a real impl belongs in a sibling `:permissions-impl`
  (api/impl discipline). No native SDKs in this api module.

## Dependencies

- `api(projects.sharedUtils)`, `api(libs.kotlinx.coroutines.core)`, `api(libs.koin.core)`. That's it.
- `commonTest`: `kotlin-test` + `kotlinx-coroutines-test`
  (`./gradlew :permissions:testAndroidHostTest`).
