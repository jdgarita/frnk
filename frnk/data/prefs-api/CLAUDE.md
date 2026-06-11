# :data-prefs-api

Pure-interface key-value persistence (split out of `shared-database-api` at restructure Stage 4). Kotlin package stays `dev.jdgarita.frnk.database` (D8: the restructure renames modules, not packages).

## Contents

- `KeyValueStore.kt` — interface for simple key/value storage (impl uses `russhwolf/multiplatform-settings`, bound by `prefsModule` in `:data-prefs-impl`).
- `Preference.kt` — typed convenience layer over `KeyValueStore` (BACKLOG P4-3). `Preference<T> : ReadWriteProperty` plus `KeyValueStore.stringPreference/booleanPreference/intPreference/enumPreference(...)` factories, so hosts get typed accessors with defaults and `var x by pref` delegation instead of stringly keys. Pure stdlib; Int/Enum encode losslessly over the String primitive (corrupt/unknown values fall back to the default; enum decode never throws). `Long`/`Double`/nullable-string are deliberately deferred (no consumer; would need extra encoding). Dogfooded by `DefaultEntitlementManager`'s god-mode persistence (`shared-monetization-api`, which `api()`-depends on this module).

## Rules

- Keep it dependency-free — `KeyValueStore`/`Preference` are pure stdlib, and `shared-monetization-api` re-exports this module to every monetization consumer.
- Don't bind a concrete `KeyValueStore` here — Koin wiring lives in `:data-prefs-impl` (`prefsModule`). Tests use the `commonTest` `InMemoryKeyValueStore`.
- New typed preference kinds extend `Preference.kt` here, with tests in `PreferenceTest`.

## Testing

`commonTest`: `InMemoryKeyValueStore` (canonical in-memory fake) + `PreferenceTest`. Run with `./gradlew :data-prefs-api:testAndroidHostTest`.

## Dependencies

None (plugin `frnk.kmp.library.hosttest` supplies the `commonTest` kotlin-test/coroutines-test pair).
