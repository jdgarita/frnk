package com.tweener.kmpship.presentation._internal.libs.alarmee

import com.tweener.alarmee.MobileAlarmeeService
import com.tweener.alarmee.configuration.AlarmeePlatformConfiguration
import dev.gitlive.firebase.Firebase
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * @author Vivien Mahe
 * @since 09/06/2025
 */
class AlarmeeConfiguration : KoinComponent {

    private val alarmeeService: MobileAlarmeeService by inject()

    fun init() {
        alarmeeService.initialize(platformConfiguration = createAlarmeePlatformConfiguration(), firebase = Firebase)
    }
}

expect fun createAlarmeePlatformConfiguration(): AlarmeePlatformConfiguration
