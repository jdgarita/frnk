package com.tweener.kmpship.data.source.firebase.firestore.mapper

import com.tweener.kmpship.domain.mapper.EntityMapper
import com.tweener.kmpkit.Platform

/**
 * @author Vivien Mahe
 * @since 15/04/2025
 */
class FirestorePlatformModelMapper : EntityMapper<Platform, String> {

    companion object {
        private const val ANDROID = "android"
        private const val IOS = "ios"
        private const val WEB = "web"
        private const val DESKTOP = "desktop"
    }

    override fun convertToModel(entity: Platform): String =
        when (entity) {
            Platform.ANDROID -> "android"
            Platform.IOS -> "ios"
            Platform.JS, Platform.WASM_JS -> "web"
            Platform.JVM -> "desktop"
        }

    override fun convertToEntity(model: String): Platform =
        when (model) {
            ANDROID -> Platform.ANDROID
            IOS -> Platform.IOS
            WEB -> Platform.JS
            DESKTOP -> Platform.JVM
            else -> throw IllegalArgumentException("Unknown platform: $model")
        }
}
