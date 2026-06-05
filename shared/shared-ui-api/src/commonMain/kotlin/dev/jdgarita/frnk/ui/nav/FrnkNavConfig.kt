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
 * The toolkit's own [ToolkitRoute] members are registered here; a host merges its own routes by passing a
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
                    subclass(ToolkitRoute.Home::class, ToolkitRoute.Home.serializer())
                    subclass(ToolkitRoute.Settings::class, ToolkitRoute.Settings.serializer())
                    subclass(ToolkitRoute.Paywall::class, ToolkitRoute.Paywall.serializer())
                    subclass(ToolkitRoute.SignIn::class, ToolkitRoute.SignIn.serializer())
                    subclass(ToolkitRoute.SignUp::class, ToolkitRoute.SignUp.serializer())
                    subclass(ToolkitRoute.Custom::class, ToolkitRoute.Custom.serializer())
                }
                include(hostRoutes)
            }
    }
