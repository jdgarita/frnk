package com.tweener.kmpship.data.source.firebase.firestore.model

import com.tweener.firebase.firestore.model.FirestoreModel
import kotlinx.serialization.Serializable

/**
 * @author Vivien Mahe
 * @since 17/09/2024
 */
@Serializable
data class FirestoreSubscriptionsDiscountModel(
    override var id: String,
    val active: Boolean,
    val percent: Double,
) : FirestoreModel()
