package dev.jdgarita.frnk.domain.config.ext

import dev.jdgarita.frnk.domain.config.ClientConfiguration
import dev.jdgarita.frnk.domain.config.FrnkBuildType
import dev.jdgarita.frnk.domain.config.ServiceEnvironment

fun getBuildType(clientConfiguration: ClientConfiguration) =
    when (clientConfiguration.serviceEnvironment) {
        ServiceEnvironment.DEV -> FrnkBuildType.Dev
        ServiceEnvironment.PROD -> FrnkBuildType.Store
    }