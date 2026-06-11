# haptics

The toolkit's **simplified haptics CONTRACT** — Compose- and library-free, so ViewModels can inject it
too. **Contract only**: the `multihaptic`-backed engine binding + the `LocalFrnkHaptics` composition
local still live in `:shared-ui-atoms` (`ui/haptics/`, binding needs a Compose `Vibrator`); they move
down into this module at restructure Stage 7a.

Extracted from the old `:shared-ui-api` at restructure Stage 6. The `dev.jdgarita.frnk:shared-ui-api`
coordinate survives as a src-less facade re-exporting this module until Stage 9. Kotlin package is
unchanged (`dev.jdgarita.frnk.ui.haptics`).

## Contents

- `ui/haptics/HapticType.kt` — semantic enum: `Click`, `Selection`, `LongPress`, `Success`, `Warning`, `Error`.
- `ui/haptics/HapticFeedback.kt` — the `HapticFeedback` contract (`isEnabled: StateFlow<Boolean>` + `setEnabled` + `perform(type)`; `perform` is a no-op while disabled) **and** the `HapticEngine` SPI (`fun emit(type)`) the platform binding supplies.
- `ui/haptics/DefaultHapticFeedback.kt` — frnk-owned `HapticFeedback`: holds the in-memory enabled flag, gates `perform`, delegates to a `HapticEngine` (mirrors `DefaultEntitlementManager` wrapping a provider).
- `ui/haptics/NoOpHapticFeedback.kt` — the no-op `HapticFeedback`.

The "Haptic feedback" Settings toggle drives `setEnabled`; frnk atoms call `perform` on press.

## Conventions

- **Contract-only until Stage 7a.** No Compose, no `multihaptic`, no native cinterop here yet — the engine binding stays in `:shared-ui-atoms`. Treated as a UI-feedback concern (like the ripple), **not** an api/impl backend split.
- `api`-dep: `kotlinx-coroutines` only (for the `StateFlow<Boolean>` enabled flag).
