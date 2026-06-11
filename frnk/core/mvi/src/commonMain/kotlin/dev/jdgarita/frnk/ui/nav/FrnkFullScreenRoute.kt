package dev.jdgarita.frnk.ui.nav

import androidx.navigation3.runtime.NavKey

/**
 * Marker mix-in for a [NavKey] route that should be shown **full-screen** — without a tabbed scaffold's
 * bottom navigation bar (and reserving no bottom inset for it). A route opts in by also implementing this
 * interface, e.g. `data object Onboarding : DemoRoute, FrnkFullScreenRoute`.
 *
 * `FrnkTabbedNavScaffold`'s default `hideBarFor` is `{ it is FrnkFullScreenRoute }`, so the bar hides
 * automatically for any current top route that carries this marker — the host declares the intent **on the
 * route** (next to where it's defined and registered in the `entryProvider`) rather than maintaining a
 * separate predicate that can drift out of sync. Hosts can still pass an explicit `hideBarFor` for ad-hoc
 * rules. Pure marker (no Compose, no members) so it lives here in the Compose-free nav contract.
 */
interface FrnkFullScreenRoute : NavKey
