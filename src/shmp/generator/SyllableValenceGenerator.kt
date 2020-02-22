package shmp.generator

import shmp.containers.PhonemeContainer
import shmp.language.PhonemeType
import shmp.language.phonology.Phoneme
import shmp.language.phonology.Syllable
import shmp.language.phonology.SyllableValenceTemplate
import shmp.random.randomElement
import shmp.random.testProbability
import kotlin.random.Random

class SyllableValenceGenerator(val template: SyllableValenceTemplate) {
    private val ADD_TESTS = 10

    fun generateSyllable(
        phonemeContainer: PhonemeContainer,
        random: Random,
        canHaveFinal: Boolean = false,
        shouldHaveInitial: Boolean = false,
        shouldHaveFinal: Boolean = false,
        prefix: List<Syllable> = listOf()
    ): Syllable {
        for (i in 0 until ADD_TESTS) {
            val syllable = generateOneSyllable(phonemeContainer, random, canHaveFinal)
            if (syllable.size != 1 || prefix.isEmpty() || syllable != prefix.last())
                if (!shouldHaveInitial || syllable[0].type == PhonemeType.Consonant)
                //if (!shouldHaveFinal)
                    return syllable
        }
        return generateOneSyllable(phonemeContainer, random, canHaveFinal)
    }

    private fun generateOneSyllable(
        phonemeContainer: PhonemeContainer,
        random: Random,
        canBeClosed: Boolean
    ): Syllable {
        val phonemes = ArrayList<Phoneme>()
        for (valency in (template.nucleusIndex downTo 0).map { template.valencies[it] }) {
            if (testProbability(valency.realizationProbability, random))
                for (i in 1..ADD_TESTS) {
                    val phoneme = randomElement(
                        phonemeContainer.getPhonemesByType(valency.phonemeType),
                        random
                    )
                    if (i == ADD_TESTS)
                        phonemes.add(phoneme)
                    else
                        if (addPhonemeWithTest(phonemes, phoneme))
                            break
                }
            else
                break
        }
        phonemes.reverse()

        if (canBeClosed) {
            var shouldTest = true
            var lastType = template.valencies[template.nucleusIndex].phonemeType
            for (valency in (template.nucleusIndex + 1..template.valencies.lastIndex).map { template.valencies[it] }) {
                shouldTest = shouldTest || lastType != valency.phonemeType
                lastType = valency.phonemeType
                if (shouldTest) {
                    if (testProbability(valency.realizationProbability, random))
                        phonemes.add(
                            randomElement(
                                phonemeContainer.getPhonemesByType(
                                    valency.phonemeType
                                ), random
                            )
                        )
                    else
                        shouldTest = false
                }
            }
        }
        return Syllable(phonemes)
    }

    private fun addPhonemeWithTest(phonemes: MutableList<Phoneme>, phoneme: Phoneme): Boolean {
        if (phonemes.isNotEmpty() && phonemes.last() == phoneme)
            return false
        phonemes.add(phoneme)
        return true
    }

}