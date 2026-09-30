package dev.jdgarita.frnk.ui.haptics

import dev.jdgarita.frnk.database.KeyValueStore
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * Binds a single [HapticsPreference] ([KeyValueHapticsPreference]) stored under [key] in the graph's
 * [KeyValueStore], so install `prefsModule` alongside it. With this module in the graph, `FrnkApp`
 * makes the ambient `LocalFrnkHaptics` honour the stored switch, and the default Settings toggle
 * persists across launches.
 *
 * A single, because two readers share it: the ambient haptics and whatever ViewModel shows the toggle.
 *
 * @param key the host's own key, e.g. `"still.haptics.enabled"`. Required: the toolkit does not pick
 *   a namespace for the host.
 * @param default the value while nothing is stored. On by default.
 */
fun hapticsPreferenceModule(
    key: String,
    default: Boolean = true
): Module =
    module {
        single<HapticsPreference> { KeyValueHapticsPreference(get<KeyValueStore>(), key, default) }
    }