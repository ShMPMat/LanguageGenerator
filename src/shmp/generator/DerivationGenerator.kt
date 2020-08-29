package shmp.generator

import shmp.containers.DerivationClusterTemplate
import shmp.containers.SemanticsCoreTemplate
import shmp.containers.SemanticsTagCluster
import shmp.containers.SemanticsTagTemplate
import shmp.language.SpeechPart
import shmp.language.SpeechPart.*
import shmp.language.category.CategoryPool
import shmp.language.derivation.*
import shmp.language.derivation.DerivationType.*
import shmp.language.lexis.DerivationLink
import shmp.language.lexis.Word
import shmp.language.morphem.Prefix
import shmp.language.morphem.Suffix
import shmp.language.morphem.change.Position
import shmp.language.phonology.RestrictionsParadigm
import shmp.random.randomElement
import shmp.random.randomSublist
import kotlin.random.Random


class DerivationGenerator(
    private val restrictionsParadigm: RestrictionsParadigm,
    private val random: Random,
    private val injectors: List<DerivationInjector> = defaultInjectors
) {
    private var derivationParadigm = DerivationParadigm(listOf())

    internal fun injectDerivationOptions(words: List<SemanticsCoreTemplate>): List<SemanticsCoreTemplate> {
        if (words.isEmpty())
            return emptyList()

        val newWords = injectors.flatMap { inj ->
            words.mapNotNull { inj.injector(it) }
        }

        return newWords + injectDerivationOptions(newWords)
    }

    internal fun generateDerivationParadigm(
        changeGenerator: ChangeGenerator,
        categoryPool: CategoryPool
    ): DerivationParadigm {
        val derivations =
            randomSublist(DerivationClass.values().toList(), random, 0, DerivationClass.values().size + 1)
                .map { derivationClass ->
                    val affix = if (random.nextBoolean()) {
                        Prefix(
                            changeGenerator.generateChanges(
                                Position.Beginning,
                                restrictionsParadigm.restrictionsMapper.getValue(derivationClass.speechPart)
                            )
                        )
                    } else {
                        Suffix(
                            changeGenerator.generateChanges(
                                Position.End,
                                restrictionsParadigm.restrictionsMapper.getValue(derivationClass.speechPart)
                            )
                        )
                    }

                    val possibleCategoryMakers = listOf(ConstantCategoryMaker(
                        categoryPool.getStaticFor(derivationClass.speechPart)
                            .map { randomElement(it.actualValues, random) }
                            .toSet()
                    ))//TODO add passing possibility

                    Derivation(
                        affix,
                        derivationClass,
                        randomElement(possibleCategoryMakers, random)
                    )
                }
        derivationParadigm = DerivationParadigm(derivations)
        return derivationParadigm
    }

    internal fun makeDerivations(words: MutableList<Word>) {
        var i = 0
        while (i < words.size) {
            val word = words[i]
            for (derivation in derivationParadigm.derivations) {
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
                tagCreator(core.tagClusters)
                        + setOf(SemanticsTagCluster(listOf(SemanticsTagTemplate(type.name, 1.0)), type.name)),
                DerivationClusterTemplate(
                    appliedDerivations = core.derivationClusterTemplate.appliedDerivations + setOf(type)
                ),
                coreRealizationProbability
            ),
            probability
        )
        val existingLinks = core.derivationClusterTemplate.typeToCore[type] ?: listOf()
        core.derivationClusterTemplate.typeToCore[type] = existingLinks + listOf(link)
        return link.template
    }
}

val defaultInjectors = listOf(
    DerivationInjector(Smallness, Noun, { "little_$it" }, prohibitedTags = listOf(Big, Old).map { it.toString() }),
    DerivationInjector(
        Young,
        Noun,
        { "young_$it" },
        prohibitedTags = listOf(Big, Old).map { it.toString() },
        additionalTest = { it.tagClusters.any { c -> c.type == "species" } }
    ),
    DerivationInjector(Big, Noun, { "big_$it" }, prohibitedTags = listOf(Smallness, Young).map { it.toString() }),
    DerivationInjector(
        Old,
        Noun,
        { "old_$it" },
        prohibitedTags = listOf(Smallness, Young).map { it.toString() },
        additionalTest = { it.tagClusters.any { c -> c.type == "species" } }
    )
)