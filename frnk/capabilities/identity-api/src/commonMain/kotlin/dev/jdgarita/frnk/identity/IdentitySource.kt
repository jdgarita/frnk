package dev.jdgarita.frnk.identity

import dev.jdgarita.frnk.utils.AppError
import dev.jdgarita.frnk.utils.AppResult

interface IdentitySource {
    suspend fun identify(id: String): AppResult<Unit, IdentityError>
}

enum class IdentityError(
    override val message: String
) : AppError {
    Error("Could not identify")
}