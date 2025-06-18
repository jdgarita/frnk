package dev.jdgarita.frnk.domain.usecase.user

import dev.jdgarita.frnk.domain.repository.UserRepository
import dev.jdgarita.frnk.domain.usecase.CompletableUseCase
import com.tweener.kmpkit.kotlinextensions.now
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
