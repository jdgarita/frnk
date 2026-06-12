package dev.jdgarita.frnk.remoteconfig.firebase

import dev.jdgarita.frnk.remoteconfig.RemoteConfigService
import org.koin.dsl.module

/**
 * Firebase Remote Config binding. Install this in the host's `initializeFrnk(modules = …)` list
 * (over `noopRemoteConfigModule`) to read server-side values. Requires the native Firebase SDK +
 * config file supplied by the host.
 */
val remoteConfigModule =
    module {
        single<RemoteConfigService> { FirebaseRemoteConfigService() }
    }
