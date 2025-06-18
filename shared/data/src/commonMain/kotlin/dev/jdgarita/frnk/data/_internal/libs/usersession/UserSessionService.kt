package dev.jdgarita.frnk.data._internal.libs.usersession

import dev.jdgarita.frnk.data.source.firebase.firestore.datasource.FirestoreUserDataSource
import dev.jdgarita.frnk.data.source.firebase.firestore.model.FirestoreUserModel
import com.tweener.passage.Passage
import com.tweener.passage.model.Entrant
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.IO
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.retryWhen
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

/**
 * @author Vivien Mahe
 * @since 23/01/2025
 */
class UserSessionService(
    private val passage: Passage,
    private val firestoreUserDataSource: FirestoreUserDataSource,
) {

    private val _event = MutableSharedFlow<UserSessionEvent>(replay = 1)
    val event: SharedFlow<UserSessionEvent> = _event.asSharedFlow()

    private val scope = CoroutineScope(SupervisorJob() +Dispatchers.IO)
    private var previousUser: FirestoreUserModel? = null

    @OptIn(ExperimentalCoroutinesApi::class)
    fun subscribeToUserUpdates() {
        Napier.d { "UserSessionService#subscribeToUserUpdates()" }

        scope.launch {
            passage
                .getCurrentUserAsFlow()
                .flatMapLatest { entrant ->
                    Napier.d { "UserSessionService entrant? $entrant" }

                    entrant?.uid
                        ?.let { uid ->
                            firestoreUserDataSource
                                .getUserAsFlow(id = uid)
                                .retryWhen { cause, attempt ->
                                    Napier.d { "UserSessionService Firestore getUserAsFlow retry with error: $cause, attempt: $attempt" }

                                    if (previousUser != null) {
                                        // There previously was a user, but not anymore. The user account has been deleted. No need to retry.
                                        Napier.d { "UserSessionService User account has been deleted." }
                                        false
                                    } else {
                                        Napier.d { "UserSessionService User account has not been created yet." }

                                        previousUser = null

                                        if (cause is IllegalArgumentException && attempt == 0L) {
                                            // User does not exist yet in Firestore, let's create it
                                            _event.emit(UserSessionEvent.NeedsCreation(entrant = entrant))
                                        }

                                        delay(1.seconds) // Wait before retrying so Firestore has time to create the user

                                        true
                                    }
                                }
                                .onEach { updatedFirestoreUser ->
                                    Napier.d { "UserSessionService updatedFirestoreUser? $updatedFirestoreUser" }

                                    // If the user was authenticated and still is, emit a UserUpdated event.
                                    // If the user was not authenticated and now is, emit a LoggedIn event.
                                    previousUser
                                        ?.let { _event.emit(UserSessionEvent.UserUpdated(firestoreUser = updatedFirestoreUser)) }
                                        ?: _event.emit(UserSessionEvent.LoggedIn(entrant = entrant, firestoreUser = updatedFirestoreUser))

                                    previousUser = updatedFirestoreUser
                                }
                                .catch { throwable ->
                                    Napier.e { "UserSessionService Firestore getUserAsFlow error: $throwable" }

                                    previousUser = null
                                }
                        }
                        ?: flow {
                            // If the user was authenticated and now is not, emit a LoggedOut event.
                            // If the user was not authenticated and still is not, emit an Idle event
                            previousUser
                                ?.let { _event.emit(UserSessionEvent.LoggedOut(firestoreUser = it)) }
                                ?: _event.emit(UserSessionEvent.Idle)

                            previousUser = null
                        }
                }
                .collect()
        }
    }

    fun logout() {
        scope.launch {
            passage.signOut()
        }
    }
}

sealed class UserSessionEvent {
    data object Idle : UserSessionEvent()
    data class NeedsCreation(val entrant: Entrant) : UserSessionEvent()
    data class LoggedIn(val entrant: Entrant, val firestoreUser: FirestoreUserModel) : UserSessionEvent()
    data class UserUpdated(val firestoreUser: FirestoreUserModel) : UserSessionEvent()
    data class LoggedOut(val firestoreUser: FirestoreUserModel) : UserSessionEvent()
}
