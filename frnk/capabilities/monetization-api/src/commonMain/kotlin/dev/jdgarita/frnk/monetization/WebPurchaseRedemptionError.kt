package dev.jdgarita.frnk.monetization

import dev.jdgarita.frnk.utils.AppError

/**
 * Why redeeming a RevenueCat web-purchase link did not grant Pro. Returned by
 * [EntitlementProvider.redeemWebPurchase] / [EntitlementManager.redeemWebPurchase] — never thrown.
 *
 * The flow this serves: a customer buys on the web (RevenueCat Web Billing with Redemption Links
 * enabled), gets a one-time `rc-<app>://redeem_web_purchase?redemption_token=…` link by email, and
 * opens it on a phone with the app installed. The app hands the URL to the manager, which attaches
 * the purchase to the *current* app user. A sealed interface rather than an enum because [Expired]
 * carries the one datum a host must show — where the replacement link went.
 */
sealed interface WebPurchaseRedemptionError : AppError {
    /**
     * The URL is not a RevenueCat web-purchase redemption link at all. Hosts that route every
     * incoming deep link through the manager treat this as "not for us" and stay silent.
     */
    data object NotARedemptionLink : WebPurchaseRedemptionError {
        override val message: String get() = "Not a web purchase redemption link"
    }

    /** The link is malformed, was already consumed, or was never issued. */
    data object InvalidToken : WebPurchaseRedemptionError {
        override val message: String get() = "Invalid or already used redemption link"
    }

    /**
     * The link outlived its window (an hour, at the time of writing). RevenueCat mails a fresh one
     * to [obfuscatedEmail] unless it already did so recently — the host's copy should say so.
     */
    data class Expired(
        val obfuscatedEmail: String
    ) : WebPurchaseRedemptionError {
        override val message: String get() = "Redemption link expired"
    }

    /** The purchase was already redeemed by a different app user; it cannot move. */
    data object BelongsToOtherUser : WebPurchaseRedemptionError {
        override val message: String get() = "Purchase belongs to another user"
    }

    data object NetworkUnavailable : WebPurchaseRedemptionError {
        override val message: String get() = "Network unavailable"
    }

    /** The billing SDK is not configured on this device (the documented graceful-degradation mode). */
    data object StoreUnavailable : WebPurchaseRedemptionError {
        override val message: String get() = "Store unavailable"
    }

    data object Unknown : WebPurchaseRedemptionError {
        override val message: String get() = "Something went wrong"
    }
}