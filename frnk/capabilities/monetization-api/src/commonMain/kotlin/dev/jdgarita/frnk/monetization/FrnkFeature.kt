package dev.jdgarita.frnk.monetization

/**
 * The toolkit's own well-known [Feature]s. Modelled as an enum — the exact shape hosts are encouraged
 * to use — so the catalogue stays closed, exhaustively matchable, and typo-proof.
 */
enum class FrnkFeature(
    override val id: String
) : Feature {
    Premium("premium"),
    UnlimitedExports("unlimited_exports"),
    AdFree("ad_free")
}