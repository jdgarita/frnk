package dev.jdgarita.frnk.ui.atoms

/** How a [FrnkDialog] lays out its actions. */
enum class FrnkDialogActionLayout {
    /** Side by side, sharing the width equally. Suits two short labels. */
    Row,

    /** One per line, full width, in list order. Suits long labels or three or more actions. */
    Stacked
}