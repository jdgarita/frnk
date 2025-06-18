package com.tweener.kmpship.data.repository

import com.tweener.kmpship.data.DataConstants.LOCAL_STORAGE_APP_LAUNCH_COUNT
import com.tweener.kmpship.data.DataConstants.LOCAL_STORAGE_KEY_CRASHLYTICS_AUTHORIZATION
import com.tweener.kmpship.data.DataConstants.LOCAL_STORAGE_KEY_NOTIFICATIONS_PERMISSION_STATE
import com.tweener.kmpship.data.DataConstants.LOCAL_STORAGE_KEY_THEME_TYPE
import com.tweener.kmpship.data.source.local.datasource.LocalStorageDataSource
import com.tweener.kmpship.data.source.local.mapper.LocalThemeTypeModelMapper
import com.tweener.kmpship.domain.repository.SettingsRepository
import com.tweener.firebase.analytics.FirebaseAnalyticsService
import com.tweener.firebase.crashlytics.FirebaseCrashlyticsService
import dev.gitlive.firebase.analytics.FirebaseAnalytics
import dev.gitlive.firebase.analytics.setConsent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * @author Vivien Mahe
 * @since 14/11/2023
 */
class SettingsRepositoryImpl(
    private val firebaseCrashlyticsService: FirebaseCrashlyticsService,
    private val firebaseAnalyticsService: FirebaseAnalyticsService,
    private val localThemeTypeModelMapper: LocalThemeTypeModelMapper,
    private val localStorageDataSource: LocalStorageDataSource,
) : SettingsRepository {

    override suspend fun setThemeType(inputParams: SettingsRepository.InputParams.SetThemeType) {
        localStorageDataSource.setString(LOCAL_STORAGE_KEY_THEME_TYPE, localThemeTypeModelMapper.convertToModel(inputParams.themeType))
    }

    override suspend fun getThemeType(): Flow<SettingsRepository.OutputParams.GetThemeType> =
        localStorageDataSource
            .getStringAsFlow(LOCAL_STORAGE_KEY_THEME_TYPE, "invalid")
            .map { localThemeType -> localThemeTypeModelMapper.convertToEntity(localThemeType) }
            .map { themeType -> SettingsRepository.OutputParams.GetThemeType(themeType = themeType) }

    override suspend fun setNotificationsPermissionState(inputParams: SettingsRepository.InputParams.SetNotificationsPermissionState) {
        localStorageDataSource.setBoolean(LOCAL_STORAGE_KEY_NOTIFICATIONS_PERMISSION_STATE, inputParams.granted)
    }

    override suspend fun getNotificationsPermissionState(): Flow<SettingsRepository.OutputParams.GetNotificationsPermissionState> =
        localStorageDataSource
            .getBooleanAsFlow(LOCAL_STORAGE_KEY_NOTIFICATIONS_PERMISSION_STATE, false)
            .map { SettingsRepository.OutputParams.GetNotificationsPermissionState(granted = it) }

    override suspend fun setCrashlyticsAuthorizationState(inputParams: SettingsRepository.InputParams.SetCrashlyticsAuthorizationState) {
        firebaseAnalyticsService.getAnalytics().setConsent {
            analyticsStorage = if (inputParams.authorized) FirebaseAnalytics.ConsentStatus.GRANTED else FirebaseAnalytics.ConsentStatus.DENIED
        }

        firebaseCrashlyticsService.getCrashlytics().setCrashlyticsCollectionEnabled(enabled = inputParams.authorized)

        localStorageDataSource.setBoolean(LOCAL_STORAGE_KEY_CRASHLYTICS_AUTHORIZATION, inputParams.authorized)
    }

    override suspend fun getCrashlyticsAuthorizationState(): Flow<SettingsRepository.OutputParams.GetCrashlyticsAuthorizationState> =
        localStorageDataSource
            .getBooleanAsFlow(LOCAL_STORAGE_KEY_CRASHLYTICS_AUTHORIZATION, true)
            .map { SettingsRepository.OutputParams.GetCrashlyticsAuthorizationState(authorized = it) }

    override suspend fun setAppLaunchCount(inputParams: SettingsRepository.InputParams.SetAppLaunchCount) {
        localStorageDataSource.setInt(LOCAL_STORAGE_APP_LAUNCH_COUNT, inputParams.count)
    }

    override suspend fun getAppLaunchCount(): Flow<SettingsRepository.OutputParams.GetAppLaunchCount> =
        localStorageDataSource
            .getIntAsFlow(LOCAL_STORAGE_APP_LAUNCH_COUNT, 0)
            .map { SettingsRepository.OutputParams.GetAppLaunchCount(count = it) }
}
