package dev.jdgarita.frnk.domain.usecase.settings

import dev.jdgarita.frnk.domain.entity.ThemeType
import dev.jdgarita.frnk.domain.repository.SettingsRepository
import dev.jdgarita.frnk.domain.usecase.StreamUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * @author Vivien Mahe
 * @since 18/10/2023
 */
class GetThemeTypeUseCase(
    private val settingsRepository: SettingsRepository
) : StreamUseCase<Nothing, GetThemeTypeUseCase.OutputParams>() {

    class OutputParams(
        val themeType: ThemeType
    )

    override suspend fun buildUseCase(inputParams: Nothing?): Flow<OutputParams> =
        settingsRepository
            .getThemeType()
            .map { OutputParams(themeType = it.themeType) }
}
