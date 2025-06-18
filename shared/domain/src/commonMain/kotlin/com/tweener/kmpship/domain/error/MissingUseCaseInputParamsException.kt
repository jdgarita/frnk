package com.tweener.kmpship.domain.error

import com.tweener.kmpship.domain.usecase.BaseUseCase

/**
 * @author Vivien Mahe
 * @since 22/08/2023
 */
class MissingUseCaseInputParamsException(useCase: BaseUseCase<*, *, *>) : IllegalArgumentException("Input parameters are mandatory for " + useCase::class.simpleName)
