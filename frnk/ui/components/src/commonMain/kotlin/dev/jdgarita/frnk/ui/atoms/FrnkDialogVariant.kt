package dev.jdgarita.frnk.ui.atoms

/**
 * The tone of a [FrnkDialog]. It tints the icon badge, the eyebrow and every
 * [FrnkDialogActionKind.Primary] action, and picks the badge's default glyph:
 *
 * | Variant | Tint token | Default glyph |
 * |---|---|---|
 * | [Accent] | `colorPrimary` | `iconInfo` |
 * | [Destructive] | `colorError` | `iconDelete` |
 * | [Warning] | `colorWarning` | `iconWarning` |
 * | [Success] | `colorSuccess` | `iconCheck` |
 * | [Neutral] | `colorOnSurface` | `iconInfo` |
 */
enum class FrnkDialogVariant {
    /** A confirmation in the brand color (archive, save, continue). */
    Accent,

    /** A confirmation that throws something away (delete, discard). */
    Destructive,

    /** A recoverable problem (a failed scan, a network error). */
    Warning,

    /** A finished operation. */
    Success,

    /** Information with nothing to decide (an empty restore, a heads-up). */
    Neutral
}