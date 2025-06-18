package com.tweener.kmpship._internal.libs

import com.tweener.kmpship.data._internal.libs.room.RoomConfiguration
import com.tweener.kmpship.presentation._internal.libs.alarmee.AlarmeeConfiguration
import com.tweener.kmpship.presentation._internal.libs.firebase.FirebaseConfiguration
import com.tweener.kmpship.presentation._internal.libs.napier.NapierConfiguration
import com.tweener.kmpship.presentation._internal.libs.passage.PassageConfiguration
import com.tweener.kmpship.presentation._internal.libs.revenuecat.RevenueCatConfiguration

/**
 * @author Vivien Mahe
 * @since 14/02/2024
 */
class LibrariesConfiguration(
    private val firebaseConfiguration: FirebaseConfiguration,
    private val passageConfiguration: PassageConfiguration,
    private val alarmeeConfiguration: AlarmeeConfiguration,
    private val napierConfiguration: NapierConfiguration,
    private val roomConfiguration: RoomConfiguration,
    private val revenueCatConfiguration: RevenueCatConfiguration,
) {

    fun init() {
        firebaseConfiguration.init()
        passageConfiguration.init()
        alarmeeConfiguration.init()
        napierConfiguration.init()
        roomConfiguration.init()
        revenueCatConfiguration.init()
    }
}
