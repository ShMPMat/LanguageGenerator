package io.tashtabash.lang.language.diachronicity

import io.tashtabash.lang.language.lexis.Word
import io.tashtabash.lang.language.phonology.Phoneme
import io.tashtabash.lang.language.phonology.prosody.Prosody


sealed class ChangingPhoneme {
    open val phoneme: Phoneme? = null
    open val prosody: List<Prosody>? = null
    open val isEpenthesis: Boolean = false

    data class ExactPhoneme(
        override val phoneme: Phoneme,
        override val prosody: List<Prosody>? = null,
        override val isEpenthesis: Boolean = false
    ): ChangingPhoneme()

    object DeletedPhoneme: ChangingPhoneme()

    object Boundary: ChangingPhoneme()
}


fun clearChangingPhonemes(phonemes: List<ChangingPhoneme>): List<Phoneme> =
    phonemes.filterIsInstance<ChangingPhoneme.ExactPhoneme>()
        .map { it.phoneme }

fun getChangingPhonemes(
    word: Word,
    addStartBoundary: Boolean = true,
    addEndBoundary: Boolean = true
): MutableList<ChangingPhoneme> =
    getChangingPhonemes(word.toPhonemes().zip(getProsodyContour(word)), addStartBoundary, addEndBoundary)

fun getChangingPhonemes(
    phonemes: List<Pair<Phoneme, List<Prosody>?>>,
    addStartBoundary: Boolean,
    addEndBoundary: Boolean
): MutableList<ChangingPhoneme> {
    val rawPhonemes = mutableListOf<ChangingPhoneme>()

    if (addStartBoundary)
        rawPhonemes += ChangingPhoneme.Boundary
    rawPhonemes += phonemes.map { (phoneme, prosody) -> ChangingPhoneme.ExactPhoneme(phoneme, prosody) }
    if (addEndBoundary)
        rawPhonemes += ChangingPhoneme.Boundary

    return rawPhonemes
}

private fun getProsodyContour(word: Word): List<List<Prosody>?> {
    return word.syllables.flatMap { syllable ->
        (0 until syllable.size).map {
            if (it == syllable.nucleusIdx)
                syllable.prosody
            else null
        }
    }
}
