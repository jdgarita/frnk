package dev.jdgarita.frnk.ui.haptics

import dev.jdgarita.frnk.database.KeyValueStore
import dev.jdgarita.frnk.database.booleanPreference
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * [HapticsPreference] stored in the host's [KeyValueStore] (bound by `prefsModule`) under [key].
 *
 * The key is the host's, never the toolkit's, so each app keeps its own namespace and an app that
 * already stored the switch (Faint's `"faint.haptics.enabled"`) keeps its users' choice when it
 * moves to this class. A missing key reads as [default] — on, unless the host says otherwise — so
 * installs that predate the switch keep their haptics.
 *
 * The store is read once, at construction; after that [isEnabled] is the in-memory copy and every
 * [setEnabled] writes through. Bind it as a single instance (see [hapticsPreferenceModule]) so the
 * Settings toggle and the ambient haptics share one copy.
 */
class KeyValueHapticsPreference(
    store: KeyValueStore,
    key: String,
    default: Boolean = true
) : HapticsPreference {
    init {
        require(key.isNotBlank()) { "The haptics preference key must not be blank" }
    }

    private val stored = store.booleanPreference(key, default)
    private val enabled = MutableStateFlow(stored.value)

    override val isEnabled: StateFlow<Boolean> = enabled.asStateFlow()

    override fun setEnabled(enabled: Boolean) {
        stored.value = enabled
        this.enabled.value = enabled
    }
}