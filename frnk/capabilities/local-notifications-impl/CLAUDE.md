# :local-notifications-impl

`localNotificationsModule` binds `LocalNotificationsConfig` + the platform `LocalNotificationScheduler`.
OS APIs only (no third-party native SDK), so it is safe in an umbrella framework's common surface.

## Android (`androidMain`)

- `AndroidLocalNotificationScheduler` — one unique `OneTimeWorkRequest` per id (`REPLACE`, initial
  delay to the instant). WorkManager persists + reschedules after reboot; **inexact** delivery, no
  exact-alarm permission. Context from `:core-di`'s `DatabaseContext.application`.
- `LocalNotificationWorker` — self-contained (all data in its input), posts on the configured channel
  with tag = id; tap opens the launcher activity with `EXTRA_NOTIFICATION_ID`. Drops (does not retry)
  when permission was revoked since scheduling.
- `NotificationPermissionActivity` — translucent trampoline hosting the 13+ `POST_NOTIFICATIONS`
  prompt so `requestPermission()` is a plain suspend call; serialized by a mutex, completes on every
  finishing path. A persisted "asked" flag separates `NotDetermined` from `Denied`
  (`notificationPermissionStatusFor`, tested in `androidHostTest`).
- `src/androidMain/AndroidManifest.xml` merges `POST_NOTIFICATIONS` + the activity into hosts.

## iOS (`iosMain`)

- `IosLocalNotificationScheduler` — `UNUserNotificationCenter`, calendar trigger at the instant,
  request identifier = id. Prompts once (`requestAuthorization`). Foreground presentation is the host's
  `UNUserNotificationCenterDelegate` call.

## Tests

`commonTest` (`validateSchedule`) + `androidHostTest` (permission mapping) — `./gradlew :local-notifications-impl:testAndroidHostTest`.
