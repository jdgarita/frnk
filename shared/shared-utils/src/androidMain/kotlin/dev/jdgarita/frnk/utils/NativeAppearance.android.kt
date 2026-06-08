package dev.jdgarita.frnk.utils

// No-op on Android: the toolkit's light/dark is driven entirely by the Compose palette (FrnkTheme),
// and there is no native chrome that resolves its appearance against the system style independently of
// the composition (the way iOS UIKit blur/glass materials do). A host that also wants the system bars or
// an AppCompat activity to follow the in-app toggle wires that at the activity level.
actual fun applyNativeInterfaceStyle(dark: Boolean?) = Unit
