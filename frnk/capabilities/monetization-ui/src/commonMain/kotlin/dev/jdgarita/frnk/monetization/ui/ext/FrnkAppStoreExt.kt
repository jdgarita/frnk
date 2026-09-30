package dev.jdgarita.frnk.monetization.ui.ext

import com.composeunstyled.theme.ThemeToken
import dev.jdgarita.frnk.monetization.ui.FrnkAppStore
import dev.jdgarita.frnk.ui.theme.stringNoSubscriptionFoundBodyAppStore
import dev.jdgarita.frnk.ui.theme.stringNoSubscriptionFoundBodyGooglePlay

/** The No Subscription Found body that names this store's receipt and account. */
internal fun FrnkAppStore.noSubscriptionFoundBodyToken(): ThemeToken<String> =
    when (this) {
        FrnkAppStore.AppStore -> stringNoSubscriptionFoundBodyAppStore
        FrnkAppStore.GooglePlay -> stringNoSubscriptionFoundBodyGooglePlay
    }