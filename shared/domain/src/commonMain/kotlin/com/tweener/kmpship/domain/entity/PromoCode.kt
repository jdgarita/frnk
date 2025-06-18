package com.tweener.kmpship.domain.entity

/**
 * @author Vivien Mahe
 * @since 28/01/2025
 */
data class PromoCode(
    val id: String,
    val name: String,
    val androidCode: String,
    val iosCode: String,
)
