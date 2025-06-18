package dev.jdgarita.frnk.data.source.revenuecat.mapper

import com.revenuecat.purchases.kmp.models.Store
import dev.jdgarita.frnk.domain.entity.AccountSubscriptionStore
import dev.jdgarita.frnk.domain.mapper.ModelToEntityMapper

/**
 * @author Vivien Mahe
 * @since 20/01/2025
 */
class RevenueCatAccountSubscriptionStoreMapper : ModelToEntityMapper<Store, AccountSubscriptionStore> {

    override fun convertToEntity(model: Store): AccountSubscriptionStore =
        when (model) {
            Store.APP_STORE -> AccountSubscriptionStore.APP_STORE
            Store.PLAY_STORE -> AccountSubscriptionStore.GOOGLE_PLAY
            else -> throw IllegalArgumentException("Unknown store: $model")
        }
}
