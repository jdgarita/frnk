package dev.jdgarita.frnk.backend.posthog

import com.posthog.kmp.PostHogContext
import org.koin.android.ext.koin.androidApplication
import org.koin.core.scope.Scope

internal actual fun Scope.platformPostHogContext(): PostHogContext = PostHogContext(androidApplication())