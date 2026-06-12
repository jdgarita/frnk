package dev.jdgarita.frnk.remoteconfig

import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * No-op remote-config binding — the default for hosts without a remote-config backend. Install this
 * **or** `remoteConfigModule` (in `:remote-config-impl`) in the explicit module list passed to
 * `initializeFrnk(...)`, never both.
 */
val noopRemoteConfigModule: Module =
    module {
        single<RemoteConfigService> { NoopRemoteConfig() }
    }
