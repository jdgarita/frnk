package dev.jdgarita.frnk.ui.nav

import androidx.navigation3.runtime.NavKey

/**
 * Marker mix-in for a [NavKey] route that should be shown **full-screen** — without a tabbed scaffold's
 * bottom navigation bar (and reserving no bottom inset for it). A route opts in by also implementing this
 * interface, e.g. `data object Onboarding : DemoRoute, FrnkFullScreenRoute`.
 *
 * A route carries this marker to declare "no bottom bar" **on the route itself** (next to where it's
 * defined and registered) rather than via a separate predicate that can drift out of sync.
 * (`FrnkNestedNavScaffold` does not yet auto-hide its bar for these routes — full-screen bar hiding is a
 * planned follow-up; the intent is recorded on the route for when it lands.) Pure marker (no Compose, no
 * members) so it lives here in the Compose-free nav contract.
 */
interface FrnkFullScreenRoute : NavKey