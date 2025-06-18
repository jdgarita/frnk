package dev.jdgarita.frnk.domain.entity

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
