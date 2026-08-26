package dev.jdgarita.frnk.monetization.ui.ext

import dev.jdgarita.frnk.monetization.MonetizationError
import dev.jdgarita.frnk.ui.theme.FrnkStringSource
import dev.jdgarita.frnk.ui.theme.stringErrorAlreadyOwned
import dev.jdgarita.frnk.ui.theme.stringErrorNetworkUnavailable
import dev.jdgarita.frnk.ui.theme.stringErrorNoOfferings
import dev.jdgarita.frnk.ui.theme.stringErrorPurchaseCancelled
import dev.jdgarita.frnk.ui.theme.stringErrorPurchaseNotAllowed
import dev.jdgarita.frnk.ui.theme.stringErrorStoreUnavailable
import dev.jdgarita.frnk.ui.theme.stringGenericError

/**
 * The user-facing text for a billing failure, as a theme token so it localizes and overrides like
 * any other toolkit copy. [MonetizationError.message] stays what it is — the enum's English
 * diagnostic, fine for logs — and must not be shown to users; UI layers map through this instead.
 */
fun MonetizationError.toStringSource(): FrnkStringSource =
    FrnkStringSource.Token(
        when (this) {
            MonetizationError.UserCancelled -> stringErrorPurchaseCancelled
            MonetizationError.StoreUnavailable -> stringErrorStoreUnavailable
            MonetizationError.NetworkUnavailable -> stringErrorNetworkUnavailable
            MonetizationError.NoOfferings -> stringErrorNoOfferings
            MonetizationError.PurchaseNotAllowed -> stringErrorPurchaseNotAllowed
            MonetizationError.AlreadyOwned -> stringErrorAlreadyOwned
            MonetizationError.Unknown -> stringGenericError
        }
    )