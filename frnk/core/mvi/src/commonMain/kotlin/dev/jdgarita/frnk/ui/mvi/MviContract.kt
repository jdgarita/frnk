package dev.jdgarita.frnk.ui.mvi

// Marker interfaces — keep contracts thin; concrete types live in feature modules.

/**
 * The data-only layer of a [ModelMviViewModel] — holds just the data, no UI decoration. The VM mutates
 * a [ModelState] and the engine derives the [UiState] from it via `mapToUiState`.
 */
interface ModelState

interface UiState

interface UiIntent

interface UiEffect

/**
 * A data-only bundle of runtime inputs for a [ModelMviViewModel] (e.g. a screen's `source`, an id to
 * load). Not a constructor argument: it is supplied at **attach time** by the Compose lifecycle helper
 * and handed to [ModelMviViewModel.onAttached], where the VM seeds its [ModelState] from it. Service
 * dependencies (managers, trackers) stay as constructor params — keep those out of [Arguments].
 */
interface Arguments
