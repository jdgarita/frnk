package com.tweener.kmpship.domain.usecase.settings

import com.tweener.kmpship.domain.repository.SettingsRepository
import com.tweener.kmpship.domain.usecase.CompletableUseCase

/**
 * @author Vivien Mahe
 * @since 23/02/2025
 */
class SetCrashlyticsAuthorizationStateUseCase(
    private val settingsRepository: SettingsRepository,
) : CompletableUseCase<SetCrashlyticsAuthorizationStateUseCase.InputParams>() {

    class InputParams(
        val authorized: Boolean
    )

    override suspend fun buildUseCase(inputParams: InputParams?) {
        val params = assertInputParamsNotNull(inputParams)

        settingsRepository.setCrashlyticsAuthorizationState(SettingsRepository.InputParams.SetCrashlyticsAuthorizationState(authorized = params.authorized))
    }
}
