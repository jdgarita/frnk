package dev.jdgarita.frnk.monetization.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import com.composeunstyled.theme.Theme
import dev.jdgarita.frnk.monetization.ui.ext.noSubscriptionFoundBodyToken
import dev.jdgarita.frnk.ui.atoms.FrnkDialog
import dev.jdgarita.frnk.ui.atoms.FrnkDialogAction
import dev.jdgarita.frnk.ui.atoms.FrnkDialogActionKind
import dev.jdgarita.frnk.ui.atoms.FrnkDialogState
import dev.jdgarita.frnk.ui.atoms.FrnkDialogVariant
import dev.jdgarita.frnk.ui.theme.FrnkIconSource
import dev.jdgarita.frnk.ui.theme.FrnkStringSource
import dev.jdgarita.frnk.ui.theme.ext.resolve
import dev.jdgarita.frnk.ui.theme.iconReceipt
import dev.jdgarita.frnk.ui.theme.stringAppName
import dev.jdgarita.frnk.ui.theme.stringNoSubscriptionFoundConfirm
import dev.jdgarita.frnk.ui.theme.stringNoSubscriptionFoundTitle
import dev.jdgarita.frnk.ui.theme.stringProName
import dev.jdgarita.frnk.ui.theme.strings

/** The placeholder the No Subscription Found body tokens carry for the product name. */
internal const val PRODUCT_PLACEHOLDER = "{product}"

/** The action key of the dialog's single "Got It" button. */
const val NO_SUBSCRIPTION_FOUND_CONFIRM_KEY = "no_subscription_found_confirm"

/**
 * A restore came back empty: the store answered, and no subscription receipt is attached to the
 * account the device is signed into. Not an error — the usual cause is a new user tapping Restore
 * before ever buying — so it is a neutral [FrnkDialog] with a receipt glyph rather than a warning, and
 * its single action only acknowledges it. Upstreamed from Faint.
 *
 * The body names the **one** store this build ships through ([store]: the App Store on iOS, Google
 * Play on Android, never both) and the product the user would have bought ([productName], "<app name>
 * <pro name>" from the theme's strings by default, e.g. "Still Pro"). Copy comes from the
 * `stringNoSubscriptionFound*` tokens (EN + ES bundled); a host overriding a body token keeps the
 * `{product}` placeholder where the product name goes.
 *
 * Like every [FrnkDialog] it is drawn in the composition: compose it last in a full-screen `Box`
 * while the host's state says it is shown, and drop it on [onDismiss].
 *
 * @param onShown called once per presentation (not per recomposition), for the host's analytics — e.g.
 *   `analytics.trackCustom("restore_no_subscription_shown", mapOf("source" to "settings"))`. The
 *   dialog records nothing itself.
 * @param store which store's wording to use; defaults to this build's [frnkAppStore]. A parameter so a
 *   test or preview can render the other one.
 */
@Composable
fun NoSubscriptionFoundDialog(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    productName: FrnkStringSource =
        FrnkStringSource.Composite(listOf(FrnkStringSource.Token(stringAppName), FrnkStringSource.Token(stringProName))),
    store: FrnkAppStore = frnkAppStore,
    onShown: () -> Unit = {}
) {
    val currentOnShown by rememberUpdatedState(onShown)
    // Keyed on nothing: a recomposition never re-reports. The dialog leaves the composition on dismiss,
    // so the next empty restore reports again.
    LaunchedEffect(Unit) { currentOnShown() }

    val body = Theme[strings][store.noSubscriptionFoundBodyToken()].replace(PRODUCT_PLACEHOLDER, productName.resolve())
    FrnkDialog(
        state =
            FrnkDialogState(
                title = FrnkStringSource.Token(stringNoSubscriptionFoundTitle),
                body = FrnkStringSource.Raw(body),
                variant = FrnkDialogVariant.Neutral,
                icon = FrnkIconSource.Token(iconReceipt),
                actions =
                    listOf(
                        FrnkDialogAction(
                            key = NO_SUBSCRIPTION_FOUND_CONFIRM_KEY,
                            label = FrnkStringSource.Token(stringNoSubscriptionFoundConfirm),
                            kind = FrnkDialogActionKind.Primary
                        )
                    )
            ),
        onAction = { onDismiss() },
        modifier = modifier
    )
}