package com.tweener.kmpship.domain.usecase.settings

import com.tweener.kmpship.domain.entity.ThemeType
import com.tweener.kmpship.domain.repository.SettingsRepository
import com.tweener.kmpship.domain.usecase.CompletableUseCase

/**
 * @author Vivien Mahe
 * @since 18/10/2023
 */
class SetThemeTypeUseCase(
    private val settingsRepository: SettingsRepository
) : CompletableUseCase<SetThemeTypeUseCase.InputParams>() {

    class InputParams(
        val themeType: ThemeType,
    )

    override suspend fun buildUseCase(inputParams: InputParams?) {
        val params = assertInputParamsNotNull(inputParams)

        settingsRepository.setThemeType(SettingsRepository.InputParams.SetThemeType(themeType = params.themeType))
    }
}
