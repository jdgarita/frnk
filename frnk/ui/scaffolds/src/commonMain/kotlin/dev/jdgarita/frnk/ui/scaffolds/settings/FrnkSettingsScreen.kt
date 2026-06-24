package dev.jdgarita.frnk.ui.scaffolds.settings

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import com.composeunstyled.theme.Theme
import dev.jdgarita.frnk.ui.atoms.FrnkIcon
import dev.jdgarita.frnk.ui.atoms.FrnkIconState
import dev.jdgarita.frnk.ui.atoms.FrnkSegmentedControl
import dev.jdgarita.frnk.ui.atoms.FrnkSegmentedControlState
import dev.jdgarita.frnk.ui.atoms.FrnkSwitch
import dev.jdgarita.frnk.ui.atoms.FrnkSwitchState
import dev.jdgarita.frnk.ui.atoms.FrnkText
import dev.jdgarita.frnk.ui.atoms.FrnkTextState
import dev.jdgarita.frnk.ui.atoms.FrnkTopAppBarAction
import dev.jdgarita.frnk.ui.organisms.FrnkSectionCard
import dev.jdgarita.frnk.ui.scaffolds.FrnkScreenScaffold
import dev.jdgarita.frnk.ui.theme.FrnkIconSource
import dev.jdgarita.frnk.ui.theme.FrnkStringSource
import dev.jdgarita.frnk.ui.theme.colorBackground
import dev.jdgarita.frnk.ui.theme.colorOnPrimaryContainer
import dev.jdgarita.frnk.ui.theme.colorOnSurfaceVariant
import dev.jdgarita.frnk.ui.theme.colorPrimary
import dev.jdgarita.frnk.ui.theme.colorPrimaryContainer
import dev.jdgarita.frnk.ui.theme.colors
import dev.jdgarita.frnk.ui.theme.ext.resolve
import dev.jdgarita.frnk.ui.theme.iconChevronRight
import dev.jdgarita.frnk.ui.theme.iconSizeSm
import dev.jdgarita.frnk.ui.theme.iconSizes
import dev.jdgarita.frnk.ui.theme.labelSmall
import dev.jdgarita.frnk.ui.theme.shapeFull
import dev.jdgarita.frnk.ui.theme.shapes
import dev.jdgarita.frnk.ui.theme.spacing
import dev.jdgarita.frnk.ui.theme.spacingLg
import dev.jdgarita.frnk.ui.theme.spacingMd
import dev.jdgarita.frnk.ui.theme.spacingSm
import dev.jdgarita.frnk.ui.theme.spacingXxs

@Composable
fun FrnkSettingsScreen(
    modifier: Modifier = Modifier,
    state: SettingsScreenState,
    contentPadding: PaddingValues = PaddingValues(Theme[spacing][spacingLg]),
    onNavigationClick: () -> Unit = {},
    onActionClick: (FrnkTopAppBarAction) -> Unit = {},
    onIntent: (SettingsIntent) -> Unit
) {
    FrnkScreenScaffold(
        topBar = state.topBar,
        contentPadding = contentPadding,
        onNavigationClick = onNavigationClick,
        onActionClick = onActionClick
    ) { mergedPadding ->
        SettingsScreenContent(
            modifier = modifier,
            state = state,
            contentPadding = mergedPadding,
            onIntent = onIntent
        )
    }
}

/**
 * Stateless renderer: a scrollable list of [SettingsSectionState] cards plus an optional footer.
 * Pure and preview-friendly — all interaction is delegated to [onIntent].
 */
@Composable
internal fun SettingsScreenContent(
    modifier: Modifier,
    state: SettingsScreenState,
    onIntent: (SettingsIntent) -> Unit,
    contentPadding: PaddingValues = PaddingValues(Theme[spacing][spacingLg])
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(Theme[colors][colorBackground])
                .verticalScroll(rememberScrollState())
                .padding(contentPadding),
        verticalArrangement = Arrangement.spacedBy(Theme[spacing][spacingLg])
    ) {
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
    onIntent: (SettingsIntent) -> Unit
) {
    // Card chrome (title + surface + dividers + animateContentSize + footnote) is shared with the
    // FrnkListSection organism via FrnkSectionCard; only the heterogeneous row rendering differs.
    // FrnkSectionCard is shared chrome that takes plain Strings, so the section header/footnote refs
    // resolve here (one level up) rather than at its leaf.
    FrnkSectionCard(
        rows = section.rows,
        title = section.title?.resolve(),
        footnote = section.footnote?.resolve()
    ) { _, row ->
        SettingsRow(row = row, onIntent = onIntent)
    }
}

@Composable
private fun SettingsRow(
    row: SettingsRowState,
    onIntent: (SettingsIntent) -> Unit
) {
    when (row) {
        is SettingsThemeRowState -> {
            val selectedIndex = row.options.indexOf(row.selected).coerceAtLeast(0)
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Theme[spacing][spacingMd], vertical = Theme[spacing][spacingSm]),
                verticalArrangement = Arrangement.spacedBy(Theme[spacing][spacingSm])
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(Theme[spacing][spacingMd]),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    row.icon?.let {
                        FrnkIcon(
                            state =
                                FrnkIconState.Content(icon = it, contentDescription = null, tint = colorPrimary)
                        )
                    }
                    FrnkText(state = FrnkTextState.TitleMedium(content = row.title))
                }
                FrnkSegmentedControl(
                    state =
                        FrnkSegmentedControlState.Content(
                            options = row.optionLabels.map { it.resolve() },
                            selectedIndex = selectedIndex
                        ),
                    onOptionSelected = { index ->
                        onIntent(SettingsIntent.ThemeSelected(row.options[index]))
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        is SettingsClickableRowState ->
            SettingsRowScaffold(
                icon = row.icon,
                title = row.title,
                subtitle = row.subtitle,
                modifier = Modifier.clickable { onIntent(SettingsIntent.RowClicked(row.action)) }
            ) {
                FrnkIcon(
                    state =
                        FrnkIconState.Content(
                            icon = FrnkIconSource.Token(iconChevronRight),
                            contentDescription = null,
                            size = Theme[iconSizes][iconSizeSm],
                            tint = colorOnSurfaceVariant
                        )
                )
            }

        is SettingsToggleRowState ->
            SettingsRowScaffold(
                icon = row.icon,
                title = row.title,
                subtitle = row.subtitle
            ) {
                FrnkSwitch(
                    state = FrnkSwitchState.Content(checked = row.checked),
                    onCheckedChange = { checked -> onIntent(SettingsIntent.ToggleChanged(row.id, checked)) }
                )
            }

        is SettingsStatusRowState ->
            SettingsRowScaffold(
                icon = row.icon,
                title = row.title,
                subtitle = row.subtitle
            ) {
                row.badge?.let { SettingsBadge(text = it) }
            }
    }
}

/** A small pill badge (e.g. "PRO") drawn in the primary-container palette. Non-interactive. */
@Composable
private fun SettingsBadge(text: FrnkStringSource) {
    FrnkText(
        state =
            FrnkTextState.Raw(
                content = text,
                style = labelSmall,
                color = colorOnPrimaryContainer
            ),
        modifier =
            Modifier
                .clip(Theme[shapes][shapeFull])
                .background(Theme[colors][colorPrimaryContainer])
                .padding(horizontal = Theme[spacing][spacingSm], vertical = Theme[spacing][spacingXxs])
    )
}

/** Shared icon + title/subtitle + trailing-slot layout for clickable and toggle rows. */
@Composable
private fun SettingsRowScaffold(
    icon: FrnkIconSource,
    title: FrnkStringSource,
    subtitle: FrnkStringSource?,
    modifier: Modifier = Modifier,
    trailing: @Composable () -> Unit
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(horizontal = Theme[spacing][spacingMd], vertical = Theme[spacing][spacingSm]),
        horizontalArrangement = Arrangement.spacedBy(Theme[spacing][spacingMd]),
        verticalAlignment = Alignment.CenterVertically
    ) {
        FrnkIcon(state = FrnkIconState.Content(icon = icon, contentDescription = null, tint = colorPrimary))
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(Theme[spacing][spacingXxs])
        ) {
            FrnkText(state = FrnkTextState.TitleMedium(content = title))
            subtitle?.let {
                FrnkText(state = FrnkTextState.BodySmall(content = it, color = colorOnSurfaceVariant))
            }
        }
        trailing()
    }
}

@Composable
private fun SettingsFooter(
    footer: SettingsFooterState,
    onVersionTap: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Theme[spacing][spacingXxs])
    ) {
        FrnkText(
            state = FrnkTextState.BodySmall(content = footer.text, color = colorOnSurfaceVariant)
        )
        FrnkText(
            state =
                FrnkTextState.Raw(
                    content = footer.version,
                    style = labelSmall,
                    color = colorOnSurfaceVariant
                ),
            modifier = Modifier.clickable { onVersionTap() }
        )
    }
}