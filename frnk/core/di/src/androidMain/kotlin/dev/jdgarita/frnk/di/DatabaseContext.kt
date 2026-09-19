package dev.jdgarita.frnk.di

import android.content.Context

/**
 * Process-wide Android [Context] seam for the data layer — `:data-db-api`'s Room builder,
 * `:data-db-impl`'s database locations and `:data-prefs-impl`'s SharedPreferences-backed
 * `KeyValueStore` all read it.
 *
 * The androidMain `initializeFrnk(context, modules)` overload sets it; a host that bypasses
 * `initializeFrnk` MUST set it from `Application.onCreate` before Koin resolves either binding.
 * Lives here (not in an impl module) since restructure Stage 4 so both data impls can share it
 * without depending on each other.
 */
object DatabaseContext {
    lateinit var application: Context
}