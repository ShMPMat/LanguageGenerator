package shmp.lang.language.derivation

import shmp.lang.containers.DerivationClusterTemplate
import shmp.lang.containers.SemanticsCoreTemplate
import shmp.lang.containers.SemanticsTagCluster
import shmp.lang.containers.SemanticsTagTemplate
import shmp.lang.language.lexis.SpeechPart
import shmp.lang.language.derivation.DerivationType.*
import shmp.lang.language.lexis.DerivationLink


data class DerivationInjector(
    val type: DerivationType,
    val applicableSpeechPart: SpeechPart,
    val descriptionCreator: (String) -> String,
    val prohibitedTags: List<String> = listOf(),
    val additionalTest: (SemanticsCoreTemplate) -> Boolean = { true },
    val newSpeechPart: SpeechPart = applicableSpeechPart,
    val tagCreator: (Set<SemanticsTagCluster>) -> Set<SemanticsTagCluster> = { it },
    val probability: Double = 1.0,
    val coreRealizationProbability: Double = 0.05
) {
    fun injector(core: SemanticsCoreTemplate): SemanticsCoreTemplate? {
        if (
            core.speechPart != applicableSpeechPart
            || core.tagClusters.map { it.type }.any { it in prohibitedTags }
            || core.derivationClusterTemplate.appliedDerivations.contains(type)
            || !additionalTest(core)
        ) return null

        val link = DerivationLink(
            SemanticsCoreTemplate(
                descriptionCreator(core.word),
                newSpeechPart,
                core.connotations + type.connotations,
                tagCreator(core.tagClusters)
                        + setOf(
                    SemanticsTagCluster(
                        listOf(SemanticsTagTemplate(type.name, 1.0)),
                        type.name,
                        true
                    )
                ),
                DerivationClusterTemplate(
                    appliedDerivations = core.derivationClusterTemplate.appliedDerivations + setOf(type)
                ),
                coreRealizationProbability
            ),
            probability
        )
        val existingLinks = core.derivationClusterTemplate.typeToCore[type] ?: mutableListOf()
        existingLinks.add(link)
        core.derivationClusterTemplate.typeToCore[type] = existingLinks
        return link.template
    }
}

val defaultInjectors = listOf(
    DerivationInjector(
        Smallness,
        SpeechPart.Noun,
        { "little_$it" },
        prohibitedTags = listOf(Big, Old).map { it.toString() }
    ),
    DerivationInjector(
        Young,
        SpeechPart.Noun,
        { "young_$it" },
        prohibitedTags = listOf(Big, Old).map { it.toString() },
        additionalTest = { it.tagClusters.any { c -> c.type == "species" } }
    ),
    DerivationInjector(
        Big,
        SpeechPart.Noun,
        { "big_$it" },
        prohibitedTags = listOf(Smallness, Young).map { it.toString() }
    ),
    DerivationInjector(
        Old,
        SpeechPart.Noun,
        { "old_$it" },
        prohibitedTags = listOf(Smallness, Young).map { it.toString() },
        additionalTest = { it.tagClusters.any { c -> c.type == "species" } }
    ),
    DerivationInjector(
        VNPerson,
        SpeechPart.Verb,
        { "one_${it}ing" },
        newSpeechPart = SpeechPart.Noun,
        probability = 0.2
    ),
    DerivationInjector(
        ANAbstract,
        SpeechPart.Adjective,
        { "${it}ness" },
        newSpeechPart = SpeechPart.Noun,
        probability = 0.4,
        coreRealizationProbability = 0.1
    )
)
