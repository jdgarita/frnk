package com.tweener.kmpship.domain._internal.di

import com.tweener.kmpship.domain.usecase.authentication.GetUserAuthProviderUseCase
import com.tweener.kmpship.domain.usecase.authentication.IsUserAuthenticatedUseCase
import com.tweener.kmpship.domain.usecase.authentication.SetUserAuthProviderUseCase
import com.tweener.kmpship.domain.usecase.authentication.SignOutUserUseCase
import com.tweener.kmpship.domain.usecase.authentication.ValidateEmailAddressUseCase
import com.tweener.kmpship.domain.usecase.authentication.ValidatePasswordCreationUseCase
import com.tweener.kmpship.domain.usecase.config.GetAppRatingAskPeriodMonthsUseCase
import com.tweener.kmpship.domain.usecase.config.LoadAppConfigUseCase
import com.tweener.kmpship.domain.usecase.config.SetAppReviewRequestedUseCase
import com.tweener.kmpship.domain.usecase.payment.GetCurrentActiveSubscriptionUseCase
import com.tweener.kmpship.domain.usecase.payment.GetPaywallUseCase
import com.tweener.kmpship.domain.usecase.payment.GetRedeemUrlUseCase
import com.tweener.kmpship.domain.usecase.payment.GetSubscriptionsDiscountsUseCase
import com.tweener.kmpship.domain.usecase.payment.HasUserActiveSubscriptionUseCase
import com.tweener.kmpship.domain.usecase.payment.PurchaseProductUseCase
import com.tweener.kmpship.domain.usecase.payment.RestorePurchaseUseCase
import com.tweener.kmpship.domain.usecase.payment.WasUserPreviouslySubscribedUseCase
import com.tweener.kmpship.domain.usecase.settings.GetCrashlyticsAuthorizationStateUseCase
import com.tweener.kmpship.domain.usecase.settings.GetFeatureFlagUseCase
import com.tweener.kmpship.domain.usecase.settings.GetThemeTypeUseCase
import com.tweener.kmpship.domain.usecase.settings.SetCrashlyticsAuthorizationStateUseCase
import com.tweener.kmpship.domain.usecase.settings.SetThemeTypeUseCase
import com.tweener.kmpship.domain.usecase.user.DeleteUserUseCase
import com.tweener.kmpship.domain.usecase.user.FetchUserUseCase
import com.tweener.kmpship.domain.usecase.user.GetUserAccountCreationDateUseCase
import com.tweener.kmpship.domain.usecase.user.GetUserUseCase
import com.tweener.kmpship.domain.usecase.user.SetUserEmailVerifiedUseCase
import com.tweener.kmpship.domain.usecase.user.ShouldAskForAppReviewUseCase
import com.tweener.kmpship.domain.usecase.user.UpdateLastAskForAppReviewDateUseCase
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
