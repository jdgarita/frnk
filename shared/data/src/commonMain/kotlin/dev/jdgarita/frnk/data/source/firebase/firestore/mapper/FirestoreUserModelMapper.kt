package dev.jdgarita.frnk.data.source.firebase.firestore.mapper

import dev.jdgarita.frnk.data.source.firebase.firestore.model.FirestoreUserModel
import dev.jdgarita.frnk.domain.entity.User
import dev.jdgarita.frnk.domain.mapper.ModelToEntityMapper

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
