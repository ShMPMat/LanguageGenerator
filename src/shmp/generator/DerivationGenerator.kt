package shmp.generator

import shmp.containers.SemanticsCoreTemplate
import shmp.language.SpeechPart.Noun
import shmp.language.category.CategoryPool
import shmp.language.derivation.*
import shmp.language.derivation.DerivationType.*
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
                                restrictionsParadigm.restrictionsMapper.getValue(derivationClass.toSpeechPart)
                            )
                        )
                    } else {
                        Suffix(
                            changeGenerator.generateChanges(
                                Position.End,
                                restrictionsParadigm.restrictionsMapper.getValue(derivationClass.toSpeechPart)
                            )
                        )
                    }

                    Derivation(
                        affix,
                        derivationClass,
                        generateCategoryMaker(categoryPool, derivationClass)
                    )
                }
        derivationParadigm = DerivationParadigm(derivations)
        return derivationParadigm
    }

    private fun generateCategoryMaker(categoryPool: CategoryPool, derivationClass: DerivationClass): CategoryMaker {
        val possibleCategoryMakers = mutableListOf<CategoryMaker>(ConstantCategoryMaker(
            categoryPool.getStaticFor(derivationClass.toSpeechPart)
                .map { randomElement(it.actualValues, random) }
                .toSet()
        ))

        if (derivationClass.fromSpeechPart == derivationClass.toSpeechPart)
            possibleCategoryMakers.add(PassingCategoryMaker)

        return randomElement(possibleCategoryMakers, random)
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
