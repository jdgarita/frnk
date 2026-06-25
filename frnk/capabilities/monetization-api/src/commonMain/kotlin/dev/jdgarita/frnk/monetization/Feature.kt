package dev.jdgarita.frnk.monetization

/**
 * Stable, type-safe identifier for a gateable capability. The toolkit ships its own catalogue as
 * [FrnkFeature]; host apps add their own by implementing this interface — typically via an enum so
 * gates are checked at compile time and typos are impossible:
 *
 *   enum class AppFeature(override val id: String) : Feature { CloudSync("cloud_sync") }
 *
 * [id] is the stable string key used for free-feature matching (and any analytics/serialization a host
 * layers on top); it must be unique across the features a single app gates on.
 */
interface Feature {
    val id: String
}