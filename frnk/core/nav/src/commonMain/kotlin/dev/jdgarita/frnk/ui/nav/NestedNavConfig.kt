package dev.jdgarita.frnk.ui.nav

import androidx.navigation3.runtime.NavKey
import androidx.savedstate.serialization.SavedStateConfiguration
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic

/**
 * The `SavedStateConfiguration` for a **nested/tab** back stack — the one a tabbed shell
 * (`FrnkNestedNavScaffold`, at the [FrnkRootRoute.Tab] destination) drives. It registers the toolkit's
 * tab-level [FrnkTabRoute] members ([FrnkTabRoute.Home] / [FrnkTabRoute.Custom] / [FrnkTabRoute.Settings])
 * and `include`s the host's own routes, passed as a [hostRoutes] `SerializersModule` with a
 * `polymorphic(NavKey::class) { subclass(...) }` block.
 *
 * The **root** back stack uses [frnkRootNavConfig] instead — symmetric with this builder (same
 * `hostRoutes` extension point). Together they replace the old single `frnkNavConfiguration`, which was
 * removed when navigation went two-level.
 *
 * @param hostRoutes the host's own `@Serializable` `NavKey` routes, merged alongside the toolkit's.
 */
fun frnkNestedNavConfig(hostRoutes: SerializersModule = EmptySerializersModule()): SavedStateConfiguration =
    SavedStateConfiguration {
        serializersModule =
            SerializersModule {
                polymorphic(NavKey::class) {
                    subclass(FrnkTabRoute.Home::class, FrnkTabRoute.Home.serializer())
                    subclass(FrnkTabRoute.Custom::class, FrnkTabRoute.Custom.serializer())
                    subclass(FrnkTabRoute.Settings::class, FrnkTabRoute.Settings.serializer())
                }
                include(hostRoutes)
            }
    }