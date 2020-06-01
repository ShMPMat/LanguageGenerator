package shmp.language.phonology

import shmp.language.*

class SyllableValenceTemplate(val valencies: List<ValencyPlace>) : SyllableTemplate {
    val nucleusIndex: Int
        get() = valencies
                .zip(valencies.indices)
                .filter { it.first.realizationProbability == 1.0 }
                .lastOrNull()
                ?.second
                ?: throw ExceptionInInitializerError("No nucleus (first valency with 1 probability) found.")
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

    override fun test(phonemes: PhonemeSequence): Boolean {
        val string = phonemes.getTypeRepresentation()
        return getRegexp().containsMatchIn(string)
    }

    override fun createWord(phonemes: PhonemeSequence, semanticsCore: SemanticsCore): Word? {
        val syllables = ArrayList<Syllable>()
        val regex = getRegexp()
        val currentPhonemes = phonemes.getTypeRepresentation()
        var lastIndex = 0
        while (lastIndex < currentPhonemes.length) {
            val range = regex.find(currentPhonemes, lastIndex)?.range ?: return null
            if (range.first != lastIndex) return null
            syllables.add(
                Syllable(
                    phonemes.phonemes.subList(
                        range.first,
                        range.last + 1
                    )
                )
            )
            lastIndex = range.last + 1
        }
        return Word(syllables, this, semanticsCore)
    }

    fun getRegexp(): Regex {
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