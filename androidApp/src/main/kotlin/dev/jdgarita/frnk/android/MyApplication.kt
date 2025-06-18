package dev.jdgarita.frnk.android

import android.app.Application
import dev.jdgarita.frnk._internal.di.sharedAndroidModule
import dev.jdgarita.frnk._internal.libs.LibrariesConfiguration
import org.koin.android.ext.android.inject
import org.koin.core.context.startKoin

/**
 * @author Vivien Mahe
 * @since 05/06/2023
 */
class MyApplication : Application() {

    private val librariesConfiguration: LibrariesConfiguration by inject()

    override fun onCreate() {
        super.onCreate()

        startKoin {
            modules(sharedAndroidModule(context = this@MyApplication.applicationContext))
        }

        librariesConfiguration.init()
    }
}
