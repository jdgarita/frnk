package dev.jdgarita.frnk.backend

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Reusable test double for [AuthService] — the canonical fake pattern for the toolkit.
 *
 * Why it exists: `*-api` interfaces return [AppResult] and never throw, so tests for any
 * consumer (and the real `*-impl` backends in later tasks, e.g. BACKLOG P1-2/P1-3) can drive
 * success **and** failure branches without touching a real SDK. Copy this shape when faking a
 * new `*-api` interface: back state with a [MutableStateFlow], make every call return a
 * pre-seeded [AppResult] the test controls, and record inputs for assertions.
 *
 * Lives in `commonTest` so it compiles on every target and never ships in production artifacts.
 */
class FakeAuthService(
    initialUser: AuthUser? = null,
) : AuthService {
    private val _currentUser = MutableStateFlow(initialUser)
    override val currentUser: StateFlow<AuthUser?> = _currentUser

    /** Result the next [signIn]/[signUp] returns. Default mirrors a happy-path anonymous-less login. */
    var nextResult: AppResult<AuthUser, CommonError> =
        AppResult.Success(AuthUser(id = "fake-id", email = "user@frnk.dev", isAnonymous = false))

    /** Captured calls, for assertions: each pair is (email, password). */
    val signInCalls = mutableListOf<Pair<String, String>>()
    val signUpCalls = mutableListOf<Pair<String, String>>()
    var signOutCount = 0
        private set

    override suspend fun signIn(
        email: String,
        password: String,
    ): AppResult<AuthUser, CommonError> {
        signInCalls += email to password
        return nextResult.also { if (it is AppResult.Success) _currentUser.value = it.data }
    }

    override suspend fun signUp(
        email: String,
        password: String,
    ): AppResult<AuthUser, CommonError> {
        signUpCalls += email to password
        return nextResult.also { if (it is AppResult.Success) _currentUser.value = it.data }
    }

    override suspend fun signOut(): AppResult<Unit, CommonError> {
        signOutCount++
        _currentUser.value = null
        return AppResult.Success(Unit)
    }
}
