package shmp.lang.generator

import shmp.lang.containers.SemanticsCoreTemplate
import shmp.lang.generator.util.SyllablePosition
import shmp.lang.generator.util.SyllableRestrictions
import shmp.lang.language.derivation.*
import shmp.lang.language.category.CategoryPool
import shmp.lang.language.derivation.DerivationType.*
import shmp.lang.language.lexis.*
import shmp.lang.language.morphem.Prefix
import shmp.lang.language.morphem.Suffix
import shmp.lang.language.morphem.change.Position
import shmp.lang.language.phonology.PhonemeSequence
import shmp.lang.language.phonology.RestrictionsParadigm
import shmp.random.randomElement
import shmp.random.randomSublist
import shmp.random.singleton.chanceOf
import shmp.random.singleton.randomElement
import shmp.random.testProbability
import kotlin.math.max
import kotlin.math.pow
import kotlin.random.Random


class DerivationGenerator(
    private val restrictionsParadigm: RestrictionsParadigm,
    private val random: Random,
    private val injectors: List<DerivationInjector> = defaultInjectors
) {
    private var derivationParadigm = DerivationParadigm(listOf(), listOf())

    internal fun injectDerivationOptions(words: List<SemanticsCoreTemplate>): List<SemanticsCoreTemplate> {
        injectionByConnotations(words)

        val newWords = internalInjectDerivationOptions(words)

//        injectionByConnotations(newWords + words)

        return newWords
    }

    private fun internalInjectDerivationOptions(words: List<SemanticsCoreTemplate>): List<SemanticsCoreTemplate> {
        if (words.isEmpty()) {
            injectionByConnotations(words)
            return emptyList()
        }

        val newWords = injectors.flatMap { inj ->
            words.mapNotNull { inj.injector(it) }
        }

        return newWords + internalInjectDerivationOptions(newWords)
    }

    private fun injectionByConnotations(words: List<SemanticsCoreTemplate>) {
        val bannedTypes = setOf(Big, Old, Smallness, Young)
        for (derivation in DerivationType.values().filter { it !in bannedTypes }) {
            for (from in words.filter { it.speechPart == derivation.fromSpeechPart })
                for (to in words.filter { it.speechPart == derivation.toSpeechPart }) {
                    if (from == to)
                        continue

                    val fromDerivationConnotationStrength = calculateConnotationsStrength(
                        derivation.connotations.values,
                        from.connotations.values
                    )

                    val toDerivationConnotationsStrength = calculateConnotationsStrength(
                        derivation.connotations.values,
                        to.connotations.values
                    )

                    val wordConnotationsStrength = calculateConnotationsStrength(
                        to.connotations.values.filter { !it.isGlobal },
                        from.connotations.values.filter { !it.isGlobal }
                    )

                    val probability = toDerivationConnotationsStrength * wordConnotationsStrength *
                            max(0.0, 1 - fromDerivationConnotationStrength)

                    if (probability > 0) {
                        val present = from.derivationClusterTemplate.typeToCore[derivation]
                            ?.firstOrNull { it.template == to }

                        if (present != null) {
                            println("ALREADY PRESENT ${derivation.name}  ${from.word} -> ${to.word} $probability")
                            continue
                        }

                        println("${derivation.name}  ${from.word} -> ${to.word} $probability")
                        from.derivationClusterTemplate.typeToCore[derivation]
                            ?.add(DerivationLink(to, probability))
                            ?: from.derivationClusterTemplate.typeToCore.put(
                                derivation,
                                mutableListOf(DerivationLink(to, probability))
                            )
                    }
                }
        }
    }

    private fun calculateConnotationsStrength(from: Collection<Connotation>, to: Collection<Connotation>): Double {
        val hits = from
            .mapNotNull { connotation ->
                val otherStrength = to
                    .mapNotNull { it.getCompatibility(connotation) }
                    .reduceOrNull(Double::times) ?: return@mapNotNull null
                otherStrength * connotation.strength
            }

        return hits.reduceOrNull(Double::times)?.pow(1.0 / hits.size.toDouble().pow(2.0)) ?: 0.0
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
                compounds.add(Compound(speechPart, PhonemeSequence(), changer, prosodyRule))
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
