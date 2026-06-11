# haptics

The toolkit's haptics capability — the Compose-free **contract** plus the `multihaptic`-backed **engine
binding** + `LocalFrnkHaptics` composition local. The engine moved down here from `:shared-ui-atoms` at
restructure **Stage 7a** (it needs a Compose `Vibrator`, so the module now applies
`frnk.kmp.library.compose`). Treated as a UI-feedback concern (like the ripple), **not** an api/impl
backend split.

Extracted from the old `:shared-ui-api` at restructure Stage 6 (contract) then completed at Stage 7a
(engine). The `dev.jdgarita.frnk:shared-ui-api` coordinate survives as a src-less facade re-exporting
this module until Stage 9. Kotlin package is unchanged (`dev.jdgarita.frnk.ui.haptics`).

## Contents

**Contract (Compose-free):**
- `ui/haptics/HapticType.kt` — semantic enum: `Click`, `Selection`, `LongPress`, `Success`, `Warning`, `Error`.
- `ui/haptics/HapticFeedback.kt` — the `HapticFeedback` contract (`isEnabled: StateFlow<Boolean>` + `setEnabled` + `perform(type)`; `perform` is a no-op while disabled) **and** the `HapticEngine` SPI (`fun emit(type)`) the platform binding supplies.
- `ui/haptics/DefaultHapticFeedback.kt` — frnk-owned `HapticFeedback`: holds the in-memory enabled flag, gates `perform`, delegates to a `HapticEngine` (mirrors `DefaultEntitlementManager` wrapping a provider).
- `ui/haptics/NoOpHapticFeedback.kt` — the no-op `HapticFeedback`.

**Engine binding (Compose, Stage 7a):**
- `ui/haptics/MultiHapticEngine.kt` — `HapticEngine` impl that maps each `HapticType` to a `top.ltfan.multihaptic` primitive composition and plays it through a `Vibrator` (no-op when `isVibrationSupported` is false, e.g. an iOS simulator). Reads the current `Vibrator` through a provider lambda so it stays stable across rebuilds.
- `ui/haptics/FrnkHaptics.kt` — `LocalFrnkHaptics` (the ambient `HapticFeedback`, default `NoOpHapticFeedback`); `rememberFrnkHaptics(enabled)` resolves the platform `Vibrator` via `multihaptic-compose`'s `rememberVibrator()` (reads `LocalContext` on Android — **no Context plumbing**) and wraps it in `DefaultHapticFeedback`. **Rebuilds the `Vibrator` on every return to foreground** (keyed off an `ON_STOP`→`ON_START` counter via `LocalLifecycleOwner`) because multihaptic 0.3.2 doesn't restart its iOS `CHHapticEngine` after backgrounding — without this, haptics silently die after leaving the app and coming back. `HAPTICS_TOGGLE_ID = "haptics"` is the stable id of the default Settings toggle.

The "Haptic feedback" Settings toggle drives `setEnabled`; frnk atoms call `perform` on press.
`:ui-theme`'s `FrnkTheme` installs `LocalFrnkHaptics` via `rememberFrnkHaptics()`, so atoms vibrate with
zero host wiring.

## Conventions

- **Contract + engine, not api/impl.** A UI-feedback concern (like the ripple), so it lives in one
  capability module rather than an api/impl pair. The contract stays Compose-free and usable by
  ViewModels; the engine is the Compose half.
- **No native cinterop.** multihaptic ships its own Android/iOS impls, so umbrella XCFrameworks stay
  clean and the Android `VIBRATE` permission self-merges from `multihaptic-core`'s manifest.
- Deps: `api(kotlinx-coroutines)` (the `StateFlow<Boolean>` enabled flag) + `implementation` of
  `androidx-lifecycle-runtime-compose` + `multihaptic-core`/`-compose`.
