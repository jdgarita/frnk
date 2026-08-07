package dev.jdgarita.frnk.identity.firebase

import dev.jdgarita.frnk.identity.AnonymousIdentityProvider
import dev.jdgarita.frnk.utils.AppResult
import dev.jdgarita.frnk.utils.CommonError
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import org.koin.dsl.koinApplication
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertNull

class FirebaseAuthManagerTest {
    @Test
    fun `existing user is reused and published without signing in`() =
        runTest {
            val gateway = FakeFirebaseAuthGateway(currentUid = "existing-uid")
            val manager = FirebaseAuthManager(gateway)

            val result = manager.ensureSignedIn()

            assertEquals(AppResult.Success("existing-uid"), result)
            assertEquals("existing-uid", manager.uid.value)
            assertEquals(0, gateway.signInCalls)
        }

    @Test
    fun `anonymous sign in publishes returned uid`() =
        runTest {
            val gateway = FakeFirebaseAuthGateway(signedInUid = "anonymous-uid")
            val manager = FirebaseAuthManager(gateway)

            val result = manager.ensureSignedIn()

            assertEquals(AppResult.Success("anonymous-uid"), result)
            assertEquals("anonymous-uid", manager.uid.value)
            assertEquals(1, gateway.signInCalls)
        }

    @Test
    fun `sign in failure maps to common error and leaves uid empty`() =
        runTest {
            val manager = FirebaseAuthManager(FakeFirebaseAuthGateway(failure = IllegalStateException("auth failed")))

            val result = manager.ensureSignedIn()

            assertEquals(AppResult.Failure(CommonError.Unknown), result)
            assertNull(manager.uid.value)
        }

    @Test
    fun `sign in cancellation is rethrown`() =
        runTest {
            val manager = FirebaseAuthManager(FakeFirebaseAuthGateway(failure = CancellationException("cancelled")))

            assertFailsWith<CancellationException> { manager.ensureSignedIn() }
            assertNull(manager.uid.value)
        }

    @Test
    fun `id token signs in first and returns the gateway token`() =
        runTest {
            val gateway = FakeFirebaseAuthGateway(signedInUid = "anonymous-uid", idTokenResult = "signed-jwt")
            val manager = FirebaseAuthManager(gateway)

            val result = manager.idToken()

            assertEquals(AppResult.Success("signed-jwt"), result)
            assertEquals(1, gateway.signInCalls)
            assertEquals(false, gateway.lastForceRefresh)
        }

    @Test
    fun `id token force refresh reaches the gateway`() =
        runTest {
            val gateway = FakeFirebaseAuthGateway(currentUid = "existing-uid", idTokenResult = "fresh-jwt")

            val result = FirebaseAuthManager(gateway).idToken(forceRefresh = true)

            assertEquals(AppResult.Success("fresh-jwt"), result)
            assertEquals(true, gateway.lastForceRefresh)
        }

    @Test
    fun `id token propagates sign in failure without fetching a token`() =
        runTest {
            val gateway = FakeFirebaseAuthGateway(failure = IllegalStateException("auth failed"))

            val result = FirebaseAuthManager(gateway).idToken()

            assertEquals(AppResult.Failure(CommonError.Unknown), result)
            assertEquals(0, gateway.idTokenCalls)
        }

    @Test
    fun `missing token maps to unauthorized`() =
        runTest {
            val gateway = FakeFirebaseAuthGateway(currentUid = "existing-uid", idTokenResult = null)

            val result = FirebaseAuthManager(gateway).idToken()

            assertEquals(AppResult.Failure(CommonError.Unauthorized), result)
        }

    @Test
    fun `token fetch failure maps to common error`() =
        runTest {
            val gateway =
                FakeFirebaseAuthGateway(
                    currentUid = "existing-uid",
                    idTokenFailure = IllegalStateException("token fetch failed")
                )

            val result = FirebaseAuthManager(gateway).idToken()

            assertEquals(AppResult.Failure(CommonError.Unknown), result)
        }

    @Test
    fun `token fetch cancellation is rethrown`() =
        runTest {
            val gateway =
                FakeFirebaseAuthGateway(
                    currentUid = "existing-uid",
                    idTokenFailure = CancellationException("cancelled")
                )

            assertFailsWith<CancellationException> { FirebaseAuthManager(gateway).idToken() }
        }

    @Test
    fun `firebase identity module exposes anonymous identity provider`() {
        val application = koinApplication { modules(firebaseIdentityModule) }

        try {
            assertIs<AnonymousIdentityProvider>(application.koin.get<AnonymousIdentityProvider>())
        } finally {
            application.close()
        }
    }
}

private class FakeFirebaseAuthGateway(
    override val currentUid: String? = null,
    private val signedInUid: String = "signed-in-uid",
    private val failure: Throwable? = null,
    private val idTokenResult: String? = "id-token",
    private val idTokenFailure: Throwable? = null
) : FirebaseAuthGateway {
    var signInCalls: Int = 0
        private set

    var idTokenCalls: Int = 0
        private set

    var lastForceRefresh: Boolean? = null
        private set

    override suspend fun signInAnonymously(): String {
        signInCalls += 1
        failure?.let { throw it }
        return signedInUid
    }

    override suspend fun idToken(forceRefresh: Boolean): String? {
        idTokenCalls += 1
        lastForceRefresh = forceRefresh
        idTokenFailure?.let { throw it }
        return idTokenResult
    }
}