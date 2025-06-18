package com.tweener.kmpship.data.source.firebase.firestore.mapper

import com.tweener.kmpship.data.source.firebase.firestore.model.FirestoreUserModel
import com.tweener.kmpship.domain.entity.User
import com.tweener.kmpship.domain.mapper.ModelToEntityMapper

/**
 * @author Vivien Mahe
 * @since 15/04/2025
 */
class FirestoreUserModelMapper : ModelToEntityMapper<FirestoreUserModel, User> {

    override fun convertToEntity(model: FirestoreUserModel): User =
        User(
            id = model.id,
            email = model.email,
            isEmailVerified = model.isEmailVerified,
            photoUrl = model.photoUrl,

            // TODO Add here all user's properties
        )
}
