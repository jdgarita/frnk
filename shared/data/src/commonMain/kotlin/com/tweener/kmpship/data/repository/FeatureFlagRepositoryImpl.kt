package com.tweener.kmpship.data.repository

import com.tweener.kmpship.data.source.firebase.remoteconfig.mapper.RemoteConfigFeatureFlagModelMapper
import com.tweener.kmpship.domain.repository.FeatureFlagRepository
import com.tweener.firebase.remoteconfig.datasource.FirebaseRemoteConfigDataSource

/**
 * @author Vivien Mahe
 * @since 16/12/2023
 */
class FeatureFlagRepositoryImpl(
    private val remoteConfigFeatureFlagModelMapper: RemoteConfigFeatureFlagModelMapper,
    private val remoteConfigDataSource: FirebaseRemoteConfigDataSource,
) : FeatureFlagRepository {

    override suspend fun get(inputParams: FeatureFlagRepository.InputParams.Get): FeatureFlagRepository.OutputParams.Get =
        remoteConfigDataSource
            .getBoolean(key = remoteConfigFeatureFlagModelMapper.convertToModel(inputParams.featureFlag).key.value, defaultValue = inputParams.featureFlag.defaultValue)
            .let { FeatureFlagRepository.OutputParams.Get(enabled = it) }
}
