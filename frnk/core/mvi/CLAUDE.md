# core-mvi

The toolkit's **MVI engine** — pure-Kotlin contracts + abstract base, plus the `UiText` localization
wrapper. **No Compose here** — the Compose binding (`FrnkScreen`) lives in
`:ui-scaffolds`. That separation is deliberate so a feature ViewModel can compile without dragging in
`compose.runtime`.

Extracted from the old `:shared-ui-api` at restructure Stage 6 (split into `:core-mvi` + `:core-nav` +
`:haptics`). The `dev.jdgarita.frnk:shared-ui-api` facade that `api()`-re-exported all three
(`frnk/core/ui-api-facade`) was deleted at Stage 9. Kotlin packages are
unchanged (`dev.jdgarita.frnk.ui.mvi`, `dev.jdgarita.frnk.ui`).

## Contents

- `ui/mvi/MviContract.kt` — marker interfaces `UiState`, `UiIntent`, `UiEffect`, plus `ModelState` (the data-only layer of a model-first VM) and `Arguments` (a data-only bundle of runtime inputs supplied at **attach time**, not via the constructor).
- `ui/mvi/MviViewModel.kt` — the **model-first** abstract base `MviViewModel<A : Arguments, M : ModelState, S : UiState, I : UiIntent, E : UiEffect>(factory: ModelStateFactory<M>)`. Splits state into a data-only `ModelState` (`M`, the source of truth, mutated via `updateModel { copy(...) }`) and a derived `UiState` (`S`, what the UI collects — **never set directly**: the engine re-runs the abstract, pure `mapToUiState(M): S` on every model change). The initial model comes from the `ModelStateFactory`; the initial `UiState` is that model mapped through `mapToUiState`. Runtime inputs arrive as `Arguments` at **attach time**, not via the constructor: the Compose driver (`FrnkScreen` in `:ui-scaffolds`) calls `attach(arguments)` once when the view first appears (guarded), which retains `arguments` and runs the overridable `onAttached(arguments)` hook — so seed-the-model/start-loads work runs when the screen is shown, not at construction. Owns a `SharedFlow<I>` (replay=0, buffer=16, `DROP_OLDEST`) and a `Channel<E>` (BUFFERED); features override `suspend fun onIntent(intent: I)`, dispatch with `send(intent)`, surface one-shots with `emit(effect)`, read the model via `currentModel()`, and launch work with `launchAttached { }`. Service dependencies (managers, trackers) stay ordinary constructor params, separate from `Arguments`. Also declares `ModelStateFactory<M>` (seeds the initial model). The old single-state `MviViewModel<S,I,E>` + `setState` reducer is gone — this model-first base is the only one.
- `ui/mvi/CommonUiIntent.kt` / `ui/mvi/CommonUiEffect.kt` — cross-cutting MVI types any VM can reuse: `CommonUiIntent.OnBackPressed` (handled by the base's `onIntent(CommonUiIntent)` overload) → `CommonUiEffect.DidPressBack(screenId?)`, so back handling is wired once instead of per screen.
- `ui/UiText.kt` — wrapper for raw / resource-resolved strings; ViewModels return these so the UI layer handles locale.

## Conventions

- **Vocabulary is `UiIntent`, not `UiAction`.** The marker on disk is `UiIntent`, and the repo's docs (root `CLAUDE.md`, `docs/ARCHITECTURE.md`) use `UiIntent` / `onIntent` consistently. If any external skill or agent prose says "Action" generically, follow the on-disk name.
- Stick to interfaces and small abstract bases — no concrete repository, no Compose, no third-party UI.
- `api`-deps: `:shared-utils`, `kotlinx-coroutines`, `androidx.lifecycle.viewmodel` (for the MVI base). Don't add Compose or any backend SDK here.
- Feature modules that want MVI should depend on **this** module (or the route contract in `:core-nav`) — not on `:ui-scaffolds` — if they don't need composables.
