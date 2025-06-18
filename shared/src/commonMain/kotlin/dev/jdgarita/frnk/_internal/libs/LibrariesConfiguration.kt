package dev.jdgarita.frnk._internal.libs

import dev.jdgarita.frnk.data._internal.libs.room.RoomConfiguration
import dev.jdgarita.frnk.presentation._internal.libs.alarmee.AlarmeeConfiguration
import dev.jdgarita.frnk.presentation._internal.libs.firebase.FirebaseConfiguration
import dev.jdgarita.frnk.presentation._internal.libs.napier.NapierConfiguration
import dev.jdgarita.frnk.presentation._internal.libs.passage.PassageConfiguration
import dev.jdgarita.frnk.presentation._internal.libs.revenuecat.RevenueCatConfiguration

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
