# :data-prefs-impl

Concrete key-value persistence for `:data-prefs-api` over `russhwolf/multiplatform-settings` (split out of `shared-database-impl` at restructure Stage 4). Kotlin package stays `dev.jdgarita.frnk.database.impl` (D8).

## Contents

- `PrefsModule.kt` — exports `val prefsModule = module { single<KeyValueStore> { defaultKeyValueStore() } }`. Split out of the old combined `databaseModule`; hosts install it alongside (or without) `:data-db-impl`'s driver wiring.
- `SettingsKeyValueStore.kt` — `KeyValueStore` over `multiplatform-settings` (internal).
- `PrefsDefaults.kt` (`commonMain`) + `PrefsDefaults.android.kt` / `PrefsDefaults.ios.kt` — `expect/actual` platform defaults: `SharedPreferencesSettings` over the `"frnk_toolkit"` prefs file (reads the Android `Context` from `:core-di`'s `DatabaseContext`) / `NSUserDefaultsSettings` over `standardUserDefaults`.

## Rules

- All `multiplatform-settings` imports stay in this module — the api module must not see them.
- Android needs `DatabaseContext.application` set before Koin resolves the binding; `initializeFrnk(context, …)` (`:core-di`) does that for hosts.

## Testing

`commonTest`: `SettingsKeyValueStoreTest` round-trips through `MapSettings` (`multiplatform-settings-test`). Run with `./gradlew :data-prefs-impl:testAndroidHostTest`.

## Dependencies

- `api(projects.dataPrefsApi)`; `implementation`: `koin.core`, `settings.core`.
- `androidMain`: `implementation(projects.coreDi)` (for `DatabaseContext`).
- `commonTest`: `settings.test` (MapSettings).
