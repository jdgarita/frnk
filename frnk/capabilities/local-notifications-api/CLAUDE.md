# :local-notifications-api

Pure contract for **local** (on-device, no push, no backend) one-shot notifications. No platform code,
no SDK.

## Contents

- `LocalNotificationScheduler.kt` — `schedule(id, at: Instant, title, body)` / `cancel(id)` →
  `AppResult<Unit, LocalNotificationError>`; `permissionStatus()` / `requestPermission()` →
  `NotificationPermissionStatus`. Re-using an id replaces; cancelling an unknown id succeeds.
- `NotificationPermissionStatus.kt` — `Granted` / `Denied` / `NotDetermined`.
- `LocalNotificationError.kt` — `PermissionDenied` / `InvalidTime` (not in the future, or blank id) / `Unknown`.
- `commonTest/FakeLocalNotificationScheduler.kt` — the host-test fake (the `FakeAnalyticsTracker`
  pattern); mirrors the impl's validation + permission semantics. Pinned by its own test.

## Rules

- No SDK / platform dependency here; the impl is `:local-notifications-impl`.
- Deps: `api(projects.sharedUtils)`, `api(libs.kotlinx.coroutines.core)`.
