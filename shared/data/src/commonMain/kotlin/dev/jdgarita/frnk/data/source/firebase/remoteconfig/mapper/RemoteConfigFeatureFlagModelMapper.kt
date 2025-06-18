package dev.jdgarita.frnk.data.source.firebase.remoteconfig.mapper

import dev.jdgarita.frnk.data.source.firebase.remoteconfig.model.RemoteConfigKey
import dev.jdgarita.frnk.data.source.firebase.remoteconfig.model.RemoteConfigModel
import dev.jdgarita.frnk.domain.entity.FeatureFlag
import dev.jdgarita.frnk.domain.mapper.EntityToModelMapper

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
