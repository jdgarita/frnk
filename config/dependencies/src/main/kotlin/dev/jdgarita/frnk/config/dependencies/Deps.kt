package dev.jdgarita.frnk.config.dependencies

const val platformModulePrefix: String = ModuleConfig.platformModulePrefix

object Deps {

    object Main {

        object Frnk {
            const val app = "$platformModulePrefix:app"
            const val sdk = "$platformModulePrefix:sdk"
            const val domainFramework = "$platformModulePrefix:domain:framework-domain"
            const val domainConfig = "$platformModulePrefix:domain:config-domain"
            const val dataFramework = "$platformModulePrefix:data:framework-data"
            const val presentationMvi = "$platformModulePrefix:presentation:mvi"
            const val presentationTabConfig = "$platformModulePrefix:presentation:tab-config"
            const val presentationHomeApi = "$platformModulePrefix:presentation:home-presentation-api"
            const val presentationComponentCore = "$platformModulePrefix:presentation:component-core"
            const val presentationFrnkResources = "$platformModulePrefix:presentation:frnk-resources"
            const val presentationFramework = "$platformModulePrefix:presentation:framework-presentation"
            const val uiFramework = "$platformModulePrefix:ui:framework-ui"
            const val uiComponentLibrary = "$platformModulePrefix:ui:component-library"
            const val utilDi = "$platformModulePrefix:util:di"
            const val utilCommon = "$platformModulePrefix:util:common"
            const val subs = "$platformModulePrefix:subs"
        }
    }

    object Test {
        object Frnk
    }
}