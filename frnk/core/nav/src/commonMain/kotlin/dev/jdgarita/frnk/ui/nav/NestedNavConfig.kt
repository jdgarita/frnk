package dev.jdgarita.frnk.ui.nav

import androidx.navigation3.runtime.NavKey
import androidx.savedstate.serialization.SavedStateConfiguration
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic

/**
 * The `SavedStateConfiguration` for a **nested/tab** back stack — the one a tabbed shell (e.g. the
 * [FrnkRootRoute.Tab] destination, or `FrnkTabbedNavScaffold`) drives. It registers the toolkit's
 * tab-level [FrnkRoute] members ([FrnkRoute.Home] / [FrnkRoute.Custom] / [FrnkRoute.Settings]) and
 * `include`s the host's own routes, passed as a [hostRoutes] `SerializersModule` with a
 * `polymorphic(NavKey::class) { subclass(...) }` block.
 *
 * The **root** back stack uses [frnkRootNavConfig] instead. Together they replace the old single
 * `frnkNavConfiguration`, which was removed when navigation went two-level.
 *
 * @param hostRoutes the host's own `@Serializable` `NavKey` routes, merged alongside the toolkit's.
 */
fun frnkNestedNavConfig(hostRoutes: SerializersModule = EmptySerializersModule()): SavedStateConfiguration =
    SavedStateConfiguration {
        serializersModule =
            SerializersModule {
                polymorphic(NavKey::class) {
                    subclass(FrnkRoute.Home::class, FrnkRoute.Home.serializer())
                    subclass(FrnkRoute.Custom::class, FrnkRoute.Custom.serializer())
                    subclass(FrnkRoute.Settings::class, FrnkRoute.Settings.serializer())
                }
                include(hostRoutes)
            }
    }