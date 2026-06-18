package dev.jdgarita.frnk.permissions

import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * Binds the no-op [PermissionController] default. A real impl module would override this with a
 * platform binding; today the scaffold ships only the no-op.
 */
val permissionsModule: Module =
    module {
        single<PermissionController> { NoopPermissionController() }
    }