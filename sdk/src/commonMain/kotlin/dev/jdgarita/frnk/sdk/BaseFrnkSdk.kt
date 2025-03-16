package dev.jdgarita.frnk.sdk

import co.touchlab.kermit.DefaultFormatter
import co.touchlab.kermit.Severity
import co.touchlab.kermit.loggerConfigInit
import co.touchlab.kermit.platformLogWriter
import dev.jdgarita.frnk.domain.config.AppConfiguration
import dev.jdgarita.frnk.presentation.framework.navigation.NavigationConfiguration
import dev.jdgarita.frnk.util.common.Log
import dev.jdgarita.frnk.util.common.configureLog
import org.koin.core.Koin
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin as stopKoinInternal
import org.koin.core.logger.Level
import org.koin.core.module.Module
import org.koin.mp.KoinPlatformTools

/**
 * Base class for the Swiftly SDK.
 */
abstract class BaseFrnkSdk : FrnkSdk {

    val koin: Koin
        get() = _koin
    private lateinit var _koin: Koin
    val isKoinStarted
        get() = _isKoinStarted
    private var _isKoinStarted = false

    protected lateinit var navigationConfiguration: NavigationConfiguration

    override fun initialize(
        appConfiguration: AppConfiguration,
        navigationConfiguration: NavigationConfiguration
    ) = apply {
        if (isKoinStarted) {
            return this
        }

        this.navigationConfiguration = navigationConfiguration

        configureLogging()

        Log.i { "Initializing Frnk SDK" }
        Log.d { "App configuration $appConfiguration" }
        Log.d { "Nav configuration $navigationConfiguration" }

        initKoin(appConfiguration)

        afterInitialize()
        Log.i { "Frnk SDK initialized" }
    }

    private fun configureLogging() {
        Log.configureLog(
            loggerConfigInit(
                platformLogWriter(DefaultFormatter),
                minSeverity = Severity.Verbose
            ),
            "FrnkSDK"
        )
    }

    private fun initKoin(appConfiguration: AppConfiguration) {
        startKoin {
            // allowOverride(appConfiguration.packageConfiguration.isInternalBuild)
            logger(KoinPlatformTools.defaultLogger(Level.INFO))
            koin.loadModules(getModules(appConfiguration, navigationConfiguration))
            this@BaseFrnkSdk._koin = koin
            _isKoinStarted = true
        }
    }

    protected fun stopKoin() {
        stopKoinInternal()
        _isKoinStarted = false
    }

    protected open fun afterInitialize() = Unit

    /**
     * Return the default list of Koin modules to load.
     */
    protected abstract fun getModules(
        appConfiguration: AppConfiguration,
        navigationConfiguration: NavigationConfiguration
    ): List<Module>
}