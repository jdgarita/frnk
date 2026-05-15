# shared-ui-api

Pure-interface UI module: MVI engine, navigation route marker, and the `UiText` localization wrapper. **No Compose here** — Compose lives in `:shared-ui-atoms`. That separation is deliberate so a feature ViewModel can compile without dragging in `compose.runtime`.

## Contents

- `ui/mvi/MviContract.kt` — empty marker interfaces `UiState`, `UiIntent`, `UiEffect`.
- `ui/mvi/MviViewModel.kt` — abstract `MviViewModel<S : UiState, I : UiIntent, E : UiEffect>`. Owns `StateFlow<S>`, a `SharedFlow<I>` (replay=0, buffer=16, `DROP_OLDEST`), and a `Channel<E>` (BUFFERED). Features override `suspend fun onIntent(intent: I)`. Use `setState { copy(...) }` to reduce and `emit(effect)` for one-shots.
- `ui/nav/ToolkitRoute.kt` — sealed route marker for `:shared` navigation.
- `ui/UiText.kt` — wrapper for raw / resource-resolved strings; ViewModels return these so the UI layer handles locale.

## Conventions

- **Vocabulary is `UiIntent`, not `UiAction`.** The root `CLAUDE.md` uses "Action" in prose, but the actual marker on disk is `UiIntent`. New code follows the on-disk name.
- Stick to interfaces and small abstract bases — no concrete repository, no Compose, no third-party UI.
- `api`-deps: `:shared-utils`, `kotlinx-coroutines`, `androidx.lifecycle.viewmodel` (for the MVI base). Don't add Compose or any backend SDK here.
- Feature modules that want MVI should depend on **this** module — not on `:shared-ui-atoms` — if they don't need composables.
