package com.tweener.kmpship.domain.usecase.config

import com.tweener.kmpkit.kotlinextensions.now
import com.tweener.kmpship.domain.repository.UserRepository
import com.tweener.kmpship.domain.usecase.CompletableUseCase
import kotlinx.datetime.LocalDateTime

/**
 * @author Vivien Mahe
 * @since 10/01/2024
 */
class UpdateLastAskForAppReviewDateUseCase(
    private val userRepository: UserRepository,
) : CompletableUseCase<Nothing>() {

    override suspend fun buildUseCase(inputParams: Nothing?) {
        userRepository.setLastAskForAppReviewDate(UserRepository.InputParams.SetLastAskForAppReviewDate(date = LocalDateTime.now()))
    }
}
