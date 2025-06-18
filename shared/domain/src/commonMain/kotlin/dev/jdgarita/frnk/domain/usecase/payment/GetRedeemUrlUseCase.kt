package dev.jdgarita.frnk.domain.usecase.payment

import com.tweener.kmpkit.Platform
import com.tweener.kmpkit.currentPlatform
import dev.jdgarita.frnk.domain.DomainConstants.Payments.APPLE_REDEEM_URL
import dev.jdgarita.frnk.domain.DomainConstants.Payments.APPLE_REDEEM_WITH_CODE_URL
import dev.jdgarita.frnk.domain.DomainConstants.Payments.GOOGLE_REDEEM_URL
import dev.jdgarita.frnk.domain.DomainConstants.Payments.GOOGLE_REDEEM_WITH_CODE_URL
import dev.jdgarita.frnk.domain.entity.PromoCode
import dev.jdgarita.frnk.domain.usecase.SingleUseCase

/**
 * @author Vivien Mahe
 * @since 03/02/2025
 */
class GetRedeemUrlUseCase : SingleUseCase<GetRedeemUrlUseCase.InputParams, GetRedeemUrlUseCase.OutputParams>() {

    class InputParams(
        val promoCode: PromoCode? = null,
    )

    class OutputParams(
        val redeemUrl: String? = null,
    )

    override suspend fun buildUseCase(inputParams: InputParams?): OutputParams {
        val params = assertInputParamsNotNull(inputParams)

        val url = when (currentPlatform) {
            Platform.ANDROID -> {
                when (params.promoCode) {
                    null -> GOOGLE_REDEEM_URL
                    else -> GOOGLE_REDEEM_WITH_CODE_URL.replace("{promo_code}", params.promoCode.androidCode)
                }
            }

            Platform.IOS -> {
                when (params.promoCode) {
                    null -> APPLE_REDEEM_URL
                    else -> APPLE_REDEEM_WITH_CODE_URL.replace("{promo_code}", params.promoCode.iosCode)
                }
            }

            else -> null
        }

        return OutputParams(redeemUrl = url)
    }
}
