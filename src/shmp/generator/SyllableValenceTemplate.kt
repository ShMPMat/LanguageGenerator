package shmp.generator

import shmp.containers.PhonemeContainer
import shmp.language.Phoneme
import shmp.language.Syllable
import shmp.language.PhonemeType
import kotlin.random.Random

class SyllableValenceTemplate(private val valencies: List<ValencyPlace>) : SyllableTemplate {
    private val ADD_TESTS = 10

    private val nucleusIndex: Int
        get() {
            for (i in valencies.indices) {
                if (valencies[i].realizationProbability == 1.toDouble()) {
                    return i
                }
            }
            throw ExceptionInInitializerError("No nucleus (first valency with 1 probability) found.")
        }

    override fun generateSyllable(
        phonemeContainer: PhonemeContainer,
        random: Random,
        canHaveFinal: Boolean,
        shouldHaveInitial: Boolean,
        shouldHaveFinal: Boolean,
        prefix: List<Syllable>
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

    private fun generateOneSyllable(phonemeContainer: PhonemeContainer,
                                    random: Random,
                                    canBeClosed: Boolean): Syllable {
        val phonemes = ArrayList<Phoneme>()
        for (valency in (nucleusIndex downTo 0).map { valencies[it] }) {
            if (testProbability(valency.realizationProbability, random))
                for (i in 1..ADD_TESTS) {
                    val phoneme = randomElement(phonemeContainer.getPhonemesByType(valency.phonemeType), random)
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
            var lastType = valencies[nucleusIndex].phonemeType
            for (valency in (nucleusIndex + 1..valencies.lastIndex).map { valencies[it] }) {
                shouldTest = shouldTest || lastType != valency.phonemeType
                lastType = valency.phonemeType
                if (shouldTest) {
                    if (testProbability(valency.realizationProbability, random))
                        phonemes.add(randomElement(phonemeContainer.getPhonemesByType(valency.phonemeType), random))
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

    override fun test(phonemes: List<Phoneme>): Boolean {
        val string = phonemes.map { it.type.char.toString() } .joinToString("") { it }
        return getRegexp().containsMatchIn(string)
    }

    private fun getRegexp(): Regex {
        var maxSymbols = 1
        var minSymbols = if (valencies[0].realizationProbability == 1.0) 1 else 0
        var resultString = ""
        for (i in 0 until valencies.lastIndex) {
            if (valencies[i + 1].phonemeType != valencies[i].phonemeType) {
                resultString += valencies[i].phonemeType.char + "{$minSymbols,$maxSymbols}"
                maxSymbols = 1
                minSymbols = if (valencies[i + 1].realizationProbability == 1.0) 1 else 0
            } else {
                maxSymbols++
                if (valencies[i + 1].realizationProbability == 1.0)
                    minSymbols++
            }
        }
        resultString += valencies.last().phonemeType.char + "{$minSymbols,$maxSymbols}"
        return resultString.toRegex()
    }
}

data class ValencyPlace(val phonemeType: PhonemeType, val realizationProbability: Double)