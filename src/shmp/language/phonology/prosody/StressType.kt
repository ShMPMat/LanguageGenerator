package shmp.language.phonology.prosody

import shmp.generator.GeneratorException
import shmp.language.lexis.Word
import shmp.random.SampleSpaceObject
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random


enum class StressType(override val probability: Double) : SampleSpaceObject {
    None(10.0),//No actual data
    NotFixed(220.0),
    Initial(92.0),
    Second(16.0),
    Third(1.0),
    Antepenultimate(12.0),
    Penultimate(110.0),
    Ultimate(51.0)
}

fun generateStress(stressType: StressType, word: Word, random: Random): Word {
    if (word.syllables.any { s -> s.prosodicEnums.any { it is Stress } })
        throw GeneratorException("Word $word already has stress")

    val position = when (stressType) {
        StressType.None -> return word.copy()
        StressType.NotFixed -> random.nextInt(word.syllables.size)
        else -> getFixedStressPosition(stressType, word)
    }
    return putStressOn(word, position)
}

fun getFixedStressPosition(stressType: StressType, word: Word): Int {
    val syllableAmount = word.syllables.size
    return when (stressType) {
        StressType.Initial -> 0
        StressType.Second -> min(syllableAmount - 1, 1)
        StressType.Third -> min(syllableAmount - 1, 2)
        StressType.Antepenultimate -> max(0, syllableAmount - 3)
        StressType.Penultimate -> max(0, syllableAmount - 2)
        StressType.Ultimate -> max(0, syllableAmount - 1)
        else -> throw GeneratorException("Not fixed stress type - $stressType")
    }
}

fun putStressOn(word: Word, position: Int): Word {
    val newSyllables = word.syllables.mapIndexed { i, s ->
        if (i == position)
            s.copy(prosodicEnums = s.prosodicEnums.union(setOf(Stress())).toList())
        else
            s.copy()
    }
    return word.copy(syllables = newSyllables)
}