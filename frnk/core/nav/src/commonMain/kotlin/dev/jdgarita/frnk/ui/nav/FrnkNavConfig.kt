package dev.jdgarita.frnk.ui.nav

import androidx.navigation3.runtime.NavKey
import androidx.savedstate.serialization.SavedStateConfiguration
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic

/**
 * Builds the `SavedStateConfiguration` a nav3 `NavBackStack` uses to persist/restore its entries across
 * configuration change and process death. nav3 serializes back-stack keys polymorphically over [NavKey],
 * so every concrete route type must be registered.
 *
 * The toolkit's own [FrnkRoute] members are registered here; a host merges its own routes by passing a
 * [hostRoutes] module with its `polymorphic(NavKey::class) { subclass(...) }` block:
 *
 * ```
 * val appNavConfig = frnkNavConfiguration(
 *     hostRoutes = SerializersModule {
 *         polymorphic(NavKey::class) {
 *             subclass(Home::class, Home.serializer())
 *             // … the host's routes
 *         }
 *     },
 * )
 * ```
 *
 * Pass the result to `rememberFrnkNavBackStack(appNavConfig, startRoute)` (`:shared-ui-atoms`).
 */
fun frnkNavConfiguration(hostRoutes: SerializersModule = EmptySerializersModule()): SavedStateConfiguration =
    SavedStateConfiguration {
        serializersModule =
            SerializersModule {
                polymorphic(NavKey::class) {
                    subclass(FrnkRoute.Home::class, FrnkRoute.Home.serializer())
                    subclass(FrnkRoute.Onboarding::class, FrnkRoute.Onboarding.serializer())
                    subclass(FrnkRoute.Settings::class, FrnkRoute.Settings.serializer())
                    subclass(FrnkRoute.Paywall::class, FrnkRoute.Paywall.serializer())
                    subclass(FrnkRoute.Custom::class, FrnkRoute.Custom.serializer())
                }
                include(hostRoutes)
            }
    }