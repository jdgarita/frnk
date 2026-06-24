package dev.jdgarita.frnk.demo.ui.component

import androidx.compose.runtime.Composable
import kotlin.text.get

@Composable
fun ComponentScreen(
    name: String
    // state: DemoState,
    // onIntent: (DemoIntent) -> Unit,
    //  onEffect: (DemoEffect) -> Unit
) {
    // TODO: restore the component gallery — the whole when(name) showcase (~500 lines) is commented
    //  out pending the demo's MVI/nav rewrite. Re-enable per-component demos + the DemoState wiring.
//    when (name) {
//        "FrnkText" -> {
//            FrnkText(state = FrnkTextState.HeadlineSmall(text = "HeadlineSmall"))
//            FrnkText(state = FrnkTextState.Title(text = "Title"))
//            FrnkText(state = FrnkTextState.TitleMedium(text = "TitleMedium"))/**/
//            FrnkText(state = FrnkTextState.Body(text = "Body"))
//            FrnkText(state = FrnkTextState.BodyMedium(text = "BodyMedium"))
//            FrnkText(
//                state = FrnkTextState.BodySmall(text = "BodySmall", color = colorOnSurfaceVariant)
//            )
//            FrnkText(state = FrnkTextState.AppName(annotated = buildAnnotatedString { append("FrnkKit") }))
//            FrnkText(state = FrnkTextState.BodySmall(text = "Skeleton", color = colorOnSurfaceVariant))
//            FrnkText(
//                state = FrnkTextState.Title(text = "Loading title", skeleton = FrnkSkeleton(enabled = true))
//            )
//            FrnkText(
//                state =
//                    FrnkTextState.Body(
//                        text = "Loading a longer body line of text",
//                        skeleton = FrnkSkeleton(enabled = true)
//                    )
//            )
//        }
//
//        "FrnkButton" -> {
//            Row(horizontalArrangement = Arrangement.spacedBy(FrnkSpacing.sm)) {
//                FrnkButton(state = FrnkButtonState.Content(text = "Filled"), onClick = {})
//                FrnkButton(
//                    state = FrnkButtonState.Content(text = "Outlined", variant = FrnkButtonVariant.Outlined),
//                    onClick = {}
//                )
//                FrnkButton(
//                    state = FrnkButtonState.Content(text = "Ghost", variant = FrnkButtonVariant.Ghost),
//                    onClick = {}
//                )
//            }
//            Row(horizontalArrangement = Arrangement.spacedBy(FrnkSpacing.sm)) {
//                FrnkButton(
//                    state = FrnkButtonState.Content(text = "Filled", enabled = false),
//                    onClick = {}
//                )
//                FrnkButton(
//                    state =
//                        FrnkButtonState.Content(
//                            text = "Outlined",
//                            variant = FrnkButtonVariant.Outlined,
//                            enabled = false
//                        ),
//                    onClick = {}
//                )
//            }
//            FrnkText(state = FrnkTextState.BodySmall(text = "Skeleton", color = colorOnSurfaceVariant))
//            Row(horizontalArrangement = Arrangement.spacedBy(FrnkSpacing.sm)) {
//                FrnkButton(
//                    state = FrnkButtonState.Skeleton,
//                    onClick = {}
//                )
//                FrnkButton(
//                    state =
//                        FrnkButtonState.Skeleton,
//                    onClick = {}
//                )
//            }
//        }
//
//        "FrnkIcon / FrnkIconButton" -> {
//            Row(
//                horizontalArrangement = Arrangement.spacedBy(FrnkSpacing.md),
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//                FrnkIcon(
//                    state =
//                        FrnkIconState.Content(
//                            imageVector = Theme[icons][iconSearch],
//                            contentDescription = "Search",
//                            size = FrnkIconSize.md,
//                            tint = colorPrimary
//                        )
//                )
//                FrnkIcon(
//                    state =
//                        FrnkIconState.Content(
//                            imageVector = Theme[icons][iconCheck],
//                            contentDescription = "Check",
//                            size = FrnkIconSize.lg,
//                            tint = colorPrimary
//                        )
//                )
//                FrnkIconButton(
//                    state =
//                        FrnkIconButtonState.Content(
//                            imageVector = Theme[icons][iconSettings],
//                            contentDescription = "Settings",
//                            tint = colorOnBackground
//                        ),
//                    onClick = { onEffect(DemoEffect.Toast("Icon button tapped")) }
//                )
//            }
//            FrnkText(state = FrnkTextState.BodySmall(text = "Skeleton", color = colorOnSurfaceVariant))
//            Row(
//                horizontalArrangement = Arrangement.spacedBy(FrnkSpacing.md),
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//                FrnkIcon(
//                    state =
//                        FrnkIconState.Skeleton()
//                )
//                FrnkIconButton(
//                    state =
//                        FrnkIconButtonState.Skeleton,
//                    onClick = {}
//                )
//            }
//        }
//
//        "FrnkDivider" -> {
//            FrnkDivider(state = FrnkDividerState.Horizontal())
//            Row(
//                modifier = Modifier.height(24.dp),
//                horizontalArrangement = Arrangement.spacedBy(FrnkSpacing.sm),
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//                FrnkText(state = FrnkTextState.BodySmall(text = "Left"))
//                FrnkDivider(state = FrnkDividerState.Vertical())
//                FrnkText(state = FrnkTextState.BodySmall(text = "Right"))
//            }
//        }
//
//        "FrnkSwitch" -> {
//            Row(
//                horizontalArrangement = Arrangement.spacedBy(FrnkSpacing.md),
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//                FrnkSwitch(
//                    state = FrnkSwitchState.Content(checked = state.gallerySwitchOn),
//                    onCheckedChange = { onIntent(DemoIntent.GallerySwitchChanged(it)) }
//                )
//                FrnkText(state = FrnkTextState.BodySmall(text = if (state.gallerySwitchOn) "On" else "Off"))
//                FrnkSwitch(
//                    state = FrnkSwitchState.Content(checked = true, enabled = false),
//                    onCheckedChange = {}
//                )
//                FrnkText(state = FrnkTextState.BodySmall(text = "Disabled"))
//            }
//            FrnkText(state = FrnkTextState.BodySmall(text = "Skeleton", color = colorOnSurfaceVariant))
//            FrnkSwitch(
//                state = FrnkSwitchState.Skeleton,
//                onCheckedChange = {}
//            )
//        }
//
//        "FrnkSegmentedControl" -> {
//            FrnkSegmentedControl(
//                state =
//                    FrnkSegmentedControlState.Content(
//                        options = listOf("One", "Two", "Three"),
//                        selectedIndex = state.gallerySegmentIndex
//                    ),
//                onOptionSelected = { onIntent(DemoIntent.GallerySegmentChanged(it)) }
//            )
//            FrnkText(state = FrnkTextState.BodySmall(text = "Skeleton", color = colorOnSurfaceVariant))
//            FrnkSegmentedControl(
//                state =
//                    FrnkSegmentedControlState.Skeleton,
//                onOptionSelected = {}
//            )
//        }
//
//        "FrnkBottomFloatingBar" -> {
//            FrnkText(
//                state =
//                    FrnkTextState.BodySmall(
//                        text =
//                            "The real adaptive bar — the very same component shown at the foot of every screen " +
//                                    "(a Material3 floating pill on Android, a native glassy UITabBar on iOS).",
//                        color = colorOnSurfaceVariant
//                    )
//            )
//            FrnkBottomFloatingBar(
//                items =
//                    listOf(
//                        FrnkNavBarItem("a", Theme[icons][iconSearch], "magnifyingglass", "Search"),
//                        FrnkNavBarItem("b", Theme[icons][iconCheck], "checkmark", "Check"),
//                        FrnkNavBarItem("c", Theme[icons][iconSettings], "gearshape", "Settings")
//                    ),
//                selectedIndex = state.galleryNavIndex,
//                onItemSelected = { onIntent(DemoIntent.GalleryNavChanged(it)) },
//                modifier = Modifier.fillMaxWidth()
//            )
//        }
//
//        "Ripple" -> {
//            FrnkText(
//                state =
//                    FrnkTextState.BodySmall(
//                        text =
//                            "Every interactive atom above ripples on press by default — FrnkTheme installs " +
//                                    "the ripple as LocalIndication. Host apps apply the same ripple to their own " +
//                                    "components with rememberFrnkRipple().",
//                        color = colorOnSurfaceVariant
//                    )
//            )
//            val boundedRipple = remember { MutableInteractionSource() }
//            Box(
//                modifier =
//                    Modifier
//                        .fillMaxWidth()
//                        .clip(Theme[shapes][shapeCard])
//                        .background(Theme[colors][colorSurfaceVariant])
//                        .clickable(
//                            interactionSource = boundedRipple,
//                            indication = rememberFrnkRipple()
//                        ) { onEffect(DemoEffect.Toast("Bounded ripple")) }
//                        .padding(FrnkSpacing.md)
//            ) {
//                FrnkText(state = FrnkTextState.Body(text = "Custom card — bounded ripple (content color)"))
//            }
//            val unboundedRipple = remember { MutableInteractionSource() }
//            Box(
//                modifier =
//                    Modifier
//                        .clip(Theme[shapes][shapeCard])
//                        .clickable(
//                            interactionSource = unboundedRipple,
//                            indication = rememberFrnkRipple(color = Theme[colors][colorPrimary], bounded = false)
//                        ) { onEffect(DemoEffect.Toast("Unbounded ripple")) }
//                        .padding(FrnkSpacing.md)
//            ) {
//                FrnkText(state = FrnkTextState.Body(text = "Tap for an unbounded, primary-colored ripple"))
//            }
//        }
//
//        "FrnkListRow" -> {
//            FrnkText(
//                state =
//                    FrnkTextState.BodySmall(
//                        text =
//                            "Molecule: leading icon + title/subtitle + trailing slot. Tap a row for a " +
//                                    "ripple + haptic; the whole row collapses to a skeleton while loading.",
//                        color = colorOnSurfaceVariant
//                    )
//            )
//            FrnkListRow(
//                state =
//                    FrnkListRowState.Content(
//                        title = "Notifications",
//                        subtitle = "Push, email and in-app alerts",
//                        icon = FrnkIconState.Content(Theme[icons][iconNotifications], contentDescription = null)
//                    ),
//                onClick = { onEffect(DemoEffect.Toast("Tapped Notifications")) },
//                trailing = {
//                    FrnkIcon(
//                        state =
//                            FrnkIconState.Content(
//                                imageVector = Theme[icons][iconChevronRight],
//                                contentDescription = null,
//                                tint = colorOnSurfaceVariant
//                            )
//                    )
//                }
//            )
//            FrnkListRow(
//                state = FrnkListRowState.Content(title = "Title only, non-interactive")
//            )
//            FrnkText(state = FrnkTextState.BodySmall(text = "Skeleton", color = colorOnSurfaceVariant))
//            FrnkListRow(
//                state =
//                    FrnkListRowState.Skeleton,
//                onClick = {}
//            )
//        }
//
//        "FrnkSwipeable" -> {
//            FrnkText(
//                state =
//                    FrnkTextState.BodySmall(
//                        text =
//                            "Optional swipe-to-action on any row. Reveal holds open a row of buttons " +
//                                    "(tap one, or tap the row to close); Dismiss fires on release past the " +
//                                    "threshold then snaps back. Headless — no Material3.",
//                        color = colorOnSurfaceVariant
//                    )
//            )
//            val deleteAction =
//                FrnkSwipeAction(
//                    icon = FrnkIconState.Content(Theme[icons][iconError], contentDescription = "Delete"),
//                    label = "Delete"
//                )
//            val archiveAction =
//                FrnkSwipeAction(
//                    icon = FrnkIconState.Content(Theme[icons][iconRestore], contentDescription = "Archive"),
//                    containerColor = colorSuccess,
//                    contentColor = colorOnSuccess,
//                    label = "Archive"
//                )
//            FrnkText(state = FrnkTextState.BodySmall(text = "Reveal (drag left)", color = colorOnSurfaceVariant))
//            FrnkListRow(
//                state =
//                    FrnkListRowState.Content(
//                        title = "Project Apollo",
//                        subtitle = "Swipe left to reveal actions",
//                        icon = FrnkIconState.Content(Theme[icons][iconNotifications], contentDescription = null)
//                    ),
//                onClick = { onEffect(DemoEffect.Toast("Tapped Project Apollo")) },
//                swipe =
//                    FrnkSwipeableState(
//                        behavior = FrnkSwipeBehavior.Reveal,
//                        direction = FrnkSwipeDirection.Right,
//                        rightActions = listOf(archiveAction, deleteAction)
//                    ),
//                onSwipeAction = { onEffect(DemoEffect.Toast("${it.key} (reveal)")) }
//            )
//            FrnkText(state = FrnkTextState.BodySmall(text = "Dismiss (drag left)", color = colorOnSurfaceVariant))
//            FrnkListRow(
//                state =
//                    FrnkListRowState.Content(
//                        title = "Swipe-to-delete",
//                        subtitle = "Release past the threshold to fire",
//                        icon = FrnkIconState.Content(Theme[icons][iconNotifications], contentDescription = null)
//                    ),
//                swipe =
//                    FrnkSwipeableState(
//                        behavior = FrnkSwipeBehavior.Dismiss,
//                        direction = FrnkSwipeDirection.Right,
//                        rightActions = listOf(deleteAction)
//                    ),
//                onSwipeAction = { onEffect(DemoEffect.Toast("${it.key} (dismiss)")) }
//            )
//        }
//
//        "FrnkLabeledValue" -> {
//            FrnkText(
//                state =
//                    FrnkTextState.BodySmall(
//                        text =
//                            "Molecule: a muted label paired with a value. Inline pushes the value to the " +
//                                    "end; Stacked sits it below. The value carries the skeleton while loading.",
//                        color = colorOnSurfaceVariant
//                    )
//            )
//            FrnkLabeledValue(state = FrnkLabeledValueState.Content(label = "Plan", value = "Pro"))
//            FrnkDivider(state = FrnkDividerState.Horizontal())
//            FrnkLabeledValue(state = FrnkLabeledValueState.Content(label = "Renews", value = "Jun 2026"))
//            FrnkDivider(state = FrnkDividerState.Horizontal())
//            FrnkLabeledValue(
//                state =
//                    FrnkLabeledValueState.Content(
//                        label = "Storage used",
//                        value = "4.2 GB",
//                        orientation = FrnkLabeledValueOrientation.Stacked
//                    )
//            )
//            FrnkText(state = FrnkTextState.BodySmall(text = "Skeleton", color = colorOnSurfaceVariant))
//            FrnkLabeledValue(
//                state =
//                    FrnkLabeledValueState.Skeleton
//            )
//        }
//
//        "FrnkEmptyState" -> {
//            FrnkText(
//                state =
//                    FrnkTextState.BodySmall(
//                        text =
//                            "Molecule: centered icon + title + subtitle + optional action button. A terminal " +
//                                    "zero-content state, so it has no skeleton (you'd skeletonize the eventual content " +
//                                    "instead). The action button brings its own ripple + haptic.",
//                        color = colorOnSurfaceVariant
//                    )
//            )
//            FrnkEmptyState(
//                state =
//                    FrnkEmptyStateState(
//                        icon =
//                            FrnkIconState.Content(
//                                imageVector = Theme[icons][iconSearch],
//                                contentDescription = null,
//                                size = FrnkIconSize.emptyState,
//                                tint = colorOnSurfaceVariant
//                            ),
//                        title = "No results",
//                        subtitle = "Try adjusting your search to find what you're looking for.",
//                        actionLabel = "Clear search"
//                    ),
//                onActionClick = { onEffect(DemoEffect.Toast("Cleared search")) }
//            )
//        }
//
//        "FrnkListSection" -> {
//            FrnkText(
//                state =
//                    FrnkTextState.BodySmall(
//                        text =
//                            "Organism: an optional title + a surface card stacking FrnkListRow molecules " +
//                                    "separated by dividers, animating its height as rows change. Tap a row for a " +
//                                    "ripple + haptic; per-row skeletons collapse each row independently.",
//                        color = colorOnSurfaceVariant
//                    )
//            )
//            val sectionRows =
//                listOf(
//                    FrnkListRowState.Content(
//                        title = "Notifications",
//                        subtitle = "Push, email and in-app alerts",
//                        icon = FrnkIconState.Content(Theme[icons][iconNotifications], contentDescription = null)
//                    ),
//                    FrnkListRowState.Content(
//                        title = "Preferences",
//                        subtitle = "Theme, language and units",
//                        icon = FrnkIconState.Content(Theme[icons][iconSettings], contentDescription = null)
//                    )
//                )
//            FrnkListSection(
//                state =
//                    FrnkListSectionState(
//                        title = "Account",
//                        rows = sectionRows,
//                        footnote = "Manage how you're notified across devices."
//                    ),
//                onRowClick = { index -> onEffect(DemoEffect.Toast("Tapped row $index")) },
//                trailing = {
//                    FrnkIcon(
//                        state =
//                            FrnkIconState.Content(
//                                imageVector = Theme[icons][iconChevronRight],
//                                contentDescription = null,
//                                tint = colorOnSurfaceVariant
//                            )
//                    )
//                }
//            )
//            FrnkText(state = FrnkTextState.BodySmall(text = "Skeleton", color = colorOnSurfaceVariant))
//            FrnkListSection(
//                state =
//                    FrnkListSectionState(
//                        title = "Account",
//                        rows = List(3) { FrnkListRowState.Skeleton }
//                    )
//            )
//        }
//
//        "FrnkProfileHeader" -> {
//            FrnkText(
//                state =
//                    FrnkTextState.BodySmall(
//                        text =
//                            "Organism: a circular avatar + name/subtitle, with an even row of " +
//                                    "FrnkLabeledValue stat tiles below. The skeleton flag passes through to every " +
//                                    "child (avatar, name, subtitle, each stat value).",
//                        color = colorOnSurfaceVariant
//                    )
//            )
//            val avatar =
//                FrnkIconState.Content(
//                    imageVector = Theme[icons][iconSettings],
//                    contentDescription = null,
//                    size = FrnkIconSize.lg,
//                    tint = colorOnPrimaryContainer
//                )
//            val stats =
//                listOf(
//                    FrnkLabeledValueState.Content(label = "Projects", value = "12"),
//                    FrnkLabeledValueState.Content(label = "Streak", value = "48d"),
//                    FrnkLabeledValueState.Content(label = "Plan", value = "Pro")
//                )
//            FrnkProfileHeader(
//                state =
//                    FrnkProfileHeaderState.Content(
//                        name = "Juan Diego",
//                        subtitle = "juandiego@example.com",
//                        avatar = avatar,
//                        stats = stats
//                    )
//            )
//            FrnkText(state = FrnkTextState.BodySmall(text = "No stats", color = colorOnSurfaceVariant))
//            FrnkProfileHeader(
//                state =
//                    FrnkProfileHeaderState.Content(
//                        name = "Juan Diego",
//                        subtitle = "Free plan",
//                        avatar = avatar
//                    )
//            )
//            FrnkText(state = FrnkTextState.BodySmall(text = "Skeleton", color = colorOnSurfaceVariant))
//            FrnkProfileHeader(
//                state =
//                    FrnkProfileHeaderState.Skeleton
//            )
//        }
//
//        else ->
//            FrnkText(
//                state =
//                    FrnkTextState.Body(
//                        text = "Unknown component \"$name\".",
//                        color = colorOnSurfaceVariant
//                    )
//            )
//    }
}