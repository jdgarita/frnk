package com.tweener.kmpship.data.source.local.mapper

import com.tweener.kmpship.domain.entity.UserAuthProvider
import com.tweener.kmpship.domain.mapper.EntityMapper

/**
 * @author Vivien Mahe
 * @since 25/08/2024
 */
class LocalAuthProviderMapper : EntityMapper<UserAuthProvider, String> {

    companion object {
        private const val GOOGLE = "google"
        private const val APPLE = "apple"
        private const val EMAIL = "email"

    }

    override fun convertToModel(entity: UserAuthProvider): String =
        when (entity) {
            UserAuthProvider.GOOGLE -> GOOGLE
            UserAuthProvider.APPLE -> APPLE
            UserAuthProvider.EMAIL -> EMAIL
        }

    override fun convertToEntity(model: String): UserAuthProvider =
        when (model) {
            GOOGLE -> UserAuthProvider.GOOGLE
            APPLE -> UserAuthProvider.APPLE
            EMAIL -> UserAuthProvider.EMAIL
            else -> throw IllegalArgumentException("Auth provider unknown")
        }
}
