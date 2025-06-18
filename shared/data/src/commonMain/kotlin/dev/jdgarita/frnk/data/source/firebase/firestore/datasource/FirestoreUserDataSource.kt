package dev.jdgarita.frnk.data.source.firebase.firestore.datasource

import com.tweener.firebase.firestore.FirebaseFirestoreService
import dev.jdgarita.frnk.data.source.firebase.firestore.model.FirestoreUserModel
import dev.gitlive.firebase.firestore.Timestamp
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * @author Vivien Mahe
 * @since 15/01/2024
 */
class FirestoreUserDataSource(
    private val versionName: String,
    private val firebaseFirestoreService: FirebaseFirestoreService,
) {

    companion object {
        private const val USERS_COLLECTION_NAME = "users"

        const val EMAIL_PROPERTY = "email"
        const val PHOTO_URL_PROPERTY = "photoUrl"
        const val PLATFORM_PROPERTY = "platform"
        const val ACCOUNT_CREATION_DATE_PROPERTY = "accountCreationDate"
        const val IS_EMAIL_VERIFIED_PROPERTY = "isEmailVerified"
        const val LAST_SEEN_APP_VERSION_PROPERTY = "lastSeenAppVersion"
    }

    suspend fun getUser(id: String): FirestoreUserModel =
        firebaseFirestoreService.get<FirestoreUserModel>(collection = USERS_COLLECTION_NAME, id = id).copy(id = id)

    fun getUserAsFlow(id: String): Flow<FirestoreUserModel> =
        firebaseFirestoreService.getAsFlow<FirestoreUserModel>(collection = USERS_COLLECTION_NAME, id = id).map { it.copy(id = id) }

    suspend fun createUser(id: String, email: String, photoUrl: String? = null, platform: String, accountCreationDate: Timestamp, isEmailVerified: Boolean) =
        firebaseFirestoreService.create<FirestoreUserModel>(
            collection = USERS_COLLECTION_NAME,
            id = id,
            data = hashMapOf(
                EMAIL_PROPERTY to email,
                PHOTO_URL_PROPERTY to photoUrl,
                PLATFORM_PROPERTY to platform,
                ACCOUNT_CREATION_DATE_PROPERTY to accountCreationDate,
                IS_EMAIL_VERIFIED_PROPERTY to isEmailVerified,
                LAST_SEEN_APP_VERSION_PROPERTY to versionName,
            )
        )

    suspend fun updateUser(
        id: String,
        platform: String,
        isEmailVerified: Boolean,
    ) {
        firebaseFirestoreService.update(
            collection = USERS_COLLECTION_NAME,
            id = id,
            data = mutableMapOf<String, Any?>().apply {
                put(PLATFORM_PROPERTY, platform)
                put(IS_EMAIL_VERIFIED_PROPERTY, isEmailVerified)
                put(LAST_SEEN_APP_VERSION_PROPERTY, versionName)
            }
        )
    }

    suspend fun deleteUser(id: String) {
        firebaseFirestoreService.delete(
            collection = USERS_COLLECTION_NAME,
            id = id,
        )
    }
}
