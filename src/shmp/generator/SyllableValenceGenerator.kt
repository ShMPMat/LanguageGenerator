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
        restrictions: SyllableRestrictions,
        random: Random
    ): Syllable {
        for (i in 0 until ADD_TESTS) {
            val syllable = generateOneSyllable(restrictions, random)
            if (syllable.size != 1 || restrictions.prefix.isEmpty() || syllable != restrictions.prefix.last())
                if (!restrictions.shouldHaveInitial || syllable[0].type == PhonemeType.Consonant)
                //if (!shouldHaveFinal)
                    return syllable
        }
        return generateOneSyllable(restrictions, random)
    }

    private fun generateOneSyllable(
        restrictions: SyllableRestrictions,
        random: Random
    ): Syllable {
        val phonemes = ArrayList<Phoneme>()
        for (valency in (template.nucleusIndex downTo 0).map { template.valencies[it] }) {
            if (testProbability(valency.realizationProbability, random))
                for (i in 1..ADD_TESTS) {
                    val phoneme = randomElement(
                        restrictions.phonemeContainer.getPhonemesByType(valency.phonemeType),
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

        if (restrictions.canHaveFinal) {
            var shouldTest = true
            var lastType = template.valencies[template.nucleusIndex].phonemeType
            for (valency in (template.nucleusIndex + 1..template.valencies.lastIndex).map { template.valencies[it] }) {
                shouldTest = shouldTest || lastType != valency.phonemeType
                lastType = valency.phonemeType
                if (shouldTest) {
                    if (testProbability(valency.realizationProbability, random))
                        phonemes.add(
                            randomElement(
                                restrictions.phonemeContainer.getPhonemesByType(
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