package dev.jdgarita.frnk.monetization

import dev.jdgarita.frnk.utils.AppError

/** Typed failures for offerings / purchase / restore — never thrown, always returned in an [dev.jdgarita.frnk.utils.AppResult.Failure]. */
enum class MonetizationError(
    override val message: String
) : AppError {
    UserCancelled("Purchase cancelled"),
    StoreUnavailable("Store unavailable"),
    NetworkUnavailable("Network unavailable"),
    NoOfferings("No products available"),
    PurchaseNotAllowed("Purchases are not allowed on this device"),
    AlreadyOwned("Already subscribed"),
    Unknown("Something went wrong")
}