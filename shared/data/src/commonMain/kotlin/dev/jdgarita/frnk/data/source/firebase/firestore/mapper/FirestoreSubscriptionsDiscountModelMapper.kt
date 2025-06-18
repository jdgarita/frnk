package dev.jdgarita.frnk.data.source.firebase.firestore.mapper

import dev.jdgarita.frnk.data.source.firebase.firestore.model.FirestoreSubscriptionsDiscountModel
import dev.jdgarita.frnk.domain.entity.PaywallProductPeriod
import dev.jdgarita.frnk.domain.entity.SubscriptionDiscount
import dev.jdgarita.frnk.domain.mapper.EntityMapper

/**
 * @author Vivien Mahe
 * @since 06/02/2025
 */
class FirestoreSubscriptionsDiscountModelMapper : EntityMapper<SubscriptionDiscount, FirestoreSubscriptionsDiscountModel> {

    companion object {
        private const val WEEKLY_SLUG = "weekly"
        private const val MONTHLY_SLUG = "monthly"
        private const val YEARLY_SLUG = "yearly"
        private const val LIFETIME_SLUG = "lifetime"
    }

    override fun convertToModel(entity: SubscriptionDiscount): FirestoreSubscriptionsDiscountModel =
        FirestoreSubscriptionsDiscountModel(
            id = entity.id,
            active = entity.active,
            percent = entity.percent,
        )

    override fun convertToEntity(model: FirestoreSubscriptionsDiscountModel): SubscriptionDiscount {
        val period = when (model.id) {
            WEEKLY_SLUG -> PaywallProductPeriod.WEEKLY
            MONTHLY_SLUG -> PaywallProductPeriod.MONTHLY
            YEARLY_SLUG -> PaywallProductPeriod.YEARLY
            LIFETIME_SLUG -> PaywallProductPeriod.LIFETIME
            else -> throw IllegalArgumentException("Unknown ID: ${model.id}")
        }

        return SubscriptionDiscount(
            id = model.id,
            active = model.active,
            percent = model.percent,
            period = period,
        )
    }
}
