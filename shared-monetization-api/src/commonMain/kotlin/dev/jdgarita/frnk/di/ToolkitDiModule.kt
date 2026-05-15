package dev.jdgarita.frnk.di

import org.koin.core.module.Module

/**
 * Aggregator surfaced to host apps:
 *
 *   startKoin {
 *       modules(
 *           toolkitCoreModules() +              // always-on (utils, db, monetization-api)
 *           listOf(firebaseBackendModule) +     // pick ONE backend
 *           listOf(revenueCatModule) +          // monetization impl
 *           listOf(hostSchemaModule)            // host injects its SQLDelight schema here
 *       )
 *   }
 *
 * The host stays in control of conflicting picks (firebase OR supabase) so the
 * unused impl module is never linked.
 */
expect fun toolkitCoreModules(): List<Module>
