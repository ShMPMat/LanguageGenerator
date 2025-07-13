package io.tashtabash.lang.generator

import io.tashtabash.lang.containers.*
import io.tashtabash.lang.generator.util.DataConsistencyException
import io.tashtabash.lang.generator.util.SyllablePosition
import io.tashtabash.lang.generator.util.SyllableRestrictions
import io.tashtabash.lang.generator.util.readWordClusters
import io.tashtabash.lang.language.category.CategoryPool
import io.tashtabash.lang.language.category.value.CategoryValue
import io.tashtabash.lang.language.lexis.*
import io.tashtabash.lang.language.phonology.PhonemeType
import io.tashtabash.lang.language.phonology.RestrictionsParadigm
import io.tashtabash.lang.language.phonology.Syllable
import io.tashtabash.lang.language.phonology.Syllables
import io.tashtabash.lang.language.phonology.prosody.StressType
import io.tashtabash.lang.language.phonology.prosody.generateStress
import io.tashtabash.lang.language.syntax.SyntaxParadigm
import io.tashtabash.lang.language.syntax.features.CopulaType
import io.tashtabash.lang.language.syntax.features.QuestionMarker
import io.tashtabash.random.randomSublist
import io.tashtabash.random.singleton.randomElement
import io.tashtabash.random.singleton.randomUnwrappedElement
import io.tashtabash.random.singleton.testProbability
import io.tashtabash.random.toSampleSpaceObject
import java.io.File
import kotlin.math.abs
import kotlin.math.pow
import kotlin.random.Random


class LexisGenerator(
    supplementPath: String,
    val syllableGenerator: SyllableValenceGenerator,
    val restrictionsParadigm: RestrictionsParadigm,
    val phonemeContainer: PhonemeContainer,
    private val stressType: StressType,
    private val random: Random,
    private val wordDoubleProbability: Double = 0.75
) {
    internal val derivationGenerator = DerivationGenerator(restrictionsParadigm, random)
    private val wordBase = WordBase(supplementPath)

    init {
        val allConnotations = parseConnotations("$supplementPath/Connotations")

        for (template in wordBase.allWords)
            template.connotations.values
                .forEach {
                    if (it.name[0] == 'T') {
                        it.name = it.name.drop(1)

                        allConnotations[it.name]
                            ?: throw DataConsistencyException("Unknown connotation '$it' in word - ${template.word}")
                    } else
                        it.isGlobal = allConnotations[it.name]
                            ?: throw DataConsistencyException("Unknown connotation '$it' in word - ${template.word}")
                }

        val newWords = derivationGenerator.injectDerivationOptions(wordBase.baseWords)
        wordBase.allWords += newWords
    }

    private val wordClusters = readWordClusters(supplementPath)

    init {
        val allMeanings = wordBase.allWords.map { it.word }

        val unknownClusterMeaning = wordClusters.clusters
            .flatMap { it.meanings }
            .firstOrNull { it.meaning !in allMeanings }
        if (unknownClusterMeaning != null)
            throw DataConsistencyException("Unknown meaning in cluster - $unknownClusterMeaning")

        val unknownCompoundMeaning = wordBase.allWords
            .flatMap { it.derivationClusterTemplate.possibleCompounds }
            .mapNotNull { it.templates }
            .flatten()
            .firstOrNull { it !in allMeanings }
        if (unknownCompoundMeaning != null)
            throw DataConsistencyException("Unknown meaning in compound - $unknownCompoundMeaning")

        val unknownDerivationMeaning = wordBase.allWords
            .flatMap { it.derivationClusterTemplate.typeToCore.values }
            .flatten()
            .mapNotNull { it.template }
            .firstOrNull { it !in allMeanings }
        if (unknownDerivationMeaning != null)
            throw DataConsistencyException("Unknown meaning in derivation - $unknownDerivationMeaning")
    }

    private val words = SimpleMutableLexis()

    private val syllableTests = 10

    internal fun generateLexis(
        wordAmount: Int,
        categoryPool: CategoryPool,
        syntaxParadigm: SyntaxParadigm,
        additionalWords: List<SemanticsCoreTemplate>
    ): Lexis {
        wordBase.addWords(additionalWords)

        val cores = randomSublist(
            wordBase.baseWords,
            random,
            wordAmount,
            wordAmount + 1
        ).toMutableList()

        for (core in cores) {
            if (!isWordNeeded(core))
                continue

            val staticCategories = makeStaticCategories(core, categoryPool)
            val mainCore = core.toSemanticsCore(staticCategories)
            val extendedCore = extendCore(mainCore)
            words += generateWord(extendedCore)

            derivationGenerator.makeDerivations(words.words.last(), words, wordBase)
        }

        derivationGenerator.makeCompounds(wordBase.allWords, words)

        return wrapWithWords(syntaxParadigm)
    }

    private fun wrapWithWords(syntaxParadigm: SyntaxParadigm): Lexis {
        val copula = mutableMapOf<CopulaType, WordPointer>()

        if (syntaxParadigm.copulaPresence.copulaType.any { it.feature == CopulaType.Particle }) {
            val particle = generateWord(
                SemanticsCore("copula_particle", SpeechPart.Particle.toDefault())
            )

            words += particle

            copula[CopulaType.Particle] = SimpleWordPointer(particle)
        }

        val questionMarker = mutableMapOf<QuestionMarker, WordPointer>()
        if (syntaxParadigm.questionMarkerPresence.questionMarker != null) {
            val particle = generateWord(
                SemanticsCore("question_marker", SpeechPart.Particle.toDefault())
            )

            words += particle

            questionMarker[QuestionMarker] = SimpleWordPointer(particle)
        }

        if (syntaxParadigm.copulaPresence.copulaType.any { it.feature == CopulaType.Verb })
            copula[CopulaType.Verb] = SimpleWordPointer(
                words.getWord("be")
            )

        return Lexis(words.words, copula, questionMarker)
            .reifyPointers()
    }

    private fun extendCore(core: SemanticsCore): SemanticsCore {
        var resultCore = core
        val applicableClusters = wordClusters.clusters.filter { it.main in core.meaningCluster }

        for (cluster in applicableClusters) {
            val chosenCores = cluster.chooseMeanings(random)
                .filter { it !in core.meaningCluster }
                .map { wordBase.allWords.first { w -> w.word == it } }

            for (c in chosenCores)
                resultCore = c.merge(resultCore, random)
        }

        return resultCore
    }

    private fun isWordNeeded(core: SemanticsCoreTemplate): Boolean {
        val doubles = words.words
            .map { getMeaningDistance(it.semanticsCore.meaningCluster, core.word) }
            .foldRight(0.0, Double::plus)
        val successProbability = core.probability * wordDoubleProbability.pow(doubles)
        return successProbability.testProbability()
    }

    private fun makeStaticCategories(core: SemanticsCoreTemplate, categoryPool: CategoryPool): Set<CategoryValue> {
        val resultCategories = mutableSetOf<CategoryValue>()

        for (category in categoryPool.getStaticFor(core.speechPart)) {
            val values = core.tagClusters
                .firstOrNull { it.type == category.outType }
                ?.semanticsTags
                ?.map { n ->
                    category.allPossibleValues.first { it.toString() == n.name }.toSampleSpaceObject(n.probability)
                }
                ?.filter { category.actualValues.contains(it.value) }
                ?.toMutableList()
                ?: mutableListOf()

            category.actualValues.forEach { v ->
                if (values.none { it.value == v })
                    values += v.toSampleSpaceObject(10.0)
            }

            resultCategories += values.randomUnwrappedElement()
        }
        return resultCategories
    }

    internal fun generateWord(core: SemanticsCore): Word {
        val syllables = mutableListOf<Syllable>()
        val avgWordLength = restrictionsParadigm.restrictionsMapper.getValue(core.speechPart).avgWordLength.toDouble()
        val length = (1..10).toList().randomElement { 1 / (1 + abs(it - avgWordLength).pow(4)) }

        fun makeSyllable(syllablePosition: SyllablePosition): Syllable {
            var syllable = Syllable(listOf(), 0)
            for (i in 1..syllableTests) {
                syllable = syllableGenerator.generateSyllable(
                    SyllableRestrictions(
                        phonemeContainer,
                        restrictionsParadigm.restrictionsMapper.getValue(core.speechPart),
                        syllablePosition,
                        prefix = syllables
                    )
                )
                if (checkSyllable(syllable, syllables))
                    break
            }
            return syllable
        }

        for (j in 0..length) {
            if (syllables.flatMap { it.phonemes.phonemes }.size >= length)
                break

            val syllablePosition = when (j) {
                0 -> SyllablePosition.Start
                else -> SyllablePosition.Middle
            }
            val syllable = makeSyllable(syllablePosition)
            syllables += syllable
        }
        syllables.removeAt(syllables.lastIndex)
        syllables += makeSyllable(SyllablePosition.End)

        return generateStress(stressType, Word(syllables, syllableGenerator.template, core), random)
    }

    fun checkSyllable(syllable: Syllable, prefix: Syllables) = checkBorderCoherency(syllable, prefix)

    private fun checkBorderCoherency(syllable: Syllable, prefix: Syllables): Boolean {
        if (prefix.isEmpty()) return true
        val leftBorder = prefix.last().phonemes.last()
        val rightBorder = syllable[0]
        return leftBorder != rightBorder
                && (leftBorder.type != PhonemeType.Vowel || rightBorder.type != PhonemeType.Vowel)
    }
}

typealias Connotations = Map<String, Boolean>

private fun parseConnotations(filePath: String): Connotations = File(filePath)
    .readLines()
    .filter { it.isNotBlank() }
    .associate {
        if (it[0] == '-')
            it.drop(1) to true
        else
            it to false
    }
