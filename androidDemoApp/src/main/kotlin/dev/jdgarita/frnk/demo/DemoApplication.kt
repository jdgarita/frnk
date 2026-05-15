package dev.jdgarita.frnk.demo

import android.app.Application

class DemoApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        bootstrapDemoKoin()
    }
}
