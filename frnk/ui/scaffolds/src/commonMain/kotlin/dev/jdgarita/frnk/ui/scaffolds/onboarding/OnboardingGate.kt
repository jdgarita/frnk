package dev.jdgarita.frnk.ui.scaffolds.onboarding

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import dev.jdgarita.frnk.database.KeyValueStore
import dev.jdgarita.frnk.database.booleanPreference
import org.koin.compose.getKoin

/**
 * The toolkit's **first-launch gate**: a tiny wrapper over a persisted `Boolean` preference that
 * records whether the user has been shown onboarding. Hosts use it to present onboarding exactly once
 * — see `FrnkFirstLaunchOnboardingEffect` (`:ui-bottom-nav`), which `FrnkAppScaffold` wires
 * automatically and lower-level shells opt into.
 *
 * Backed by [KeyValueStore.booleanPreference], so it persists across launches through whichever
 * `KeyValueStore` the host installed (`prefsModule` on a real host; an in-memory fake in the demo).
 *
 * **Semantics:** callers `markSeen()` **when onboarding is presented**, not when it completes — so a
 * process kill mid-onboarding never re-shows it. A host that prefers "only after the user finishes"
 * can instead call [markSeen] from its onboarding-complete handler and skip the auto-present.
 *
 * Obtain one via [rememberOnboardingGate] — the constructor is `internal` so `KeyValueStore` never
 * leaks into this module's public API and hosts can't build a gate over an out-of-graph store.
 *
 * @param store the persisted backing store (resolved from Koin by [rememberOnboardingGate]).
 * @param key the preference key; override only to gate multiple independent first-run flows.
 */
class OnboardingGate internal constructor(
    store: KeyValueStore,
    key: String = DEFAULT_KEY
) {
    private val seenPref = store.booleanPreference(key, default = false)

    /** Whether onboarding has already been presented. */
    val seen: Boolean
        get() = seenPref.value

    /** Record that onboarding has been presented; subsequent launches won't auto-present it. */
    fun markSeen() {
        seenPref.value = true
    }

    companion object {
        const val DEFAULT_KEY: String = "frnk.onboarding.seen"
    }
}

/**
 * Resolves an [OnboardingGate] from the Koin graph's [KeyValueStore], or `null` when no store is bound
 * (a host that installs no `prefsModule` simply has no first-launch persistence — gating no-ops rather
 * than crashing, matching how `FrnkAppScaffold` resolves `EntitlementManager` leniently).
 */
@Composable
fun rememberOnboardingGate(key: String = OnboardingGate.DEFAULT_KEY): OnboardingGate? {
    val koin = getKoin()
    val store = remember(koin) { koin.getOrNull<KeyValueStore>() }
    return remember(store, key) { store?.let { OnboardingGate(it, key) } }
}