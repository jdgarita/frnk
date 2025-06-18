package com.tweener.kmpship.domain.entity

/**
 * @author Vivien Mahe
 * @since 07/11/2023
 */
data class User(
    val id: String,
    val email: String,
    val isEmailVerified: Boolean,
    val photoUrl: String?,
)
