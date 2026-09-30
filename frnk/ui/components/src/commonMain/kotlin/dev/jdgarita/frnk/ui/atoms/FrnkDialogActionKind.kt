package dev.jdgarita.frnk.ui.atoms

/** The role of one [FrnkDialogAction], which decides how its button is drawn. */
enum class FrnkDialogActionKind {
    /** Backs out: an outlined button in the surface's content color. */
    Cancel,

    /** The dialog's answer: a filled button in the [FrnkDialogVariant]'s tint. */
    Primary,

    /** An answer that destroys something: a filled button in `colorError`, whatever the variant. */
    Destructive
}