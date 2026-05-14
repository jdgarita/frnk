package dev.jdgarita.frnk.network.internal

import dev.jdgarita.frnk.network.NetworkClient
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import org.koin.dsl.module

val networkModule =
    module {
        single {
            HttpClient {
                install(ContentNegotiation) { json() }
                install(Logging)
            }
        }
        single<NetworkClient> { KtorNetworkClient(get()) }
    }
