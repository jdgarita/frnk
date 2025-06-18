package dev.jdgarita.frnk.domain._internal.di

import dev.jdgarita.frnk.domain.usecase.authentication.GetUserAuthProviderUseCase
import dev.jdgarita.frnk.domain.usecase.authentication.IsUserAuthenticatedUseCase
import dev.jdgarita.frnk.domain.usecase.authentication.SetUserAuthProviderUseCase
import dev.jdgarita.frnk.domain.usecase.authentication.SignOutUserUseCase
import dev.jdgarita.frnk.domain.usecase.authentication.ValidateEmailAddressUseCase
import dev.jdgarita.frnk.domain.usecase.authentication.ValidatePasswordCreationUseCase
import dev.jdgarita.frnk.domain.usecase.config.GetAppRatingAskPeriodMonthsUseCase
import dev.jdgarita.frnk.domain.usecase.config.LoadAppConfigUseCase
import dev.jdgarita.frnk.domain.usecase.config.SetAppReviewRequestedUseCase
import dev.jdgarita.frnk.domain.usecase.payment.GetCurrentActiveSubscriptionUseCase
import dev.jdgarita.frnk.domain.usecase.payment.GetPaywallUseCase
import dev.jdgarita.frnk.domain.usecase.payment.GetRedeemUrlUseCase
import dev.jdgarita.frnk.domain.usecase.payment.GetSubscriptionsDiscountsUseCase
import dev.jdgarita.frnk.domain.usecase.payment.HasUserActiveSubscriptionUseCase
import dev.jdgarita.frnk.domain.usecase.payment.PurchaseProductUseCase
import dev.jdgarita.frnk.domain.usecase.payment.RestorePurchaseUseCase
import dev.jdgarita.frnk.domain.usecase.payment.WasUserPreviouslySubscribedUseCase
import dev.jdgarita.frnk.domain.usecase.settings.GetCrashlyticsAuthorizationStateUseCase
import dev.jdgarita.frnk.domain.usecase.settings.GetFeatureFlagUseCase
import dev.jdgarita.frnk.domain.usecase.settings.GetThemeTypeUseCase
import dev.jdgarita.frnk.domain.usecase.settings.SetCrashlyticsAuthorizationStateUseCase
import dev.jdgarita.frnk.domain.usecase.settings.SetThemeTypeUseCase
import dev.jdgarita.frnk.domain.usecase.user.DeleteUserUseCase
import dev.jdgarita.frnk.domain.usecase.user.FetchUserUseCase
import dev.jdgarita.frnk.domain.usecase.user.GetUserAccountCreationDateUseCase
import dev.jdgarita.frnk.domain.usecase.user.GetUserUseCase
import dev.jdgarita.frnk.domain.usecase.user.SetUserEmailVerifiedUseCase
import dev.jdgarita.frnk.domain.usecase.user.ShouldAskForAppReviewUseCase
import dev.jdgarita.frnk.domain.usecase.user.UpdateLastAskForAppReviewDateUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

/**
 * @author Vivien Mahe
 * @since 01/11/2023
 */

val useCaseModule = module {

    // Authentication
    factoryOf(::IsUserAuthenticatedUseCase)
    factoryOf(::GetUserAuthProviderUseCase)
    factoryOf(::SetUserAuthProviderUseCase)
    factoryOf(::SignOutUserUseCase)
    factoryOf(::ValidateEmailAddressUseCase)
    factoryOf(::ValidatePasswordCreationUseCase)

    // Config
    factoryOf(::LoadAppConfigUseCase)
    factoryOf(::GetAppRatingAskPeriodMonthsUseCase)
    factoryOf(::ShouldAskForAppReviewUseCase)
    factoryOf(::UpdateLastAskForAppReviewDateUseCase)
    factoryOf(::SetAppReviewRequestedUseCase)

    // User
    factoryOf(::GetUserUseCase)
    factoryOf(::FetchUserUseCase)
    factoryOf(::IsUserAuthenticatedUseCase)
    factoryOf(::ShouldAskForAppReviewUseCase)
    factoryOf(::UpdateLastAskForAppReviewDateUseCase)
    factoryOf(::SignOutUserUseCase)
    factoryOf(::DeleteUserUseCase)
    factoryOf(::GetUserAccountCreationDateUseCase)
    factoryOf(::SetUserEmailVerifiedUseCase)

    // Payments
    factoryOf(::GetPaywallUseCase)
    factoryOf(::GetRedeemUrlUseCase)
    factoryOf(::PurchaseProductUseCase)
    factoryOf(::RestorePurchaseUseCase)
    factoryOf(::HasUserActiveSubscriptionUseCase)
    factoryOf(::GetCurrentActiveSubscriptionUseCase)
    factoryOf(::WasUserPreviouslySubscribedUseCase)
    factoryOf(::GetSubscriptionsDiscountsUseCase)

    // Settings
    factoryOf(::GetFeatureFlagUseCase)
    factoryOf(::SetThemeTypeUseCase)
    factoryOf(::GetThemeTypeUseCase)
    factoryOf(::GetCrashlyticsAuthorizationStateUseCase)
    factoryOf(::SetCrashlyticsAuthorizationStateUseCase)

}
