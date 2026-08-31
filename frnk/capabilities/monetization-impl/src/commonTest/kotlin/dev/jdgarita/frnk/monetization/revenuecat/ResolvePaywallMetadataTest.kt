package dev.jdgarita.frnk.monetization.revenuecat

import dev.jdgarita.frnk.monetization.ProBenefit
import dev.jdgarita.frnk.monetization.ProMetadata
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Pins [resolvePaywallMetadata]'s three-tier resolution (locale override → flat keys → fallback)
 * and, critically, that the **flat-only schema shipped clients read keeps parsing unchanged** —
 * the `localizations` block is additive, so a dashboard gaining it must never alter what the flat
 * keys produce.
 */
class ResolvePaywallMetadataTest {
    private val fallback =
        ProMetadata(
            title = "fallback title",
            subtitle = "fallback subtitle",
            benefits = listOf(ProBenefit("FALLBACK", "fallback value"))
        )

    private val flatBenefits =
        listOf(
            mapOf("key" to "SCANNER", "value" to "Unlock Pro AI Label Scanner"),
            mapOf("key" to "JOURNAL", "value" to "Build Your Personal Coffee Journal")
        )

    private val esBenefits =
        listOf(
            mapOf("key" to "ESCÁNER", "value" to "Desbloquea el Escáner de Etiquetas de IA Pro"),
            mapOf("key" to "DIARIO", "value" to "Crea tu Diario de Café Personal")
        )

    private val flatOnly =
        mapOf(
            "title" to "Keep your tasting\nmomentum.",
            "subtitle" to "Unlock the Pro AI scanner.",
            "benefits" to flatBenefits
        )

    private val withEsLocalization =
        flatOnly +
            mapOf(
                "localizations" to
                    mapOf(
                        "es" to
                            mapOf(
                                "title" to "Mantén tu ritmo\nde catas.",
                                "subtitle" to "Desbloquea el escáner de IA Pro.",
                                "benefits" to esBenefits
                            )
                    )
            )

    @Test
    fun flat_only_schema_parses_unchanged() {
        val resolved = resolvePaywallMetadata(flatOnly, languageTag = "en-US", fallback = fallback)
        assertEquals("Keep your tasting\nmomentum.", resolved.title)
        assertEquals("Unlock the Pro AI scanner.", resolved.subtitle)
        assertEquals(
            listOf(
                ProBenefit("SCANNER", "Unlock Pro AI Label Scanner"),
                ProBenefit("JOURNAL", "Build Your Personal Coffee Journal")
            ),
            resolved.benefits
        )
    }

    @Test
    fun localization_wins_for_exact_language_match() {
        val resolved = resolvePaywallMetadata(withEsLocalization, languageTag = "es", fallback = fallback)
        assertEquals("Mantén tu ritmo\nde catas.", resolved.title)
        assertEquals("Desbloquea el escáner de IA Pro.", resolved.subtitle)
        assertEquals(
            listOf(
                ProBenefit("ESCÁNER", "Desbloquea el Escáner de Etiquetas de IA Pro"),
                ProBenefit("DIARIO", "Crea tu Diario de Café Personal")
            ),
            resolved.benefits
        )
    }

    @Test
    fun regional_tag_matches_primary_language_localization() {
        val resolved = resolvePaywallMetadata(withEsLocalization, languageTag = "es-MX", fallback = fallback)
        assertEquals("Mantén tu ritmo\nde catas.", resolved.title)
    }

    @Test
    fun underscore_regional_tag_matches_primary_language_localization() {
        val resolved = resolvePaywallMetadata(withEsLocalization, languageTag = "es_419", fallback = fallback)
        assertEquals("Mantén tu ritmo\nde catas.", resolved.title)
    }

    @Test
    fun exact_regional_key_beats_primary_language_key() {
        val metadata =
            flatOnly +
                mapOf(
                    "localizations" to
                        mapOf(
                            "es" to mapOf("title" to "genérico"),
                            "es-mx" to mapOf("title" to "mexicano")
                        )
                )
        val resolved = resolvePaywallMetadata(metadata, languageTag = "es-MX", fallback = fallback)
        assertEquals("mexicano", resolved.title)
    }

    @Test
    fun english_device_ignores_spanish_localization() {
        val resolved = resolvePaywallMetadata(withEsLocalization, languageTag = "en-US", fallback = fallback)
        assertEquals("Keep your tasting\nmomentum.", resolved.title)
        assertEquals("Unlock Pro AI Label Scanner", resolved.benefits.first().value)
    }

    @Test
    fun unknown_language_falls_back_to_flat_keys() {
        val resolved = resolvePaywallMetadata(withEsLocalization, languageTag = "fr-FR", fallback = fallback)
        assertEquals("Keep your tasting\nmomentum.", resolved.title)
    }

    @Test
    fun partial_localization_mixes_per_field_with_flat_keys() {
        val metadata =
            flatOnly + mapOf("localizations" to mapOf("es" to mapOf("title" to "Sólo el título")))
        val resolved = resolvePaywallMetadata(metadata, languageTag = "es", fallback = fallback)
        assertEquals("Sólo el título", resolved.title)
        assertEquals("Unlock the Pro AI scanner.", resolved.subtitle)
        assertEquals(ProBenefit("SCANNER", "Unlock Pro AI Label Scanner"), resolved.benefits.first())
    }

    @Test
    fun empty_metadata_resolves_to_fallback() {
        assertEquals(fallback, resolvePaywallMetadata(emptyMap<String, Any?>(), "es", fallback))
    }

    @Test
    fun missing_fields_resolve_to_fallback_per_field() {
        val metadata = mapOf("title" to "only a title")
        val resolved = resolvePaywallMetadata(metadata, languageTag = "en", fallback = fallback)
        assertEquals("only a title", resolved.title)
        assertEquals(fallback.subtitle, resolved.subtitle)
        assertEquals(fallback.benefits, resolved.benefits)
    }

    @Test
    fun malformed_nodes_degrade_without_throwing() {
        val metadata =
            mapOf(
                "title" to 42,
                "subtitle" to listOf("not", "a", "string"),
                "benefits" to "not a list",
                "localizations" to "not a map"
            )
        assertEquals(fallback, resolvePaywallMetadata(metadata, "es", fallback))
    }

    @Test
    fun malformed_localized_benefits_degrade_to_flat_benefits() {
        val metadata =
            flatOnly +
                mapOf(
                    "localizations" to
                        mapOf("es" to mapOf("benefits" to listOf("garbage", mapOf("key" to "NO_VALUE"))))
                )
        val resolved = resolvePaywallMetadata(metadata, languageTag = "es", fallback = fallback)
        assertEquals(ProBenefit("SCANNER", "Unlock Pro AI Label Scanner"), resolved.benefits.first())
    }
}