package dev.jdgarita.frnk.domain.usecase.config

import com.tweener.kmpkit.kotlinextensions.now
import dev.jdgarita.frnk.domain.repository.SettingsRepository
import dev.jdgarita.frnk.domain.repository.UserRepository
import dev.jdgarita.frnk.domain.usecase.SingleUseCase
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus

/**
 * We ask the user for app review every x months.
 *
 * @author Vivien Mahe
 * @since 10/01/2024
 */
class ShouldAskForAppReviewUseCase(
    private val userRepository: UserRepository,
    private val getAppRatingAskPeriodMonthsUseCase: GetAppRatingAskPeriodMonthsUseCase,
    private val settingsRepository: SettingsRepository,
) : SingleUseCase<Nothing, ShouldAskForAppReviewUseCase.OutputParams>() {

    class OutputParams(
        val shouldAsk: Boolean
    )

    override suspend fun buildUseCase(inputParams: Nothing?): OutputParams {
        val getLastAskForAppReviewDate = userRepository.getLastAskForAppReviewDate().date
        val appRatingAskPeriodMonths = getAppRatingAskPeriodMonthsUseCase.execute().periodMonths

        val shouldAsk = when (getLastAskForAppReviewDate) {
            null -> {
                // Never asked the user yet, so we ask if there is one of the following:
                // - If its requested due to an action made somewhere
                // - Or if it's the second app launch

                val isAppReviewRequested = userRepository.isAppReviewRequested().firstOrNull()?.requested ?: false
                val appLaunchCount = settingsRepository.getAppLaunchCount().firstOrNull()?.count ?: 0

                Napier.d { "isAppReviewRequested: $isAppReviewRequested & App launch count: $appLaunchCount" }

                isAppReviewRequested || appLaunchCount == 2
            }

            else -> {
                // Already asked the user previously
                val periodSinceLastAsked = getLastAskForAppReviewDate.date.minus(LocalDate.now())
                periodSinceLastAsked.months >= appRatingAskPeriodMonths
            }
        }

        Napier.d { "Should ask for app review? $shouldAsk" }

        return OutputParams(shouldAsk = shouldAsk)
    }
}
