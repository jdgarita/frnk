# core-mvi

The toolkit's **MVI engine** — pure-Kotlin contracts + abstract base, plus the `UiText` localization
wrapper. **No Compose here** — the Compose binding (`FrnkMviScreen`, `EffectCollector`) lives in
`:shared-ui-atoms`. That separation is deliberate so a feature ViewModel can compile without dragging in
`compose.runtime`.

Extracted from the old `:shared-ui-api` at restructure Stage 6 (split into `:core-mvi` + `:core-nav` +
`:haptics`). The `dev.jdgarita.frnk:shared-ui-api` coordinate survives as a src-less facade
(`frnk/core/ui-api-facade`) that `api()`-re-exports all three until Stage 9. Kotlin packages are
unchanged (`dev.jdgarita.frnk.ui.mvi`, `dev.jdgarita.frnk.ui`).

## Contents

- `ui/mvi/MviContract.kt` — empty marker interfaces `UiState`, `UiIntent`, `UiEffect`.
- `ui/mvi/MviViewModel.kt` — abstract `MviViewModel<S : UiState, I : UiIntent, E : UiEffect>`. Owns `StateFlow<S>`, a `SharedFlow<I>` (replay=0, buffer=16, `DROP_OLDEST`), and a `Channel<E>` (BUFFERED). Features override `suspend fun onIntent(intent: I)`. Use `setState { copy(...) }` to reduce and `emit(effect)` for one-shots.
- `ui/UiText.kt` — wrapper for raw / resource-resolved strings; ViewModels return these so the UI layer handles locale.

## Conventions

- **Vocabulary is `UiIntent`, not `UiAction`.** The marker on disk is `UiIntent`, and the repo's docs (root `CLAUDE.md`, `docs/ARCHITECTURE.md`) use `UiIntent` / `onIntent` consistently. If any external skill or agent prose says "Action" generically, follow the on-disk name.
- Stick to interfaces and small abstract bases — no concrete repository, no Compose, no third-party UI.
- `api`-deps: `:shared-utils`, `kotlinx-coroutines`, `androidx.lifecycle.viewmodel` (for the MVI base). Don't add Compose or any backend SDK here.
- Feature modules that want MVI should depend on **this** module (or the route contract in `:core-nav`) — not on `:shared-ui-atoms` — if they don't need composables.
