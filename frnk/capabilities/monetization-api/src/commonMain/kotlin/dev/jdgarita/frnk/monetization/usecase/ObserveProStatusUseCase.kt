package dev.jdgarita.frnk.monetization.usecase

import kotlinx.coroutines.flow.StateFlow

/**
 * The toolkit's first **use case** — a thin, injectable domain operation that ViewModels resolve via
 * Koin instead of receiving domain state threaded down through Compose.
 *
 * Exposes the canonical reactive Free/Pro flag (provider OR god mode) sourced from
 * [EntitlementManager][dev.jdgarita.frnk.monetization.EntitlementManager]. The default binding
 * ([DefaultObserveProStatusUseCase]) ships in
 * [monetizationModule][dev.jdgarita.frnk.monetization.monetizationModule]; consumers that may run
 * without monetization installed resolve it leniently (`getOrNull()`) and degrade to Free.
 */
fun interface ObserveProStatusUseCase {
    /** Reactive Free/Pro status (provider OR god mode). */
    operator fun invoke(): StateFlow<Boolean>
}