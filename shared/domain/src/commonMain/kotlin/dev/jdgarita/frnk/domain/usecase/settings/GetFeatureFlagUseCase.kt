package dev.jdgarita.frnk.domain.usecase.settings

import dev.jdgarita.frnk.domain.entity.FeatureFlag
import dev.jdgarita.frnk.domain.repository.FeatureFlagRepository
import dev.jdgarita.frnk.domain.usecase.SingleUseCase

/**
 * @author Vivien Mahe
 * @since 16/12/2023
 */
class GetFeatureFlagUseCase(
    private val featureFlagRepository: FeatureFlagRepository,
) : SingleUseCase<GetFeatureFlagUseCase.InputParams, GetFeatureFlagUseCase.OutputParams>() {

    class InputParams(
        val featureFlag: FeatureFlag
    )

    class OutputParams(
        val enabled: Boolean
    )

    override suspend fun buildUseCase(inputParams: InputParams?): OutputParams {
        val params = assertInputParamsNotNull(inputParams)

        return featureFlagRepository
            .get(FeatureFlagRepository.InputParams.Get(featureFlag = params.featureFlag))
            .let { OutputParams(enabled = it.enabled) }
    }
}
