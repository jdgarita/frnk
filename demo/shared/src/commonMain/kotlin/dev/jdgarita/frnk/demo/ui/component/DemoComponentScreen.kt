package dev.jdgarita.frnk.demo.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavKey
import com.composeunstyled.theme.Theme
import dev.jdgarita.frnk.demo.ui.home.DemoHomeEffect
import dev.jdgarita.frnk.demo.ui.home.DemoHomeIntent
import dev.jdgarita.frnk.demo.ui.home.DemoHomeScreenState
import dev.jdgarita.frnk.demo.ui.home.DemoHomeViewModel
import dev.jdgarita.frnk.monetization.ui.FrnkAppStore
import dev.jdgarita.frnk.monetization.ui.NoSubscriptionFoundDialog
import dev.jdgarita.frnk.monetization.ui.frnkAppStore
import dev.jdgarita.frnk.ui.app.shell.FrnkShellNavigator
import dev.jdgarita.frnk.ui.app.shell.FrnkShellSheet
import dev.jdgarita.frnk.ui.app.shell.FrnkStackShell
import dev.jdgarita.frnk.ui.atoms.FrnkButton
import dev.jdgarita.frnk.ui.atoms.FrnkButtonState
import dev.jdgarita.frnk.ui.atoms.FrnkButtonVariant
import dev.jdgarita.frnk.ui.atoms.FrnkDialog
import dev.jdgarita.frnk.ui.atoms.FrnkDialogAction
import dev.jdgarita.frnk.ui.atoms.FrnkDialogActionKind
import dev.jdgarita.frnk.ui.atoms.FrnkDialogActionLayout
import dev.jdgarita.frnk.ui.atoms.FrnkDialogState
import dev.jdgarita.frnk.ui.atoms.FrnkDialogVariant
import dev.jdgarita.frnk.ui.atoms.FrnkDivider
import dev.jdgarita.frnk.ui.atoms.FrnkDividerState
import dev.jdgarita.frnk.ui.atoms.FrnkHeadlineAction
import dev.jdgarita.frnk.ui.atoms.FrnkHeadlineBadge
import dev.jdgarita.frnk.ui.atoms.FrnkHeadlineTopBar
import dev.jdgarita.frnk.ui.atoms.FrnkHeadlineTopBarState
import dev.jdgarita.frnk.ui.atoms.FrnkHeadlineTrailing
import dev.jdgarita.frnk.ui.atoms.FrnkIcon
import dev.jdgarita.frnk.ui.atoms.FrnkIconButton
import dev.jdgarita.frnk.ui.atoms.FrnkIconButtonState
import dev.jdgarita.frnk.ui.atoms.FrnkIconState
import dev.jdgarita.frnk.ui.atoms.FrnkSegmentedControl
import dev.jdgarita.frnk.ui.atoms.FrnkSegmentedControlState
import dev.jdgarita.frnk.ui.atoms.FrnkSkeleton
import dev.jdgarita.frnk.ui.atoms.FrnkSwitch
import dev.jdgarita.frnk.ui.atoms.FrnkSwitchState
import dev.jdgarita.frnk.ui.atoms.FrnkText
import dev.jdgarita.frnk.ui.atoms.FrnkTextState
import dev.jdgarita.frnk.ui.bottomnav.FrnkBottomFloatingBar
import dev.jdgarita.frnk.ui.bottomnav.FrnkNavBarItem
import dev.jdgarita.frnk.ui.molecules.FrnkEmptyState
import dev.jdgarita.frnk.ui.molecules.FrnkEmptyStateState
import dev.jdgarita.frnk.ui.molecules.FrnkLabeledValue
import dev.jdgarita.frnk.ui.molecules.FrnkLabeledValueOrientation
import dev.jdgarita.frnk.ui.molecules.FrnkLabeledValueState
import dev.jdgarita.frnk.ui.molecules.FrnkListRow
import dev.jdgarita.frnk.ui.molecules.FrnkListRowState
import dev.jdgarita.frnk.ui.molecules.FrnkSwipeAction
import dev.jdgarita.frnk.ui.molecules.FrnkSwipeBehavior
import dev.jdgarita.frnk.ui.molecules.FrnkSwipeDirection
import dev.jdgarita.frnk.ui.molecules.FrnkSwipeableState
import dev.jdgarita.frnk.ui.organisms.FrnkListSection
import dev.jdgarita.frnk.ui.organisms.FrnkListSectionState
import dev.jdgarita.frnk.ui.organisms.FrnkProfileHeader
import dev.jdgarita.frnk.ui.organisms.FrnkProfileHeaderState
import dev.jdgarita.frnk.ui.scaffolds.sheet.FrnkModalSheet
import dev.jdgarita.frnk.ui.scaffolds.sheet.FrnkModalSheetCloseHandler
import dev.jdgarita.frnk.ui.scaffolds.sheet.FrnkSheetHeader
import dev.jdgarita.frnk.ui.theme.FrnkIconSource
import dev.jdgarita.frnk.ui.theme.FrnkStringSource
import dev.jdgarita.frnk.ui.theme.colorOnBackground
import dev.jdgarita.frnk.ui.theme.colorOnPrimaryContainer
import dev.jdgarita.frnk.ui.theme.colorOnSuccess
import dev.jdgarita.frnk.ui.theme.colorOnSurfaceVariant
import dev.jdgarita.frnk.ui.theme.colorPrimary
import dev.jdgarita.frnk.ui.theme.colorSuccess
import dev.jdgarita.frnk.ui.theme.colorSurfaceVariant
import dev.jdgarita.frnk.ui.theme.colors
import dev.jdgarita.frnk.ui.theme.iconCheck
import dev.jdgarita.frnk.ui.theme.iconChevronRight
import dev.jdgarita.frnk.ui.theme.iconError
import dev.jdgarita.frnk.ui.theme.iconNavSettings
import dev.jdgarita.frnk.ui.theme.iconNotifications
import dev.jdgarita.frnk.ui.theme.iconRestore
import dev.jdgarita.frnk.ui.theme.iconSearch
import dev.jdgarita.frnk.ui.theme.icons
import dev.jdgarita.frnk.ui.theme.rememberFrnkRipple
import dev.jdgarita.frnk.ui.theme.shapeCard
import dev.jdgarita.frnk.ui.theme.shapes
import dev.jdgarita.frnk.ui.theme.stringCancel
import dev.jdgarita.frnk.ui.theme.stringSearch
import dev.jdgarita.frnk.ui.theme.stringSettings
import dev.jdgarita.frnk.ui.tokens.FrnkIconSize
import dev.jdgarita.frnk.ui.tokens.FrnkSpacing
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import org.koin.compose.viewmodel.koinViewModel

/**
 * The per-component showcase rendered inside [ComponentDetailScreen]. Resolves the demo's
 * [DemoHomeViewModel] for the few interactive widgets (switch / segmented control / nav bar) and renders
 * the matching gallery for [name]. Effect feedback ([DemoHomeEffect.Toast]) is wired but not surfaced
 * app-wide yet in this demo, so press feedback is carried by the automatic ripple/haptics instead.
 */
@Composable
fun ComponentScreen(name: String) {
    val viewModel: DemoHomeViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    ComponentGallery(
        name = name,
        state = state,
        onIntent = viewModel::send,
        onEffect = {}
    )
}

@Composable
private fun ComponentGallery(
    name: String,
    state: DemoHomeScreenState,
    onIntent: (DemoHomeIntent) -> Unit,
    onEffect: (DemoHomeEffect) -> Unit
) {
    when (name) {
        "FrnkText" -> {
            FrnkText(state = FrnkTextState.HeadlineSmall(text = "HeadlineSmall"))
            FrnkText(state = FrnkTextState.Title(text = "Title"))
            FrnkText(state = FrnkTextState.TitleMedium(text = "TitleMedium"))
            FrnkText(state = FrnkTextState.Body(text = "Body"))
            FrnkText(state = FrnkTextState.BodyMedium(text = "BodyMedium"))
            FrnkText(
                state = FrnkTextState.BodySmall(text = "BodySmall", color = colorOnSurfaceVariant)
            )
            FrnkText(state = FrnkTextState.AppName(annotated = buildAnnotatedString { append("FrnkKit") }))
            FrnkText(state = FrnkTextState.BodySmall(text = "Skeleton", color = colorOnSurfaceVariant))
            FrnkText(
                state = FrnkTextState.Title(text = "Loading title", skeleton = FrnkSkeleton(enabled = true))
            )
            FrnkText(
                state =
                    FrnkTextState.Body(
                        text = "Loading a longer body line of text",
                        skeleton = FrnkSkeleton(enabled = true)
                    )
            )
        }

        "FrnkButton" -> {
            Row(horizontalArrangement = Arrangement.spacedBy(FrnkSpacing.sm)) {
                FrnkButton(state = FrnkButtonState.Content(text = "Filled"), onClick = {})
                FrnkButton(
                    state = FrnkButtonState.Content(text = "Outlined", variant = FrnkButtonVariant.Outlined),
                    onClick = {}
                )
                FrnkButton(
                    state = FrnkButtonState.Content(text = "Ghost", variant = FrnkButtonVariant.Ghost),
                    onClick = {}
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(FrnkSpacing.sm)) {
                FrnkButton(
                    state = FrnkButtonState.Content(text = "Filled", enabled = false),
                    onClick = {}
                )
                FrnkButton(
                    state =
                        FrnkButtonState.Content(
                            text = "Outlined",
                            variant = FrnkButtonVariant.Outlined,
                            enabled = false
                        ),
                    onClick = {}
                )
            }
            FrnkText(state = FrnkTextState.BodySmall(text = "Skeleton", color = colorOnSurfaceVariant))
            Row(horizontalArrangement = Arrangement.spacedBy(FrnkSpacing.sm)) {
                FrnkButton(state = FrnkButtonState.Skeleton, onClick = {})
                FrnkButton(state = FrnkButtonState.Skeleton, onClick = {})
            }
        }

        "FrnkIcon / FrnkIconButton" -> {
            Row(
                horizontalArrangement = Arrangement.spacedBy(FrnkSpacing.md),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FrnkIcon(
                    state =
                        FrnkIconState.Content(
                            imageVector = Theme[icons][iconSearch],
                            contentDescription = "Search",
                            size = FrnkIconSize.md,
                            tint = colorPrimary
                        )
                )
                FrnkIcon(
                    state =
                        FrnkIconState.Content(
                            imageVector = Theme[icons][iconCheck],
                            contentDescription = "Check",
                            size = FrnkIconSize.lg,
                            tint = colorPrimary
                        )
                )
                FrnkIconButton(
                    state =
                        FrnkIconButtonState.Content(
                            imageVector = Theme[icons][iconNavSettings],
                            contentDescription = "Settings",
                            tint = colorOnBackground
                        ),
                    onClick = { onEffect(DemoHomeEffect.Toast("Icon button tapped")) }
                )
            }
            FrnkText(state = FrnkTextState.BodySmall(text = "Skeleton", color = colorOnSurfaceVariant))
            Row(
                horizontalArrangement = Arrangement.spacedBy(FrnkSpacing.md),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FrnkIcon(state = FrnkIconState.Skeleton())
                FrnkIconButton(state = FrnkIconButtonState.Skeleton, onClick = {})
            }
        }

        "FrnkDivider" -> {
            FrnkDivider(state = FrnkDividerState.Horizontal())
            Row(
                modifier = Modifier.height(24.dp),
                horizontalArrangement = Arrangement.spacedBy(FrnkSpacing.sm),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FrnkText(state = FrnkTextState.BodySmall(text = "Left"))
                FrnkDivider(state = FrnkDividerState.Vertical())
                FrnkText(state = FrnkTextState.BodySmall(text = "Right"))
            }
        }

        "FrnkSwitch" -> {
            Row(
                horizontalArrangement = Arrangement.spacedBy(FrnkSpacing.md),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FrnkSwitch(
                    state = FrnkSwitchState.Content(checked = state.gallerySwitchOn),
                    onCheckedChange = { onIntent(DemoHomeIntent.GallerySwitchChanged(it)) }
                )
                FrnkText(state = FrnkTextState.BodySmall(text = if (state.gallerySwitchOn) "On" else "Off"))
                FrnkSwitch(
                    state = FrnkSwitchState.Content(checked = true, enabled = false),
                    onCheckedChange = {}
                )
                FrnkText(state = FrnkTextState.BodySmall(text = "Disabled"))
            }
            FrnkText(state = FrnkTextState.BodySmall(text = "Skeleton", color = colorOnSurfaceVariant))
            FrnkSwitch(state = FrnkSwitchState.Skeleton, onCheckedChange = {})
        }

        "FrnkSegmentedControl" -> {
            FrnkSegmentedControl(
                state =
                    FrnkSegmentedControlState.Content(
                        options = listOf("One", "Two", "Three"),
                        selectedIndex = state.gallerySegmentIndex
                    ),
                onOptionSelected = { onIntent(DemoHomeIntent.GallerySegmentChanged(it)) }
            )
            FrnkText(state = FrnkTextState.BodySmall(text = "Skeleton", color = colorOnSurfaceVariant))
            FrnkSegmentedControl(
                state = FrnkSegmentedControlState.Skeleton,
                onOptionSelected = {}
            )
        }

        "FrnkBottomFloatingBar" -> {
            FrnkText(
                state =
                    FrnkTextState.BodySmall(
                        text =
                            "The real adaptive bar — the very same component shown at the foot of every screen " +
                                "(a Material3 floating pill on Android, a native glassy UITabBar on iOS).",
                        color = colorOnSurfaceVariant
                    )
            )
            FrnkBottomFloatingBar(
                items =
                    listOf(
                        FrnkNavBarItem(
                            "a",
                            FrnkIconSource.Token(iconSearch),
                            "magnifyingglass",
                            FrnkStringSource.Token(stringSearch)
                        ),
                        FrnkNavBarItem(
                            "b",
                            FrnkIconSource.Token(iconCheck),
                            "checkmark",
                            FrnkStringSource.Raw("Check")
                        ),
                        FrnkNavBarItem(
                            "c",
                            FrnkIconSource.Token(iconNavSettings),
                            "gearshape",
                            FrnkStringSource.Token(stringSettings)
                        )
                    ),
                selectedIndex = state.galleryNavIndex,
                onItemSelected = { onIntent(DemoHomeIntent.GalleryNavChanged(it)) },
                modifier = Modifier.fillMaxWidth()
            )
        }

        "Ripple" -> {
            FrnkText(
                state =
                    FrnkTextState.BodySmall(
                        text =
                            "Every interactive atom above ripples on press by default — FrnkTheme installs " +
                                "the ripple as LocalIndication. Host apps apply the same ripple to their own " +
                                "components with rememberFrnkRipple().",
                        color = colorOnSurfaceVariant
                    )
            )
            val boundedRipple = remember { MutableInteractionSource() }
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .clip(Theme[shapes][shapeCard])
                        .background(Theme[colors][colorSurfaceVariant])
                        .clickable(
                            interactionSource = boundedRipple,
                            indication = rememberFrnkRipple()
                        ) { onEffect(DemoHomeEffect.Toast("Bounded ripple")) }
                        .padding(FrnkSpacing.md)
            ) {
                FrnkText(state = FrnkTextState.Body(text = "Custom card — bounded ripple (content color)"))
            }
            val unboundedRipple = remember { MutableInteractionSource() }
            Box(
                modifier =
                    Modifier
                        .clip(Theme[shapes][shapeCard])
                        .clickable(
                            interactionSource = unboundedRipple,
                            indication = rememberFrnkRipple(color = Theme[colors][colorPrimary], bounded = false)
                        ) { onEffect(DemoHomeEffect.Toast("Unbounded ripple")) }
                        .padding(FrnkSpacing.md)
            ) {
                FrnkText(state = FrnkTextState.Body(text = "Tap for an unbounded, primary-colored ripple"))
            }
        }

        "FrnkListRow" -> {
            FrnkText(
                state =
                    FrnkTextState.BodySmall(
                        text =
                            "Molecule: leading icon + title/subtitle + trailing slot. Tap a row for a " +
                                "ripple + haptic; the whole row collapses to a skeleton while loading.",
                        color = colorOnSurfaceVariant
                    )
            )
            FrnkListRow(
                state =
                    FrnkListRowState.Content(
                        title = "Notifications",
                        subtitle = "Push, email and in-app alerts",
                        icon = FrnkIconState.Content(Theme[icons][iconNotifications], contentDescription = null)
                    ),
                onClick = { onEffect(DemoHomeEffect.Toast("Tapped Notifications")) },
                trailing = {
                    FrnkIcon(
                        state =
                            FrnkIconState.Content(
                                imageVector = Theme[icons][iconChevronRight],
                                contentDescription = null,
                                tint = colorOnSurfaceVariant
                            )
                    )
                }
            )
            FrnkListRow(
                state = FrnkListRowState.Content(title = "Title only, non-interactive")
            )
            FrnkText(state = FrnkTextState.BodySmall(text = "Skeleton", color = colorOnSurfaceVariant))
            FrnkListRow(state = FrnkListRowState.Skeleton, onClick = {})
        }

        "FrnkSwipeable" -> {
            FrnkText(
                state =
                    FrnkTextState.BodySmall(
                        text =
                            "Optional swipe-to-action on any row. Reveal holds open a row of buttons " +
                                "(tap one, or tap the row to close); Dismiss fires on release past the " +
                                "threshold then snaps back. Headless — no Material3.",
                        color = colorOnSurfaceVariant
                    )
            )
            val deleteAction =
                FrnkSwipeAction(
                    icon = FrnkIconState.Content(Theme[icons][iconError], contentDescription = "Delete"),
                    label = "Delete"
                )
            val archiveAction =
                FrnkSwipeAction(
                    icon = FrnkIconState.Content(Theme[icons][iconRestore], contentDescription = "Archive"),
                    containerColor = colorSuccess,
                    contentColor = colorOnSuccess,
                    label = "Archive"
                )
            FrnkText(state = FrnkTextState.BodySmall(text = "Reveal (drag left)", color = colorOnSurfaceVariant))
            FrnkListRow(
                state =
                    FrnkListRowState.Content(
                        title = "Project Apollo",
                        subtitle = "Swipe left to reveal actions",
                        icon = FrnkIconState.Content(Theme[icons][iconNotifications], contentDescription = null)
                    ),
                onClick = { onEffect(DemoHomeEffect.Toast("Tapped Project Apollo")) },
                swipe =
                    FrnkSwipeableState(
                        behavior = FrnkSwipeBehavior.Reveal,
                        direction = FrnkSwipeDirection.Right,
                        rightActions = listOf(archiveAction, deleteAction)
                    ),
                onSwipeAction = { onEffect(DemoHomeEffect.Toast("${it.key} (reveal)")) }
            )
            FrnkText(state = FrnkTextState.BodySmall(text = "Dismiss (drag left)", color = colorOnSurfaceVariant))
            FrnkListRow(
                state =
                    FrnkListRowState.Content(
                        title = "Swipe-to-delete",
                        subtitle = "Release past the threshold to fire",
                        icon = FrnkIconState.Content(Theme[icons][iconNotifications], contentDescription = null)
                    ),
                swipe =
                    FrnkSwipeableState(
                        behavior = FrnkSwipeBehavior.Dismiss,
                        direction = FrnkSwipeDirection.Right,
                        rightActions = listOf(deleteAction)
                    ),
                onSwipeAction = { onEffect(DemoHomeEffect.Toast("${it.key} (dismiss)")) }
            )
        }

        "FrnkLabeledValue" -> {
            FrnkText(
                state =
                    FrnkTextState.BodySmall(
                        text =
                            "Molecule: a muted label paired with a value. Inline pushes the value to the " +
                                "end; Stacked sits it below. The value carries the skeleton while loading.",
                        color = colorOnSurfaceVariant
                    )
            )
            FrnkLabeledValue(state = FrnkLabeledValueState.Content(label = "Plan", value = "Pro"))
            FrnkDivider(state = FrnkDividerState.Horizontal())
            FrnkLabeledValue(state = FrnkLabeledValueState.Content(label = "Renews", value = "Jun 2026"))
            FrnkDivider(state = FrnkDividerState.Horizontal())
            FrnkLabeledValue(
                state =
                    FrnkLabeledValueState.Content(
                        label = "Storage used",
                        value = "4.2 GB",
                        orientation = FrnkLabeledValueOrientation.Stacked
                    )
            )
            FrnkText(state = FrnkTextState.BodySmall(text = "Skeleton", color = colorOnSurfaceVariant))
            FrnkLabeledValue(state = FrnkLabeledValueState.Skeleton)
        }

        "FrnkEmptyState" -> {
            FrnkText(
                state =
                    FrnkTextState.BodySmall(
                        text =
                            "Molecule: centered icon + title + subtitle + optional action button. A terminal " +
                                "zero-content state, so it has no skeleton (you'd skeletonize the eventual content " +
                                "instead). The action button brings its own ripple + haptic.",
                        color = colorOnSurfaceVariant
                    )
            )
            FrnkEmptyState(
                state =
                    FrnkEmptyStateState(
                        icon =
                            FrnkIconState.Content(
                                imageVector = Theme[icons][iconSearch],
                                contentDescription = null,
                                size = FrnkIconSize.emptyState,
                                tint = colorOnSurfaceVariant
                            ),
                        title = "No results",
                        subtitle = "Try adjusting your search to find what you're looking for.",
                        actionLabel = "Clear search"
                    ),
                onActionClick = { onEffect(DemoHomeEffect.Toast("Cleared search")) }
            )
        }

        "FrnkListSection" -> {
            FrnkText(
                state =
                    FrnkTextState.BodySmall(
                        text =
                            "Organism: an optional title + a surface card stacking FrnkListRow molecules " +
                                "separated by dividers, animating its height as rows change. Tap a row for a " +
                                "ripple + haptic; per-row skeletons collapse each row independently.",
                        color = colorOnSurfaceVariant
                    )
            )
            val sectionRows =
                listOf(
                    FrnkListRowState.Content(
                        title = "Notifications",
                        subtitle = "Push, email and in-app alerts",
                        icon = FrnkIconState.Content(Theme[icons][iconNotifications], contentDescription = null)
                    ),
                    FrnkListRowState.Content(
                        title = "Preferences",
                        subtitle = "Theme, language and units",
                        icon = FrnkIconState.Content(Theme[icons][iconNavSettings], contentDescription = null)
                    )
                )
            FrnkListSection(
                state =
                    FrnkListSectionState(
                        title = "Account",
                        rows = sectionRows,
                        footnote = "Manage how you're notified across devices."
                    ),
                onRowClick = { index -> onEffect(DemoHomeEffect.Toast("Tapped row $index")) },
                trailing = {
                    FrnkIcon(
                        state =
                            FrnkIconState.Content(
                                imageVector = Theme[icons][iconChevronRight],
                                contentDescription = null,
                                tint = colorOnSurfaceVariant
                            )
                    )
                }
            )
            FrnkText(state = FrnkTextState.BodySmall(text = "Skeleton", color = colorOnSurfaceVariant))
            FrnkListSection(
                state =
                    FrnkListSectionState(
                        title = "Account",
                        rows = List(3) { FrnkListRowState.Skeleton }
                    )
            )
        }

        "FrnkProfileHeader" -> {
            FrnkText(
                state =
                    FrnkTextState.BodySmall(
                        text =
                            "Organism: a circular avatar + name/subtitle, with an even row of " +
                                "FrnkLabeledValue stat tiles below. The skeleton flag passes through to every " +
                                "child (avatar, name, subtitle, each stat value).",
                        color = colorOnSurfaceVariant
                    )
            )
            val avatar =
                FrnkIconState.Content(
                    imageVector = Theme[icons][iconNavSettings],
                    contentDescription = null,
                    size = FrnkIconSize.lg,
                    tint = colorOnPrimaryContainer
                )
            val stats =
                listOf(
                    FrnkLabeledValueState.Content(label = "Projects", value = "12"),
                    FrnkLabeledValueState.Content(label = "Streak", value = "48d"),
                    FrnkLabeledValueState.Content(label = "Plan", value = "Pro")
                )
            FrnkProfileHeader(
                state =
                    FrnkProfileHeaderState.Content(
                        name = "Juan Diego",
                        subtitle = "juandiego@example.com",
                        avatar = avatar,
                        stats = stats
                    )
            )
            FrnkText(state = FrnkTextState.BodySmall(text = "No stats", color = colorOnSurfaceVariant))
            FrnkProfileHeader(
                state =
                    FrnkProfileHeaderState.Content(
                        name = "Juan Diego",
                        subtitle = "Free plan",
                        avatar = avatar
                    )
            )
            FrnkText(state = FrnkTextState.BodySmall(text = "Skeleton", color = colorOnSurfaceVariant))
            FrnkProfileHeader(state = FrnkProfileHeaderState.Skeleton)
        }

        "FrnkModalSheet" -> ModalSheetDemo()

        "FrnkHeadlineTopBar" ->
            HeadlineTopBarDemo(
                searchActive = state.searchActive,
                searchQuery = state.searchQuery,
                onIntent = onIntent
            )

        "FrnkStackShell" -> StackShellDemo()

        "FrnkDialog" -> DialogDemo()

        "NoSubscriptionFoundDialog" -> NoSubscriptionFoundDemo()

        else ->
            FrnkText(
                state =
                    FrnkTextState.Body(
                        text = "Unknown component \"$name\".",
                        color = colorOnSurfaceVariant
                    )
            )
    }
}

@Composable
private fun ModalSheetDemo() {
    var basicVisible by remember { mutableStateOf(false) }
    var lockedVisible by remember { mutableStateOf(false) }
    FrnkText(
        state =
            FrnkTextState.BodySmall(
                text =
                    "A modal sheet over the screen. The second one vetoes every user exit (back, swipe, " +
                        "outside tap) with canDismiss = { false }; its button closes it from the host.",
                color = colorOnSurfaceVariant
            )
    )
    FrnkButton(
        state = FrnkButtonState.Content(text = "Present sheet"),
        onClick = { basicVisible = true }
    )
    FrnkButton(
        state = FrnkButtonState.Content(text = "Present non-dismissible sheet", variant = FrnkButtonVariant.Outlined),
        onClick = { lockedVisible = true }
    )
    FrnkModalSheet(
        visible = basicVisible,
        onDismiss = { basicVisible = false },
        heightFraction = 0.5f,
        dismissOnClickOutside = true,
        surfaceColor = Theme[colors][colorSurfaceVariant]
    ) {
        FrnkModalSheetCloseHandler(contentDescription = "Close sheet", onClose = { basicVisible = false })
        FrnkSheetHeader(
            title = "Sheet",
            eyebrow = "Demo",
            body = "Swipe down, tap outside, press back or tap the close button to dismiss."
        )
    }
    FrnkModalSheet(
        visible = lockedVisible,
        onDismiss = {},
        heightFraction = 0.4f,
        surfaceColor = Theme[colors][colorSurfaceVariant],
        // Vetoes only the user's gestures; the host's own hide (the button below) is never refused.
        canDismiss = { false }
    ) {
        FrnkSheetHeader(
            title = "Locked sheet",
            body = "Back, swipe and outside taps are all refused. Only the button below closes it."
        )
        FrnkButton(
            state = FrnkButtonState.Content(text = "Close"),
            onClick = { lockedVisible = false },
            modifier = Modifier.padding(horizontal = FrnkSpacing.md)
        )
    }
}

@Composable
private fun HeadlineTopBarDemo(
    searchActive: Boolean,
    searchQuery: String,
    onIntent: (DemoHomeIntent) -> Unit
) {
    var sortEnabled by remember { mutableStateOf(true) }
    var lastAction by remember { mutableStateOf("none") }
    FrnkText(
        state =
            FrnkTextState.BodySmall(
                text = "Search trailing + PRO badge + add action (search state lives in the demo's view model).",
                color = colorOnSurfaceVariant
            )
    )
    FrnkHeadlineTopBar(
        state =
            FrnkHeadlineTopBarState(
                title = FrnkStringSource.Raw("Library"),
                leading =
                    FrnkHeadlineAction(
                        key = "settings",
                        icon = FrnkIconSource.Token(iconNavSettings),
                        contentDescription = FrnkStringSource.Raw("Settings")
                    ),
                badge =
                    FrnkHeadlineBadge(
                        key = "pro",
                        label = FrnkStringSource.Raw("PRO"),
                        contentDescription = FrnkStringSource.Raw("Upgrade to Pro")
                    ),
                actions =
                    listOf(
                        FrnkHeadlineAction(
                            key = "add",
                            icon = FrnkIconSource.Token(iconCheck),
                            contentDescription = FrnkStringSource.Raw("Add")
                        )
                    ),
                trailing =
                    FrnkHeadlineTrailing.Search(
                        isActive = searchActive,
                        query = searchQuery,
                        openContentDescription = FrnkStringSource.Raw("Search"),
                        closeContentDescription = FrnkStringSource.Raw("Close search")
                    )
            ),
        onActionClick = { lastAction = it },
        onSearchOpen = { onIntent(DemoHomeIntent.SearchOpened) },
        onSearchQueryChange = { onIntent(DemoHomeIntent.SearchQueryChanged(it)) },
        onSearchClose = { onIntent(DemoHomeIntent.SearchClosed) }
    )
    FrnkText(
        state =
            FrnkTextState.BodySmall(
                text = "Action trailing (\"Sort\"), never searches. Toggle enabled below.",
                color = colorOnSurfaceVariant
            )
    )
    FrnkHeadlineTopBar(
        state =
            FrnkHeadlineTopBarState(
                title = FrnkStringSource.Raw("Activity"),
                leading =
                    FrnkHeadlineAction(
                        key = "settings",
                        icon = FrnkIconSource.Token(iconNavSettings),
                        contentDescription = FrnkStringSource.Raw("Settings")
                    ),
                trailing =
                    FrnkHeadlineTrailing.Action(
                        FrnkHeadlineAction(
                            key = "sort",
                            icon = FrnkIconSource.Token(iconRestore),
                            contentDescription = FrnkStringSource.Raw("Sort"),
                            enabled = sortEnabled
                        )
                    )
            ),
        onActionClick = { lastAction = it }
    )
    FrnkButton(
        state =
            FrnkButtonState.Content(
                text = if (sortEnabled) "Disable Sort" else "Enable Sort",
                variant = FrnkButtonVariant.Outlined
            ),
        onClick = { sortEnabled = !sortEnabled }
    )
    FrnkText(state = FrnkTextState.BodySmall(text = "Last action: $lastAction", color = colorOnSurfaceVariant))
}

@Serializable
private sealed interface DemoShellRoute : NavKey {
    @Serializable
    data object Home : DemoShellRoute

    @Serializable
    data object Detail : DemoShellRoute
}

private const val DEMO_SHEET_KEY = "demo-sheet"

private val demoShellRoutes =
    SerializersModule {
        polymorphic(NavKey::class) {
            subclass(DemoShellRoute.Home::class)
            subclass(DemoShellRoute.Detail::class)
        }
    }

private val demoShellSheets =
    listOf(
        FrnkShellSheet(key = DEMO_SHEET_KEY) { navigator ->
            FrnkModalSheetCloseHandler(contentDescription = "Close sheet", onClose = { navigator.dismiss(DEMO_SHEET_KEY) })
            FrnkSheetHeader(title = "Shell sheet", body = "Held above the stack by the shell; back closes it.")
        }
    )

@Composable
private fun StackShellDemo() {
    FrnkText(
        state =
            FrnkTextState.BodySmall(
                text = "An embedded FrnkStackShell: two routes on one back stack and one sheet the shell holds above it.",
                color = colorOnSurfaceVariant
            )
    )
    FrnkStackShell(
        startRoute = DemoShellRoute.Home,
        hostRoutes = demoShellRoutes,
        sheets = demoShellSheets,
        modifier = Modifier.height(320.dp).clip(Theme[shapes][shapeCard])
    ) { navigator ->
        entry<DemoShellRoute.Home> { ShellPage("Home", "Push detail", navigator, DemoShellRoute.Detail) }
        entry<DemoShellRoute.Detail> { ShellPage("Detail", "Back", navigator, null) }
    }
}

@Composable
private fun ShellPage(
    title: String,
    primaryLabel: String,
    navigator: FrnkShellNavigator,
    next: NavKey?
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .background(Theme[colors][colorSurfaceVariant])
                .padding(FrnkSpacing.md),
        verticalArrangement = Arrangement.spacedBy(FrnkSpacing.sm)
    ) {
        FrnkText(state = FrnkTextState.Title(text = title))
        FrnkButton(
            state = FrnkButtonState.Content(text = primaryLabel),
            onClick = { if (next != null) navigator.push(next) else navigator.back() }
        )
        FrnkButton(
            state = FrnkButtonState.Content(text = "Present sheet", variant = FrnkButtonVariant.Outlined),
            onClick = { navigator.present(DEMO_SHEET_KEY) }
        )
    }
}

/** Height of the in-place stage the dialog demos draw into (the demo screen scrolls, so no full-screen overlay). */
private val DialogStageHeight = 520.dp

@Composable
private fun DialogDemo() {
    var shown by remember { mutableStateOf<FrnkDialogVariant?>(null) }
    var lastAction by remember { mutableStateOf("none") }
    FrnkText(
        state =
            FrnkTextState.BodySmall(
                text =
                    "FrnkDialog draws in the composition over a scrim that swallows taps. Here it fills the " +
                        "stage below; an app composes it last in its shell. Last action: $lastAction",
                color = colorOnSurfaceVariant
            )
    )
    FrnkDialogVariant.entries.forEach { variant ->
        FrnkButton(
            state = FrnkButtonState.Content(text = "Show $variant", variant = FrnkButtonVariant.Outlined),
            onClick = { shown = variant }
        )
    }
    val variant = shown ?: return
    val cancel = FrnkDialogAction(key = "cancel", label = FrnkStringSource.Token(stringCancel), kind = FrnkDialogActionKind.Cancel)
    val answer =
        when (variant) {
            FrnkDialogVariant.Destructive ->
                FrnkDialogAction(key = "delete", label = FrnkStringSource.Raw("Delete"), kind = FrnkDialogActionKind.Destructive)
            else -> FrnkDialogAction(key = "ok", label = FrnkStringSource.Raw("OK"), kind = FrnkDialogActionKind.Primary)
        }
    Box(Modifier.fillMaxWidth().height(DialogStageHeight)) {
        FrnkDialog(
            state =
                FrnkDialogState(
                    title = FrnkStringSource.Raw("$variant dialog"),
                    eyebrow = FrnkStringSource.Raw("Demo"),
                    body = FrnkStringSource.Raw("Tap an action to close it. Taps on the scrim do nothing."),
                    variant = variant,
                    actions = if (variant == FrnkDialogVariant.Warning) listOf(answer, cancel) else listOf(cancel, answer),
                    actionLayout =
                        if (variant == FrnkDialogVariant.Warning) FrnkDialogActionLayout.Stacked else FrnkDialogActionLayout.Row
                ),
            onAction = { action ->
                lastAction = action.key
                shown = null
            }
        )
    }
}

@Composable
private fun NoSubscriptionFoundDemo() {
    var store by remember { mutableStateOf<FrnkAppStore?>(null) }
    FrnkText(
        state =
            FrnkTextState.BodySmall(
                text =
                    "The empty-restore dialog. This build ships through $frnkAppStore; " +
                        "the other store's wording is shown for comparison.",
                color = colorOnSurfaceVariant
            )
    )
    FrnkAppStore.entries.forEach { option ->
        FrnkButton(
            state = FrnkButtonState.Content(text = "Show ($option)", variant = FrnkButtonVariant.Outlined),
            onClick = { store = option }
        )
    }
    val selected = store ?: return
    Box(Modifier.fillMaxWidth().height(DialogStageHeight)) {
        NoSubscriptionFoundDialog(onDismiss = { store = null }, store = selected)
    }
}