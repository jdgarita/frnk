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
    private val failure: Throwable? = null
) : FirebaseAuthGateway {
    var signInCalls: Int = 0
        private set

    override suspend fun signInAnonymously(): String {
        signInCalls += 1
        failure?.let { throw it }
        return signedInUid
    }
}