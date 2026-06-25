package dev.jdgarita.frnk.di

import android.content.Context
import org.koin.android.ext.koin.androidContext
import org.koin.core.Koin
import org.koin.core.KoinApplication
import org.koin.core.module.Module

/**
 * Android one-shot bootstrap: [initializeFrnk] plus the two Android-only wiring steps every host
 * used to hand-roll in `Application.onCreate` — setting [DatabaseContext.application] (so the
 * SQLDelight driver and SharedPreferences-backed `KeyValueStore` can resolve) and registering
 * `androidContext(...)` on the Koin application.
 *
 * Call from `Application.onCreate()`:
 *
 * ```kotlin
 * override fun onCreate() {
 *     super.onCreate()
 *     initializeFrnk(context = this, modules = frnkUiModules() + databaseModule + prefsModule + …)
 * }
 * ```
 *
 * iOS hosts keep calling the common [initializeFrnk] (no context to thread).
 *
 * [validate]/[validator] forward to the common overload — pass `validator = Koin::validateFrnkBootstrap`
 * (`:ui-app`) to fail fast on a missing required module.
 */
fun initializeFrnk(
    context: Context,
    modules: List<Module>,
    validate: Boolean = false,
    validator: (Koin) -> Unit = {},
    extraConfig: KoinApplication.() -> Unit = {}
): KoinApplication {
    val applicationContext = context.applicationContext
    DatabaseContext.application = applicationContext
    return initializeFrnk(modules, validate, validator) {
        androidContext(applicationContext)
        extraConfig()
    }
}