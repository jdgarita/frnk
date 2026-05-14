package dev.jdgarita.frnk.android

/**
 * Public entry point for downstream Android consumers. Apps call
 * [FrnkAndroid.initialize] from their Application.onCreate to wire Koin
 * modules from the library.
 */
object FrnkAndroid {
    fun initialize() {
        // Bootstrap Koin modules from all impl modules here.
    }
}
