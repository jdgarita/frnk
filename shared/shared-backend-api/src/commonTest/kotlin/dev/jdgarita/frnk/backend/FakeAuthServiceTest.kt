package dev.jdgarita.frnk.backend

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Exercises [FakeAuthService] to lock in the reusable fake pattern: a `suspend` `*-api` call
 * returning [AppResult] is driven through both success and failure without any real SDK.
 */
class FakeAuthServiceTest {
    @Test
    fun signIn_returns_the_seeded_success_and_updates_currentUser() =
        runTest {
            val service = FakeAuthService()

            val result = service.signIn("user@frnk.dev", "secret")

            assertTrue(result is AppResult.Success)
            assertEquals("user@frnk.dev", result.data.email)
            assertEquals(listOf("user@frnk.dev" to "secret"), service.signInCalls)
            assertEquals("user@frnk.dev", service.currentUser.first()?.email)
        }

    @Test
    fun signIn_propagates_a_seeded_failure_without_touching_currentUser() =
        runTest {
            val service = FakeAuthService()
            service.nextResult = AppResult.Failure(CommonError.Unauthorized)

            val result = service.signIn("user@frnk.dev", "wrong")

            assertTrue(result is AppResult.Failure)
            assertEquals(CommonError.Unauthorized, result.error)
            assertNull(service.currentUser.first())
        }

    @Test
    fun signOut_clears_currentUser() =
        runTest {
            val service = FakeAuthService(initialUser = AuthUser("id", "user@frnk.dev", isAnonymous = false))

            val result = service.signOut()

            assertTrue(result is AppResult.Success)
            assertEquals(1, service.signOutCount)
            assertNull(service.currentUser.first())
        }
}
