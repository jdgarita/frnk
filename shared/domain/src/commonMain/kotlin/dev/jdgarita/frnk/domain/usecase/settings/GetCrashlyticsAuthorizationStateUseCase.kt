package dev.jdgarita.frnk.domain.usecase.settings

import dev.jdgarita.frnk.domain.repository.SettingsRepository
import dev.jdgarita.frnk.domain.usecase.StreamUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * @author Vivien Mahe
 * @since 23/02/2025
 */
class GetCrashlyticsAuthorizationStateUseCase(
    private val settingsRepository: SettingsRepository
) : StreamUseCase<Nothing, GetCrashlyticsAuthorizationStateUseCase.OutputParams>() {

    class OutputParams(
        val authorized: Boolean
    )

    override suspend fun buildUseCase(inputParams: Nothing?): Flow<OutputParams> =
        settingsRepository
            .getCrashlyticsAuthorizationState()
            .map { OutputParams(authorized = it.authorized) }
}
