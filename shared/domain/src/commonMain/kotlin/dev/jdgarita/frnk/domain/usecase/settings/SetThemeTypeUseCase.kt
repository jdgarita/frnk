package dev.jdgarita.frnk.domain.usecase.settings

import dev.jdgarita.frnk.domain.entity.ThemeType
import dev.jdgarita.frnk.domain.repository.SettingsRepository
import dev.jdgarita.frnk.domain.usecase.CompletableUseCase

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
