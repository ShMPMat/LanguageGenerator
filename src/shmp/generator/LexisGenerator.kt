package shmp.generator

import shmp.containers.PhonemeContainer
import shmp.containers.WordBase
import shmp.language.CategoryValue
import shmp.language.SyntaxCore
import shmp.language.Word
import shmp.language.categories.Category
import shmp.language.categories.Gender
import shmp.language.phonology.RestrictionsParadigm
import shmp.language.phonology.Syllable
import shmp.random.randomElementWithProbability
import shmp.random.randomSublist
import kotlin.random.Random

class LexisGenerator(
    val syllableGenerator: SyllableValenceGenerator,
    private val restrictionsParadigm: RestrictionsParadigm,
    val phonemeContainer: PhonemeContainer,
    private val random: Random
) {
    private val wordBase = WordBase()

    private val SYLLABLE_TESTS = 10

    internal fun generateWords(
        wordAmount: Int,
        categories: List<Category>
    ): ArrayList<Word> {
        val words = ArrayList<Word>()
        val cores = randomSublist(wordBase.words, random, wordAmount, wordAmount + 1)
        val gender = categories.find { it is Gender } ?: throw GeneratorException("Gender category wasn't generated")
        for (i in 0 until wordAmount) {
            val staticCategories = mutableSetOf<CategoryValue>()
            if (gender.values.isNotEmpty()) staticCategories.add(
                randomElementWithProbability(
                    gender.values,
                    { 1.0 },
                    random
                )
            )
            words.add(randomWord(SyntaxCore(cores[i].word, cores[i].speechPart, staticCategories)))
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
                if (syllables.isNotEmpty() && syllables.last().phonemeSequence.last() == syllable[0])
                    continue
                syllables.add(syllable)
                break
            }
        return Word(syllables, syllableGenerator.template, core)
    }

    private fun getRandomWordLength(max: Int, lengthWeight: (Int) -> Double) =
        randomElementWithProbability((1..max), lengthWeight, random)
}