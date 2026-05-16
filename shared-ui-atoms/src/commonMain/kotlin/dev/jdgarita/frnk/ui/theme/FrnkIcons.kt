package dev.jdgarita.frnk.ui.theme

import androidx.compose.ui.graphics.vector.ImageVector
import com.composables.icons.lucide.ArrowLeft
import com.composables.icons.lucide.Check
import com.composables.icons.lucide.CircleAlert
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Search
import com.composables.icons.lucide.Settings
import com.composables.icons.lucide.X
import com.composeunstyled.theme.ThemeToken

// region Icon tokens — toolkit-internal icons the host can override.
val iconBack = ThemeToken<ImageVector>("icon_back")
val iconClose = ThemeToken<ImageVector>("icon_close")
val iconSearch = ThemeToken<ImageVector>("icon_search")
val iconSettings = ThemeToken<ImageVector>("icon_settings")
val iconCheck = ThemeToken<ImageVector>("icon_check")
val iconError = ThemeToken<ImageVector>("icon_error")
// endregion

internal val DefaultFrnkIcons: Map<ThemeToken<ImageVector>, ImageVector> =
    mapOf(
        iconBack to Lucide.ArrowLeft,
        iconClose to Lucide.X,
        iconSearch to Lucide.Search,
        iconSettings to Lucide.Settings,
        iconCheck to Lucide.Check,
        iconError to Lucide.CircleAlert,
    )
