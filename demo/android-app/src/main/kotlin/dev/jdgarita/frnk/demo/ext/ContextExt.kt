package dev.jdgarita.frnk.demo.ext

import android.content.Context
import android.widget.Toast

/**
 * Shows a short [Toast] with [message] in this [Context].
 *
 * The demo host's one-liner for surfacing MVI effects (and any other transient feedback) as Android
 * toasts — see `MainActivity.handleEffect`. Prefer this over inlining `Toast.makeText(...).show()`:
 * keep toast plumbing in this single extension so call sites stay terse and consistent.
 */
internal fun Context.toast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}
