package dev.jdgarita.frnk.ui.atoms

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Theme tokens the toolkit consumes. Host apps override any subset; atoms below
 * (ToolkitButton, ToolkitTextField, …) read from these locals, so host overrides
 * propagate without re-wrapping individual components.
 */
@Immutable
data class ToolkitColors(
    val primary: Color = Color(0xFF6750A4),
    val onPrimary: Color = Color.White,
    val surface: Color = Color(0xFFFFFBFE),
    val onSurface: Color = Color(0xFF1C1B1F),
    val outline: Color = Color(0xFF79747E),
    val error: Color = Color(0xFFB3261E),
)

@Immutable
data class ToolkitTypography(
    val body: TextStyle = TextStyle(fontSize = 14.sp),
    val title: TextStyle = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.SemiBold),
    val button: TextStyle = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Medium),
)

@Immutable
data class ToolkitStrings(
    val signIn: String = "Sign in",
    val signOut: String = "Sign out",
    val upgrade: String = "Upgrade to Pro",
    val cancel: String = "Cancel",
    val retry: String = "Retry",
    val genericError: String = "Something went wrong",
)

val LocalToolkitColors      = staticCompositionLocalOf { ToolkitColors() }
val LocalToolkitTypography  = staticCompositionLocalOf { ToolkitTypography() }
val LocalToolkitStrings     = staticCompositionLocalOf { ToolkitStrings() }

object ToolkitTheme {
    val colors:     ToolkitColors     @Composable @ReadOnlyComposable get() = LocalToolkitColors.current
    val typography: ToolkitTypography @Composable @ReadOnlyComposable get() = LocalToolkitTypography.current
    val strings:    ToolkitStrings    @Composable @ReadOnlyComposable get() = LocalToolkitStrings.current
}

@Composable
fun ProvideToolkitTheme(
    colors: ToolkitColors = ToolkitColors(),
    typography: ToolkitTypography = ToolkitTypography(),
    strings: ToolkitStrings = ToolkitStrings(),
    content: @Composable () -> Unit,
) = CompositionLocalProvider(
    LocalToolkitColors provides colors,
    LocalToolkitTypography provides typography,
    LocalToolkitStrings provides strings,
    content = content,
)
