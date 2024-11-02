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
import io.tashtabash.random.randomElement
import io.tashtabash.random.randomSublist
import io.tashtabash.random.singleton.RandomSingleton
import io.tashtabash.random.singleton.chanceOf
import io.tashtabash.random.singleton.randomElement
import io.tashtabash.random.testProbability
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

    internal fun injectDerivationOptions(words: List<SemanticsCoreTemplate>): List<SemanticsCoreTemplate> {
        val newWords = internalInjectDerivationOptions(words, mainInjectors)
            .toMutableList()

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

    private fun injectionByConnotations(words: List<SemanticsCoreTemplate>) {
        val bannedTypes = setOf(Big, Old, Smallness, Young)
        for (derivationType in DerivationType.values().filter { it !in bannedTypes })
            for (from in words.filter { it.speechPart == derivationType.fromSpeechPart })
                for (to in words.filter { it.speechPart == derivationType.toSpeechPart }) {
                    val derivationLink = createDerivationByConnotation(derivationType, from, to)
                        ?: continue

                    from.derivationClusterTemplate
                        .typeToCore
                        .getOrPut(derivationType) { mutableListOf() }
                        .add(derivationLink)
                }

        injectCompounds(words)
    }

    private fun createDerivationByConnotation(
        derivationType: DerivationType,
        from: SemanticsCoreTemplate,
        to: SemanticsCoreTemplate
    ): DerivationLink? {
        if (from == to)
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
                max(0.0, 1 - fromDerivationConnotationStrength)).pow(0.5)

        if (probability == 0.0)
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

    private fun injectCompounds(words: List<SemanticsCoreTemplate>) {
        for (target in words)
            for ((index, left) in words.withIndex())
                for (right in words.drop(index)) {
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
        val distance = target.connotations distance connotationsSum
        val leftDistance = target.connotations localDistance left.connotations
        val rightDistance = target.connotations localDistance right.connotations
        val clearDistance = target.connotations.values.toList() distance
                connotationsSum.values.filter { !it.isGlobal }

        if (clearDistance == 0.0 || leftDistance == 0.0 || rightDistance == 0.0)
            return null

        val present = target.derivationClusterTemplate.possibleCompounds.firstOrNull {
            it.templates == listOf(left.word, right.word) || it.templates == listOf(
                right.word,
                left.word
            )
        }

        if (present != null) {
            println("COMPOUND ALREADY PRESENT ${left.word} + ${right.word} = ${target.word} $distance")
            return null
        }
//        println("${left.word} + ${right.word} = ${target.word} $distance")
        return CompoundLink(
            listOf(left.word, right.word),
            distance
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
            ?: 0.0
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
            .flatMap { derivationClass ->
                val affectedSpeechParts = restrictionsParadigm.getSpeechParts(derivationClass.toSpeechPart)

                affectedSpeechParts.map { generateDerivation(derivationClass, it, changeGenerator, categoryPool) }
            }

    private fun generateDerivation(
        derivationClass: DerivationClass,
        speechPart: TypedSpeechPart,
        changeGenerator: ChangeGenerator,
        categoryPool: CategoryPool
    ): Derivation {
        val affix = if (random.nextBoolean()) Prefix(
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

        return Derivation(
            affix,
            derivationClass,
            speechPart,
            0.5.chanceOf<Double> {
                RandomSingleton.random.nextDouble(0.1, 1.0)
            } ?: RandomSingleton.random.nextDouble(1.0, 10.0),
            generateCategoryMaker(categoryPool, derivationClass, speechPart)
        )
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

            0.5.chanceOf {
                val changer = PassingCategoryChanger(random.nextInt(2))
                val prosodyRule = generateCompoundProsodyRule()
                compounds += Compound(speechPart, PhonemeSequence(), changer, prosodyRule)
            }
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
        derivationClass: DerivationClass,
        speechPart: TypedSpeechPart
    ): CategoryChanger {
        val possibleCategoryMakers = mutableListOf<CategoryChanger>(ConstantCategoryChanger(
            categoryPool.getStaticFor(derivationClass.toSpeechPart)
                .map { it.actualValues.randomElement() }
                .toSet(),
            speechPart
        ))

        if (derivationClass.fromSpeechPart == derivationClass.toSpeechPart)
            possibleCategoryMakers += PassingCategoryChanger(0)

        return randomElement(possibleCategoryMakers, random)
    }

    internal fun makeDerivations(words: MutableList<Word>, wordBase: WordBase) {
        var i = 0
        while (i < words.size) {
            val word = words[i]
            for (derivation in derivationParadigm.derivations) {
                val derivedWord = derivation.derive(word, wordBase, random)
                    ?: continue
                words += derivedWord
            }
            i++
        }
    }

    internal fun makeCompounds(templates: List<SemanticsCoreTemplate>, availableWords: MutableList<Word>) {
        for (template in templates.shuffled(RandomSingleton.random))
            for (compound in derivationParadigm.compounds.shuffled(RandomSingleton.random)) {
                val derivedWord = compound.compose(availableWords, template, random)
                    ?: continue

                availableWords += derivedWord
                break
            }
    }
}
