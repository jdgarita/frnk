package dev.jdgarita.frnk.domain.error

import dev.jdgarita.frnk.domain.usecase.BaseUseCase

/**
 * @author Vivien Mahe
 * @since 22/08/2023
 */
class MissingUseCaseInputParamsException(useCase: BaseUseCase<*, *, *>) : IllegalArgumentException("Input parameters are mandatory for " + useCase::class.simpleName)
