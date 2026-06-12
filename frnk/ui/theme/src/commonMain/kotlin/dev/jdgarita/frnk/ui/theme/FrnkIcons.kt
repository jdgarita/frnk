package dev.jdgarita.frnk.ui.theme

import androidx.compose.ui.graphics.vector.ImageVector
import com.composables.icons.lucide.ArrowLeft
import com.composables.icons.lucide.Bell
import com.composables.icons.lucide.BookOpen
import com.composables.icons.lucide.Check
import com.composables.icons.lucide.ChevronRight
import com.composables.icons.lucide.CircleAlert
import com.composables.icons.lucide.CreditCard
import com.composables.icons.lucide.Crown
import com.composables.icons.lucide.FileText
import com.composables.icons.lucide.House
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.MessageSquare
import com.composables.icons.lucide.Plus
import com.composables.icons.lucide.RefreshCw
import com.composables.icons.lucide.Search
import com.composables.icons.lucide.Settings
import com.composables.icons.lucide.Shield
import com.composables.icons.lucide.Star
import com.composables.icons.lucide.Vibrate
import com.composables.icons.lucide.X
import com.composeunstyled.theme.ThemeToken

// region Icon tokens — toolkit-internal icons the host can override.
val iconBack = ThemeToken<ImageVector>("icon_back")
val iconClose = ThemeToken<ImageVector>("icon_close")
val iconSearch = ThemeToken<ImageVector>("icon_search")
val iconSettings = ThemeToken<ImageVector>("icon_settings")
val iconCheck = ThemeToken<ImageVector>("icon_check")
val iconError = ThemeToken<ImageVector>("icon_error")

// Settings scaffold icons. Used by the default settings catalog
// (SettingsDefaults.rememberDefaultSettingsState); hosts override per token.
val iconChevronRight = ThemeToken<ImageVector>("icon_chevron_right")
val iconUpgrade = ThemeToken<ImageVector>("icon_upgrade")
val iconRestore = ThemeToken<ImageVector>("icon_restore")
val iconManageSubscription = ThemeToken<ImageVector>("icon_manage_subscription")
val iconFeedback = ThemeToken<ImageVector>("icon_feedback")
val iconRate = ThemeToken<ImageVector>("icon_rate")
val iconPrivacy = ThemeToken<ImageVector>("icon_privacy")
val iconTerms = ThemeToken<ImageVector>("icon_terms")
val iconNotifications = ThemeToken<ImageVector>("icon_notifications")
val iconOnboarding = ThemeToken<ImageVector>("icon_onboarding")
val iconHaptics = ThemeToken<ImageVector>("icon_haptics")

// Bottom-nav scaffold icons. The Home tab uses [iconNavHome]; the Settings tab reuses
// [iconSettings]; the configurable middle tab supplies its own ImageVector at the call site.
// [iconNavAdd] is the default glyph for the adaptive bar's primary-action (add) button.
val iconNavHome = ThemeToken<ImageVector>("icon_nav_home")
val iconNavAdd = ThemeToken<ImageVector>("icon_nav_add")
// endregion

internal val DefaultFrnkIcons: Map<ThemeToken<ImageVector>, ImageVector> =
    mapOf(
        iconBack to Lucide.ArrowLeft,
        iconClose to Lucide.X,
        iconSearch to Lucide.Search,
        iconSettings to Lucide.Settings,
        iconCheck to Lucide.Check,
        iconError to Lucide.CircleAlert,
        iconChevronRight to Lucide.ChevronRight,
        iconUpgrade to Lucide.Crown,
        iconRestore to Lucide.RefreshCw,
        iconManageSubscription to Lucide.CreditCard,
        iconFeedback to Lucide.MessageSquare,
        iconRate to Lucide.Star,
        iconPrivacy to Lucide.Shield,
        iconTerms to Lucide.FileText,
        iconNotifications to Lucide.Bell,
        iconOnboarding to Lucide.BookOpen,
        iconHaptics to Lucide.Vibrate,
        iconNavHome to Lucide.House,
        iconNavAdd to Lucide.Plus,
    )
