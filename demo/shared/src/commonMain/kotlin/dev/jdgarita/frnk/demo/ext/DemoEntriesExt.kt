package dev.jdgarita.frnk.demo.ext

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import dev.jdgarita.frnk.demo.ComponentContent
import dev.jdgarita.frnk.demo.ComponentDetailScreen
import dev.jdgarita.frnk.demo.ComponentsListScreen
import dev.jdgarita.frnk.demo.DemoEffect
import dev.jdgarita.frnk.demo.DemoIntent
import dev.jdgarita.frnk.demo.DemoRoute
import dev.jdgarita.frnk.demo.DemoState
import dev.jdgarita.frnk.ui.bottomnav.FrnkAppScope

/**
 * Registers the demo's own destinations — the center "Components" tab root ([DemoRoute.Components]) and
 * its pushed detail ([DemoRoute.ComponentDetail]) — on a scaffold's `entryProvider`. Scaffold-agnostic:
 * both the bare-shell host (`DemoScreen` over `FrnkTabbedNavScaffold`) and the batteries-included host
 * (demo-android's `MainActivity` over `FrnkAppScaffold`) call this, so there is one source of truth for
 * the demo's screens.
 *
 * It deliberately does **not** register `ToolkitRoute.Paywall`: the bare-shell host mounts that itself
 * while `FrnkAppScaffold` auto-mounts it, so leaving it out keeps this reusable across both (nav3 throws
 * on a duplicate route registration).
 *
 * All screens share the one host-scoped [dev.jdgarita.frnk.demo.DemoViewModel] (passed in as [state] +
 * [onIntent] + [onEffect]) rather than per-entry Koin VMs, hence inline entries over a Koin
 * `navigation<Route>` DSL.
 */
fun EntryProviderScope<NavKey>.demoFeatureEntries(
    scope: FrnkAppScope,
    state: DemoState,
    onIntent: (DemoIntent) -> Unit,
    onEffect: (DemoEffect) -> Unit,
) {
    entry<DemoRoute.Components> {
        ComponentsListScreen(
            state = state,
            onIntent = onIntent,
            onOpenComponent = { name -> scope.navigateTo(DemoRoute.ComponentDetail(name)) },
        )
    }
    entry<DemoRoute.ComponentDetail> { route ->
        ComponentDetailScreen(
            name = route.name,
            onBack = { scope.back() },
        ) {
            ComponentContent(route.name, state, onIntent, onEffect)
        }
    }
}
