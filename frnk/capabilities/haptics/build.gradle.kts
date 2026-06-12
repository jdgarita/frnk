plugins {
    // Compose because the multihaptic engine binding (FrnkHaptics / MultiHapticEngine, moved down from
    // :shared-ui-atoms at restructure Stage 7a) is a Compose-aware module — LocalFrnkHaptics is a
    // CompositionLocal and rememberFrnkHaptics resolves the platform Vibrator inside composition. hosttest
    // keeps the commonTest opt-in for the contract's DefaultHapticFeedbackTest. Both apply the shared base
    // once (idempotent).
    id("frnk.kmp.library.compose")
    id("frnk.kmp.library.hosttest")
}

kotlin {
    android {
        namespace = "${libs.versions.frnk.groupId.get()}.haptics"
    }
    sourceSets {
        commonMain.dependencies {
            // Contract (HapticType / HapticFeedback / HapticEngine SPI + Default/NoOp) — StateFlow drives
            // the enabled flag. Compose-free at the contract level; usable without the engine.
            api(libs.kotlinx.coroutines.core)

            // The multihaptic engine binding (Stage 7a): wraps top.ltfan.multihaptic behind the Compose-free
            // HapticFeedback contract. Like the ripple, this is a UI-feedback library, not a swappable
            // backend SDK — no api/impl split. multihaptic ships its own Android/iOS impls (no native
            // cinterop), so umbrella XCFrameworks stay clean and the Android VIBRATE permission self-merges
            // from multihaptic-core's manifest. LocalLifecycleOwner (lifecycle-runtime-compose) re-resolves
            // the Vibrator across lifecycle changes.
            implementation(libs.androidx.lifecycle.runtime.compose)
            implementation(libs.multihaptic.core)
            implementation(libs.multihaptic.compose)
        }
    }
}
