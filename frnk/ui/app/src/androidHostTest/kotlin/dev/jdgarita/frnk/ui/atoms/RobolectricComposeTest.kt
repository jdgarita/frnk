package dev.jdgarita.frnk.ui.atoms

import androidx.compose.runtime.Composable
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import dev.jdgarita.frnk.ui.theme.FrnkTheme
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Per-module copy of the Robolectric Compose-host test base. The original lives in `:ui-components`'s
 * `androidHostTest`; that source set isn't shared across modules, so `:ui-scaffolds` and `:ui-app` (for
 * `FrnkStackShellTest`) each carry their own copy, kept in the same `…ui.atoms` package so test files
 * import `RobolectricComposeTest` / `setFrnkContent` unchanged. Keep the copies in sync; promoting them to
 * a shared test-fixtures set is the follow-up once a fourth module needs one.
 *
 * The atoms/scaffolds are headless `@Composable`s, so their state-driven behavior is verified by driving
 * a real composition with `runComposeUiTest` and querying the semantics tree. These run as JVM **host**
 * tests (`testAndroidHostTest` — what CI gates) under Robolectric, so the suite needs no device/emulator.
 *
 * `@RunWith` and Robolectric's `@Config` are `@Inherited`, so subclasses pick them up. `sdk = [34]` pins
 * the emulated framework to a level Robolectric 4.15.x ships. `GraphicsMode.LEGACY` is deliberate: these
 * tests only assert semantics + inject gestures (no pixel/screenshot assertions), so they don't need the
 * heavier NATIVE (Skia) runtime — and LEGACY avoids loading a platform-native graphics lib on CI.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.LEGACY)
@Config(sdk = [34])
abstract class RobolectricComposeTest

/**
 * Sets test content wrapped in [FrnkTheme] — the toolkit theme that installs the ambient atoms rely on
 * (`Theme[...]` token lookups, `LocalFrnkHaptics`, the ripple). Collapses the
 * `setContent { FrnkTheme { … } }` boilerplate every test would otherwise repeat.
 */
@OptIn(ExperimentalTestApi::class)
internal fun ComposeUiTest.setFrnkContent(content: @Composable () -> Unit) {
    setContent { FrnkTheme { content() } }
}