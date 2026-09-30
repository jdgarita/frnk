package dev.jdgarita.frnk.ui.atoms.previews

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.jdgarita.frnk.ui.atoms.FrnkDialog
import dev.jdgarita.frnk.ui.atoms.FrnkDialogAction
import dev.jdgarita.frnk.ui.atoms.FrnkDialogActionKind
import dev.jdgarita.frnk.ui.atoms.FrnkDialogActionLayout
import dev.jdgarita.frnk.ui.atoms.FrnkDialogState
import dev.jdgarita.frnk.ui.atoms.FrnkDialogSurface
import dev.jdgarita.frnk.ui.atoms.FrnkDialogVariant
import dev.jdgarita.frnk.ui.theme.Appearance
import dev.jdgarita.frnk.ui.theme.FrnkIconSource
import dev.jdgarita.frnk.ui.theme.FrnkStringSource
import dev.jdgarita.frnk.ui.theme.iconReceipt
import dev.jdgarita.frnk.ui.theme.stringCancel

private fun action(
    key: String,
    label: String,
    kind: FrnkDialogActionKind
) = FrnkDialogAction(key = key, label = FrnkStringSource.Raw(label), kind = kind)

private val cancel = FrnkDialogAction(key = "cancel", label = FrnkStringSource.Token(stringCancel), kind = FrnkDialogActionKind.Cancel)

private val accentState =
    FrnkDialogState(
        title = FrnkStringSource.Raw("Archive this item?"),
        eyebrow = FrnkStringSource.Raw("Archive"),
        body = FrnkStringSource.Raw("It leaves your list but stays in your history. You can restore it anytime."),
        variant = FrnkDialogVariant.Accent,
        actions = listOf(cancel, action("archive", "Archive", FrnkDialogActionKind.Primary))
    )

@Preview(widthDp = 390)
@Composable
private fun FrnkDialog_Accent_Light() {
    PreviewSurface(appearance = Appearance.Light) {
        FrnkDialogSurface(state = accentState, onAction = {})
    }
}

@Preview(widthDp = 390)
@Composable
private fun FrnkDialog_Destructive_Dark() {
    PreviewSurface(appearance = Appearance.Dark) {
        FrnkDialogSurface(
            state =
                FrnkDialogState(
                    title = FrnkStringSource.Raw("Delete item?"),
                    eyebrow = FrnkStringSource.Raw("Destructive"),
                    body = FrnkStringSource.Raw("This permanently removes the item. This can't be undone."),
                    variant = FrnkDialogVariant.Destructive,
                    actions = listOf(cancel, action("delete", "Delete", FrnkDialogActionKind.Destructive))
                ),
            onAction = {}
        )
    }
}

@Preview(widthDp = 390)
@Composable
private fun FrnkDialog_Warning_Stacked_Light() {
    PreviewSurface(appearance = Appearance.Light) {
        FrnkDialogSurface(
            state =
                FrnkDialogState(
                    title = FrnkStringSource.Raw("Couldn't read the label"),
                    body = FrnkStringSource.Raw("Try better lighting or enter the details by hand."),
                    variant = FrnkDialogVariant.Warning,
                    actionLayout = FrnkDialogActionLayout.Stacked,
                    actions =
                        listOf(
                            action("retry", "Retry", FrnkDialogActionKind.Primary),
                            action("manual", "Enter manually", FrnkDialogActionKind.Cancel)
                        )
                ),
            onAction = {}
        )
    }
}

@Preview(widthDp = 390)
@Composable
private fun FrnkDialog_Success_Dark() {
    PreviewSurface(appearance = Appearance.Dark) {
        FrnkDialogSurface(
            state =
                FrnkDialogState(
                    title = FrnkStringSource.Raw("Saved"),
                    body = FrnkStringSource.Raw("Your changes are stored on this device."),
                    variant = FrnkDialogVariant.Success,
                    actions = listOf(action("done", "Done", FrnkDialogActionKind.Primary))
                ),
            onAction = {}
        )
    }
}

@Preview(widthDp = 390)
@Composable
private fun FrnkDialog_Neutral_CustomIcon_Light() {
    PreviewSurface(appearance = Appearance.Light) {
        FrnkDialogSurface(
            state =
                FrnkDialogState(
                    title = FrnkStringSource.Raw("No Subscription Found"),
                    body = FrnkStringSource.Raw("We couldn't find an active subscription on this account."),
                    variant = FrnkDialogVariant.Neutral,
                    icon = FrnkIconSource.Token(iconReceipt),
                    actions = listOf(action("ok", "Got It", FrnkDialogActionKind.Primary))
                ),
            onAction = {}
        )
    }
}

/** The full overlay: the scrim fills the space and the card sits centered over it. */
@Preview(widthDp = 390, heightDp = 640)
@Composable
private fun FrnkDialog_Overlay_Dark() {
    PreviewSurface(appearance = Appearance.Dark) {
        Box(Modifier.height(600.dp)) {
            FrnkDialog(state = accentState, onAction = {})
        }
    }
}