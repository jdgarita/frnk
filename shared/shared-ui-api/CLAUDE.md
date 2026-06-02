# shared-ui-api

Pure-interface UI module: MVI engine, the type-safe navigation route contract + `FrnkNavigator` abstraction, and the `UiText` localization wrapper. **No Compose here** — Compose lives in `:shared-ui-atoms`. That separation is deliberate so a feature ViewModel can compile without dragging in `compose.runtime`.

## Contents

- `ui/mvi/MviContract.kt` — empty marker interfaces `UiState`, `UiIntent`, `UiEffect`.
- `ui/mvi/MviViewModel.kt` — abstract `MviViewModel<S : UiState, I : UiIntent, E : UiEffect>`. Owns `StateFlow<S>`, a `SharedFlow<I>` (replay=0, buffer=16, `DROP_OLDEST`), and a `Channel<E>` (BUFFERED). Features override `suspend fun onIntent(intent: I)`. Use `setState { copy(...) }` to reduce and `emit(effect)` for one-shots.
- `ui/nav/ToolkitRoute.kt` — the toolkit's default catalogue of type-safe routes. Now a **`@Serializable` sealed interface** (each member `@Serializable`) so navigation-compose can use it as a destination. Hosts may also declare their own `@Serializable` routes — the nav primitives are generic. The toolkit ships the `FrnkNavHost` machinery (in `:shared-ui-atoms`) but the **host owns the back-stack instance** (`NavController`).
- `ui/nav/FrnkNavigator.kt` — Compose-free navigation abstraction (`navigate(route)`, `navigate(route, options)`, `navigateUp`, `popBackStack`) + `FrnkNavOptions` (a nested `PopUpTo(route, inclusive, saveState)` so `saveState` can't be set without the pop it belongs to, plus `launchSingleTop` / `restoreState`). Covers both the multiple-back-stack tab switch and clearing a finished flow off the stack (`inclusive = true`). Lets MVI effect handlers drive navigation without depending on Compose. The Compose-backed adapter (`rememberFrnkNavigator`) lives in `:shared-ui-atoms`.
- `ui/UiText.kt` — wrapper for raw / resource-resolved strings; ViewModels return these so the UI layer handles locale.
- `ui/haptics/` — the toolkit's **simplified haptics contract** (Compose- and library-free, so ViewModels can inject it too). `HapticType` (semantic enum: `Click`, `Selection`, `LongPress`, `Success`, `Warning`, `Error`), `HapticFeedback` (`isEnabled: StateFlow<Boolean>` + `setEnabled` + `perform(type)`; `perform` is a no-op while disabled), the `HapticEngine` SPI (`fun emit(type)`) the platform binding supplies, `DefaultHapticFeedback` (frnk-owned: holds the in-memory enabled flag, gates `perform`, delegates to a `HapticEngine` — mirrors `DefaultEntitlementManager` wrapping a provider), and `NoOpHapticFeedback`. The `multihaptic`-backed engine + the `LocalFrnkHaptics` composition local that exposes a `HapticFeedback` to composables live one layer up in `:shared-ui-atoms` (binding needs a Compose `Vibrator`). The "Haptic feedback" Settings toggle drives `setEnabled`; frnk atoms call `perform` on press.

## Conventions

- **Vocabulary is `UiIntent`, not `UiAction`.** The marker on disk is `UiIntent`, and the repo's docs (root `CLAUDE.md`, `docs/ARCHITECTURE.md`) use `UiIntent` / `onIntent` consistently. If any external skill or agent prose says "Action" generically, follow the on-disk name.
- Stick to interfaces and small abstract bases — no concrete repository, no Compose, no third-party UI.
- `api`-deps: `:shared-utils`, `kotlinx-coroutines`, `androidx.lifecycle.viewmodel` (for the MVI base), `kotlinx-serialization-core` (for `@Serializable` routes — **core only**, never `-json`; navigation-compose encodes routes via its own `SavedStateEncoder`). The `kotlin-serialization` plugin is applied here for the route contract. Don't add Compose, navigation-compose, or any backend SDK here.
- Feature modules that want MVI should depend on **this** module — not on `:shared-ui-atoms` — if they don't need composables.
