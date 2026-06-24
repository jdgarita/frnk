package dev.jdgarita.frnk.ui.nav

import androidx.navigation3.runtime.NavKey
import androidx.savedstate.serialization.SavedStateConfiguration
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic

/**
 * The `SavedStateConfiguration` for the **root** back stack of the lower-level `FrnkApp` path: it
 * persists/restores the root entries across configuration change and process death. nav3 serializes
 * back-stack keys polymorphically over [NavKey], so every concrete route type must be registered — this
 * registers the toolkit's [FrnkRootRoute] members ([FrnkRootRoute.Onboarding] / [FrnkRootRoute.Tab] /
 * [FrnkRootRoute.Paywall]).
 *
 * Pass it to the root `NavDisplay` in `:ui-app`'s `FrnkApp` (via its `onSavedStateConfiguration`). The
 * **nested** tab back stack inside [FrnkRootRoute.Tab] uses [frnkNestedNavConfig] instead, which registers
 * [FrnkRoute] and merges the host's own routes.
 */
val frnkRootNavConfig =
    SavedStateConfiguration {
        serializersModule =
            SerializersModule {
                polymorphic(NavKey::class) {
                    subclass(FrnkRootRoute.Onboarding::class, FrnkRootRoute.Onboarding.serializer())
                    subclass(FrnkRootRoute.Tab::class, FrnkRootRoute.Tab.serializer())
                    subclass(FrnkRootRoute.Paywall::class, FrnkRootRoute.Paywall.serializer())
                }
            }
    }