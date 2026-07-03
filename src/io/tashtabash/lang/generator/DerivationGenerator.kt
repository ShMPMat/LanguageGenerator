package io.tashtabash.lang.generator

import io.tashtabash.lang.containers.SemanticsCoreTemplate
import io.tashtabash.lang.containers.WordBase
import io.tashtabash.lang.generator.util.SyllablePosition
import io.tashtabash.lang.generator.util.SyllableRestrictions
import io.tashtabash.lang.language.derivation.*
import io.tashtabash.lang.language.category.CategoryPool
import io.tashtabash.lang.language.derivation.DerivationType.*
import io.tashtabash.lang.language.lexis.*
import io.tashtabash.lang.language.morphem.Prefix
import io.tashtabash.lang.language.morphem.Suffix
import io.tashtabash.lang.language.morphem.change.Position
import io.tashtabash.lang.language.phonology.PhonemeSequence
import io.tashtabash.lang.language.phonology.RestrictionsParadigm
import io.tashtabash.lang.language.syntax.ChangeParadigm
import io.tashtabash.random.randomElement
import io.tashtabash.random.randomSublist
import io.tashtabash.random.singleton.RandomSingleton
import io.tashtabash.random.singleton.chanceOf
import io.tashtabash.random.singleton.randomElement
import io.tashtabash.random.singleton.testProbability
import io.tashtabash.random.testProbability
import java.util.*
import kotlin.math.max
import kotlin.math.pow
import kotlin.random.Random


class DerivationGenerator(
    private val restrictionsParadigm: RestrictionsParadigm,
    private val random: Random,
    private val mainInjectors: List<DerivationInjector> = defaultMainInjectors,
    private val additionalInjectors: List<DerivationInjector> = defaultInjectors,
) {
    private var derivationParadigm = DerivationParadigm(listOf(), listOf())

    internal fun injectDerivationOptions(
        words: List<SemanticsCoreTemplate>,
        injectByConnotations: Boolean = true
    ): List<SemanticsCoreTemplate> {
        val newWords = internalInjectDerivationOptions(words, mainInjectors)
            .toMutableList()

        if (injectByConnotations)
            injectionByConnotations(newWords + words)

        newWords += internalInjectDerivationOptions(words, additionalInjectors)

        return newWords
    }

    private fun internalInjectDerivationOptions(
        words: List<SemanticsCoreTemplate>,
        injectors: List<DerivationInjector>
    ): List<SemanticsCoreTemplate> {
        if (words.isEmpty())
            return emptyList()

        val newWords = injectors.flatMap { inj ->
            words.mapNotNull { inj.inject(it) }
        }

        return newWords + internalInjectDerivationOptions(newWords, injectors)
    }

    private fun injectionByConnotations(words: List<SemanticsCoreTemplate>, injectCompounds: Boolean = false) {
        injectDerivations(words)

        if (injectCompounds)
            injectCompounds(words)
    }

    private fun injectDerivations(words: List<SemanticsCoreTemplate>) {
        val bannedTypes = setOf(Big, Old, Smallness, Young)
        for (derivationType in entries.filter { it !in bannedTypes })
            for (from in words.filter { it.speechPart == derivationType.fromSpeechPart })
                for (to in words.filter { it.speechPart == derivationType.toSpeechPart }) {
                    val derivationLink = createDerivationByConnotation(derivationType, from, to)
                        ?: continue

                    from.derivationClusterTemplate
                        .typeToCore
                        .getOrPut(derivationType) { mutableListOf() }
                        .add(derivationLink)
                }
    }

    private fun createDerivationByConnotation(
        derivationType: DerivationType,
        from: SemanticsCoreTemplate,
        to: SemanticsCoreTemplate
    ): DerivationLink? {
        if (from == to)
            return null
        if (to.word.any { it.isUpperCase() }) // No connotation derivation for INF etc.
            return null

        val fromDerivationConnotationStrength = calculateConnotationsStrength(
            derivationType.connotations.values,
            from.connotations.values
        )

        val toDerivationConnotationsStrength = calculateConnotationsStrength(
            derivationType.connotations.values,
            to.connotations.values
        )

        val wordConnotationsStrength = calculateConnotationsStrength(
            to.connotations.values,
            from.connotations.values.filter { !it.isGlobal }
        )

        val probability = (toDerivationConnotationsStrength * wordConnotationsStrength *
                max(.0, 1 - fromDerivationConnotationStrength)).pow(.5)

        if (probability == .0)
            return null

        val present = from.derivationClusterTemplate.typeToCore[derivationType]
            ?.firstOrNull { it.template == to.word }

        if (present != null) {
            println("ALREADY PRESENT ${derivationType.name}  ${from.word} -> ${to.word} $probability")
            return null
        }
//        println("${derivation.name}  ${from.word} -> ${to.word} $probability")
        return DerivationLink(to.word, probability)
    }

    // O(n^3), use with caution
    private fun injectCompounds(words: List<SemanticsCoreTemplate>) {
        for (target in words)
            for ((i, left) in words.withIndex())
                for (right in words.drop(i)) {
                    val compoundLink = createCompound(target, left, right)
                        ?: continue

                    target.derivationClusterTemplate.possibleCompounds += compoundLink
                }
    }

    private fun createCompound(
        target: SemanticsCoreTemplate,
        left: SemanticsCoreTemplate,
        right: SemanticsCoreTemplate
    ): CompoundLink? {
        if (left == target || right == target)
            return null

        val connotationsSum = left.connotations + right.connotations
        val closeness = target.connotations closeness  connotationsSum
        val leftCloseness = target.connotations localCloseness left.connotations
        val rightCloseness = target.connotations localCloseness right.connotations
        val clearCloseness = target.connotations.values.toList() closeness
                connotationsSum.values.filter { !it.isGlobal }

        if (clearCloseness == .0 || leftCloseness == .0 || rightCloseness == .0)
            return null

        val present = target.derivationClusterTemplate.possibleCompounds.firstOrNull {
            it.templates == listOf(left.word, right.word) || it.templates == listOf(
                right.word,
                left.word
            )
        }

        if (present != null) {
            println("COMPOUND ALREADY PRESENT ${left.word} + ${right.word} = ${target.word} $closeness")
            return null
        }
//        println("${left.word} + ${right.word} = ${target.word} $closeness")
        return CompoundLink(
            listOf(left.word, right.word),
            closeness
        )
    }

    private fun calculateConnotationsStrength(from: Collection<Connotation>, to: Collection<Connotation>): Double {
        val hits = from.mapNotNull { connotation ->
            val otherStrength = to
                .mapNotNull { it.getCompatibility(connotation) }
                .reduceOrNull(Double::times) ?: return@mapNotNull null
            otherStrength * connotation.strength
        }

        return hits.reduceOrNull(Double::plus)
            ?.div(hits.size)
            ?.pow(1.0 / hits.size.toDouble().pow(2.0))
            ?: .0
    }

    internal fun generateDerivationParadigm(
        changeGenerator: ChangeGenerator,
        categoryPool: CategoryPool,
        changeParadigm: ChangeParadigm
    ): DerivationParadigm {
        derivationParadigm = DerivationParadigm(
            generateDerivations(changeGenerator, categoryPool, changeParadigm),
            generateCompounds(changeGenerator, categoryPool)
        )

        return derivationParadigm
    }

    private fun generateDerivations(
        changeGenerator: ChangeGenerator,
        categoryPool: CategoryPool,
        changeParadigm: ChangeParadigm
    ): List<Derivation> =
        randomSublist(generatedDerivationClasses, random, 2, generatedDerivationClasses.size + 1)
            .map { derivationClass ->
                generateDerivation(derivationClass, derivationClass.toSpeechPart, changeGenerator, categoryPool)
            } + generateInfDerivation(changeParadigm, changeGenerator, categoryPool)

    private fun generateInfDerivation(
        changeParadigm: ChangeParadigm,
        changeGenerator: ChangeGenerator,
        categoryPool: CategoryPool
    ): List<Derivation> {
        if (SpeechPart.Verb.toInf() !in changeParadigm.wordChangeParadigm.speechParts)
            return listOf()

        return listOf(
            generateDerivation(DerivationClass.InfinitiveVerb, SpeechPart.Verb.toInf(), changeGenerator, categoryPool, Double.MAX_VALUE)
        )
    }

    private fun generateDerivation(
        derivationClass: DerivationClass,
        speechPart: TypedSpeechPart,
        changeGenerator: ChangeGenerator,
        categoryPool: CategoryPool,
        strength: Double = .5.chanceOf<Double> {
            RandomSingleton.random.nextDouble(.1, 1.0)
        } ?: RandomSingleton.random.nextDouble(1.0, 10.0),
    ): Derivation {
        val affix = if (.5.testProbability()) Prefix(
            changeGenerator.generateChanges(
                Position.Beginning,
                restrictionsParadigm.restrictionsMapper.getValue(speechPart)
            )
        ) else Suffix(
            changeGenerator.generateChanges(
                Position.End,
                restrictionsParadigm.restrictionsMapper.getValue(speechPart)
            )
        )
        val categoryMaker = generateCategoryMaker(categoryPool, derivationClass, speechPart)

        return Derivation(affix, derivationClass, speechPart, strength, categoryMaker)
    }

    private fun generateCompounds(changeGenerator: ChangeGenerator, categoryPool: CategoryPool): List<Compound> {
        val compounds = mutableListOf<Compound>()

        val changer = PassingCategoryChanger(random.nextInt(2))
        val prosodyRule = generateCompoundProsodyRule()

        val infixCompoundsAmount = random.nextInt(1, 5)

        val speechParts = changeGenerator.lexisGenerator.restrictionsParadigm.getSpeechParts(SpeechPart.Noun)
        for (speechPart in speechParts) {
            for (i in 1..infixCompoundsAmount) {
                val compound = Compound(
                    speechPart,
                    changeGenerator.lexisGenerator.syllableGenerator.generateSyllable(
                        SyllableRestrictions(
                            changeGenerator.lexisGenerator.phonemeContainer,
                            changeGenerator.lexisGenerator.restrictionsParadigm.restrictionsMapper.getValue(speechPart)
                                .copy(avgWordLength = 1),
                            SyllablePosition.Middle
                        )
                    ).phonemes,
                    changer,
                    prosodyRule
                )

                if (compound in compounds)
                    continue

                compounds += compound
            }

            .5.chanceOf {
                compounds += constructPassingCompound(speechPart)
            }
        }
        return compounds
    }

    private fun constructPassingCompound(speechPart: TypedSpeechPart): Compound {
        val changer = PassingCategoryChanger(random.nextInt(2))
        val prosodyRule = generateCompoundProsodyRule()
        return Compound(speechPart, PhonemeSequence(), changer, prosodyRule)
    }

    private fun generateCompoundProsodyRule() =
        if (testProbability(.2, random))
            PassingProsodyRule
        else
            StressOnWordRule(random.nextInt(2))

    private fun generateCategoryMaker(
        categoryPool: CategoryPool,
        derivationClass: DerivationClass,
        speechPart: TypedSpeechPart
    ): CategoryChanger {
        val possibleCategoryMakers = mutableListOf<CategoryChanger>(ConstantCategoryChanger(
            categoryPool.getStatic(derivationClass.toSpeechPart.type)
                .map { it.actualValues.randomElement() }
                .toSet(),
            speechPart
        ))

        if (derivationClass.fromSpeechPart == derivationClass.toSpeechPart.type)
            possibleCategoryMakers += PassingCategoryChanger(0)

        return randomElement(possibleCategoryMakers, random)
    }

    internal fun makeDerivations(word: Word, words: SimpleMutableLexis, wordBase: WordBase) {
        val queue = ArrayDeque(listOf(word))

        while (queue.isNotEmpty()) {
            val curWord = queue.poll()
            for (derivation in derivationParadigm.derivations) {
                val derivedWord = derivation.deriveRandom(curWord, random) { m ->
                    wordBase.allWords.first { it.word == m }
                }
                    ?: continue

                words += derivedWord
                queue += words.words.last()
            }
        }
    }

    internal fun makeCompounds(templates: List<SemanticsCoreTemplate>, availableWords: SimpleMutableLexis) {
        for (template in templates.shuffled(RandomSingleton.random))
            for (compound in derivationParadigm.compounds.shuffled(RandomSingleton.random)) {
                val incompleteLexis = Lexis(availableWords.words, mapOf())
                val derivedWord = compound.compose(incompleteLexis, template, random)
                    ?: continue

                availableWords += derivedWord
                break
            }
    }
}


val generatedDerivationClasses = listOf(
    DerivationClass.Diminutive,
    DerivationClass.Augmentative,
    DerivationClass.PlaceFromNoun,
    DerivationClass.PersonFromNoun,
    DerivationClass.AbstractNounFromNoun,
    DerivationClass.AbstractNounFromAdjective,
    DerivationClass.PlaceFromAdjective,
    DerivationClass.BeingStateFromAdjective,
    DerivationClass.PlaceFromVerb,
    DerivationClass.PersonFromVerb,
    DerivationClass.AbstractNounFromVerb,
)
