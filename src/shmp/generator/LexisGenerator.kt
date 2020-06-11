package shmp.generator

import shmp.containers.PhonemeContainer
import shmp.containers.SemanticsCoreTemplate
import shmp.containers.WordBase
import shmp.language.*
import shmp.language.category.Category
import shmp.language.lexis.SemanticsCore
import shmp.language.lexis.SemanticsTag
import shmp.language.lexis.Word
import shmp.language.phonology.RestrictionsParadigm
import shmp.language.phonology.Syllable
import shmp.language.phonology.prosody.StressType
import shmp.language.phonology.prosody.generateStress
import shmp.random.SampleSpaceObject
import shmp.random.randomElement
import shmp.random.randomSublist
import kotlin.math.abs
import kotlin.random.Random

class LexisGenerator(
    val syllableGenerator: SyllableValenceGenerator,
    private val restrictionsParadigm: RestrictionsParadigm,
    val phonemeContainer: PhonemeContainer,
    val stressType: StressType,
    private val random: Random
) {
    private val wordBase = WordBase()

    private val SYLLABLE_TESTS = 10

    internal fun generateWords(
        wordAmount: Int,
        categories: List<Category>
    ): List<Word> {
        val words = ArrayList<Word>()
        val cores = randomSublist(wordBase.words, random, wordAmount, wordAmount + 1).toMutableList()
        cores.add(wordBase.words.first { it.word == "_personal_pronoun" })
        for (core in cores) {
            val staticCategories = computeStaticCategories(core, categories)
            words.add(randomWord(SemanticsCore(
                core.word,
                core.speechPart,
                core.tagClusters
                    .map {
                        SemanticsTag(
                            randomElement(
                                it.semanticsTags,
                                random
                            ).name
                        )
                    }
                    .toSet(),
                staticCategories
            )))
        }
        return words
    }

    private fun computeStaticCategories(
        core: SemanticsCoreTemplate,
        categories: List<Category>
    ): Set<CategoryValue> {
        val staticCategories = mutableSetOf<CategoryValue>()
        val neededCategories = categories
            .filter { it.actualValues.isNotEmpty() && core.speechPart in it.staticSpeechParts }

        for (category in neededCategories) {
            val genderAndMappers = core.tagClusters.firstOrNull { it.type == category.outType }?.semanticsTags
                ?.map { n -> Box(category.allPossibleValues.first { it.toString() == n.name }, n.probability) }
                ?.filter { category.actualValues.contains(it.categoryValue) }
                ?.toMutableList()
                ?: mutableListOf()
            category.actualValues.forEach { v ->
                if (genderAndMappers.none { it.categoryValue == v }) {
                    genderAndMappers.add(Box(v, 10.0))
                }
            }
            staticCategories.add(randomElement(genderAndMappers, random).categoryValue)
        }
        return staticCategories
    }

    internal fun randomWord(
        core: SemanticsCore
    ): Word {
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

private data class Box(val categoryValue: CategoryValue, override val probability: Double) : SampleSpaceObject