package shmp.generator

import shmp.containers.SemanticsCoreTemplate
import shmp.language.SpeechPart
import shmp.language.category.CategoryPool
import shmp.language.derivation.*
import shmp.language.lexis.Word
import shmp.language.morphem.Prefix
import shmp.language.morphem.Suffix
import shmp.language.morphem.change.Position
import shmp.language.phonology.PhonemeSequence
import shmp.language.phonology.RestrictionsParadigm
import shmp.random.randomElement
import shmp.random.randomSublist
import shmp.random.testProbability
import kotlin.random.Random


class DerivationGenerator(
    private val restrictionsParadigm: RestrictionsParadigm,
    private val random: Random,
    private val injectors: List<DerivationInjector> = defaultInjectors
) {
    private var derivationParadigm = DerivationParadigm(listOf(), listOf())

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
        derivationParadigm = DerivationParadigm(
            generateDerivations(changeGenerator, categoryPool),
            generateCompounds(changeGenerator, categoryPool)
        )

        return derivationParadigm
    }

    private fun generateDerivations(changeGenerator: ChangeGenerator, categoryPool: CategoryPool) =
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

    private fun generateCompounds(changeGenerator: ChangeGenerator, categoryPool: CategoryPool): List<Compound> {
        val compounds = mutableListOf<Compound>()

        val changer = PassingCategoryChanger(random.nextInt(2))

        val infixCompoundsAmount = random.nextInt(3)
        for (i in 0 until infixCompoundsAmount) {
            val speechPart = SpeechPart.Noun

            compounds.add(
                Compound(
                    speechPart,
                    changeGenerator.lexisGenerator.syllableGenerator.generateSyllable(
                        SyllableRestrictions(
                            changeGenerator.lexisGenerator.phonemeContainer,
                            changeGenerator.lexisGenerator.restrictionsParadigm.restrictionsMapper.getValue(speechPart),
                            SyllablePosition.Middle
                        ),
                        random
                    ).phonemeSequence,
                    changer
                )
            )
        }

        if (testProbability(0.5, random))
            compounds.add(Compound(SpeechPart.Noun, PhonemeSequence(), changer))

        return compounds
    }

    private fun generateCategoryMaker(
        categoryPool: CategoryPool,
        derivationClass: DerivationClass
    ): CategoryChanger {
        val possibleCategoryMakers = mutableListOf<CategoryChanger>(ConstantCategoryChanger(
            categoryPool.getStaticFor(derivationClass.toSpeechPart)
                .map { randomElement(it.actualValues, random) }
                .toSet()
        ))

        if (derivationClass.fromSpeechPart == derivationClass.toSpeechPart)
            possibleCategoryMakers.add(PassingCategoryChanger(0))

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
