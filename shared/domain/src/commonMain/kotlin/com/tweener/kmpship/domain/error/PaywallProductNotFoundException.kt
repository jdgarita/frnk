package com.tweener.kmpship.domain.error

/**
 * @author Vivien Mahe
 * @since 17/01/2025
 */
class PaywallProductNotFoundException(val id: String) : NoSuchElementException("No product found with ID: $id.")
