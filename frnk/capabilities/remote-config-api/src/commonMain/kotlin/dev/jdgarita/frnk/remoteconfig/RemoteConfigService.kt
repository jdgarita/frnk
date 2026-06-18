package dev.jdgarita.frnk.remoteconfig

import dev.jdgarita.frnk.utils.AppResult
import dev.jdgarita.frnk.utils.CommonError

/**
 * Read-only, typed key→value surface backed by a remote-config provider (Firebase Remote Config in
 * `:remote-config-impl`). Values can be overridden server-side without an app update — the canonical
 * use is resolving things like legal URLs, feature flags, or copy at runtime.
 *
 * This is **not** a generic CRUD/document store (the old Firestore-shaped `RemoteData` it replaced):
 * there is no `set`. The lifecycle is fetch-then-read — call [fetchAndActivate] to pull the latest
 * values, then read them with the typed getters. Each getter takes a [default] returned whenever the
 * key has no fetched/bundled override, so a read always yields a value and never throws.
 *
 * Implementations are installed via Koin (the host's `initializeFrnk(modules = …)` list):
 * [noopRemoteConfigModule] (this module) for telemetry-free hosts, or `remoteConfigModule`
 * (`:remote-config-impl`) for the real Firebase binding.
 */
interface RemoteConfigService {
    /** Fetch the latest values from the backend and activate them. Returns [AppResult], never throws. */
    suspend fun fetchAndActivate(): AppResult<Unit, CommonError>

    fun getString(
        key: String,
        default: String
    ): String

    fun getBoolean(
        key: String,
        default: Boolean
    ): Boolean

    fun getLong(
        key: String,
        default: Long
    ): Long

    fun getDouble(
        key: String,
        default: Double
    ): Double
}