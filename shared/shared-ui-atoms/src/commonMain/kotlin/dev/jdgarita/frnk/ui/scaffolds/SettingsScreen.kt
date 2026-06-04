package dev.jdgarita.frnk.ui.scaffolds

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.composeunstyled.theme.Theme
import dev.jdgarita.frnk.ui.atoms.FrnkIcon
import dev.jdgarita.frnk.ui.atoms.FrnkIconState
import dev.jdgarita.frnk.ui.atoms.FrnkSegmentedControl
import dev.jdgarita.frnk.ui.atoms.FrnkSegmentedControlState
import dev.jdgarita.frnk.ui.atoms.FrnkSwitch
import dev.jdgarita.frnk.ui.atoms.FrnkSwitchState
import dev.jdgarita.frnk.ui.atoms.FrnkText
import dev.jdgarita.frnk.ui.atoms.FrnkTextState
import dev.jdgarita.frnk.ui.mvi.EffectCollector
import dev.jdgarita.frnk.ui.organisms.FrnkSectionCard
import dev.jdgarita.frnk.ui.theme.colorBackground
import dev.jdgarita.frnk.ui.theme.colorOnPrimaryContainer
import dev.jdgarita.frnk.ui.theme.colorOnSurfaceVariant
import dev.jdgarita.frnk.ui.theme.colorPrimaryContainer
import dev.jdgarita.frnk.ui.theme.colors
import dev.jdgarita.frnk.ui.theme.iconChevronRight
import dev.jdgarita.frnk.ui.theme.icons
import dev.jdgarita.frnk.ui.theme.labelSmall
import dev.jdgarita.frnk.ui.theme.shapeFull
import dev.jdgarita.frnk.ui.theme.shapes
import dev.jdgarita.frnk.ui.tokens.FrnkIconSize
import dev.jdgarita.frnk.ui.tokens.FrnkSpacing
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

/**
 * VM-backed convenience wrapper around [SettingsScreenContent]. Resolves a [SettingsViewModel] from
 * Koin (initialised with [initialState] via `parametersOf`), forwards its state to the stateless
 * renderer, and surfaces one-shot effects to [onEffect]. Mirrors
 * [OnboardingScreen][dev.jdgarita.frnk.ui.scaffolds.OnboardingScreen] exactly.
 *
 * [vmKey] scopes the ViewModel inside the host's `ViewModelStore`. By default the VM is reused for
 * the lifetime of the enclosing `ViewModelStoreOwner`; change [vmKey] each time the screen is shown
 * (e.g. `key = "settings-$openCounter"`) to get a fresh flow. Hosts that want full state-hoisting
 * control should call [SettingsScreenContent] directly.
 */
@Composable
fun SettingsScreen(
    initialState: SettingsScreenState,
    modifier: Modifier = Modifier,
    vmKey: String? = null,
    contentPadding: PaddingValues = PaddingValues(FrnkSpacing.lg),
    onEffect: (SettingsEffect) -> Unit = {},
) {
    val vm: SettingsViewModel = koinViewModel(key = vmKey) { parametersOf(initialState) }
    val state by vm.state.collectAsStateWithLifecycle()

    EffectCollector(vm.effects, onEffect = onEffect)

    SettingsScreenContent(
        state = state,
        onIntent = vm::send,
        modifier = modifier,
        contentPadding = contentPadding,
    )
}

/**
 * Stateless renderer: a scrollable list of [SettingsSectionState] cards plus an optional footer.
 * Pure and preview-friendly — all interaction is delegated to [onIntent].
 */
@Composable
fun SettingsScreenContent(
    state: SettingsScreenState,
    onIntent: (SettingsIntent) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(FrnkSpacing.lg),
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(Theme[colors][colorBackground])
                .verticalScroll(rememberScrollState())
                .padding(contentPadding),
        verticalArrangement = Arrangement.spacedBy(FrnkSpacing.lg),
    ) {
        // Hosts that render their own header (e.g. a FrnkTopAppBar) pass a blank title to suppress
        // this one and avoid a duplicate heading.
        if (state.title.isNotBlank()) {
            FrnkText(state = FrnkTextState.HeadlineSmall(text = state.title))
        }

        state.sections.forEach { section ->
            SettingsSection(section = section, onIntent = onIntent)
        }

        if (state.developerVisible) {
            state.developerSection?.let { SettingsSection(section = it, onIntent = onIntent) }
        }

        state.footer?.let {
            SettingsFooter(footer = it, onVersionTap = { onIntent(SettingsIntent.VersionTapped) })
        }
    }
}

@Composable
private fun SettingsSection(
    section: SettingsSectionState,
    onIntent: (SettingsIntent) -> Unit,
) {
    // Card chrome (title + surface + dividers + animateContentSize + footnote) is shared with the
    // FrnkListSection organism via FrnkSectionCard; only the heterogeneous row rendering differs.
    FrnkSectionCard(
        rows = section.rows,
        title = section.title,
        footnote = section.footnote,
    ) { _, row ->
        SettingsRow(row = row, onIntent = onIntent)
    }
}

@Composable
private fun SettingsRow(
    row: SettingsRowState,
    onIntent: (SettingsIntent) -> Unit,
) {
    when (row) {
        is SettingsThemeRowState -> {
            val selectedIndex = row.options.indexOf(row.selected).coerceAtLeast(0)
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = FrnkSpacing.md, vertical = FrnkSpacing.sm),
                verticalArrangement = Arrangement.spacedBy(FrnkSpacing.sm),
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(FrnkSpacing.md),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    row.icon?.let { FrnkIcon(state = it) }
                    FrnkText(state = FrnkTextState.TitleMedium(text = row.title))
                }
                FrnkSegmentedControl(
                    state =
                        FrnkSegmentedControlState(
                            options = row.optionLabels,
                            selectedIndex = selectedIndex,
                        ),
                    onOptionSelected = { index ->
                        onIntent(SettingsIntent.ThemeSelected(row.options[index]))
                    },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        is SettingsClickableRowState ->
            SettingsRowScaffold(
                icon = row.icon,
                title = row.title,
                subtitle = row.subtitle,
                modifier = Modifier.clickable { onIntent(SettingsIntent.RowClicked(row.action)) },
            ) {
                FrnkIcon(
                    state =
                        FrnkIconState(
                            imageVector = Theme[icons][iconChevronRight],
                            contentDescription = null,
                            size = FrnkIconSize.sm,
                            tint = colorOnSurfaceVariant,
                        ),
                )
            }

        is SettingsToggleRowState ->
            SettingsRowScaffold(
                icon = row.icon,
                title = row.title,
                subtitle = row.subtitle,
            ) {
                FrnkSwitch(
                    state = FrnkSwitchState(checked = row.checked),
                    onCheckedChange = { checked -> onIntent(SettingsIntent.ToggleChanged(row.id, checked)) },
                )
            }

        is SettingsStatusRowState ->
            SettingsRowScaffold(
                icon = row.icon,
                title = row.title,
                subtitle = row.subtitle,
            ) {
                row.badge?.let { SettingsBadge(text = it) }
            }
    }
}

/** A small pill badge (e.g. "PRO") drawn in the primary-container palette. Non-interactive. */
@Composable
private fun SettingsBadge(text: String) {
    FrnkText(
        state =
            FrnkTextState.Raw(
                text = text,
                style = labelSmall,
                color = colorOnPrimaryContainer,
            ),
        modifier =
            Modifier
                .clip(Theme[shapes][shapeFull])
                .background(Theme[colors][colorPrimaryContainer])
                .padding(horizontal = FrnkSpacing.sm, vertical = FrnkSpacing.xxs),
    )
}

/** Shared icon + title/subtitle + trailing-slot layout for clickable and toggle rows. */
@Composable
private fun SettingsRowScaffold(
    icon: FrnkIconState,
    title: String,
    subtitle: String?,
    modifier: Modifier = Modifier,
    trailing: @Composable () -> Unit,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(horizontal = FrnkSpacing.md, vertical = FrnkSpacing.sm),
        horizontalArrangement = Arrangement.spacedBy(FrnkSpacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        FrnkIcon(state = icon)
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(FrnkSpacing.xxs),
        ) {
            FrnkText(state = FrnkTextState.TitleMedium(text = title))
            subtitle?.let {
                FrnkText(state = FrnkTextState.BodySmall(text = it, color = colorOnSurfaceVariant))
            }
        }
        trailing()
    }
}

@Composable
private fun SettingsFooter(
    footer: SettingsFooterState,
    onVersionTap: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(FrnkSpacing.xxs),
    ) {
        FrnkText(
            state = FrnkTextState.BodySmall(text = footer.text, color = colorOnSurfaceVariant),
        )
        FrnkText(
            state =
                FrnkTextState.Raw(
                    text = footer.version,
                    style = labelSmall,
                    color = colorOnSurfaceVariant,
                ),
            modifier = Modifier.clickable { onVersionTap() },
        )
    }
}
