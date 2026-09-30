package dev.jdgarita.frnk.ui.app.shell

import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/**
 * Koin binding for [FrnkStackShell]'s sheet-presentation ViewModel. `frnkUiModules()` includes it, so a
 * host that installs the toolkit's UI modules needs nothing more.
 */
val frnkShellModule: Module =
    module {
        viewModel { FrnkShellViewModel() }
    }