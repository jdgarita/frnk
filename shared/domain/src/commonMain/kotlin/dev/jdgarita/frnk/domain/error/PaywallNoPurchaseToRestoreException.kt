package dev.jdgarita.frnk.domain.error

/**
 * @author Vivien Mahe
 * @since 24/01/2025
 */
class PaywallNoPurchaseToRestoreException(val errorMessage: String? = null) : Error("There is no purchases to restore for the current user: $errorMessage")
