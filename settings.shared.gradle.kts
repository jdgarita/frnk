// Set platformModulePrefix= in gradle.properties
val platformModulePrefix: String by settings
val platformPathPrefix: String by settings

include(
    "$platformModulePrefix:app",
    "$platformModulePrefix:presentation:mvi",
    "$platformModulePrefix:presentation:framework-presentation",
    "$platformModulePrefix:presentation:identity-presentation",

    "$platformModulePrefix:presentation:tab-config",
    "$platformModulePrefix:data:framework-data",
    "$platformModulePrefix:presentation:home-presentation-api",
    "$platformModulePrefix:presentation:component-core",
    "$platformModulePrefix:ui:component-library",
    "$platformModulePrefix:presentation:frnk-resources",
    "$platformModulePrefix:ui:framework-ui",
    "$platformModulePrefix:ui:identity-ui",
    "$platformModulePrefix:util:di",
    "$platformModulePrefix:util:common",
    "$platformModulePrefix:domain:framework-domain",
    "$platformModulePrefix:domain:config-domain",
    "$platformModulePrefix:subs",

    "$platformModulePrefix:sdk"
)

project("$platformModulePrefix:app").projectDir = File("${platformPathPrefix}app")
project("$platformModulePrefix:presentation:mvi").projectDir = File("${platformPathPrefix}presentation/mvi")
project("$platformModulePrefix:presentation:framework-presentation").projectDir = File(
    "${platformPathPrefix}presentation/framework-presentation"
)
project("$platformModulePrefix:presentation:identity-presentation").projectDir = File(
    "${platformPathPrefix}presentation/identity-presentation"
)
project("$platformModulePrefix:presentation:tab-config").projectDir = File(
    "${platformPathPrefix}presentation/tab-config"
)
project("$platformModulePrefix:data:framework-data").projectDir = File("${platformPathPrefix}data/framework-data")
project("$platformModulePrefix:presentation:home-presentation-api").projectDir = File(
    "${platformPathPrefix}presentation/home-presentation-api"
)
project("$platformModulePrefix:presentation:component-core").projectDir = File(
    "${platformPathPrefix}presentation/component-core"
)
project("$platformModulePrefix:ui:component-library").projectDir = File("${platformPathPrefix}ui/component-library")
project("$platformModulePrefix:presentation:frnk-resources").projectDir = File(
    "${platformPathPrefix}presentation/frnk-resources"
)
project("$platformModulePrefix:ui:framework-ui").projectDir = File("${platformPathPrefix}ui/framework-ui")
project("$platformModulePrefix:ui:identity-ui").projectDir = File("${platformPathPrefix}ui/identity-ui")
project("$platformModulePrefix:util:di").projectDir = File("${platformPathPrefix}util/di")
project("$platformModulePrefix:util:common").projectDir = File("${platformPathPrefix}util/common")
project("$platformModulePrefix:domain:framework-domain").projectDir = File(
    "${platformPathPrefix}domain/framework-domain"
)
project("$platformModulePrefix:domain:config-domain").projectDir = File("${platformPathPrefix}domain/config-domain")