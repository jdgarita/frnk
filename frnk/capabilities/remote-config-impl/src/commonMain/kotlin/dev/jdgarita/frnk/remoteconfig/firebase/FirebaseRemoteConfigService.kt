package dev.jdgarita.frnk.remoteconfig.firebase

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.remoteconfig.ValueSource
import dev.gitlive.firebase.remoteconfig.remoteConfig
import dev.jdgarita.frnk.remoteconfig.RemoteConfigService
import dev.jdgarita.frnk.utils.AppResult
import dev.jdgarita.frnk.utils.CommonError
import kotlin.coroutines.cancellation.CancellationException
import dev.gitlive.firebase.remoteconfig.FirebaseRemoteConfig as GitliveRemoteConfig

/**
 * Firebase-backed [RemoteConfigService] over gitlive `dev.gitlive.firebase.remoteconfig`. Reads honour
 * the caller-supplied default whenever a key resolves to [ValueSource.Static] (no fetched value and no
 * registered default) — so an unset/cleared parameter can't surface an empty string or a `0`. A blank
 * remote string also falls back to the default (matches still's native impl).
 *
 * Installed as `remoteConfigModule`; the host supplies the native Firebase SDK + `GoogleService-Info`
 * / `google-services.json`. **Not** part of any toolkit module's common surface (api/impl discipline) —
 * resolved through Koin, exactly like `:analytics-impl`.
 */
internal class FirebaseRemoteConfigService(
    private val remoteConfig: GitliveRemoteConfig = Firebase.remoteConfig,
) : RemoteConfigService {
    override suspend fun fetchAndActivate(): AppResult<Unit, CommonError> =
        try {
            remoteConfig.fetchAndActivate()
            AppResult.Success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            AppResult.Failure(CommonError.Unknown)
        }

    override fun getString(
        key: String,
        default: String,
    ): String {
        val value = remoteConfig.getValue(key)
        return if (value.getSource() == ValueSource.Static) default else value.asString().ifBlank { default }
    }

    override fun getBoolean(
        key: String,
        default: Boolean,
    ): Boolean {
        val value = remoteConfig.getValue(key)
        return if (value.getSource() == ValueSource.Static) default else value.asBoolean()
    }

    override fun getLong(
        key: String,
        default: Long,
    ): Long {
        val value = remoteConfig.getValue(key)
        return if (value.getSource() == ValueSource.Static) default else value.asLong()
    }

    override fun getDouble(
        key: String,
        default: Double,
    ): Double {
        val value = remoteConfig.getValue(key)
        return if (value.getSource() == ValueSource.Static) default else value.asDouble()
    }
}
