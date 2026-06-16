package dev.jdgarita.frnk.ui.app.ext

import dev.jdgarita.frnk.ui.app.FrnkAppConfig
import dev.jdgarita.frnk.ui.bottomnav.FrnkTabbedNavConfig

/** Projects the shared feature sub-configs onto the lower-level [FrnkTabbedNavConfig]. */
internal fun FrnkAppConfig.toTabbedNavConfig(): FrnkTabbedNavConfig =
    FrnkTabbedNavConfig(
        app = app,
        nav = nav,
        theme = theme,
        home = home,
        settings = settings,
        onboarding = onboarding,
    )
