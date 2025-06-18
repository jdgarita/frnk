package com.tweener.kmpship.data.source.local.mapper

import com.tweener.kmpship.domain.entity.ThemeType
import com.tweener.kmpship.domain.mapper.EntityMapper

/**
 * @author Vivien Mahe
 * @since 13/08/2024
 */
class LocalThemeTypeModelMapper : EntityMapper<ThemeType, String> {

    companion object {
        private const val LIGHT_THEME_TYPE = "light"
        private const val DARK_THEME_TYPE = "dark"
        private const val SYSTEM_THEME_TYPE = "system"
    }

    override fun convertToModel(entity: ThemeType): String =
        when (entity) {
            ThemeType.LIGHT -> LIGHT_THEME_TYPE
            ThemeType.DARK -> DARK_THEME_TYPE
            ThemeType.SYSTEM -> SYSTEM_THEME_TYPE
        }

    override fun convertToEntity(model: String): ThemeType =
        when (model) {
            LIGHT_THEME_TYPE -> ThemeType.LIGHT
            DARK_THEME_TYPE -> ThemeType.DARK
            SYSTEM_THEME_TYPE -> ThemeType.SYSTEM
            else -> ThemeType.SYSTEM
        }
}
