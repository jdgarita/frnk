package dev.jdgarita.frnk.di

import android.content.Context
import dev.jdgarita.frnk.database.impl.DatabaseContext
import org.koin.android.ext.koin.androidContext
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
 *     initializeFrnk(context = this, modules = frnkUiModules() + databaseModule + …)
 * }
 * ```
 *
 * iOS hosts keep calling the common [initializeFrnk] (no context to thread).
 */
fun initializeFrnk(
    context: Context,
    modules: List<Module>,
    extraConfig: KoinApplication.() -> Unit = {},
): KoinApplication {
    val applicationContext = context.applicationContext
    DatabaseContext.application = applicationContext
    return initializeFrnk(modules) {
        androidContext(applicationContext)
        extraConfig()
    }
}
