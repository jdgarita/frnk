package dev.jdgarita.frnk.data

import dev.jdgarita.frnk.data.DataConstants.LOCAL_STORAGE_KEY_IS_EMAIL_VERIFIED
import dev.jdgarita.frnk.data.DataConstants.LOCAL_STORAGE_KEY_USER_SYNC_REQUESTED
import dev.jdgarita.frnk.data._internal.libs.usersession.UserSessionEvent
import dev.jdgarita.frnk.data._internal.libs.usersession.UserSessionService
import dev.jdgarita.frnk.data.source.firebase.firestore.datasource.FirestoreUserDataSource
import dev.jdgarita.frnk.data.source.firebase.firestore.mapper.FirestorePlatformModelMapper
import dev.jdgarita.frnk.data.source.local.datasource.LocalStorageDataSource
import com.tweener.kmpkit.currentPlatform
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

/**
 * @author Vivien Mahe
 * @since 15/04/2025
 */
class UserSyncScheduler(
    private val userSessionService: UserSessionService,
    private val firestorePlatformModelMapper: FirestorePlatformModelMapper,
    private val localStorageDataSource: LocalStorageDataSource,
    private val firestoreUserDataSource: FirestoreUserDataSource,
) {

    companion object {
        private val SYNC_DELAY = 5.seconds
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun scheduleSync() {
        scope.launch {
            flow {
                emit(Unit)

                while (true) {
                    delay(SYNC_DELAY)
                    emit(Unit)
                }
            }.collectLatest {
                if (shouldSyncUser()) syncUserInfo()
            }
        }
    }

    suspend fun setUserSyncRequested(requested: Boolean) {
        localStorageDataSource.setBoolean(LOCAL_STORAGE_KEY_USER_SYNC_REQUESTED, requested)
    }

    private suspend fun shouldSyncUser(): Boolean =
        localStorageDataSource.getBoolean(LOCAL_STORAGE_KEY_USER_SYNC_REQUESTED, false)

    private suspend fun syncUserInfo() {
        val event = userSessionService.event.firstOrNull()
        if (event == null) {
            Napier.d { "${this::class.simpleName} syncUserInfo: event is null, no updated needed." }
            return
        }

        Napier.d { "${this::class.simpleName} syncUserInfo: event=$event" }

        val firestoreUser = when (event) {
            is UserSessionEvent.LoggedIn -> event.firestoreUser
            is UserSessionEvent.UserUpdated -> event.firestoreUser
            else -> null
        }

        if (firestoreUser == null) {
            Napier.d { "${this::class.simpleName} syncUserInfo: user is not logged in, no updated needed." }
            return
        }

        // Update the user info to Firestore
        updateUserInfo(userId = firestoreUser.id)

        // Update the flag to false since there is nothing new to update for now
        setUserSyncRequested(requested = false)
    }

    private suspend fun updateUserInfo(userId: String) {
        Napier.d { "${this::class.simpleName} Updating user info to Firestore.." }

        val isEmailVerified = localStorageDataSource.getBoolean(LOCAL_STORAGE_KEY_IS_EMAIL_VERIFIED, false)
        val platform = firestorePlatformModelMapper.convertToModel(currentPlatform)

        firestoreUserDataSource.updateUser(
            id = userId,
            platform = platform,
            isEmailVerified = isEmailVerified,
        )
    }
}
