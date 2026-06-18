package dev.jdgarita.frnk.remoteconfig

import dev.jdgarita.frnk.utils.AppResult
import dev.jdgarita.frnk.utils.CommonError

/**
 * SDK-free [RemoteConfigService] default: every getter returns the caller-supplied default and
 * [fetchAndActivate] is a successful no-op. Lets a host run without any remote-config backend (or a
 * test exercise call sites) and always see the bundled fallback values — same precedent as
 * `NoopAnalyticsTracker` / `NoopCrashReporter` in `:analytics-api`.
 */
class NoopRemoteConfig : RemoteConfigService {
    override suspend fun fetchAndActivate(): AppResult<Unit, CommonError> = AppResult.Success(Unit)

    override fun getString(
        key: String,
        default: String
    ): String = default

    override fun getBoolean(
        key: String,
        default: Boolean
    ): Boolean = default

    override fun getLong(
        key: String,
        default: Long
    ): Long = default

    override fun getDouble(
        key: String,
        default: Double
    ): Double = default
}