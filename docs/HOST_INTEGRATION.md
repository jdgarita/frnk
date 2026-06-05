# Host App Integration

`frnk` is a Kotlin Multiplatform toolkit consumed by host apps as a Gradle composite build
(`includeBuild("../frnk")` in the host's `settings.gradle.kts`). The toolkit ships interfaces;
the host wires impls. There are three integration points.

## 1. Inject your SQLDelight schema

The toolkit owns the **driver factory**, not the schema. In your host app's DI graph:

```kotlin
val hostDatabaseModule = module {
    single<MyHostDatabase> {
        val factory: SqlDriverFactory = get()
        MyHostDatabase(factory.create(MyHostDatabase.Schema, "host.db"))
    }
}
```

On Android, before `startKoin { ... }`, point the toolkit at your `Application` context:

```kotlin
DatabaseContext.application = applicationContext
```

## 2. Override UI tokens (colors, typography, strings)

Wrap your host's content in `ProvideToolkitTheme` with your palette/strings. Every
toolkit atom (`ToolkitButton`, `ToolkitTextField`) reads from these locals automatically.

```kotlin
ProvideToolkitTheme(
    colors = ToolkitColors(primary = MyBrand.Primary, onPrimary = MyBrand.OnPrimary),
    strings = ToolkitStrings(upgrade = "Go Pro"),
) {
    HostNavHost()
}
```

## 3. Map ToolkitRoute to Compose screens

The toolkit emits `ToolkitRoute` values (e.g. when `FeatureGate.requestUpgrade(...)` fires);
the host owns the NavHost and decides what each route renders:

```kotlin
val navigator: Navigator = { route ->
    when (route) {
        ToolkitRoute.Paywall    -> navController.navigate(HostRoutes.Paywall)
        ToolkitRoute.Onboarding -> navController.navigate(HostRoutes.Onboarding)
        ToolkitRoute.Home       -> navController.navigate(HostRoutes.Home)
        else                  -> Unit
    }
}
```

## 4. Pick a backend (Firebase XOR Supabase)

Include exactly one of the backend impl modules in your host's dependencies and pass
its Koin module to `startKoin`. Importing both will fail at Koin start with a duplicate
binding for `AuthService`/`AnalyticsTracker`/`RemoteData` — by design.

```kotlin
startKoin {
    modules(
        toolkitCoreModules() +
        listOf(
            firebaseBackendModule,   // OR supabaseBackendModule, never both
            revenueCatModule,
            hostDatabaseModule,
            hostFeatureModules,
        )
    )
}
```

## 5. Custom analytics

The toolkit fires a generic event vocabulary (`ToolkitEvent.AppOpened`, `PaywallViewed`, …)
through whichever `AnalyticsTracker` is bound. Push your own events through the same instance:

```kotlin
val analytics: AnalyticsTracker by inject()
analytics.trackCustom("Recipe_Saved", mapOf("recipe_id" to id))
```
