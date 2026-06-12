# core-nav

The toolkit's **Compose-free type-safe Navigation3 contract**: `ToolkitRoute`, the back-stack mutation
helpers, the deep-link signal, the primary-action registry, and the saved-state config builder. The
nav3 *runtime* (`NavKey` / `NavBackStack` / `SavedStateConfiguration`) is pure Kotlin/MP, so it lives
here **without Compose** — feature ViewModels (and MVI nav-effect handlers) compile without dragging in
`compose.runtime`. The Compose nav engine (`FrnkNavDisplay`, tabbed back stacks, animations) lives one
layer up in `:ui-scaffolds`.

Extracted from the old `:shared-ui-api` at restructure Stage 6. The `dev.jdgarita.frnk:shared-ui-api`
facade that re-exported this module was deleted at Stage 9. Kotlin package is
unchanged (`dev.jdgarita.frnk.ui.nav`).

## Contents

- `ui/nav/ToolkitRoute.kt` — the toolkit's default catalogue of type-safe destinations. A **`@Serializable sealed interface ToolkitRoute : NavKey`** (each member `@Serializable`) so it can key a nav3 `NavBackStack` and restore via `SavedStateConfiguration`. Hosts may also declare their own `@Serializable` `NavKey` routes — the nav engine (`FrnkNavDisplay` in `:ui-scaffolds`) is generic over any `NavKey`. The **host owns the back-stack instance** (`NavBackStack<NavKey>`).
- `ui/nav/NavBackStackExt.kt` — back-stack helpers: `NavBackStack<NavKey>.navigateTo(screen, popScreen?, singleTop = true)`, `.back()`, `.clearAndNavigateTo(screen)`. In nav3 the back stack is a `MutableList<NavKey>`, so "navigating" is mutating it; these name the common mutations and stay Compose-free so MVI effect handlers can call them. `navigateTo` is **single-top by default** (the nav2 `launchSingleTop` equivalent): a push equal to the current top entry is skipped, so a doubly-fired nav effect can't stack a duplicate destination — distinct instances of the same route type still push; pass `singleTop = false` to force a duplicate.
- `ui/nav/FrnkFullScreenRoute.kt` — `interface FrnkFullScreenRoute : NavKey`, a pure marker mix-in. A route implementing it (e.g. `data object Onboarding : DemoRoute, FrnkFullScreenRoute`) is shown full-screen — `FrnkTabbedNavScaffold`'s default `hideBarFor` is `{ it is FrnkFullScreenRoute }`, so the bottom bar hides automatically. `ToolkitRoute.Onboarding` / `ToolkitRoute.Paywall` carry it. Declares "no bottom bar" **on the route** (next to its `entryProvider` registration) instead of a separate host predicate that can drift.
- `ui/nav/FrnkPendingRouteRequest.kt` — cross-process deep-link signal (`StateFlow<NavKey?>` + `request`/`consume`). State-based so a signal set before the observer attaches still delivers. Generic over `NavKey`; register as a DI singleton.
- `ui/nav/FrnkPrimaryActionRegistry.kt` — routes the bottom bar's **primary-action button** (the Create/Add FAB) to the currently active screen: a handler **stack** (last-registered wins, unregister restores the previous — covers the nav-transition overlap) exposing `active: StateFlow<(() -> Unit)?>`; `register(handler)` returns an idempotent `FrnkPrimaryActionRegistration`. Compose-free (mirrors `FrnkPendingRouteRequest`); the Compose binding (`LocalFrnkPrimaryActionRegistry` + `FrnkPrimaryActionHandler`) lives in `:ui-scaffolds`, and `FrnkTabbedNavScaffold`/`FrnkAppShell` provide + observe it. Canonical screen usage sends an MVI intent: `FrnkPrimaryActionHandler { onIntent(HomeIntent.PrimaryActionClicked) }`.
- `ui/nav/FrnkNavConfig.kt` — `frnkNavConfiguration(hostRoutes: SerializersModule = …)` builds the `SavedStateConfiguration` a nav3 `NavBackStack` needs, registering `ToolkitRoute`'s polymorphic serializers and `include`-ing the host's own route module. Pass the result to `rememberFrnkNavBackStack(...)` in `:ui-scaffolds`.

## Conventions

- Stick to the Compose-free nav3 runtime — no `navigation3-ui`, no `compose.runtime`. The Compose nav engine (`FrnkNavDisplay`, tabbed stacks, animations) belongs in `:ui-scaffolds`.
- `api`-deps: `kotlinx-coroutines`, `kotlinx-serialization-core` (for `@Serializable` routes — **core only**, never `-json`; nav3 encodes routes via savedstate's `SavedStateEncoder`), and `androidx-navigation3-runtime` (`NavKey`/`NavBackStack`/`SavedStateConfiguration`). The `kotlin-serialization` plugin is applied here for the route contract.
- Feature modules emitting route effects should depend on **this** module, not on `:ui-scaffolds`, when they don't need composables.
