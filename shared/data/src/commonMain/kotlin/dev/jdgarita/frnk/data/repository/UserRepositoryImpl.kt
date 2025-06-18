package dev.jdgarita.frnk.data.repository

import com.tweener.firebase.firestore.toLocalDateTime
import com.tweener.firebase.firestore.toTimestamp
import com.tweener.kmpkit.currentPlatform
import com.tweener.kmpkit.currentPlatformVersion
import com.tweener.kmpkit.kotlinextensions.now
import dev.jdgarita.frnk.data.DataConstants.LOCAL_STORAGE_ACCOUNT_CREATION_DATE
import dev.jdgarita.frnk.data.DataConstants.LOCAL_STORAGE_APP_REVIEW_REQUESTED
import dev.jdgarita.frnk.data.DataConstants.LOCAL_STORAGE_KEY_AUTH_PROVIDER_ID
import dev.jdgarita.frnk.data.DataConstants.LOCAL_STORAGE_KEY_FIRST_TIME_PAYWALL_SHOWN
import dev.jdgarita.frnk.data.DataConstants.LOCAL_STORAGE_KEY_IS_EMAIL_VERIFIED
import dev.jdgarita.frnk.data.DataConstants.LOCAL_STORAGE_KEY_LAST_ASK_APP_REVIEW_DATE
import dev.jdgarita.frnk.data.DataConstants.LOCAL_STORAGE_KEY_NOTIFICATIONS_PERMISSION_ASKED
import dev.jdgarita.frnk.data.UserSyncScheduler
import dev.jdgarita.frnk.data._internal.libs.room.RoomDatabaseHelper
import dev.jdgarita.frnk.data._internal.libs.usersession.UserSessionEvent
import dev.jdgarita.frnk.data._internal.libs.usersession.UserSessionService
import dev.jdgarita.frnk.data.source.firebase.firestore.datasource.FirestoreUserDataSource
import dev.jdgarita.frnk.data.source.firebase.firestore.mapper.FirestorePlatformModelMapper
import dev.jdgarita.frnk.data.source.firebase.firestore.mapper.FirestoreUserModelMapper
import dev.jdgarita.frnk.data.source.firebase.firestore.model.FirestoreUserModel
import dev.jdgarita.frnk.data.source.local.datasource.LocalStorageDataSource
import dev.jdgarita.frnk.data.source.local.mapper.LocalAuthProviderMapper
import dev.jdgarita.frnk.data.source.revenuecat.datasource.RevenueCatDataSource
import dev.jdgarita.frnk.domain.error.UserNotAuthenticatedException
import dev.jdgarita.frnk.domain.repository.UserRepository
import com.tweener.passage.Passage
import com.tweener.passage.gatekeeper.email.model.PassageEmailAuthParams
import com.tweener.passage.model.Entrant
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDateTime

/**
 * @author Vivien Mahe
 * @since 17/10/2023
 */
class UserRepositoryImpl(
    private val passage: Passage,
    private val userSessionService: UserSessionService,
    private val userSyncScheduler: UserSyncScheduler,
    private val roomDatabaseHelper: RoomDatabaseHelper,
    private val localAuthProviderMapper: LocalAuthProviderMapper,
    private val firestoreUserModelMapper: FirestoreUserModelMapper,
    private val firestorePlatformModelMapper: FirestorePlatformModelMapper,
    private val localStorageDataSource: LocalStorageDataSource,
    private val firestoreUserDataSource: FirestoreUserDataSource,
    private val revenueCatDataSource: RevenueCatDataSource,
) : UserRepository {

    private val scope = CoroutineScope(Dispatchers.IO)
    private val _authenticatedUser = MutableSharedFlow<FirestoreUserModel?>(replay = 1)

    init {
        userSyncScheduler.scheduleSync()
        userSessionService.subscribeToUserUpdates()
        observeUserSession()
    }

    override suspend fun isAuthenticated(): Flow<UserRepository.OutputParams.IsAuthenticated> =
        _authenticatedUser.map { UserRepository.OutputParams.IsAuthenticated(authenticated = it != null) }

    override suspend fun getAuthenticatedUser(): Flow<Result<UserRepository.OutputParams.GetAuthenticatedUser>> =
        _authenticatedUser
            .map { firestoreUser ->
                firestoreUser
                    ?.let {
                        val user = firestoreUserModelMapper.convertToEntity(it)
                        Result.success(UserRepository.OutputParams.GetAuthenticatedUser(user = user))
                    }
                    ?: Result.failure(UserNotAuthenticatedException())
            }

    override suspend fun setAuthenticatedUserProvider(inputParams: UserRepository.InputParams.SetAuthenticatedUserProvider) {
        val authProvider = localAuthProviderMapper.convertToModel(inputParams.provider)
        localStorageDataSource.setString(LOCAL_STORAGE_KEY_AUTH_PROVIDER_ID, authProvider)
    }

    override suspend fun getAuthenticatedUserProvider(): Result<UserRepository.OutputParams.GetAuthenticatedUserProvider> {
        Napier.d { "${this::class.simpleName}#getAuthenticatedUserProvider()" }

        return try {
            val authProviderId = localStorageDataSource.getString(LOCAL_STORAGE_KEY_AUTH_PROVIDER_ID)
            val provider = localAuthProviderMapper.convertToEntity(authProviderId ?: "")

            Result.success(UserRepository.OutputParams.GetAuthenticatedUserProvider(provider = provider))
        } catch (throwable: Throwable) {
            Result.failure(UserNotAuthenticatedException())
        }
    }

    override suspend fun setAppReviewRequested() {
        Napier.d { "${this::class.simpleName}#setAppReviewRequested()" }

        localStorageDataSource.setBoolean(LOCAL_STORAGE_APP_REVIEW_REQUESTED, true)
    }

    override suspend fun isAppReviewRequested(): Flow<UserRepository.OutputParams.IsAppReviewRequested> {
        Napier.d { "${this::class.simpleName}#isAppReviewRequested()" }

        return localStorageDataSource
            .getBooleanAsFlow(LOCAL_STORAGE_APP_REVIEW_REQUESTED, false)
            .map { UserRepository.OutputParams.IsAppReviewRequested(requested = it) }
    }

    override suspend fun setLastAskForAppReviewDate(inputParams: UserRepository.InputParams.SetLastAskForAppReviewDate) {
        Napier.d { "${this::class.simpleName}#setLastAskForAppReviewDate($inputParams)" }
        localStorageDataSource.setString(LOCAL_STORAGE_KEY_LAST_ASK_APP_REVIEW_DATE, inputParams.date.toString())
    }

    override suspend fun getLastAskForAppReviewDate(): UserRepository.OutputParams.GetLastAskForAppReviewDate {
        Napier.d { "${this::class.simpleName}#getLastAskForAppReviewDate()" }

        val lastAskForAppReviewDate = localStorageDataSource.getString(LOCAL_STORAGE_KEY_LAST_ASK_APP_REVIEW_DATE)
        return UserRepository.OutputParams.GetLastAskForAppReviewDate(date = lastAskForAppReviewDate?.let { LocalDateTime.parse(it) })
    }

    override suspend fun getAccountCreationDate(): UserRepository.OutputParams.GetAccountCreationDate {
        Napier.d { "${this::class.simpleName}#getAccountCreationDate()" }

        val accountCreationDate = localStorageDataSource.getString(LOCAL_STORAGE_ACCOUNT_CREATION_DATE)!!
        return UserRepository.OutputParams.GetAccountCreationDate(date = LocalDateTime.parse(accountCreationDate))
    }

    override suspend fun synchronize(inputParams: UserRepository.InputParams.Synchronize) {
        Napier.d { "${this::class.simpleName}#synchronize($inputParams)" }

        localStorageDataSource.setBoolean(key = LOCAL_STORAGE_KEY_IS_EMAIL_VERIFIED, value = inputParams.isEmailVerified)
    }

    override suspend fun delete(inputParams: UserRepository.InputParams.Delete): Result<Unit> {
        Napier.d { "${this::class.simpleName}#delete($inputParams)" }

        val uid = passage.getCurrentUser()?.uid ?: return Result.failure(UserNotAuthenticatedException())

        // If the user is authenticated via email/password, before deleting the account, we need to reauthenticate the user.
        // Since deleting user's account from Firebase Authentication is a sensitive operation, it only works if the user has been logged in within the 5 last minutes
        if (passage.getCurrentUser()?.email != null && inputParams.password != null) {
            Napier.d { "Reauthenticate user through Passage" }
            passage.reauthenticateWithEmailAndPassword(PassageEmailAuthParams(email = passage.getCurrentUser()?.email!!, password = inputParams.password!!))
        }

        // Remove any data in the local storage
        Napier.d { "Remove any data in the local storage" }
        localStorageDataSource.clear()

        // Delete Room database
        Napier.d { "Delete Room database" }
        roomDatabaseHelper.resetDatabase()

        // Log out from RevenueCat
        Napier.d { "Log out user from RevenueCat" }
        revenueCatDataSource.logOut()

        // Delete all user's data in Firestore
        Napier.d { "Delete all user's data in Firestore" }
        firestoreUserDataSource.deleteUser(id = uid)

        // Then delete the user's account from Firebase Authentication
        Napier.d { "Delete the user's account from Firebase Auth" }
        passage.deleteCurrentUser()

        Napier.d { "Delete done" }
        return Result.success(Unit)
    }

    override suspend fun signOut() {
        Napier.d { "${this::class.simpleName}#signOut()" }

        userSessionService.logout()
    }

    private suspend fun setAccountCreationDate(date: LocalDateTime) {
        Napier.d { "${this::class.simpleName}#setAccountCreationDate($date)" }

        localStorageDataSource.setString(LOCAL_STORAGE_ACCOUNT_CREATION_DATE, date.toString())
    }

    override suspend fun setEmailVerified(inputParams: UserRepository.InputParams.SetEmailVerified) {
        Napier.d { "${this::class.simpleName}#setEmailVerified($inputParams)" }

        localStorageDataSource.setBoolean(LOCAL_STORAGE_KEY_IS_EMAIL_VERIFIED, inputParams.isEmailVerified)

        // Request sync for user info
        userSyncScheduler.setUserSyncRequested(requested = true)
    }

    override suspend fun isEmailVerified(): Flow<UserRepository.OutputParams.IsEmailVerified> {
        Napier.d { "${this::class.simpleName}#isEmailVerified()" }

        return localStorageDataSource
            .getBooleanAsFlow(LOCAL_STORAGE_KEY_IS_EMAIL_VERIFIED, false)
            .map { UserRepository.OutputParams.IsEmailVerified(isEmailVerified = it) }
    }

    override suspend fun setNotificationsPermissionAsked() {
        Napier.d { "${this::class.simpleName}#setNotificationsPermissionAsked()" }

        localStorageDataSource.setBoolean(LOCAL_STORAGE_KEY_NOTIFICATIONS_PERMISSION_ASKED, true)
    }

    override suspend fun isNotificationsPermissionAsked(): Flow<UserRepository.OutputParams.IsNotificationsPermissionAsked> {
        Napier.d { "${this::class.simpleName}#isNotificationsPermissionAsked()" }

        return localStorageDataSource
            .getBooleanAsFlow(LOCAL_STORAGE_KEY_NOTIFICATIONS_PERMISSION_ASKED, false)
            .map { UserRepository.OutputParams.IsNotificationsPermissionAsked(isAsked = it) }
    }

    override suspend fun setFirstTimePaywallShown() {
        Napier.d { "${this::class.simpleName}#setDiscoverResultsDone()" }

        localStorageDataSource.setBoolean(key = LOCAL_STORAGE_KEY_FIRST_TIME_PAYWALL_SHOWN, value = true)
    }

    override suspend fun isFirstTimePaywallShown(): Flow<UserRepository.OutputParams.IsFirstTimePaywallShown> {
        Napier.d { "${this::class.simpleName}#isFirstTimePaywallShown()" }

        return localStorageDataSource
            .getBooleanAsFlow(LOCAL_STORAGE_KEY_FIRST_TIME_PAYWALL_SHOWN, false)
            .map { UserRepository.OutputParams.IsFirstTimePaywallShown(shown = it) }
    }


    private fun observeUserSession() {
        userSessionService.event
            .map { event ->
                Napier.d { "UserSessionService event: $event" }

                when (event) {
                    is UserSessionEvent.Idle -> _authenticatedUser.emit(null)
                    is UserSessionEvent.NeedsCreation -> onCreateUser(entrant = event.entrant)
                    is UserSessionEvent.LoggedIn -> onUserLoggedIn(entrant = event.entrant, firestoreUser = event.firestoreUser)
                    is UserSessionEvent.LoggedOut -> onUserLoggedOut(firestoreUser = event.firestoreUser)
                    is UserSessionEvent.UserUpdated -> _authenticatedUser.emit(event.firestoreUser)
                }
            }
            .launchIn(scope)
    }

    private suspend fun onCreateUser(entrant: Entrant) {
        Napier.d { "${this::class.simpleName}#onCreateUser($entrant)" }

        // Get the current time from server for the user's account creation date
//        val serverNow = timeApiDataSource.getDateTime().let { timeModelMapper.convertToEntity(it) }.toTimestamp()
        val serverNow = LocalDateTime.now().toTimestamp()

        // Get Firestore user info
        val isEmailVerified = entrant.isEmailVerified

        firestoreUserDataSource.createUser(
            id = entrant.uid,
            email = entrant.email!!,
            photoUrl = entrant.photoUrl,
            platform = firestorePlatformModelMapper.convertToModel(currentPlatform),
            accountCreationDate = serverNow,
            isEmailVerified = isEmailVerified,
        )

        setAccountCreationDate(date = serverNow.toLocalDateTime())
        setEmailVerified(UserRepository.InputParams.SetEmailVerified(isEmailVerified = isEmailVerified))

        Napier.d { "User (ID: ${entrant.uid}) has been created!" }
    }

    private suspend fun onUserLoggedIn(entrant: Entrant, firestoreUser: FirestoreUserModel) {
        Napier.d { "${this::class.simpleName}#onUserLoggedIn(entrant=$entrant, firestoreUser=$firestoreUser)" }

        // We need to make sure the following values are set in the case the user reinstalled the app but already had an account.
        // TODO Add here all the values that need to be set from Local storage
//        localStorageDataSource.setBoolean(LOCAL_STORAGE_KEY_INTRODUCTION_DONE, true)

        // Log in the user in RevenueCat
        revenueCatDataSource.logIn(userId = entrant.uid, email = entrant.email!!, deviceVersion = currentPlatformVersion)

        // Log in the user for analytics
//        analyticsTracker.identifyUser(userId = entrant.uid)

        // Emit the authenticated user
        _authenticatedUser.emit(firestoreUser)

        Napier.d { "User (ID: ${entrant.uid} has been signed in!" }
    }

    private suspend fun onUserLoggedOut(firestoreUser: FirestoreUserModel) {
        Napier.d { "${this::class.simpleName}#onUserLoggedOut(firestoreUser=$firestoreUser)" }

        // Log out from RevenueCat
        revenueCatDataSource.logOut()

        // Log out from analytics
//        analyticsTracker.clear()

        // Clear local storage
        localStorageDataSource.clear()

        // Reset Room database
        roomDatabaseHelper.resetDatabase()

        // Reset the authenticated user to null
        _authenticatedUser.emit(null)

        Napier.d { "User (ID: ${firestoreUser.id}) has been signed out!" }
    }
}
