package dev.jdgarita.frnk.config.dependencies

const val platformModulePrefix: String = ModuleConfig.platformModulePrefix

object Deps {

    object Main {

        object Frnk {
            const val app = "$platformModulePrefix:app"
            const val domainFramework = "$platformModulePrefix:domain:framework-domain"
            const val presentationMvi = "$platformModulePrefix:presentation:mvi"
            const val presentationComponentCore = "$platformModulePrefix:presentation:component-core"
            const val presentationFrnkResources = "$platformModulePrefix:presentation:frnk-resources"
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