package shmp.generator

import shmp.containers.*
import shmp.language.CategoryValue
import shmp.language.PhonemeType
import shmp.language.category.Category
import shmp.language.category.CategoryPool
import shmp.language.lexis.SemanticsCore
import shmp.language.lexis.Word
import shmp.language.phonology.RestrictionsParadigm
import shmp.language.phonology.Syllable
import shmp.language.phonology.prosody.StressType
import shmp.language.phonology.prosody.generateStress
import shmp.random.SampleSpaceObject
import shmp.random.randomElement
import shmp.random.randomSublist
import shmp.random.testProbability
import kotlin.math.abs
import kotlin.math.pow
import kotlin.random.Random

class LexisGenerator(
    supplementPath: String,
    val syllableGenerator: SyllableValenceGenerator,
    private val restrictionsParadigm: RestrictionsParadigm,
    val phonemeContainer: PhonemeContainer,
    private val stressType: StressType,
    private val random: Random,
    private val wordDoubleProbability: Double = 0.75
) {
    internal val derivationGenerator = DerivationGenerator(restrictionsParadigm, random)
    private val wordBase = WordBase(supplementPath)

    init {
        val newWords = derivationGenerator.injectDerivationOptions(wordBase.baseWords)
        wordBase.allWords.addAll(newWords)
    }

    private val wordClusters = readWordClusters(supplementPath)

    init {
        val allMeanings = wordBase.allWords.map { it.word }

        val unknownMeaning = wordClusters.clusters
            .flatMap { it.meanings }
            .firstOrNull { it.meaning !in allMeanings }

        if (unknownMeaning != null) throw DataConsistencyException("Unknown meaning in cluster - $unknownMeaning")
    }

    private val words = mutableListOf<Word>()
    private val usedMeanings = mutableListOf<String>()

    private val SYLLABLE_TESTS = 10

    internal fun generateWords(
        wordAmount: Int,
        categoryPool: CategoryPool
    ): List<Word> {
        val cores = randomSublist(wordBase.baseWords, random, wordAmount, wordAmount + 1).toMutableList()
        for (core in cores) {
            if (!isWordNeeded(core))
                continue
            val staticCategories = computeStaticCategories(core, categoryPool)
            val mainCore = core.toSemanticsCore(staticCategories, random)
            val extendedCore = extendCore(mainCore)
            val newWords = mutableListOf(randomWord(extendedCore))
            derivationGenerator.makeDerivations(newWords)
            words.addAll(newWords)
            usedMeanings.addAll(newWords.flatMap { it.semanticsCore.meanings })
        }

        return words
    }

    private fun extendCore(core: SemanticsCore): SemanticsCore {
        var resultCore = core
        val applicableClusters = wordClusters.clusters.filter { it.main in core.words }

        for (cluster in applicableClusters) {
            val chosenCores = cluster.chooseMeanings(random)
                .filter { it !in core.meanings }
                .map { wordBase.allWords.first { w -> w.word == it } }

            for (c in chosenCores)
                resultCore = c.merge(resultCore, random)
        }

        return resultCore
    }

    private fun isWordNeeded(core: SemanticsCoreTemplate): Boolean {
        val successProbability = core.probability * wordDoubleProbability.pow(usedMeanings.count { it == core.word })
        return testProbability(successProbability, random)
    }

    private fun computeStaticCategories(
        core: SemanticsCoreTemplate,
        categoryPool: CategoryPool
    ): Set<CategoryValue> {
        val resultCategories = mutableSetOf<CategoryValue>()

        for (category in categoryPool.getStaticFor(core.speechPart)) {
            val values = core.tagClusters
                .firstOrNull { it.type == category.outType }
                ?.semanticsTags
                ?.map { n -> CValueBox(category.allPossibleValues.first { it.toString() == n.name }, n.probability) }
                ?.filter { category.actualValues.contains(it.categoryValue) }
                ?.toMutableList()
                ?: mutableListOf()

            category.actualValues.forEach { v ->
                if (values.none { it.categoryValue == v }) {
                    values.add(CValueBox(v, 10.0))
                }
            }

            resultCategories.add(randomElement(values, random).categoryValue)
        }
        return resultCategories
    }

    internal fun randomWord(core: SemanticsCore): Word {
        val syllables = mutableListOf<Syllable>()
        val avgWordLength = restrictionsParadigm.restrictionsMapper.getValue(core.speechPart).avgWordLength.toDouble()
        val length = randomElement(1..10, { 1 / (1 + abs(it - avgWordLength)) }, random)

        fun makeSyllable(syllablePosition: SyllablePosition): Syllable {
            var syllable = Syllable(listOf())
            for (i in 1..SYLLABLE_TESTS) {
                syllable = syllableGenerator.generateSyllable(
                    SyllableRestrictions(
                        phonemeContainer,
                        restrictionsParadigm.restrictionsMapper.getValue(core.speechPart),
                        syllablePosition,
                        prefix = syllables
                    ),
                    random
                )
                if (checkSyllable(syllable, syllables))
                    break
            }
            return syllable
        }

        for (j in 0..length) {
            if (syllables.flatMap { it.phonemeSequence.phonemes }.size >= length)
                break

            val syllablePosition = when (j) {
                0 -> SyllablePosition.Start
                else -> SyllablePosition.Middle
            }
            val syllable = makeSyllable(syllablePosition)
            syllables.add(syllable)
        }
        syllables.removeAt(syllables.lastIndex)
        syllables.add(makeSyllable(SyllablePosition.End))

        return generateStress(stressType, Word(syllables, syllableGenerator.template, core), random)
    }

    fun checkSyllable(syllable: Syllable, prefix: List<Syllable>) = checkBorderCoherency(syllable, prefix)

    private fun checkBorderCoherency(syllable: Syllable, prefix: List<Syllable>): Boolean {
        if (prefix.isEmpty()) return true
        val leftBorder = prefix.last().phonemeSequence.last()
        val rightBorder = syllable[0]
        return leftBorder != rightBorder
                && (leftBorder.type != PhonemeType.Vowel || rightBorder.type != PhonemeType.Vowel)
    }
}

private data class CValueBox(val categoryValue: CategoryValue, override val probability: Double) : SampleSpaceObject