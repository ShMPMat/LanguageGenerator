package shmp.generator

import shmp.containers.DerivationClusterTemplate
import shmp.containers.SemanticsCoreTemplate
import shmp.containers.SemanticsTagCluster
import shmp.language.SpeechPart
import shmp.language.SpeechPart.*
import shmp.language.derivation.Derivation
import shmp.language.derivation.DerivationClass
import shmp.language.derivation.DerivationType
import shmp.language.derivation.DerivationType.*
import shmp.language.lexis.DerivationLink
import shmp.language.lexis.Word
import shmp.language.morphem.Prefix
import shmp.language.morphem.Suffix
import shmp.language.morphem.change.Position
import shmp.language.phonology.RestrictionsParadigm
import shmp.random.randomSublist
import kotlin.random.Random

class DerivationGenerator(
    private val restrictionsParadigm: RestrictionsParadigm,
    private val random: Random,
    private val injectors: List<DerivationInjector> = defaultInjectors
) {
    internal fun injectDerivationOptions(words: List<SemanticsCoreTemplate>): List<SemanticsCoreTemplate> {
        if (words.isEmpty())
            return emptyList()

        val newWords = injectors.flatMap { inj ->
            words.mapNotNull { inj.injector(it) }
        }

        return newWords + injectDerivationOptions(newWords)
    }

    internal fun makeDerivations(words: MutableList<Word>, changeGenerator: ChangeGenerator) {
        val derivations =
            randomSublist(DerivationClass.values().toList(), random, 0, DerivationClass.values().size + 1)
                .map {
                    val affix = if (random.nextBoolean()) {
                        Prefix(
                            changeGenerator.generateChanges(
                                Position.Beginning,
                                restrictionsParadigm.restrictionsMapper.getValue(it.speechPart)
                            )
                        )
                    } else {
                        Suffix(
                            changeGenerator.generateChanges(
                                Position.End,
                                restrictionsParadigm.restrictionsMapper.getValue(it.speechPart)
                            )
                        )
                    }
                    Derivation(affix, it)
                }

        var i = 0
        while (i < words.size) {
            val word = words[i]
            for (derivation in derivations) {
                val derivedWord = derivation.derive(word, random)
                    ?: continue
                words.add(derivedWord)
            }
            i++
        }
    }
}

data class DerivationInjector(
    val type: DerivationType,
    val applicableSpeechPart: SpeechPart,
    val descriptionCreator: (String) -> String,
    val additionalTest: (SemanticsCoreTemplate) -> Boolean = { true },
    val newSpeechPart: SpeechPart = applicableSpeechPart,
    val tagCreator: (Set<SemanticsTagCluster>) -> Set<SemanticsTagCluster> = { it },
    val probability: Double = 1.0
) {
    fun injector(core: SemanticsCoreTemplate): SemanticsCoreTemplate? {
        if (
            core.speechPart != applicableSpeechPart
            || core.derivationClusterTemplate.internalTypes.contains(type)
            || !additionalTest(core)
        ) return null

        val link = DerivationLink(
            SemanticsCoreTemplate(
                descriptionCreator(core.word),
                newSpeechPart,
                tagCreator(core.tagClusters),
                DerivationClusterTemplate(
                    internalTypes = core.derivationClusterTemplate.internalTypes + setOf(type)
                )
            ),
            probability
        )
        core.derivationClusterTemplate.typeToCore[type] = link
        return link.template
    }
}

val defaultInjectors = listOf(
    DerivationInjector(Smallness, Noun, { "little_$it" }),
    DerivationInjector(
        Young,
        Noun,
        { "young_$it" },
        additionalTest = { it.tagClusters.any { c -> c.type == "species" } }
    )
)