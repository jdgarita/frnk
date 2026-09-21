package dev.jdgarita.frnk.monetization.revenuecat

import dev.jdgarita.frnk.identity.AnonymousIdentityProvider
import dev.jdgarita.frnk.utils.AppResult
import dev.jdgarita.frnk.utils.CommonError
import kotlinx.coroutines.test.runTest
import org.koin.dsl.koinApplication
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull

class RevenueCatIdentityProviderTest {
    @Test
    fun `app user id is returned and published`() =
        runTest {
            val provider = RevenueCatIdentityProvider(FakeIdentityGateway(id = "\$RCAnonymousID:abc"))

            val result = provider.ensureSignedIn()

            assertEquals(AppResult.Success("\$RCAnonymousID:abc"), result)
            assertEquals("\$RCAnonymousID:abc", provider.uid.value)
        }

    @Test
    fun `an identified app user id is reported verbatim`() =
        runTest {
            // An install that once ran Purchases.logIn(firebaseUid) keeps that id; nothing here logs out.
            val provider = RevenueCatIdentityProvider(FakeIdentityGateway(id = "firebase-uid-123"))

            assertEquals(AppResult.Success("firebase-uid-123"), provider.ensureSignedIn())
        }

    @Test
    fun `unconfigured sdk maps to common error and leaves uid empty`() =
        runTest {
            val provider =
                RevenueCatIdentityProvider(
                    FakeIdentityGateway(failure = IllegalStateException("Purchases has not been configured"))
                )

            val result = provider.ensureSignedIn()

            assertEquals(AppResult.Failure(CommonError.Unknown), result)
            assertNull(provider.uid.value)
        }

    @Test
    fun `revenuecat module binds the anonymous identity provider`() {
        val application = koinApplication { modules(revenueCatModule) }

        try {
            // Construction never touches Purchases.sharedInstance, so an unconfigured SDK still resolves.
            assertIs<AnonymousIdentityProvider>(application.koin.get<AnonymousIdentityProvider>())
        } finally {
            application.close()
        }
    }
}

private class FakeIdentityGateway(
    private val id: String = "\$RCAnonymousID:fake",
    private val failure: Throwable? = null
) : RevenueCatIdentityGateway {
    override val appUserId: String
        get() {
            failure?.let { throw it }
            return id
        }
}