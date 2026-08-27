package dev.jdgarita.frnk.monetization.revenuecat

import dev.jdgarita.frnk.monetization.ProBenefit
import dev.jdgarita.frnk.monetization.ProMetadata

/**
 * Pure, SDK-free resolution of an offering's paywall metadata against the device language, split
 * out (like [isProFor]) so it is unit-testable without `Purchases.sharedInstance`.
 *
 * The metadata schema is **additive**: the flat keys are the canonical copy and the only thing
 * older clients read, so dashboards must keep them intact and layer languages on top:
 *
 * ```json
 * {
 *   "title": "…",
 *   "subtitle": "…",
 *   "benefits": [ { "key": "…", "value": "…" } ],
 *   "localizations": {
 *     "es": { "title": "…", "subtitle": "…", "benefits": [ … ] }
 *   }
 * }
 * ```
 *
 * [languageTag] is matched case-insensitively against the `localizations` keys — the exact tag
 * first (`es-MX`), then its primary-language prefix (`es`), mirroring
 * `FrnkLanguage.fromLanguageTag`'s treatment of `platformLanguageTag()`. Each field resolves
 * independently: locale override → flat key → [fallback]. Benefits resolve at whole-list
 * granularity (a valid non-empty locale list replaces the flat list entirely). Malformed nodes
 * degrade to the next tier rather than throwing.
 */
internal fun resolvePaywallMetadata(
    metadata: Map<String, Any?>,
    languageTag: String,
    fallback: ProMetadata
): ProMetadata =
    try {
        val localized = localizationFor(metadata, languageTag)
        ProMetadata(
            title = localized.string("title") ?: metadata.string("title") ?: fallback.title,
            subtitle = localized.string("subtitle") ?: metadata.string("subtitle") ?: fallback.subtitle,
            benefits = localized.benefits() ?: metadata.benefits() ?: fallback.benefits
        )
    } catch (_: Exception) {
        fallback
    }

/** The `localizations` entry matching [languageTag], or an empty map when there is none. */
private fun localizationFor(
    metadata: Map<String, Any?>,
    languageTag: String
): Map<*, *> {
    val localizations = metadata["localizations"] as? Map<*, *> ?: return emptyMap<String, Any?>()
    val tag = languageTag.lowercase()
    // Primary-language prefix: "es-MX" / "es_MX" → "es".
    val primary = tag.substringBefore('-').substringBefore('_')
    val match =
        localizations.entries.firstOrNull { (it.key as? String)?.lowercase() == tag }
            ?: localizations.entries.firstOrNull { (it.key as? String)?.lowercase() == primary }
    return match?.value as? Map<*, *> ?: emptyMap<String, Any?>()
}

private fun Map<*, *>.string(key: String): String? = this[key] as? String

private fun Map<*, *>.benefits(): List<ProBenefit>? {
    val list = this["benefits"] as? List<*> ?: return null
    return list
        .mapNotNull { item ->
            val entry = item as? Map<*, *> ?: return@mapNotNull null
            val id = entry["key"] as? String
            val text = entry["value"] as? String
            if (id != null && text != null) ProBenefit(id, text) else null
        }.takeIf { it.isNotEmpty() }
}
