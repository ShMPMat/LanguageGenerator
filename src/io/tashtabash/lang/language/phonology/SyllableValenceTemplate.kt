package io.tashtabash.lang.language.phonology

import io.tashtabash.lang.language.LanguageException
import kotlin.math.max
import kotlin.math.min


data class SyllableValenceTemplate(val valencies: List<ValencyPlace>) : SyllableTemplate {
    constructor(vararg valencies: ValencyPlace) : this(valencies.toList())

    val nucleusIndex: Int
        get() = valencies.zip(valencies.indices)
            .lastOrNull { it.first.realizationProbability == 1.0 }
            ?.second
            ?: throw ExceptionInInitializerError("No nucleus (last valency with probability 1) found")
    override val nucleusPhonemeTypes: Set<PhonemeType> = setOf(valencies[nucleusIndex].phonemeType)

    override val initialPhonemeTypes: Set<PhonemeType> =
        valencies
            .takeWhile { it.realizationProbability != 1.0 }
            .map { it.phonemeType }
            .union(
                setOf(
                    valencies
                        .dropWhile { it.realizationProbability != 1.0 }
                        .getOrNull(0)?.phonemeType
                ).filterNotNull()
            )

    override val finalPhonemeTypes: Set<PhonemeType> = valencies.subList(nucleusIndex, valencies.size)
        .map { it.phonemeType }
        .toSet()

    override val maxSize = valencies.size

    override fun splitOnSyllables(phonemes: PhonemeSequence): Syllables? {
        val syllables = mutableListOf<Syllable>()
        val regex = getRegexp()
        val currentPhonemes = phonemes.getTypeRepresentation()

        var lastIndex = 0
        while (lastIndex < currentPhonemes.length) {
            val range = regex.find(currentPhonemes, lastIndex)?.range
                ?: return null
            if (range.first != lastIndex)
                return null
            val nucleusIdx = findNucleus(phonemes.phonemes.drop(lastIndex))
            syllables += Syllable(phonemes.phonemes.subList(range.first, range.last + 1), nucleusIdx)
            lastIndex = range.last + 1
        }

        return syllables
    }

    override fun addInitial(initial: PhonemeType) = SyllableValenceTemplate(
        listOf(ValencyPlace(initial, 0.5)) + valencies
    )

    override fun addFinal(initial: PhonemeType) = SyllableValenceTemplate(
        valencies + listOf(ValencyPlace(initial, 0.5))
    )

    override fun merge(that: SyllableTemplate): SyllableTemplate {
        if (that !is SyllableValenceTemplate)
            throw LanguageException("Can't merge 'SyllableValenceTemplate' with '${that.javaClass.name}'")

        val newValencies = mutableListOf<ValencyPlace>()
        val shift = that.nucleusIndex - nucleusIndex
        val startIndex = min(0, -shift)
        val endIndex = max(maxSize, that.maxSize - shift)

        for (i in startIndex until endIndex) {
            val thisValency = valencies.getOrNull(i)
            val thatValency = that.valencies.getOrNull(i + shift)

            newValencies +=
                if (thisValency == null)
                    thatValency!!
                else if (thatValency == null)
                    thisValency
                else {
                    if (thisValency.phonemeType != thatValency.phonemeType)
                        throw LanguageException("Can't unite SyllableValenceTemplates")

                    ValencyPlace(
                        thisValency.phonemeType,
                        (thisValency.realizationProbability + thatValency.realizationProbability) / 2
                    )
                }
        }

        return SyllableValenceTemplate(newValencies)
    }

    private fun findNucleus(phonemes: List<Phoneme>): Int = phonemes
        .indexOfFirst { it.type == PhonemeType.Vowel }

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

    override fun toString() = valencies
        .joinToString("")
}


data class ValencyPlace(val phonemeType: PhonemeType, val realizationProbability: Double) {
    override fun toString() = if (realizationProbability == 1.0)
        phonemeType.char.toString()
    else
        "(${phonemeType.char})"
}
