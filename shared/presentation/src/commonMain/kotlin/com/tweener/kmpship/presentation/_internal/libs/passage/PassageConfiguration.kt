package com.tweener.kmpship.presentation._internal.libs.passage

import com.tweener.passage.Passage
import com.tweener.passage.model.AppleGatekeeperConfiguration
import com.tweener.passage.model.EmailPasswordGatekeeperConfiguration
import com.tweener.passage.model.GoogleGatekeeperAndroidConfiguration
import com.tweener.passage.model.GoogleGatekeeperConfiguration
import dev.gitlive.firebase.Firebase
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * @author Vivien Mahe
 * @since 04/12/2024
 */
class PassageConfiguration(
    private val serverClientId: String,
) : KoinComponent {

    private val passage: Passage by inject()

    fun init() {
        passage.initialize(
            gatekeeperConfigurations = listOf(
                GoogleGatekeeperConfiguration(
                    serverClientId = serverClientId,
                    android = GoogleGatekeeperAndroidConfiguration(maxRetries = 1),
                ),
                AppleGatekeeperConfiguration(),
                EmailPasswordGatekeeperConfiguration,
            ),
            firebase = Firebase,
        )
    }
}
