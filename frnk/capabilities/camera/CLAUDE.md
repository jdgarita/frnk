# :camera

Capability **scaffold** (restructure Stage 11 / D4) — api-only. A minimal cross-platform camera
contract + a no-op default + a Koin module. **No `expect/actual`, no native cinterop**, so the
XCFrameworks stay clean. A real CameraX / AVFoundation implementation is future feature work,
explicitly out of scope for now.

## Contents

- `CameraController.kt` — `suspend fun capturePhoto(): AppResult<CameraImage, CommonError>` +
  the tiny `CameraImage(bytes, mimeType)` value type.
- `NoopCameraController.kt` — `capturePhoto()` returns `AppResult.Failure(CommonError.Unknown)`
  (no camera wired).
- `CameraModule.kt` — `val cameraModule`, binding `NoopCameraController`.

## Rules

- **`capturePhoto()` returns `AppResult`, never throws.**
- When a real impl arrives it goes in a sibling `:camera-impl` (api/impl discipline); keep native
  SDKs out of this api module.

## Dependencies

- `api(projects.sharedUtils)`, `api(libs.kotlinx.coroutines.core)`, `api(libs.koin.core)`. That's it.
- `commonTest`: `kotlin-test` + `kotlinx-coroutines-test`
  (`./gradlew :camera:testAndroidHostTest`).
