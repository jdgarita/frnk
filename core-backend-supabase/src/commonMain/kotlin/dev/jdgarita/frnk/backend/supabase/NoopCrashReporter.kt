package dev.jdgarita.frnk.backend.supabase

import dev.jdgarita.frnk.backend.CrashReporter

internal class NoopCrashReporter : CrashReporter {
    override fun recordException(throwable: Throwable, extras: Map<String, String>) = Unit
    override fun setUserId(id: String?) = Unit
    override fun log(message: String) = Unit
}
