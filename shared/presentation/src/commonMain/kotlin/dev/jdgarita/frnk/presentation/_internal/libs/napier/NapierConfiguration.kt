package dev.jdgarita.frnk.presentation._internal.libs.napier

import com.tweener.firebase.crashlytics.FirebaseCrashlyticsService
import com.tweener.passage.Passage
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * @author Vivien Mahe
 * @since 21/09/2023
 */
class NapierConfiguration(
    private val isDebug: Boolean,
    private val passage: Passage,
    private val crashlyticsAntilog: CrashlyticsAntilog,
    private val firebaseCrashlyticsService: FirebaseCrashlyticsService,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun init() {
        // Listen to updates on the currentUser
        scope.launch {
            passage.getCurrentUserAsFlow().collect { currentUser -> firebaseCrashlyticsService.getCrashlytics().setUserId(userId = currentUser?.uid ?: "") }
        }

        when (isDebug) {
            true -> Napier.base(DebugAntilog())
            false -> Napier.base(crashlyticsAntilog)
        }
    }
}
