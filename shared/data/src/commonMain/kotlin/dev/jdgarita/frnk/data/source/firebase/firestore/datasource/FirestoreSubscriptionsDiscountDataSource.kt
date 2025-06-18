package dev.jdgarita.frnk.data.source.firebase.firestore.datasource

import com.tweener.firebase.firestore.FirebaseFirestoreService
import dev.jdgarita.frnk.data.source.firebase.firestore.model.FirestoreSubscriptionsDiscountModel

/**
 * @author Vivien Mahe
 * @since 06/02/2025
 */
class FirestoreSubscriptionsDiscountDataSource(
    private val firebaseFirestoreService: FirebaseFirestoreService,
) {

    companion object {
        private const val SUBSCRIPTIONS_DISCOUNT_COLLECTION_NAME = "subscriptions_discount"
    }

    private val discounts: MutableList<FirestoreSubscriptionsDiscountModel> = mutableListOf()

    suspend fun getAllDiscounts(): List<FirestoreSubscriptionsDiscountModel> =
        discounts.ifEmpty {
            firebaseFirestoreService
                .getAll<FirestoreSubscriptionsDiscountModel>(collection = SUBSCRIPTIONS_DISCOUNT_COLLECTION_NAME)
                .also { discounts.addAll(it) }
        }
}
