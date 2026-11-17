package dev.jdgarita.frnk.notifications.impl

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Translucent, UI-less activity that hosts the Android 13+ `POST_NOTIFICATIONS` prompt, so
 * `requestPermission()` is a plain suspend call (usable from a ViewModel) instead of needing an
 * `ActivityResultLauncher` registered by the host. Concurrent requests are serialized.
 */
internal class NotificationPermissionActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Recreated mid-prompt (config change): the pending result is delivered to this instance.
        if (savedInstanceState == null) requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), REQUEST_CODE)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        // Completes on every finishing path (answered, dismissed, killed task) so the caller never hangs.
        if (isFinishing) pending?.complete(Unit)
    }

    companion object {
        private const val REQUEST_CODE = 0x0F12
        private val mutex = Mutex()

        @Volatile
        private var pending: CompletableDeferred<Unit>? = null

        /** Shows the prompt and suspends until it is answered or dismissed. */
        suspend fun request(context: Context) {
            mutex.withLock {
                val done = CompletableDeferred<Unit>()
                pending = done
                context.startActivity(
                    Intent(context, NotificationPermissionActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                )
                try {
                    done.await()
                } finally {
                    pending = null
                }
            }
        }
    }
}