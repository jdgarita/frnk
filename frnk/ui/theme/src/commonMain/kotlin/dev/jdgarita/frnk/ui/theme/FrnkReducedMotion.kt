package dev.jdgarita.frnk.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf

/**
 * Whether the user asked the OS for less motion: Android's "Remove animations" (an animator
 * duration scale of 0), iOS's Reduce Motion. Decorative motion — a drifting marquee, a page that
 * slides in — should stand still or cut to a short crossfade when this is on; motion that carries
 * meaning (a progress bar, a sheet rising) stays.
 *
 * Read once per composition and remembered: a change in the OS setting is picked up the next time
 * the caller enters the composition, which is how both platforms' own toolkits behave too. A test
 * or preview pins the answer with [LocalFrnkReducedMotion].
 */
@Composable
fun frnkReducedMotion(): Boolean = LocalFrnkReducedMotion.current ?: platformReducedMotion()

/** An override for [frnkReducedMotion]; `null` (the default) reads the platform setting. */
val LocalFrnkReducedMotion: ProvidableCompositionLocal<Boolean?> = compositionLocalOf { null }

@Composable
internal expect fun platformReducedMotion(): Boolean