package shmp.generator

import shmp.containers.PhonemeContainer
import shmp.containers.WordBase
import shmp.language.*
import shmp.language.categories.Category
import shmp.language.categories.Gender
import shmp.language.categories.GenderValue
import shmp.language.phonology.RestrictionsParadigm
import shmp.language.phonology.Syllable
import shmp.language.phonology.prosody.StressType
import shmp.language.phonology.prosody.generateStress
import shmp.random.SampleSpaceObject
import shmp.random.randomElement
import shmp.random.randomSublist
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
        val gender = categories.find { it is Gender } ?: throw GeneratorException("Gender category wasn't generated")
        for (i in 0 until cores.size) {
            val core = cores[i]
            val staticCategories = mutableSetOf<CategoryValue>()
            if (gender.actualValues.isNotEmpty() && SpeechPart.Noun == core.speechPart) {
                val genderAndMappers = core.tagClusters.firstOrNull { it.type == gender.outType }?.syntaxTags
                    ?.map { Box(GenderValue.valueOf(it.name), it.probability) }
                    ?.filter { gender.actualValues.contains(it.categoryValue) }
                    ?.toMutableList()
                    ?: mutableListOf()
                gender.actualValues.forEach { v ->
                    if (genderAndMappers.none { it.categoryValue == v }) {
                        genderAndMappers.add(Box(v, 10.0))
                    }
                }
                staticCategories.add(
                    randomElement(genderAndMappers, random).categoryValue
                )
            }
            words.add(randomWord(SyntaxCore(
                core.word,
                core.speechPart,
                core.tagClusters
                    .map { SyntaxTag(randomElement(it.syntaxTags, random).name) }
                    .toSet(),
                staticCategories
            )))
        }
        return words
    }

    internal fun randomWord(
        core: SyntaxCore,
        maxSyllableLength: Int = 4,
        lengthWeight: (Int) -> Double = { (maxSyllableLength * maxSyllableLength + 1 - it * it).toDouble() }
    ): Word {
        val syllables = ArrayList<Syllable>()
        val length = getRandomWordLength(maxSyllableLength, lengthWeight)
        for (j in 0..length)
            for (i in 1..SYLLABLE_TESTS) {
                val syllable = syllableGenerator.generateSyllable(
                    SyllableRestrictions(
                        phonemeContainer,
                        restrictionsParadigm.restrictionsMapper.getValue(core.speechPart),
                        when (j) {
                            0 -> SyllablePosition.Start
                            length -> SyllablePosition.End
                            else -> SyllablePosition.Middle
                        },
                        prefix = syllables
                    ),
                    random
                )
                if (!checkSyllable(syllable, syllables))
                    continue
                syllables.add(syllable)
                break
            }
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

    private fun getRandomWordLength(max: Int, lengthWeight: (Int) -> Double) =
        randomElement((1..max), lengthWeight, random)
}

private data class Box(val categoryValue: CategoryValue, override val probability: Double) : SampleSpaceObject