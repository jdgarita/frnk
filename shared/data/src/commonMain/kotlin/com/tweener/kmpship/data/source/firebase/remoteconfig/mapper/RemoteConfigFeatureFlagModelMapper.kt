package com.tweener.kmpship.data.source.firebase.remoteconfig.mapper

import com.tweener.kmpship.data.source.firebase.remoteconfig.model.RemoteConfigKey
import com.tweener.kmpship.data.source.firebase.remoteconfig.model.RemoteConfigModel
import com.tweener.kmpship.domain.entity.FeatureFlag
import com.tweener.kmpship.domain.mapper.EntityToModelMapper

/**
 * @author Vivien Mahe
 * @since 16/12/2023
 */
class RemoteConfigFeatureFlagModelMapper : EntityToModelMapper<FeatureFlag, RemoteConfigModel> {

    override fun convertToModel(entity: FeatureFlag): RemoteConfigModel {
        val key = when (entity) {
            // TODO Add here mapping for feature flags, ie:
            FeatureFlag.EXAMPLE_FEATURE_FLAG -> RemoteConfigKey.EXAMPLE_FEATURE_FLAG
        }

        return RemoteConfigModel(key = key)
    }
}
