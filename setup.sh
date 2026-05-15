#!/usr/bin/env bash
# ============================================================================
# Indie App Toolkit — scaffold / migration script for `frnk`
# ----------------------------------------------------------------------------
# DESTRUCTIVE. Replaces the current core-network-*, core-database-*,
# core-ui-atoms, core-common, androidApp, iosApp, androidDemoApp, iosDemoApp
# layout with a capability-based KMP/CMP toolkit:
#
#   core-utils
#   core-ui-api          (MVI engine, routing sealed interface, UiText)
#   core-ui-atoms        (compose-unstyled design system + theme tokens)
#   core-database-api    (SQLDelight driver factory contract, settings)
#   core-database-impl   (default SQLDelight + multiplatform-settings impl)
#   core-backend-api     (auth, remote data, analytics, crash interfaces)
#   core-backend-firebase
#   core-backend-supabase
#   core-monetization-api
#   core-monetization-revenuecat
#   androidApp / iosApp  (library aggregators; iosApp emits FrnkKit.xcframework)
#
# USAGE:
#   ./setup.sh             # dry-run, prints planned changes
#   ./setup.sh --apply     # actually performs the migration
#
# Preserves:  .git/, gradle/wrapper/, gradlew, gradlew.bat, .github/, docs/,
#             buildSrc/, local.properties*, .gitignore, CLAUDE.md, README.md
# ============================================================================
set -euo pipefail

APPLY=0
[[ "${1:-}" == "--apply" ]] && APPLY=1

ROOT="$(cd "$(dirname "$0")" && pwd)"
cd "$ROOT"

say() { printf '%s\n' "$*"; }
run() {
  if (( APPLY )); then eval "$@"; else say "  [dry] $*"; fi
}
write_file() {
  local path="$1"
  if (( APPLY )); then
    mkdir -p "$(dirname "$path")"
    cat > "$path"
  else
    say "  [dry] write $path ($(wc -l) lines)"
    cat > /dev/null
  fi
}

say "============================================================"
say " Indie App Toolkit migration"
say " mode: $([[ $APPLY -eq 1 ]] && echo APPLY || echo DRY-RUN)"
say " root: $ROOT"
say "============================================================"

# ---------------------------------------------------------------------------
# 1. Remove the obsolete module tree (api/impl-by-tech layout).
# ---------------------------------------------------------------------------
OLD_MODULES=(
  core-common
  core-network-api
  core-network-impl
  core-database-api
  core-database-impl
  core-ui-atoms
  androidApp
  iosApp
  androidDemoApp
  iosDemoApp
)
say
say "[1/6] removing legacy modules"
for m in "${OLD_MODULES[@]}"; do
  [[ -d "$ROOT/$m" ]] && run "rm -rf '$ROOT/$m'"
done

# ---------------------------------------------------------------------------
# 2. Top-level Gradle wiring (settings, root build, gradle.properties, catalog)
# ---------------------------------------------------------------------------
say
say "[2/6] writing root Gradle files"

write_file "$ROOT/settings.gradle.kts" <<'EOF'
@file:Suppress("UnstableApiUsage")

pluginManagement {
    repositories {
        google { content { includeGroupByRegex(".*google.*"); includeGroupByRegex(".*android.*") } }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }
}

plugins { id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0" }

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

rootProject.name = "frnk"

include(
    ":core-utils",
    ":core-ui-api",
    ":core-ui-atoms",
    ":core-database-api",
    ":core-database-impl",
    ":core-backend-api",
    ":core-backend-firebase",
    ":core-backend-supabase",
    ":core-monetization-api",
    ":core-monetization-revenuecat",
    ":androidApp",
    ":iosApp",
    ":androidDemoApp",
)
EOF

write_file "$ROOT/build.gradle.kts" <<'EOF'
plugins {
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.compose.multiplatform) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.sqldelight) apply false
    alias(libs.plugins.ktlint) apply false
}

allprojects {
    apply(plugin = rootProject.libs.plugins.ktlint.get().pluginId)
    extensions.configure<org.jlleitschuh.gradle.ktlint.KtlintExtension> {
        version.set("1.3.1")
        ignoreFailures.set(false)
        filter { exclude { it.file.path.contains("build/") } }
    }
}
EOF

write_file "$ROOT/gradle.properties" <<'EOF'
org.gradle.jvmargs=-Xmx4g -Dfile.encoding=UTF-8
org.gradle.parallel=true
org.gradle.caching=true
org.gradle.configuration-cache=true
kotlin.code.style=official
kotlin.mpp.androidGradlePluginCompatibility.nowarn=true
kotlin.mpp.enableCInteropCommonization=true
android.useAndroidX=true
android.nonTransitiveRClass=true
EOF

write_file "$ROOT/gradle/libs.versions.toml" <<'EOF'
[versions]
agp = "8.7.0"
kotlin = "2.2.20"
compose-multiplatform = "1.9.0"
compose-unstyled = "1.20.1"
coroutines = "1.9.0"
serialization = "1.7.3"
datetime = "0.6.1"
koin = "4.0.0"
sqldelight = "2.0.2"
settings = "1.2.0"
ktor = "3.0.0"
viewmodel = "2.8.3"
navigation = "2.8.0-alpha10"
gitlive-firebase = "2.1.0"
supabase = "3.0.2"
revenuecat = "1.8.5+13.38.1"
ktlint = "12.1.1"
androidx-activity = "1.9.3"
android-minSdk = "26"
android-compileSdk = "35"
android-targetSdk = "35"

[libraries]
kotlinx-coroutines-core = { module = "org.jetbrains.kotlinx:kotlinx-coroutines-core", version.ref = "coroutines" }
kotlinx-serialization-json = { module = "org.jetbrains.kotlinx:kotlinx-serialization-json", version.ref = "serialization" }
kotlinx-datetime = { module = "org.jetbrains.kotlinx:kotlinx-datetime", version.ref = "datetime" }

koin-core = { module = "io.insert-koin:koin-core", version.ref = "koin" }
koin-compose = { module = "io.insert-koin:koin-compose", version.ref = "koin" }
koin-compose-viewmodel = { module = "io.insert-koin:koin-compose-viewmodel", version.ref = "koin" }

compose-unstyled = { module = "com.composables:core", version.ref = "compose-unstyled" }
androidx-lifecycle-viewmodel = { module = "org.jetbrains.androidx.lifecycle:lifecycle-viewmodel-compose", version.ref = "viewmodel" }
androidx-navigation = { module = "org.jetbrains.androidx.navigation:navigation-compose", version.ref = "navigation" }
androidx-activity-compose = { module = "androidx.activity:activity-compose", version.ref = "androidx-activity" }

sqldelight-runtime = { module = "app.cash.sqldelight:runtime", version.ref = "sqldelight" }
sqldelight-coroutines = { module = "app.cash.sqldelight:coroutines-extensions", version.ref = "sqldelight" }
sqldelight-android-driver = { module = "app.cash.sqldelight:android-driver", version.ref = "sqldelight" }
sqldelight-native-driver = { module = "app.cash.sqldelight:native-driver", version.ref = "sqldelight" }

settings-core = { module = "com.russhwolf:multiplatform-settings", version.ref = "settings" }
settings-coroutines = { module = "com.russhwolf:multiplatform-settings-coroutines", version.ref = "settings" }

ktor-client-core = { module = "io.ktor:ktor-client-core", version.ref = "ktor" }
ktor-client-content-negotiation = { module = "io.ktor:ktor-client-content-negotiation", version.ref = "ktor" }
ktor-client-serialization = { module = "io.ktor:ktor-serialization-kotlinx-json", version.ref = "ktor" }
ktor-client-android = { module = "io.ktor:ktor-client-okhttp", version.ref = "ktor" }
ktor-client-darwin = { module = "io.ktor:ktor-client-darwin", version.ref = "ktor" }

firebase-auth = { module = "dev.gitlive:firebase-auth", version.ref = "gitlive-firebase" }
firebase-firestore = { module = "dev.gitlive:firebase-firestore", version.ref = "gitlive-firebase" }
firebase-analytics = { module = "dev.gitlive:firebase-analytics", version.ref = "gitlive-firebase" }
firebase-crashlytics = { module = "dev.gitlive:firebase-crashlytics", version.ref = "gitlive-firebase" }

supabase-postgrest = { module = "io.github.jan-tennert.supabase:postgrest-kt", version.ref = "supabase" }
supabase-auth = { module = "io.github.jan-tennert.supabase:auth-kt", version.ref = "supabase" }
supabase-storage = { module = "io.github.jan-tennert.supabase:storage-kt", version.ref = "supabase" }

revenuecat-core = { module = "com.revenuecat.purchases:purchases-kmp-core", version.ref = "revenuecat" }
revenuecat-result = { module = "com.revenuecat.purchases:purchases-kmp-result", version.ref = "revenuecat" }
revenuecat-datetime = { module = "com.revenuecat.purchases:purchases-kmp-datetime", version.ref = "revenuecat" }

[plugins]
kotlin-multiplatform   = { id = "org.jetbrains.kotlin.multiplatform",       version.ref = "kotlin" }
kotlin-android         = { id = "org.jetbrains.kotlin.android",             version.ref = "kotlin" }
kotlin-compose         = { id = "org.jetbrains.kotlin.plugin.compose",      version.ref = "kotlin" }
kotlin-serialization   = { id = "org.jetbrains.kotlin.plugin.serialization", version.ref = "kotlin" }
compose-multiplatform  = { id = "org.jetbrains.compose",                    version.ref = "compose-multiplatform" }
android-library        = { id = "com.android.library",                      version.ref = "agp" }
android-application    = { id = "com.android.application",                  version.ref = "agp" }
sqldelight             = { id = "app.cash.sqldelight",                      version.ref = "sqldelight" }
ktlint                 = { id = "org.jlleitschuh.gradle.ktlint",            version.ref = "ktlint" }
EOF

write_file "$ROOT/buildSrc/build.gradle.kts" <<'EOF'
plugins { `kotlin-dsl` }
repositories { mavenCentral(); google(); gradlePluginPortal() }
EOF

write_file "$ROOT/buildSrc/src/main/kotlin/ProjectConfiguration.kt" <<'EOF'
object ProjectConfiguration {
    const val GROUP_ID = "dev.jdgarita.frnk"
    const val IOS_FRAMEWORK_NAME = "FrnkKit"
    const val MIN_SDK = 26
    const val COMPILE_SDK = 35
    const val TARGET_SDK = 35
}
EOF

# ---------------------------------------------------------------------------
# Helper: emit a standard KMP library build.gradle.kts.
# Args: <module-path> <namespace-suffix> <extra-plugins> <extra-deps-block>
# We inline per-module for clarity below (some need plugin/dep tweaks).
# ---------------------------------------------------------------------------

# ===========================================================================
# 3. core-utils  — leaf module, no other toolkit deps
# ===========================================================================
say
say "[3/6] writing module: core-utils"

write_file "$ROOT/core-utils/build.gradle.kts" <<'EOF'
plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
}

kotlin {
    jvmToolchain(17)
    androidTarget()
    listOf(iosX64(), iosArm64(), iosSimulatorArm64()).forEach { it.binaries.framework { baseName = "core_utils" } }
    sourceSets {
        commonMain.dependencies {
            api(libs.kotlinx.coroutines.core)
            api(libs.kotlinx.datetime)
        }
    }
}

android {
    namespace = "${ProjectConfiguration.GROUP_ID}.utils"
    compileSdk = ProjectConfiguration.COMPILE_SDK
    defaultConfig { minSdk = ProjectConfiguration.MIN_SDK }
}
EOF

write_file "$ROOT/core-utils/src/commonMain/kotlin/dev/jdgarita/frnk/utils/Logger.kt" <<'EOF'
package dev.jdgarita.frnk.utils

/** Minimal multiplatform logger; host can replace via Koin if richer output is needed. */
interface Logger {
    fun d(tag: String, message: String, throwable: Throwable? = null)
    fun i(tag: String, message: String, throwable: Throwable? = null)
    fun w(tag: String, message: String, throwable: Throwable? = null)
    fun e(tag: String, message: String, throwable: Throwable? = null)
}

object PrintLogger : Logger {
    override fun d(tag: String, message: String, throwable: Throwable?) = log("D", tag, message, throwable)
    override fun i(tag: String, message: String, throwable: Throwable?) = log("I", tag, message, throwable)
    override fun w(tag: String, message: String, throwable: Throwable?) = log("W", tag, message, throwable)
    override fun e(tag: String, message: String, throwable: Throwable?) = log("E", tag, message, throwable)
    private fun log(level: String, tag: String, msg: String, t: Throwable?) {
        println("[$level] $tag: $msg${t?.let { " :: ${it.message}" } ?: ""}")
    }
}
EOF

write_file "$ROOT/core-utils/src/commonMain/kotlin/dev/jdgarita/frnk/utils/DateTimeFormat.kt" <<'EOF'
package dev.jdgarita.frnk.utils

import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/** Generic, locale-agnostic ISO-style formatter. Host apps can wrap if they need richer formatting. */
object DateTimeFormat {
    fun isoDate(instant: Instant, tz: TimeZone = TimeZone.currentSystemDefault()): String =
        instant.toLocalDateTime(tz).date.toString()
}
EOF

# ===========================================================================
# 4. core-ui-api  — MVI engine, routing, UiText  (NO Compose)
# ===========================================================================
say
say "[3/6] writing module: core-ui-api"

write_file "$ROOT/core-ui-api/build.gradle.kts" <<'EOF'
plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
}

kotlin {
    jvmToolchain(17)
    androidTarget()
    listOf(iosX64(), iosArm64(), iosSimulatorArm64()).forEach { it.binaries.framework { baseName = "core_ui_api" } }
    sourceSets {
        commonMain.dependencies {
            api(projects.coreUtils)
            api(libs.kotlinx.coroutines.core)
            api(libs.androidx.lifecycle.viewmodel)
        }
    }
}

android {
    namespace = "${ProjectConfiguration.GROUP_ID}.ui.api"
    compileSdk = ProjectConfiguration.COMPILE_SDK
    defaultConfig { minSdk = ProjectConfiguration.MIN_SDK }
}
EOF

write_file "$ROOT/core-ui-api/src/commonMain/kotlin/dev/jdgarita/frnk/ui/UiText.kt" <<'EOF'
package dev.jdgarita.frnk.ui

/**
 * Platform-agnostic text holder. The host resolves it against its string catalog so the
 * toolkit can surface errors without coupling to platform resource systems.
 */
sealed interface UiText {
    data class Raw(val value: String) : UiText
    data class Resource(val key: String, val args: List<Any?> = emptyList()) : UiText
}
EOF

write_file "$ROOT/core-ui-api/src/commonMain/kotlin/dev/jdgarita/frnk/ui/mvi/MviContract.kt" <<'EOF'
package dev.jdgarita.frnk.ui.mvi

/** Marker interfaces — keep contracts thin; concrete types live in feature modules. */
interface UiState
interface UiIntent
interface UiEffect
EOF

write_file "$ROOT/core-ui-api/src/commonMain/kotlin/dev/jdgarita/frnk/ui/mvi/MviViewModel.kt" <<'EOF'
package dev.jdgarita.frnk.ui.mvi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

/**
 * Redux-lite MVI base. Features:
 *  - StateFlow<S> for the rendered state
 *  - SharedFlow<I> for fire-and-forget intents (replay = 0)
 *  - Channel<E> for one-shot effects (no replay, conflated)
 *
 * Override [onIntent] to perform side-effectful work and emit new state via [setState].
 */
abstract class MviViewModel<S : UiState, I : UiIntent, E : UiEffect>(initial: S) : ViewModel() {

    private val _state = MutableStateFlow(initial)
    val state: StateFlow<S> = _state.asStateFlow()

    private val _intents = MutableSharedFlow<I>(extraBufferCapacity = 16, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    val intents = _intents.asSharedFlow()

    private val _effects = Channel<E>(capacity = Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    init {
        viewModelScope.launch {
            _intents.onEach { onIntent(it) }.collect()
        }
    }

    fun send(intent: I) { _intents.tryEmit(intent) }

    protected fun setState(reducer: S.() -> S) { _state.value = _state.value.reducer() }
    protected fun currentState(): S = _state.value
    protected suspend fun emit(effect: E) { _effects.send(effect) }

    protected abstract suspend fun onIntent(intent: I)
}

private suspend fun <T> kotlinx.coroutines.flow.Flow<T>.collect() = collect {}
EOF

write_file "$ROOT/core-ui-api/src/commonMain/kotlin/dev/jdgarita/frnk/ui/nav/ToolkitRoute.kt" <<'EOF'
package dev.jdgarita.frnk.ui.nav

/**
 * Generic route catalogue surfaced by the toolkit. Host apps MAP these to concrete
 * Compose destinations in their own NavHost. The toolkit never owns the NavHost.
 */
sealed interface ToolkitRoute {
    data object Home : ToolkitRoute
    data object Settings : ToolkitRoute
    data object Paywall : ToolkitRoute
    data object SignIn : ToolkitRoute
    data object SignUp : ToolkitRoute
    data class Custom(val id: String) : ToolkitRoute
}

/** A function the host wires up to launch a toolkit-defined navigation. */
typealias Navigator = (ToolkitRoute) -> Unit
EOF

# ===========================================================================
# 5. core-ui-atoms  — design system on compose-unstyled + theme tokens
# ===========================================================================
say "[3/6] writing module: core-ui-atoms"

write_file "$ROOT/core-ui-atoms/build.gradle.kts" <<'EOF'
plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.kotlin.compose)
}

kotlin {
    jvmToolchain(17)
    androidTarget()
    listOf(iosX64(), iosArm64(), iosSimulatorArm64()).forEach { it.binaries.framework { baseName = "core_ui_atoms" } }
    sourceSets {
        commonMain.dependencies {
            api(projects.coreUiApi)
            api(compose.runtime)
            api(compose.foundation)
            api(compose.ui)
            api(libs.compose.unstyled)
            api(libs.koin.compose)
            api(libs.koin.compose.viewmodel)
        }
    }
}

android {
    namespace = "${ProjectConfiguration.GROUP_ID}.ui.atoms"
    compileSdk = ProjectConfiguration.COMPILE_SDK
    defaultConfig { minSdk = ProjectConfiguration.MIN_SDK }
}
EOF

write_file "$ROOT/core-ui-atoms/src/commonMain/kotlin/dev/jdgarita/frnk/ui/atoms/ToolkitTheme.kt" <<'EOF'
package dev.jdgarita.frnk.ui.atoms

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Theme tokens the toolkit consumes. Host apps override any subset; atoms below
 * (ToolkitButton, ToolkitTextField, …) read from these locals, so host overrides
 * propagate without re-wrapping individual components.
 */
@Immutable
data class ToolkitColors(
    val primary: Color = Color(0xFF6750A4),
    val onPrimary: Color = Color.White,
    val surface: Color = Color(0xFFFFFBFE),
    val onSurface: Color = Color(0xFF1C1B1F),
    val outline: Color = Color(0xFF79747E),
    val error: Color = Color(0xFFB3261E),
)

@Immutable
data class ToolkitTypography(
    val body: TextStyle = TextStyle(fontSize = 14.sp),
    val title: TextStyle = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.SemiBold),
    val button: TextStyle = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Medium),
)

@Immutable
data class ToolkitStrings(
    val signIn: String = "Sign in",
    val signOut: String = "Sign out",
    val upgrade: String = "Upgrade to Pro",
    val cancel: String = "Cancel",
    val retry: String = "Retry",
    val genericError: String = "Something went wrong",
)

val LocalToolkitColors      = staticCompositionLocalOf { ToolkitColors() }
val LocalToolkitTypography  = staticCompositionLocalOf { ToolkitTypography() }
val LocalToolkitStrings     = staticCompositionLocalOf { ToolkitStrings() }

object ToolkitTheme {
    val colors:     ToolkitColors     @Composable @ReadOnlyComposable get() = LocalToolkitColors.current
    val typography: ToolkitTypography @Composable @ReadOnlyComposable get() = LocalToolkitTypography.current
    val strings:    ToolkitStrings    @Composable @ReadOnlyComposable get() = LocalToolkitStrings.current
}

@Composable
fun ProvideToolkitTheme(
    colors: ToolkitColors = ToolkitColors(),
    typography: ToolkitTypography = ToolkitTypography(),
    strings: ToolkitStrings = ToolkitStrings(),
    content: @Composable () -> Unit,
) = CompositionLocalProvider(
    LocalToolkitColors provides colors,
    LocalToolkitTypography provides typography,
    LocalToolkitStrings provides strings,
    content = content,
)
EOF

write_file "$ROOT/core-ui-atoms/src/commonMain/kotlin/dev/jdgarita/frnk/ui/atoms/ToolkitButton.kt" <<'EOF'
package dev.jdgarita.frnk.ui.atoms

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

/** Atomic button. Pure layout + tokens — no Material chrome. */
@Composable
fun ToolkitButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val colors = ToolkitTheme.colors
    val typography = ToolkitTheme.typography
    BasicText(
        text = label,
        style = typography.button.copy(color = colors.onPrimary),
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(colors.primary)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 12.dp),
    )
}
EOF

write_file "$ROOT/core-ui-atoms/src/commonMain/kotlin/dev/jdgarita/frnk/ui/atoms/ToolkitTextField.kt" <<'EOF'
package dev.jdgarita.frnk.ui.atoms

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp

@Composable
fun ToolkitTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
) {
    val colors = ToolkitTheme.colors
    val style: TextStyle = ToolkitTheme.typography.body.copy(color = colors.onSurface)
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        textStyle = style,
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .border(1.dp, colors.outline, RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
    )
}
EOF

# ===========================================================================
# 6. core-database-api / -impl
# ===========================================================================
say "[3/6] writing module: core-database-api / -impl"

write_file "$ROOT/core-database-api/build.gradle.kts" <<'EOF'
plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
}

kotlin {
    jvmToolchain(17)
    androidTarget()
    listOf(iosX64(), iosArm64(), iosSimulatorArm64()).forEach { it.binaries.framework { baseName = "core_database_api" } }
    sourceSets {
        commonMain.dependencies {
            api(projects.coreUtils)
            api(libs.sqldelight.runtime)
            api(libs.settings.core)
        }
    }
}

android {
    namespace = "${ProjectConfiguration.GROUP_ID}.database.api"
    compileSdk = ProjectConfiguration.COMPILE_SDK
    defaultConfig { minSdk = ProjectConfiguration.MIN_SDK }
}
EOF

write_file "$ROOT/core-database-api/src/commonMain/kotlin/dev/jdgarita/frnk/database/SqlDriverFactory.kt" <<'EOF'
package dev.jdgarita.frnk.database

import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.db.SqlSchema

/**
 * Contract for producing a SqlDriver bound to the host app's schema.
 * The toolkit does NOT own the schema — the host injects it.
 */
fun interface SqlDriverFactory {
    fun create(schema: SqlSchema<QueryResult.Value<Unit>>, name: String): SqlDriver
}
EOF

write_file "$ROOT/core-database-api/src/commonMain/kotlin/dev/jdgarita/frnk/database/KeyValueStore.kt" <<'EOF'
package dev.jdgarita.frnk.database

/** Thin port over multiplatform-settings so callers don't depend on the impl module. */
interface KeyValueStore {
    fun putString(key: String, value: String)
    fun getString(key: String, default: String? = null): String?
    fun putBoolean(key: String, value: Boolean)
    fun getBoolean(key: String, default: Boolean = false): Boolean
    fun remove(key: String)
}
EOF

write_file "$ROOT/core-database-impl/build.gradle.kts" <<'EOF'
plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
}

kotlin {
    jvmToolchain(17)
    androidTarget()
    listOf(iosX64(), iosArm64(), iosSimulatorArm64()).forEach { it.binaries.framework { baseName = "core_database_impl" } }
    sourceSets {
        commonMain.dependencies {
            api(projects.coreDatabaseApi)
            implementation(libs.koin.core)
            implementation(libs.settings.core)
            implementation(libs.settings.coroutines)
        }
        androidMain.dependencies { implementation(libs.sqldelight.android.driver) }
        iosMain.dependencies { implementation(libs.sqldelight.native.driver) }
    }
}

android {
    namespace = "${ProjectConfiguration.GROUP_ID}.database.impl"
    compileSdk = ProjectConfiguration.COMPILE_SDK
    defaultConfig { minSdk = ProjectConfiguration.MIN_SDK }
}
EOF

write_file "$ROOT/core-database-impl/src/commonMain/kotlin/dev/jdgarita/frnk/database/impl/DatabaseModule.kt" <<'EOF'
package dev.jdgarita.frnk.database.impl

import dev.jdgarita.frnk.database.KeyValueStore
import dev.jdgarita.frnk.database.SqlDriverFactory
import org.koin.dsl.module

/** Provides default impls. Host apps still inject their schema directly into their own DI graph. */
val databaseModule = module {
    single<SqlDriverFactory> { defaultSqlDriverFactory() }
    single<KeyValueStore> { defaultKeyValueStore() }
}
EOF

write_file "$ROOT/core-database-impl/src/commonMain/kotlin/dev/jdgarita/frnk/database/impl/Defaults.kt" <<'EOF'
package dev.jdgarita.frnk.database.impl

import dev.jdgarita.frnk.database.KeyValueStore
import dev.jdgarita.frnk.database.SqlDriverFactory

/** expect — bound per platform. */
expect fun defaultSqlDriverFactory(): SqlDriverFactory
expect fun defaultKeyValueStore(): KeyValueStore
EOF

write_file "$ROOT/core-database-impl/src/androidMain/kotlin/dev/jdgarita/frnk/database/impl/Defaults.android.kt" <<'EOF'
package dev.jdgarita.frnk.database.impl

import android.content.Context
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.russhwolf.settings.SharedPreferencesSettings
import dev.jdgarita.frnk.database.KeyValueStore
import dev.jdgarita.frnk.database.SqlDriverFactory

/** Host MUST set this from its Application.onCreate before Koin starts. */
object DatabaseContext { lateinit var application: Context }

actual fun defaultSqlDriverFactory(): SqlDriverFactory = SqlDriverFactory { schema, name ->
    AndroidSqliteDriver(schema, DatabaseContext.application, name)
}

actual fun defaultKeyValueStore(): KeyValueStore {
    val prefs = DatabaseContext.application.getSharedPreferences("frnk_toolkit", Context.MODE_PRIVATE)
    val settings = SharedPreferencesSettings(prefs)
    return SettingsKeyValueStore(settings)
}
EOF

write_file "$ROOT/core-database-impl/src/iosMain/kotlin/dev/jdgarita/frnk/database/impl/Defaults.ios.kt" <<'EOF'
package dev.jdgarita.frnk.database.impl

import app.cash.sqldelight.driver.native.NativeSqliteDriver
import com.russhwolf.settings.NSUserDefaultsSettings
import dev.jdgarita.frnk.database.KeyValueStore
import dev.jdgarita.frnk.database.SqlDriverFactory
import platform.Foundation.NSUserDefaults

actual fun defaultSqlDriverFactory(): SqlDriverFactory = SqlDriverFactory { schema, name ->
    NativeSqliteDriver(schema, name)
}

actual fun defaultKeyValueStore(): KeyValueStore =
    SettingsKeyValueStore(NSUserDefaultsSettings(NSUserDefaults.standardUserDefaults))
EOF

write_file "$ROOT/core-database-impl/src/commonMain/kotlin/dev/jdgarita/frnk/database/impl/SettingsKeyValueStore.kt" <<'EOF'
package dev.jdgarita.frnk.database.impl

import com.russhwolf.settings.Settings
import dev.jdgarita.frnk.database.KeyValueStore

internal class SettingsKeyValueStore(private val s: Settings) : KeyValueStore {
    override fun putString(key: String, value: String) { s.putString(key, value) }
    override fun getString(key: String, default: String?): String? = s.getStringOrNull(key) ?: default
    override fun putBoolean(key: String, value: Boolean) { s.putBoolean(key, value) }
    override fun getBoolean(key: String, default: Boolean): Boolean = s.getBoolean(key, default)
    override fun remove(key: String) { s.remove(key) }
}
EOF

# ===========================================================================
# 7. core-backend-api  + firebase + supabase
# ===========================================================================
say "[3/6] writing module: core-backend-api / firebase / supabase"

write_file "$ROOT/core-backend-api/build.gradle.kts" <<'EOF'
plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
}

kotlin {
    jvmToolchain(17)
    androidTarget()
    listOf(iosX64(), iosArm64(), iosSimulatorArm64()).forEach { it.binaries.framework { baseName = "core_backend_api" } }
    sourceSets {
        commonMain.dependencies {
            api(projects.coreUtils)
            api(libs.kotlinx.coroutines.core)
        }
    }
}

android {
    namespace = "${ProjectConfiguration.GROUP_ID}.backend.api"
    compileSdk = ProjectConfiguration.COMPILE_SDK
    defaultConfig { minSdk = ProjectConfiguration.MIN_SDK }
}
EOF

write_file "$ROOT/core-backend-api/src/commonMain/kotlin/dev/jdgarita/frnk/backend/AppResult.kt" <<'EOF'
package dev.jdgarita.frnk.backend

/** Toolkit-wide result envelope. Every backend interface returns this; callers exhaustive-when on it. */
sealed interface AppResult<out D, out E : AppError> {
    data class Success<D>(val data: D) : AppResult<D, Nothing>
    data class Failure<E : AppError>(val error: E) : AppResult<Nothing, E>
}

interface AppError {
    val message: String
}

enum class CommonError(override val message: String) : AppError {
    Network("Network unavailable"),
    Unauthorized("Authentication required"),
    NotFound("Resource not found"),
    Unknown("Unknown error"),
}
EOF

write_file "$ROOT/core-backend-api/src/commonMain/kotlin/dev/jdgarita/frnk/backend/Auth.kt" <<'EOF'
package dev.jdgarita.frnk.backend

import kotlinx.coroutines.flow.Flow

data class AuthUser(val id: String, val email: String?, val isAnonymous: Boolean)

interface AuthService {
    val currentUser: Flow<AuthUser?>
    suspend fun signIn(email: String, password: String): AppResult<AuthUser, CommonError>
    suspend fun signUp(email: String, password: String): AppResult<AuthUser, CommonError>
    suspend fun signOut(): AppResult<Unit, CommonError>
}
EOF

write_file "$ROOT/core-backend-api/src/commonMain/kotlin/dev/jdgarita/frnk/backend/Analytics.kt" <<'EOF'
package dev.jdgarita.frnk.backend

/**
 * Analytics surface used by toolkit + host. The toolkit emits a generic event vocabulary
 * (App_Opened, Paywall_Viewed, …) via [AnalyticsTracker.track]; host apps can route any
 * extra events through the same instance via [trackCustom].
 */
interface AnalyticsTracker {
    fun track(event: ToolkitEvent, params: Map<String, Any?> = emptyMap())
    fun trackCustom(name: String, params: Map<String, Any?> = emptyMap())
    fun setUserProperty(key: String, value: String?)
}

enum class ToolkitEvent(val key: String) {
    AppOpened("App_Opened"),
    PaywallViewed("Paywall_Viewed"),
    PaywallDismissed("Paywall_Dismissed"),
    PurchaseStarted("Purchase_Started"),
    PurchaseCompleted("Purchase_Completed"),
    PurchaseFailed("Purchase_Failed"),
    SignInStarted("SignIn_Started"),
    SignInCompleted("SignIn_Completed"),
}

interface CrashReporter {
    fun recordException(throwable: Throwable, extras: Map<String, String> = emptyMap())
    fun setUserId(id: String?)
    fun log(message: String)
}
EOF

write_file "$ROOT/core-backend-api/src/commonMain/kotlin/dev/jdgarita/frnk/backend/RemoteData.kt" <<'EOF'
package dev.jdgarita.frnk.backend

/** Minimal remote read/write surface. Implementations decide what 'collection' means. */
interface RemoteData {
    suspend fun <T : Any> get(collection: String, id: String, decode: (Map<String, Any?>) -> T): AppResult<T, CommonError>
    suspend fun <T : Any> set(collection: String, id: String, value: T, encode: (T) -> Map<String, Any?>): AppResult<Unit, CommonError>
}
EOF

# firebase impl
write_file "$ROOT/core-backend-firebase/build.gradle.kts" <<'EOF'
plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    jvmToolchain(17)
    androidTarget()
    listOf(iosX64(), iosArm64(), iosSimulatorArm64()).forEach { it.binaries.framework { baseName = "core_backend_firebase" } }
    sourceSets {
        commonMain.dependencies {
            api(projects.coreBackendApi)
            implementation(libs.koin.core)
            implementation(libs.firebase.auth)
            implementation(libs.firebase.firestore)
            implementation(libs.firebase.analytics)
            implementation(libs.firebase.crashlytics)
        }
    }
}

android {
    namespace = "${ProjectConfiguration.GROUP_ID}.backend.firebase"
    compileSdk = ProjectConfiguration.COMPILE_SDK
    defaultConfig { minSdk = ProjectConfiguration.MIN_SDK }
}
EOF

write_file "$ROOT/core-backend-firebase/src/commonMain/kotlin/dev/jdgarita/frnk/backend/firebase/FirebaseBackendModule.kt" <<'EOF'
package dev.jdgarita.frnk.backend.firebase

import dev.jdgarita.frnk.backend.AnalyticsTracker
import dev.jdgarita.frnk.backend.AuthService
import dev.jdgarita.frnk.backend.CrashReporter
import dev.jdgarita.frnk.backend.RemoteData
import org.koin.dsl.module

/** Host picks ONE of firebase/supabase modules to satisfy the backend-api contracts. */
val firebaseBackendModule = module {
    single<AuthService> { FirebaseAuthService() }
    single<AnalyticsTracker> { FirebaseAnalyticsTracker() }
    single<CrashReporter> { FirebaseCrashReporter() }
    single<RemoteData> { FirestoreRemoteData() }
}
EOF

# Stubs that actually implement the backend-api interfaces (with TODO() bodies).
write_file "$ROOT/core-backend-firebase/src/commonMain/kotlin/dev/jdgarita/frnk/backend/firebase/FirebaseAuthService.kt" <<'EOF'
package dev.jdgarita.frnk.backend.firebase

import dev.jdgarita.frnk.backend.AppResult
import dev.jdgarita.frnk.backend.AuthService
import dev.jdgarita.frnk.backend.AuthUser
import dev.jdgarita.frnk.backend.CommonError
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

internal class FirebaseAuthService : AuthService {
    override val currentUser: Flow<AuthUser?> = flowOf(null)
    override suspend fun signIn(email: String, password: String): AppResult<AuthUser, CommonError> = TODO("wire dev.gitlive:firebase-auth")
    override suspend fun signUp(email: String, password: String): AppResult<AuthUser, CommonError> = TODO("wire dev.gitlive:firebase-auth")
    override suspend fun signOut(): AppResult<Unit, CommonError> = TODO("wire dev.gitlive:firebase-auth")
}
EOF

write_file "$ROOT/core-backend-firebase/src/commonMain/kotlin/dev/jdgarita/frnk/backend/firebase/FirebaseAnalyticsTracker.kt" <<'EOF'
package dev.jdgarita.frnk.backend.firebase

import dev.jdgarita.frnk.backend.AnalyticsTracker
import dev.jdgarita.frnk.backend.ToolkitEvent

internal class FirebaseAnalyticsTracker : AnalyticsTracker {
    override fun track(event: ToolkitEvent, params: Map<String, Any?>) { /* TODO: Firebase.analytics.logEvent(event.key, params) */ }
    override fun trackCustom(name: String, params: Map<String, Any?>) { /* TODO: Firebase.analytics.logEvent(name, params) */ }
    override fun setUserProperty(key: String, value: String?) { /* TODO: Firebase.analytics.setUserProperty(key, value) */ }
}
EOF

write_file "$ROOT/core-backend-firebase/src/commonMain/kotlin/dev/jdgarita/frnk/backend/firebase/FirebaseCrashReporter.kt" <<'EOF'
package dev.jdgarita.frnk.backend.firebase

import dev.jdgarita.frnk.backend.CrashReporter

internal class FirebaseCrashReporter : CrashReporter {
    override fun recordException(throwable: Throwable, extras: Map<String, String>) { /* TODO: Firebase.crashlytics.recordException */ }
    override fun setUserId(id: String?) { /* TODO: Firebase.crashlytics.setUserId */ }
    override fun log(message: String) { /* TODO: Firebase.crashlytics.log */ }
}
EOF

write_file "$ROOT/core-backend-firebase/src/commonMain/kotlin/dev/jdgarita/frnk/backend/firebase/FirestoreRemoteData.kt" <<'EOF'
package dev.jdgarita.frnk.backend.firebase

import dev.jdgarita.frnk.backend.AppResult
import dev.jdgarita.frnk.backend.CommonError
import dev.jdgarita.frnk.backend.RemoteData

internal class FirestoreRemoteData : RemoteData {
    override suspend fun <T : Any> get(collection: String, id: String, decode: (Map<String, Any?>) -> T): AppResult<T, CommonError> =
        TODO("wire dev.gitlive:firebase-firestore")
    override suspend fun <T : Any> set(collection: String, id: String, value: T, encode: (T) -> Map<String, Any?>): AppResult<Unit, CommonError> =
        TODO("wire dev.gitlive:firebase-firestore")
}
EOF

# supabase impl
write_file "$ROOT/core-backend-supabase/build.gradle.kts" <<'EOF'
plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    jvmToolchain(17)
    androidTarget()
    listOf(iosX64(), iosArm64(), iosSimulatorArm64()).forEach { it.binaries.framework { baseName = "core_backend_supabase" } }
    sourceSets {
        commonMain.dependencies {
            api(projects.coreBackendApi)
            implementation(libs.koin.core)
            implementation(libs.supabase.postgrest)
            implementation(libs.supabase.auth)
            implementation(libs.supabase.storage)
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.client.serialization)
        }
        androidMain.dependencies { implementation(libs.ktor.client.android) }
        iosMain.dependencies { implementation(libs.ktor.client.darwin) }
    }
}

android {
    namespace = "${ProjectConfiguration.GROUP_ID}.backend.supabase"
    compileSdk = ProjectConfiguration.COMPILE_SDK
    defaultConfig { minSdk = ProjectConfiguration.MIN_SDK }
}
EOF

write_file "$ROOT/core-backend-supabase/src/commonMain/kotlin/dev/jdgarita/frnk/backend/supabase/SupabaseBackendModule.kt" <<'EOF'
package dev.jdgarita.frnk.backend.supabase

import dev.jdgarita.frnk.backend.AnalyticsTracker
import dev.jdgarita.frnk.backend.AuthService
import dev.jdgarita.frnk.backend.CrashReporter
import dev.jdgarita.frnk.backend.RemoteData
import org.koin.dsl.module

val supabaseBackendModule = module {
    single<AuthService> { SupabaseAuthService() }
    single<AnalyticsTracker> { NoopAnalyticsTracker() }
    single<CrashReporter> { NoopCrashReporter() }
    single<RemoteData> { SupabaseRemoteData() }
}
EOF

write_file "$ROOT/core-backend-supabase/src/commonMain/kotlin/dev/jdgarita/frnk/backend/supabase/SupabaseAuthService.kt" <<'EOF'
package dev.jdgarita.frnk.backend.supabase

import dev.jdgarita.frnk.backend.AppResult
import dev.jdgarita.frnk.backend.AuthService
import dev.jdgarita.frnk.backend.AuthUser
import dev.jdgarita.frnk.backend.CommonError
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

internal class SupabaseAuthService : AuthService {
    override val currentUser: Flow<AuthUser?> = flowOf(null)
    override suspend fun signIn(email: String, password: String): AppResult<AuthUser, CommonError> = TODO("wire supabase-auth-kt")
    override suspend fun signUp(email: String, password: String): AppResult<AuthUser, CommonError> = TODO("wire supabase-auth-kt")
    override suspend fun signOut(): AppResult<Unit, CommonError> = TODO("wire supabase-auth-kt")
}
EOF

write_file "$ROOT/core-backend-supabase/src/commonMain/kotlin/dev/jdgarita/frnk/backend/supabase/SupabaseRemoteData.kt" <<'EOF'
package dev.jdgarita.frnk.backend.supabase

import dev.jdgarita.frnk.backend.AppResult
import dev.jdgarita.frnk.backend.CommonError
import dev.jdgarita.frnk.backend.RemoteData

internal class SupabaseRemoteData : RemoteData {
    override suspend fun <T : Any> get(collection: String, id: String, decode: (Map<String, Any?>) -> T): AppResult<T, CommonError> =
        TODO("wire supabase-postgrest-kt")
    override suspend fun <T : Any> set(collection: String, id: String, value: T, encode: (T) -> Map<String, Any?>): AppResult<Unit, CommonError> =
        TODO("wire supabase-postgrest-kt")
}
EOF

write_file "$ROOT/core-backend-supabase/src/commonMain/kotlin/dev/jdgarita/frnk/backend/supabase/NoopAnalyticsTracker.kt" <<'EOF'
package dev.jdgarita.frnk.backend.supabase

import dev.jdgarita.frnk.backend.AnalyticsTracker
import dev.jdgarita.frnk.backend.ToolkitEvent

/** Supabase has no first-party analytics product; host apps that want events should bind their own [AnalyticsTracker]. */
internal class NoopAnalyticsTracker : AnalyticsTracker {
    override fun track(event: ToolkitEvent, params: Map<String, Any?>) = Unit
    override fun trackCustom(name: String, params: Map<String, Any?>) = Unit
    override fun setUserProperty(key: String, value: String?) = Unit
}
EOF

write_file "$ROOT/core-backend-supabase/src/commonMain/kotlin/dev/jdgarita/frnk/backend/supabase/NoopCrashReporter.kt" <<'EOF'
package dev.jdgarita.frnk.backend.supabase

import dev.jdgarita.frnk.backend.CrashReporter

internal class NoopCrashReporter : CrashReporter {
    override fun recordException(throwable: Throwable, extras: Map<String, String>) = Unit
    override fun setUserId(id: String?) = Unit
    override fun log(message: String) = Unit
}
EOF

# ===========================================================================
# 8. core-monetization-api + revenuecat
# ===========================================================================
say "[3/6] writing module: core-monetization-api / revenuecat"

write_file "$ROOT/core-monetization-api/build.gradle.kts" <<'EOF'
plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
}

kotlin {
    jvmToolchain(17)
    androidTarget()
    listOf(iosX64(), iosArm64(), iosSimulatorArm64()).forEach { it.binaries.framework { baseName = "core_monetization_api" } }
    sourceSets {
        commonMain.dependencies {
            api(projects.coreBackendApi)
            api(libs.kotlinx.coroutines.core)
            // koin-core is exposed because this module surfaces ToolkitDiModule (expect fun
            // toolkitCoreModules(): List<Module>). Host apps consume the aggregator directly.
            api(libs.koin.core)
        }
    }
}

android {
    namespace = "${ProjectConfiguration.GROUP_ID}.monetization.api"
    compileSdk = ProjectConfiguration.COMPILE_SDK
    defaultConfig { minSdk = ProjectConfiguration.MIN_SDK }
}
EOF

write_file "$ROOT/core-monetization-api/src/commonMain/kotlin/dev/jdgarita/frnk/monetization/EntitlementManager.kt" <<'EOF'
package dev.jdgarita.frnk.monetization

import kotlinx.coroutines.flow.StateFlow

/** Single source of truth for Free vs Pro across the toolkit. */
interface EntitlementManager {
    val isPro: StateFlow<Boolean>
    suspend fun refresh()
    suspend fun restorePurchases(): Boolean
}

/** Stable, opaque feature identifiers. Host apps add their own as enum-like constants. */
data class Feature(val id: String) {
    companion object {
        val Premium = Feature("premium")
        val UnlimitedExports = Feature("unlimited_exports")
        val AdFree = Feature("ad_free")
    }
}
EOF

write_file "$ROOT/core-monetization-api/src/commonMain/kotlin/dev/jdgarita/frnk/monetization/FeatureGate.kt" <<'EOF'
package dev.jdgarita.frnk.monetization

import dev.jdgarita.frnk.backend.AnalyticsTracker
import dev.jdgarita.frnk.backend.ToolkitEvent
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map

/**
 * Gatekeeper used everywhere in the toolkit to decide whether to expose a Pro behaviour.
 *
 *   if (gate.canUse(Feature.UnlimitedExports)) doIt() else gate.requestUpgrade(...)
 *
 * `requestUpgrade` emits a generic "Paywall_Viewed" analytics event and returns a
 * route the caller can hand to the host's navigator. The toolkit never owns the paywall UI.
 */
class FeatureGate(
    private val entitlements: EntitlementManager,
    private val analytics: AnalyticsTracker,
) {
    val isPro: StateFlow<Boolean> get() = entitlements.isPro

    fun canUse(feature: Feature): Boolean = isPro.value || feature in freeFeatures

    /** Default fallback hook: log + return a route the host should navigate to. */
    fun requestUpgrade(source: String): String {
        analytics.track(ToolkitEvent.PaywallViewed, mapOf("source" to source))
        return PAYWALL_ROUTE_KEY
    }

    companion object {
        const val PAYWALL_ROUTE_KEY = "toolkit/paywall"
        /** Host can override by replacing the bound FeatureGate. */
        private val freeFeatures = emptySet<Feature>()
    }
}
EOF

write_file "$ROOT/core-monetization-revenuecat/build.gradle.kts" <<'EOF'
plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
}

kotlin {
    jvmToolchain(17)
    androidTarget()
    listOf(iosX64(), iosArm64(), iosSimulatorArm64()).forEach { it.binaries.framework { baseName = "core_monetization_revenuecat" } }
    sourceSets {
        commonMain.dependencies {
            api(projects.coreMonetizationApi)
            implementation(libs.koin.core)
            implementation(libs.revenuecat.core)
            implementation(libs.revenuecat.result)
            implementation(libs.revenuecat.datetime)
        }
    }
}

android {
    namespace = "${ProjectConfiguration.GROUP_ID}.monetization.revenuecat"
    compileSdk = ProjectConfiguration.COMPILE_SDK
    defaultConfig { minSdk = ProjectConfiguration.MIN_SDK }
}
EOF

write_file "$ROOT/core-monetization-revenuecat/src/commonMain/kotlin/dev/jdgarita/frnk/monetization/revenuecat/RevenueCatModule.kt" <<'EOF'
package dev.jdgarita.frnk.monetization.revenuecat

import dev.jdgarita.frnk.monetization.EntitlementManager
import dev.jdgarita.frnk.monetization.FeatureGate
import org.koin.dsl.module

val revenueCatModule = module {
    single<EntitlementManager> { RevenueCatEntitlementManager() }
    single { FeatureGate(get(), get()) }
}
EOF

write_file "$ROOT/core-monetization-revenuecat/src/commonMain/kotlin/dev/jdgarita/frnk/monetization/revenuecat/RevenueCatEntitlementManager.kt" <<'EOF'
package dev.jdgarita.frnk.monetization.revenuecat

import dev.jdgarita.frnk.monetization.EntitlementManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** TODO: wire to com.revenuecat.purchases.kmp.Purchases. Skeleton kept callable. */
internal class RevenueCatEntitlementManager : EntitlementManager {
    private val _isPro = MutableStateFlow(false)
    override val isPro: StateFlow<Boolean> = _isPro.asStateFlow()
    override suspend fun refresh() { /* call Purchases.sharedInstance.getCustomerInfo() */ }
    override suspend fun restorePurchases(): Boolean = false
}
EOF

# ===========================================================================
# 9. ToolkitDiModule  — placed in core-monetization-api side (no Compose) so
#    any module can depend on it. Aggregates everything that has zero conflicts.
# ===========================================================================
write_file "$ROOT/core-monetization-api/src/commonMain/kotlin/dev/jdgarita/frnk/di/ToolkitDiModule.kt" <<'EOF'
package dev.jdgarita.frnk.di

import org.koin.core.module.Module

/**
 * Aggregator surfaced to host apps:
 *
 *   startKoin {
 *       modules(
 *           toolkitCoreModules() +              // always-on (utils, db, monetization-api)
 *           listOf(firebaseBackendModule) +     // pick ONE backend
 *           listOf(revenueCatModule) +          // monetization impl
 *           listOf(hostSchemaModule)            // host injects its SQLDelight schema here
 *       )
 *   }
 *
 * The host stays in control of conflicting picks (firebase OR supabase) so the
 * unused impl module is never linked.
 */
expect fun toolkitCoreModules(): List<Module>
EOF

write_file "$ROOT/core-monetization-api/src/androidMain/kotlin/dev/jdgarita/frnk/di/ToolkitDiModule.android.kt" <<'EOF'
package dev.jdgarita.frnk.di

import org.koin.core.module.Module
/** Host wires database/backend/monetization impl modules separately; this is a placeholder for shared infra. */
actual fun toolkitCoreModules(): List<Module> = emptyList()
EOF

write_file "$ROOT/core-monetization-api/src/iosMain/kotlin/dev/jdgarita/frnk/di/ToolkitDiModule.ios.kt" <<'EOF'
package dev.jdgarita.frnk.di

import org.koin.core.module.Module
actual fun toolkitCoreModules(): List<Module> = emptyList()
EOF

# ===========================================================================
# 10. androidApp & iosApp aggregators
# ===========================================================================
say "[4/6] writing aggregators: androidApp, iosApp"

write_file "$ROOT/androidApp/build.gradle.kts" <<'EOF'
plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.kotlin.compose)
}

kotlin {
    jvmToolchain(17)
    androidTarget()
    sourceSets.androidMain.dependencies {
        api(projects.coreUtils)
        api(projects.coreUiApi)
        api(projects.coreUiAtoms)
        api(projects.coreDatabaseApi)
        api(projects.coreBackendApi)
        api(projects.coreMonetizationApi)
        // Host apps may include impl modules selectively; toolkit only re-exports API surface here.
    }
}

android {
    namespace = "${ProjectConfiguration.GROUP_ID}.android"
    compileSdk = ProjectConfiguration.COMPILE_SDK
    defaultConfig { minSdk = ProjectConfiguration.MIN_SDK }
}
EOF

write_file "$ROOT/androidApp/src/androidMain/AndroidManifest.xml" <<'EOF'
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android" />
EOF

write_file "$ROOT/iosApp/build.gradle.kts" <<'EOF'
import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.kotlin.compose)
}

kotlin {
    val xcf = XCFramework(ProjectConfiguration.IOS_FRAMEWORK_NAME)
    listOf(iosX64(), iosArm64(), iosSimulatorArm64()).forEach { t ->
        t.binaries.framework {
            baseName = ProjectConfiguration.IOS_FRAMEWORK_NAME
            xcf.add(this)
            isStatic = true
            export(projects.coreUtils)
            export(projects.coreUiApi)
            export(projects.coreUiAtoms)
            export(projects.coreDatabaseApi)
            export(projects.coreBackendApi)
            export(projects.coreMonetizationApi)
        }
    }
    sourceSets.iosMain.dependencies {
        api(projects.coreUtils)
        api(projects.coreUiApi)
        api(projects.coreUiAtoms)
        api(projects.coreDatabaseApi)
        api(projects.coreBackendApi)
        api(projects.coreMonetizationApi)
    }
}
EOF

# ---------------------------------------------------------------------------
# 10b. androidDemoApp — smoke harness app (com.android.application).
# Exercises ToolkitTheme + atoms, Koin/FeatureGate (with a fake EntitlementManager
# so no RevenueCat key is required), and the MVI engine end-to-end.
# ---------------------------------------------------------------------------
say
say "[4/6] writing androidDemoApp"

write_file "$ROOT/androidDemoApp/build.gradle.kts" <<'EOF'
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.compose.multiplatform)
}

kotlin { jvmToolchain(17) }

android {
    namespace = "${ProjectConfiguration.GROUP_ID}.demo"
    compileSdk = ProjectConfiguration.COMPILE_SDK
    defaultConfig {
        applicationId = "${ProjectConfiguration.GROUP_ID}.demo"
        minSdk = ProjectConfiguration.MIN_SDK
        targetSdk = ProjectConfiguration.TARGET_SDK
        versionCode = 1
        versionName = "0.1.0"
    }
    buildFeatures { compose = true }
    buildTypes { getByName("release") { isMinifyEnabled = false } }
    lint {
        // AGP 8.7's bundled lint uses Kotlin Analysis API 2.0 and crashes on Kotlin 2.2.20
        // module metadata. The demo is a smoke harness, not a shipping app. Bump AGP to 8.8+
        // to re-enable release lint.
        checkReleaseBuilds = false
        abortOnError = false
    }
}

dependencies {
    implementation(projects.androidApp)
    implementation(projects.coreUiAtoms)
    implementation(projects.coreUiApi)
    implementation(projects.coreMonetizationApi)
    implementation(projects.coreBackendApi)
    implementation(projects.coreDatabaseImpl)

    implementation(compose.runtime)
    implementation(compose.foundation)
    implementation(compose.ui)

    implementation(libs.androidx.activity.compose)

    implementation(libs.koin.core)
    implementation(libs.koin.compose)
    implementation(libs.koin.compose.viewmodel)
}
EOF

write_file "$ROOT/androidDemoApp/src/main/AndroidManifest.xml" <<'EOF'
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
    <application
        android:name=".DemoApplication"
        android:label="Frnk Demo"
        android:theme="@style/Theme.FrnkDemo"
        android:allowBackup="true">
        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:theme="@style/Theme.FrnkDemo">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>
</manifest>
EOF

write_file "$ROOT/androidDemoApp/src/main/res/values/themes.xml" <<'EOF'
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <style name="Theme.FrnkDemo" parent="android:Theme.Material.Light.NoActionBar" />
</resources>
EOF

write_file "$ROOT/androidDemoApp/src/main/kotlin/dev/jdgarita/frnk/demo/DemoApplication.kt" <<'EOF'
package dev.jdgarita.frnk.demo

import android.app.Application
import dev.jdgarita.frnk.database.impl.DatabaseContext
import org.koin.core.context.startKoin

class DemoApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        DatabaseContext.application = this
        startKoin { modules(demoModule) }
    }
}
EOF

write_file "$ROOT/androidDemoApp/src/main/kotlin/dev/jdgarita/frnk/demo/DemoModule.kt" <<'EOF'
package dev.jdgarita.frnk.demo

import android.util.Log
import dev.jdgarita.frnk.backend.AnalyticsTracker
import dev.jdgarita.frnk.backend.CrashReporter
import dev.jdgarita.frnk.backend.ToolkitEvent
import dev.jdgarita.frnk.monetization.EntitlementManager
import dev.jdgarita.frnk.monetization.FeatureGate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/** Demo wiring. A real host swaps these for revenueCatModule / firebaseBackendModule / etc. */
val demoModule = module {
    // Concrete fake registered as itself so DemoViewModel can pull it for setPro(),
    // then exposed via EntitlementManager as the same singleton.
    single { FakeEntitlementManager() }
    single<EntitlementManager> { get<FakeEntitlementManager>() }
    single<AnalyticsTracker> { LoggingAnalyticsTracker() }
    single<CrashReporter> { LoggingCrashReporter() }
    single { FeatureGate(get(), get()) }
    viewModel { DemoViewModel(get(), get(), get()) }
}

class FakeEntitlementManager : EntitlementManager {
    private val _isPro = MutableStateFlow(false)
    override val isPro: StateFlow<Boolean> = _isPro.asStateFlow()
    fun setPro(value: Boolean) { _isPro.value = value }
    override suspend fun refresh() = Unit
    override suspend fun restorePurchases(): Boolean { _isPro.value = true; return true }
}

class LoggingAnalyticsTracker : AnalyticsTracker {
    override fun track(event: ToolkitEvent, params: Map<String, Any?>) { Log.d("Analytics", "${event.key} $params") }
    override fun trackCustom(name: String, params: Map<String, Any?>) { Log.d("Analytics", "$name $params") }
    override fun setUserProperty(key: String, value: String?) { Log.d("Analytics", "user[$key]=$value") }
}

class LoggingCrashReporter : CrashReporter {
    override fun recordException(throwable: Throwable, extras: Map<String, String>) { Log.e("Crash", "$extras", throwable) }
    override fun setUserId(id: String?) { Log.d("Crash", "userId=$id") }
    override fun log(message: String) { Log.d("Crash", message) }
}
EOF

write_file "$ROOT/androidDemoApp/src/main/kotlin/dev/jdgarita/frnk/demo/DemoViewModel.kt" <<'EOF'
package dev.jdgarita.frnk.demo

import androidx.lifecycle.viewModelScope
import dev.jdgarita.frnk.backend.AnalyticsTracker
import dev.jdgarita.frnk.backend.ToolkitEvent
import dev.jdgarita.frnk.monetization.Feature
import dev.jdgarita.frnk.monetization.FeatureGate
import dev.jdgarita.frnk.ui.mvi.MviViewModel
import dev.jdgarita.frnk.ui.mvi.UiEffect
import dev.jdgarita.frnk.ui.mvi.UiIntent
import dev.jdgarita.frnk.ui.mvi.UiState
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

data class DemoState(val count: Int = 0, val email: String = "", val isPro: Boolean = false) : UiState

sealed interface DemoIntent : UiIntent {
    data object Increment : DemoIntent
    data object Decrement : DemoIntent
    data class EmailChanged(val value: String) : DemoIntent
    data object TogglePro : DemoIntent
    data object RequestUpgrade : DemoIntent
}

sealed interface DemoEffect : UiEffect {
    data class Navigate(val routeKey: String) : DemoEffect
    data class Toast(val message: String) : DemoEffect
}

class DemoViewModel(
    private val gate: FeatureGate,
    private val analytics: AnalyticsTracker,
    private val entitlements: FakeEntitlementManager,
) : MviViewModel<DemoState, DemoIntent, DemoEffect>(DemoState()) {

    init {
        analytics.track(ToolkitEvent.AppOpened, mapOf("source" to "demo"))
        gate.isPro.onEach { setState { copy(isPro = it) } }.launchIn(viewModelScope)
    }

    override suspend fun onIntent(intent: DemoIntent) {
        when (intent) {
            DemoIntent.Increment -> setState { copy(count = count + 1) }
            DemoIntent.Decrement -> setState { copy(count = count - 1) }
            is DemoIntent.EmailChanged -> setState { copy(email = intent.value) }
            DemoIntent.TogglePro -> entitlements.setPro(!currentState().isPro)
            DemoIntent.RequestUpgrade ->
                if (gate.canUse(Feature.Premium)) emit(DemoEffect.Toast("Already Pro"))
                else emit(DemoEffect.Navigate(gate.requestUpgrade(source = "demo_button")))
        }
    }
}
EOF

write_file "$ROOT/androidDemoApp/src/main/kotlin/dev/jdgarita/frnk/demo/DemoScreen.kt" <<'EOF'
package dev.jdgarita.frnk.demo

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.jdgarita.frnk.ui.atoms.ToolkitButton
import dev.jdgarita.frnk.ui.atoms.ToolkitTextField
import dev.jdgarita.frnk.ui.atoms.ToolkitTheme
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun DemoScreen(onEffect: (DemoEffect) -> Unit = {}) {
    val vm: DemoViewModel = koinViewModel()
    val state by vm.state.collectAsState()
    LaunchedEffect(vm) { vm.effects.collect(onEffect) }

    val colors = ToolkitTheme.colors
    val typography = ToolkitTheme.typography

    Column(
        modifier = Modifier.fillMaxSize().background(colors.surface).padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        BasicText("Frnk Toolkit Demo", style = typography.title.copy(color = colors.onSurface))

        Section("1. Theme + Atoms") {
            ToolkitTextField(
                value = state.email,
                onValueChange = { vm.send(DemoIntent.EmailChanged(it)) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = "you@example.com",
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ToolkitButton(label = "−", onClick = { vm.send(DemoIntent.Decrement) })
                BasicText("Count: ${state.count}", style = typography.body.copy(color = colors.onSurface))
                ToolkitButton(label = "+", onClick = { vm.send(DemoIntent.Increment) })
            }
        }

        Section("2. FeatureGate (Pro = ${state.isPro})") {
            ToolkitButton(label = "Toggle Pro", onClick = { vm.send(DemoIntent.TogglePro) })
            ToolkitButton(label = "Request Upgrade", onClick = { vm.send(DemoIntent.RequestUpgrade) })
        }

        Section("3. MVI") {
            BasicText(
                "DemoViewModel = MviViewModel<DemoState, DemoIntent, DemoEffect>.\n" +
                    "Composable → send(Intent) → reducer → State → recomposition.",
                style = typography.body.copy(color = colors.onSurface),
            )
        }
    }
}

@Composable
private fun Section(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        BasicText(title, style = ToolkitTheme.typography.button.copy(color = ToolkitTheme.colors.onSurface))
        content()
        Spacer(Modifier.height(4.dp))
    }
}
EOF

write_file "$ROOT/androidDemoApp/src/main/kotlin/dev/jdgarita/frnk/demo/MainActivity.kt" <<'EOF'
package dev.jdgarita.frnk.demo

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.ui.graphics.Color
import dev.jdgarita.frnk.ui.atoms.ProvideToolkitTheme
import dev.jdgarita.frnk.ui.atoms.ToolkitColors

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ProvideToolkitTheme(
                colors = ToolkitColors(primary = Color(0xFF0A84FF), onPrimary = Color.White),
            ) { DemoScreen(onEffect = ::handleEffect) }
        }
    }

    private fun handleEffect(effect: DemoEffect) = when (effect) {
        is DemoEffect.Navigate -> Toast.makeText(this, "Navigate → ${effect.routeKey}", Toast.LENGTH_SHORT).show()
        is DemoEffect.Toast -> Toast.makeText(this, effect.message, Toast.LENGTH_SHORT).show()
    }
}
EOF

# ---------------------------------------------------------------------------
# 10c. iosDemoApp — Swift sources + README. NOT a Gradle module; user creates
# the Xcode project locally and links FrnkKit.xcframework from :iosApp.
# ---------------------------------------------------------------------------
say
say "[4/6] writing iosDemoApp"

write_file "$ROOT/iosDemoApp/README.md" <<'EOF'
# iosDemoApp

Open this folder in Xcode after running:

```bash
./gradlew :iosApp:assembleXCFramework
```

Then add `iosApp/build/XCFrameworks/release/FrnkKit.xcframework`
to the Xcode project's Frameworks, Libraries, and Embedded Content.

Drop your real `GoogleService-Info.plist` into `iosDemoApp/iosDemoApp/`
(gitignored).
EOF

write_file "$ROOT/iosDemoApp/iosDemoApp/iosDemoApp.swift" <<'EOF'
import SwiftUI

@main
struct iosDemoAppApp: App {
    var body: some Scene {
        WindowGroup { ContentView() }
    }
}
EOF

write_file "$ROOT/iosDemoApp/iosDemoApp/ContentView.swift" <<'EOF'
import SwiftUI
import FrnkKit

struct ContentView: View {
    var body: some View {
        Text("frnk demo — link FrnkKit and call into MVI here")
    }
}
EOF

write_file "$ROOT/iosDemoApp/iosDemoApp/GoogleService-Info.plist.template" <<'EOF'
<?xml version="1.0" encoding="UTF-8"?>
<plist version="1.0">
  <dict>
    <key>_comment</key>
    <string>Drop your real GoogleService-Info.plist from the Firebase console next to this template (gitignored).</string>
  </dict>
</plist>
EOF

# ---------------------------------------------------------------------------
# 11. Minimal README addendum (do not overwrite existing README.md)
# ---------------------------------------------------------------------------
say
say "[5/6] writing docs/HOST_INTEGRATION.md"

write_file "$ROOT/docs/HOST_INTEGRATION.md" <<'EOF'
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
        ToolkitRoute.Paywall  -> navController.navigate(HostRoutes.Paywall)
        ToolkitRoute.SignIn   -> navController.navigate(HostRoutes.SignIn)
        ToolkitRoute.Home     -> navController.navigate(HostRoutes.Home)
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
EOF

# ---------------------------------------------------------------------------
# 12. Done
# ---------------------------------------------------------------------------
say
say "[6/6] complete"
if (( APPLY )); then
  say "Migration applied. Next steps:"
  say "  - Update root README.md to point at docs/HOST_INTEGRATION.md"
  say "  - Run: ./gradlew ktlintFormat && ./gradlew assemble"
  say "  - Wire firebase/supabase stubs (TODO markers in each impl)"
else
  say "Dry-run complete. Re-run with --apply to perform the migration."
fi
