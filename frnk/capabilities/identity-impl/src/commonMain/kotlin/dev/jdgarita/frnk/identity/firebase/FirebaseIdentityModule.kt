package dev.jdgarita.frnk.identity.firebase

import dev.jdgarita.frnk.identity.AnonymousIdentityProvider
import org.koin.dsl.module

/** Firebase anonymous identity binding selected explicitly by a host application's bootstrap. */
val firebaseIdentityModule =
    module {
        single<AnonymousIdentityProvider> { FirebaseAuthManager() }
    }