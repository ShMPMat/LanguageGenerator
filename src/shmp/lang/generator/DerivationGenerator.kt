package shmp.lang.generator

import shmp.lang.containers.SemanticsCoreTemplate
import shmp.lang.generator.util.SyllablePosition
import shmp.lang.generator.util.SyllableRestrictions
import shmp.lang.language.derivation.*
import shmp.lang.language.lexis.SpeechPart
import shmp.lang.language.category.CategoryPool
import shmp.lang.language.lexis.Word
import shmp.lang.language.morphem.Prefix
import shmp.lang.language.morphem.Suffix
import shmp.lang.language.morphem.change.Position
import shmp.lang.language.phonology.PhonemeSequence
import shmp.lang.language.phonology.RestrictionsParadigm
import shmp.random.randomElement
import shmp.random.randomSublist
import shmp.random.singleton.chanceOf
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
        randomSublist(DerivationClass.values().toList(), random, 2, DerivationClass.values().size + 1)
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
        val prosodyRule = generateCompoundProsodyRule()

        val infixCompoundsAmount = random.nextInt(1, 5)
        for (i in 1..infixCompoundsAmount) {
            val speechPart = SpeechPart.Noun
            val compound = Compound(
                speechPart,
                changeGenerator.lexisGenerator.syllableGenerator.generateSyllable(
                    SyllableRestrictions(
                        changeGenerator.lexisGenerator.phonemeContainer,
                        changeGenerator.lexisGenerator.restrictionsParadigm.restrictionsMapper.getValue(speechPart)
                            .copy(avgWordLength = 1),
                        SyllablePosition.Middle
                    ),
                    random
                ).phonemeSequence,
                changer,
                prosodyRule
            )

            if (compound in compounds)
                continue

            compounds.add(compound)
        }

        0.5.chanceOf {
            val changer = PassingCategoryChanger(random.nextInt(2))
            val prosodyRule = generateCompoundProsodyRule()
            compounds.add(Compound(SpeechPart.Noun, PhonemeSequence(), changer, prosodyRule))
        }

        return compounds
    }

    private fun generateCompoundProsodyRule() =
        if (testProbability(0.2, random))
            PassingProsodyRule
        else
            StressOnWordRule(random.nextInt(2))

    private fun generateCategoryMaker(
        categoryPool: CategoryPool,
        derivationClass: DerivationClass
    ): CategoryChanger {
        val possibleCategoryMakers = mutableListOf<CategoryChanger>(ConstantCategoryChanger(
            categoryPool.getStaticFor(derivationClass.toSpeechPart)
                .map { randomElement(it.actualValues, random) }
                .toSet(),
            derivationClass.toSpeechPart
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

    internal fun makeCompounds(templates: List<SemanticsCoreTemplate>, availableWords: MutableList<Word>) {
        for (template in templates.shuffled())
            for (compound in derivationParadigm.compounds.shuffled()) {
                val derivedWord = compound.compose(availableWords, template, random)
                    ?: continue

                availableWords.add(derivedWord)
                break
            }
    }
}
