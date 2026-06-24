# core-nav

The toolkit's **Compose-free type-safe Navigation3 contract**: `FrnkRoute`, the back-stack mutation
helpers, the deep-link signal, and the saved-state config builder. The
nav3 *runtime* (`NavKey` / `NavBackStack` / `SavedStateConfiguration`) is pure Kotlin/MP, so it lives
here **without Compose** — feature ViewModels (and MVI nav-effect handlers) compile without dragging in
`compose.runtime`. The Compose nav engine (`FrnkNavDisplay`, tabbed back stacks, animations) lives one
layer up in `:ui-scaffolds`.

Extracted from the old `:shared-ui-api` at restructure Stage 6. The `dev.jdgarita.frnk:shared-ui-api`
facade that re-exported this module was deleted at Stage 9. Kotlin package is
unchanged (`dev.jdgarita.frnk.ui.nav`).

## Contents

- `ui/nav/FrnkRoute.kt` — the toolkit's default **tab-level** catalogue of type-safe destinations (`Home`, `Onboarding`, `Settings`, `Paywall`, `Custom(id)`). A **`@Serializable sealed interface FrnkRoute : NavKey`** (each member `@Serializable`) so it can key a nav3 `NavBackStack` and restore via `SavedStateConfiguration`. Hosts may also declare their own `@Serializable` `NavKey` routes — the nav engine (`FrnkNavDisplay` in `:ui-scaffolds`) is generic over any `NavKey`. The **host owns the back-stack instance** (`NavBackStack<NavKey>`).
- `ui/nav/FrnkRootRoute.kt` — a **parallel root-level** catalogue (`Onboarding`, `Tab`, `Paywall`, `Custom(id)`) for the lower-level `FrnkApp` root path (`:ui-app`): a two-level shape where modal/full-screen flows (`Onboarding`, `Paywall`) and the tabbed shell (`Tab`, which hosts an inner `FrnkRoute` back stack) live at the app root, while `FrnkRoute` keys the nested tab stack inside `Tab`. `Onboarding`/`Paywall` carry `FrnkFullScreenRoute`. **Coexists with** `FrnkRoute` — `FrnkRootRoute` is the root catalogue for the `FrnkApp` path, while `FrnkRoute` keys the nested tab stack (used by `FrnkNestedNavScaffold`).
- `ui/nav/NavBackStackExt.kt` — back-stack helpers: `NavBackStack<NavKey>.navigateTo(screen, popScreen?, singleTop = true)`, `.back()`, `.clearAndNavigateTo(screen)`. In nav3 the back stack is a `MutableList<NavKey>`, so "navigating" is mutating it; these name the common mutations and stay Compose-free so MVI effect handlers can call them. `navigateTo` is **single-top by default** (the nav2 `launchSingleTop` equivalent): a push equal to the current top entry is skipped, so a doubly-fired nav effect can't stack a duplicate destination — distinct instances of the same route type still push; pass `singleTop = false` to force a duplicate.
- `ui/nav/FrnkFullScreenRoute.kt` — `interface FrnkFullScreenRoute : NavKey`, a pure marker mix-in. A route implementing it (e.g. `data object Onboarding : DemoRoute, FrnkFullScreenRoute`) is intended to render full-screen, with no bottom bar. `FrnkRoute.Onboarding` / `FrnkRoute.Paywall` carry it (as do `FrnkRootRoute.Onboarding` / `FrnkRootRoute.Paywall`). Declares "no bottom bar" **on the route** instead of a separate host predicate that can drift. (`FrnkNestedNavScaffold` does not yet auto-hide the bar off this marker — the intent is recorded on the route for when it does.)
- `ui/nav/FrnkPendingRouteRequest.kt` — cross-process deep-link signal (`StateFlow<NavKey?>` + `request`/`consume`). State-based so a signal set before the observer attaches still delivers. Generic over `NavKey`; register as a DI singleton.
- `ui/nav/RootNavConfig.kt` — `val frnkRootNavConfig`: the `SavedStateConfiguration` for the **root** back stack, registering `FrnkRootRoute`'s polymorphic serializers (`Onboarding`/`Tab`/`Paywall`). Passed to the root `NavDisplay` in `:ui-app`'s `FrnkApp`.
- `ui/nav/NestedNavConfig.kt` — `frnkNestedNavConfig(hostRoutes: SerializersModule = …)`: the `SavedStateConfiguration` for a **nested/tab** back stack, registering `FrnkRoute.Home`/`Custom`/`Settings` and `include`-ing the host's own route module. (Replaces the old single `frnkNavConfiguration(...)`, which is gone — the config split in two when navigation went two-level.)

## Conventions

- Stick to the Compose-free nav3 runtime — no `navigation3-ui`, no `compose.runtime`. The Compose nav engine (`FrnkNavDisplay`, tabbed stacks, animations) belongs in `:ui-scaffolds`.
- `api`-deps: `kotlinx-coroutines`, `kotlinx-serialization-core` (for `@Serializable` routes — **core only**, never `-json`; nav3 encodes routes via savedstate's `SavedStateEncoder`), and `androidx-navigation3-runtime` (`NavKey`/`NavBackStack`/`SavedStateConfiguration`). The `kotlin-serialization` plugin is applied here for the route contract.
- Feature modules emitting route effects should depend on **this** module, not on `:ui-scaffolds`, when they don't need composables.
