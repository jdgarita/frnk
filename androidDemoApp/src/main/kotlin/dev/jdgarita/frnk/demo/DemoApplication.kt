package dev.jdgarita.frnk.demo

import android.app.Application
import dev.jdgarita.frnk.database.impl.DatabaseContext
import dev.jdgarita.frnk.shared.BackendChoice

class DemoApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // The toolkit's default database/key-value bindings read this lateinit Context.
        DatabaseContext.application = this
        bootstrapDemoKoin(BackendChoice.Supabase)
    }
}
