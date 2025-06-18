package com.tweener.kmpship.data.source.firebase.firestore.model

import com.tweener.firebase.firestore.model.FirestoreModel
import com.tweener.kmpship.data.source.firebase.firestore.datasource.FirestoreUserDataSource.Companion.ACCOUNT_CREATION_DATE_PROPERTY
import com.tweener.kmpship.data.source.firebase.firestore.datasource.FirestoreUserDataSource.Companion.EMAIL_PROPERTY
import com.tweener.kmpship.data.source.firebase.firestore.datasource.FirestoreUserDataSource.Companion.IS_EMAIL_VERIFIED_PROPERTY
import com.tweener.kmpship.data.source.firebase.firestore.datasource.FirestoreUserDataSource.Companion.LAST_SEEN_APP_VERSION_PROPERTY
import com.tweener.kmpship.data.source.firebase.firestore.datasource.FirestoreUserDataSource.Companion.PHOTO_URL_PROPERTY
import com.tweener.kmpship.data.source.firebase.firestore.datasource.FirestoreUserDataSource.Companion.PLATFORM_PROPERTY
import dev.gitlive.firebase.firestore.Timestamp
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * @author Vivien Mahe
 * @since 15/01/2024
 */

@Serializable
data class FirestoreUserModel(
    override var id: String,
    @SerialName(EMAIL_PROPERTY) val email: String,
    @SerialName(PHOTO_URL_PROPERTY) val photoUrl: String?,
    @SerialName(PLATFORM_PROPERTY) val platform: String,
    @SerialName(ACCOUNT_CREATION_DATE_PROPERTY) val accountCreationDate: Timestamp,
    @SerialName(IS_EMAIL_VERIFIED_PROPERTY) val isEmailVerified: Boolean,
    @SerialName(LAST_SEEN_APP_VERSION_PROPERTY) val lastSeenAppVersion: String,
) : FirestoreModel()
