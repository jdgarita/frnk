package dev.jdgarita.frnk.monetization.usecase

import dev.jdgarita.frnk.monetization.EntitlementManager
import kotlinx.coroutines.flow.StateFlow

/**
 * Default [ObserveProStatusUseCase] — re-exposes [EntitlementManager.isPro] verbatim. Bound by
 * [monetizationModule][dev.jdgarita.frnk.monetization.monetizationModule] when monetization is installed.
 */
internal class DefaultObserveProStatusUseCase(
    private val entitlements: EntitlementManager
) : ObserveProStatusUseCase {
    override fun invoke(): StateFlow<Boolean> = entitlements.isPro
}