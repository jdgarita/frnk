package com.tweener.kmpship.domain.repository

import com.tweener.kmpship.domain.entity.ThemeType
import kotlinx.coroutines.flow.Flow

/**
 * @author Vivien Mahe
 * @since 18/10/2023
 */
interface SettingsRepository {

    sealed class InputParams {
        data class SetThemeType(val themeType: ThemeType) : InputParams()
        data class SetNotificationsPermissionState(val granted: Boolean) : InputParams()
        data class SetCrashlyticsAuthorizationState(val authorized: Boolean) : InputParams()
        data class SetAppLaunchCount(val count: Int) : InputParams()
    }

    sealed class OutputParams {
        data class GetThemeType(val themeType: ThemeType) : OutputParams()
        data class GetNotificationsPermissionState(val granted: Boolean) : OutputParams()
        data class GetCrashlyticsAuthorizationState(val authorized: Boolean) : OutputParams()
        data class GetAppLaunchCount(val count: Int) : OutputParams()
    }

    suspend fun setThemeType(inputParams: InputParams.SetThemeType)

    suspend fun getThemeType(): Flow<OutputParams.GetThemeType>

    suspend fun setNotificationsPermissionState(inputParams: InputParams.SetNotificationsPermissionState)

    suspend fun getNotificationsPermissionState(): Flow<OutputParams.GetNotificationsPermissionState>

    suspend fun setCrashlyticsAuthorizationState(inputParams: InputParams.SetCrashlyticsAuthorizationState)

    suspend fun getCrashlyticsAuthorizationState(): Flow<OutputParams.GetCrashlyticsAuthorizationState>

    suspend fun setAppLaunchCount(inputParams: InputParams.SetAppLaunchCount)

    suspend fun getAppLaunchCount(): Flow<OutputParams.GetAppLaunchCount>
}
