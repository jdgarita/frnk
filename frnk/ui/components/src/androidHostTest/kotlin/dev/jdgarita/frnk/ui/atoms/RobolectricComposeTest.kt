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
 * Base for the design-system Compose UI tests (P4-4).
 *
 * The atoms are headless `@Composable`s, so their state-driven behavior is verified by driving a real
 * composition with `runComposeUiTest` and querying the semantics tree — not by unit-testing a reducer
 * (atoms have none). These run as JVM **host** tests (`testAndroidHostTest`) under Robolectric, so the
 * suite gates in CI with no device/emulator (CI runs only `compileAndroidMain` + `testAndroidHostTest`).
 * That's also why this lives in `androidHostTest` rather than `commonTest`: the Compose UI-test runtime
 * and Robolectric have no common/iOS variant (mirrors `shared-database-impl`'s androidHostTest JDBC driver).
 *
 * `@RunWith` and Robolectric's `@Config` are `@Inherited`, so subclasses pick them up. `sdk = [34]` pins
 * the emulated framework to a level Robolectric 4.15.x ships (the module's compileSdk is higher and
 * unsupported as a *runtime* target). `GraphicsMode.LEGACY` is deliberate: these tests only assert
 * semantics + inject gestures (no pixel/screenshot assertions), so they don't need the heavier NATIVE
 * (Skia) runtime — and LEGACY avoids loading a platform-native graphics lib on the Linux CI runner.
 *
 * **Reuse note (altitude):** if a second module needs Compose host tests, promote this base class +
 * [setFrnkContent] + the `androidHostTest` dependency bundle (see `build.gradle.kts`) into a shared
 * test-fixtures source set rather than copying them — they're package-private here on purpose (single
 * consumer today; the project keeps build logic per-module, so no convention plugin yet).
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.LEGACY)
@Config(sdk = [34])
abstract class RobolectricComposeTest

/**
 * Sets test content wrapped in [FrnkTheme] — the toolkit theme that installs the ambient atoms rely on
 * (`Theme[...]` token lookups, `LocalFrnkHaptics`, the ripple). Collapses the
 * `setContent { FrnkTheme { … } }` boilerplate every atom test would otherwise repeat, and makes the
 * theme wrap a guaranteed invariant rather than a per-test copy-paste. If test-time theming ever needs
 * an extra ambient (a pinned appearance, a deterministic clock), add it here once.
 */
@OptIn(ExperimentalTestApi::class)
internal fun ComposeUiTest.setFrnkContent(content: @Composable () -> Unit) {
    setContent { FrnkTheme { content() } }
}
