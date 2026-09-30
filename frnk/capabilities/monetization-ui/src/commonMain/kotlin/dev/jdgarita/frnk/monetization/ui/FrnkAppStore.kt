package dev.jdgarita.frnk.monetization.ui

/**
 * The store this binary is distributed through: Google Play on Android, the App Store on iOS. Copy
 * that names the receipt a restore searched, or the account it is tied to, reads this rather than
 * hedging with "Apple ID / Google Account" — a user only ever has one of the two.
 */
enum class FrnkAppStore {
    AppStore,
    GooglePlay
}

/** The store this platform build ships through; a per-target constant, never a runtime probe. */
expect val frnkAppStore: FrnkAppStore